package com.akira.island.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.akira.island.R
import com.akira.island.core.IslandBus
import com.akira.island.core.Lang
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Ios
import com.akira.island.island.releaseHighlights
import com.akira.island.service.NotificationParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class Release(
    val version: String,
    val notes: String,
    val apkUrl: String?,
    val pageUrl: String,
    val sizeBytes: Long,
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val release: Release) : UpdateState
    data class Error(val message: String) : UpdateState
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Running(val progress: Float) : DownloadState
    data class Ready(val file: File) : DownloadState
    data class Failed(val message: String) : DownloadState
}

object UpdateChecker {
    const val REPO = "KagaTetsuo/Capsule-VN"
    private const val CHECK_EVERY_MS = 3 * 60 * 60 * 1000L

    val state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val download = MutableStateFlow<DownloadState>(DownloadState.Idle)

    private var announced: String? = null

    fun currentVersion(context: Context): String = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "0"

    suspend fun check(context: Context, force: Boolean, announce: Boolean = false) {
        val s = SettingsRepo.current
        val now = System.currentTimeMillis()
        if (!force && (!s.autoUpdateCheck || now - s.lastUpdateCheck < CHECK_EVERY_MS)) return
        if (state.value is UpdateState.Checking) return
        state.value = UpdateState.Checking
        val result = runCatching { withContext(Dispatchers.IO) { fetchLatest() } }
        
        if (result.isSuccess) SettingsRepo.update { copy(lastUpdateCheck = now) }
        val release = result.getOrNull()
        state.value = when {
            result.isFailure -> if (force) UpdateState.Error(
                tr("Không thể kết nối GitHub. Vui lòng kiểm tra mạng và thử lại.", "Can't reach GitHub. Check your internet connection and try again."),
            ) else UpdateState.Idle
            release == null -> if (force) UpdateState.UpToDate else UpdateState.Idle
            !isNewer(release.version, currentVersion(context)) -> if (force) UpdateState.UpToDate else UpdateState.Idle
            !force && release.version == s.skippedVersion -> UpdateState.Idle
            else -> UpdateState.Available(release)
        }
        val available = (state.value as? UpdateState.Available)?.release
        
        if (available != null && download.value is DownloadState.Failed) download.value = DownloadState.Idle
        if (announce && available != null && available.version != announced) {
            announced = available.version
            IslandBus.post(
                Notice(
                    title = tr("Đã có phiên bản ${available.version}", "Version ${available.version} is available"),
                    icon = Ios.load(context, R.drawable.ios_download),
                    tint = Tints.Lavender,
                    caption = releaseHighlights(available.notes) ?: tr("Tải xuống và cài đặt chỉ với một chạm", "Downloads and installs in one tap"),
                    style = NoticeStyle.UPDATE,
                    appIcon = NotificationParser.icon(context, context.packageName),
                    update = available,
                    openPkg = context.packageName,
                    extraMs = 3_500,
                ),
            )
        }
    }

    const val DEMO_APK = "demo://update"

    fun updatedNotice(context: Context, version: String) = Notice(
        title = tr("Đã cập nhật lên $version", "Updated to $version"),
        icon = Ios.load(context, R.drawable.ios_check),
        tint = Tints.Green,
        caption = tr("Hoàn tất, cài đặt của bạn được giữ nguyên", "All set, your settings are kept"),
        style = NoticeStyle.UPDATE,
        appIcon = NotificationParser.icon(context, context.packageName),
        openPkg = context.packageName,
        extraMs = 2_000,
    )

    fun showDemo(context: Context) {
        download.value = DownloadState.Idle
        IslandBus.post(
            Notice(
                title = tr("Đã có phiên bản 1.7", "Version 1.7 is available"),
                icon = Ios.load(context, R.drawable.ios_download),
                tint = Tints.Lavender,
                caption = tr("Ví dụ: khi có phiên bản mới", "Example: a new version arrives"),
                style = NoticeStyle.UPDATE,
                appIcon = NotificationParser.icon(context, context.packageName),
                update = Release("1.7", "", DEMO_APK, "", 0L),
                openPkg = context.packageName,
                extraMs = 3_500,
            ),
        )
    }

    private fun apkName(version: String) = "DynamicIsland-${version.filter { it.isLetterOrDigit() || it == '.' }}.apk"

    fun readyFile(release: Release, state: DownloadState): File? =
        (state as? DownloadState.Ready)?.file?.takeIf { it.name == apkName(release.version) && it.exists() }

    fun skip(version: String) {
        SettingsRepo.update { copy(skippedVersion = version) }
        state.value = UpdateState.Idle
        download.value = DownloadState.Idle
    }

    suspend fun downloadAndInstall(context: Context, release: Release) {
        val url = release.apkUrl
        if (url == DEMO_APK) {
            if (download.value is DownloadState.Running) return
            for (p in 0..100 step 5) {
                download.value = DownloadState.Running(p / 100f)
                delay(70)
            }
            download.value = DownloadState.Idle
            IslandBus.notice.value = null
            delay(350)
            IslandBus.post(updatedNotice(context, release.version))
            return
        }
        if (url == null) {
            download.value = DownloadState.Failed(tr("Bản phát hành không có file APK. Hãy mở trang phát hành.", "The release has no APK. Open the release page."))
            return
        }
        if (download.value is DownloadState.Running) return
        download.value = DownloadState.Running(0f)
        val result = runCatching {
            withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "update").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                val file = File(dir, apkName(release.version))
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", "DynamicIsland-Android")
                }
                try {
                    if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
                    val total = conn.contentLengthLong.takeIf { it > 0 } ?: release.sizeBytes
                    var done = 0L
                    conn.inputStream.use { input ->
                        file.outputStream().use { out ->
                            val buf = ByteArray(64 * 1024)
                            var lastShown = -1
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                if (total > 0) {
                                    val percent = (done * 100 / total).toInt()
                                    if (percent != lastShown) {
                                        lastShown = percent
                                        download.value = DownloadState.Running(percent / 100f)
                                    }
                                }
                            }
                        }
                    }
                    
                    if (total > 0 && done < total) error(tr("Đã tải $done trên $total byte", "Downloaded $done of $total bytes"))
                } finally {
                    conn.disconnect()
                }

                if (context.packageManager.getPackageArchiveInfo(file.path, 0)?.packageName != context.packageName) {
                    file.delete()
                    error(tr("Tệp cập nhật bị hỏng", "The update file is damaged"))
                }
                file
            }
        }
        val file = result.getOrNull()
        if (file == null) {
            download.value = DownloadState.Failed(tr("Không thể tải xuống. Vui lòng kiểm tra mạng và thử lại.", "Couldn't download. Check your internet and try again."))
            return
        }
        download.value = DownloadState.Ready(file)
        install(context, file)
    }

    fun install(context: Context, file: File) {
        runCatching {
            if (!context.packageManager.canRequestPackageInstalls()) {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                return
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun fetchLatest(): Release? {
        val conn = (URL("https://api.github.com/repos/$REPO/releases/latest").openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "DynamicIsland-Android")
        }
        try {
            
            if (conn.responseCode == 404) return null
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            return parseRelease(conn.inputStream.bufferedReader().use { it.readText() })
        } finally {
            conn.disconnect()
        }
    }

    internal fun parseRelease(json: String): Release? {
        val o = Json.parseToJsonElement(json).jsonObject
        val tag = o["tag_name"]?.jsonPrimitive?.contentOrNull ?: return null
        val apk: JsonObject? = o["assets"]?.jsonArray?.map { it.jsonObject }?.firstOrNull { a ->
            val name = a["name"]?.jsonPrimitive?.contentOrNull.orEmpty().lowercase()
            name.endsWith(".apk") && "debug" !in name
        }
        return Release(
            version = tag.removePrefix("v").removePrefix("V"),
            notes = o["body"]?.jsonPrimitive?.contentOrNull.orEmpty().trim(),
            apkUrl = apk?.get("browser_download_url")?.jsonPrimitive?.contentOrNull,
            pageUrl = o["html_url"]?.jsonPrimitive?.contentOrNull ?: "https://github.com/$REPO/releases",
            sizeBytes = apk?.get("size")?.jsonPrimitive?.longOrNull ?: 0L,
        )
    }

    internal fun isNewer(remote: String, local: String): Boolean {
        fun parts(v: String) = v.takeWhile { it.isDigit() || it == '.' }.split('.').map { it.toIntOrNull() ?: 0 }
        val r = parts(remote)
        val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}

fun localNotes(markdown: String): List<String> {
    val all = markdown.lines()
    val start = all.indexOfFirst { it.startsWith("## ") && (if (Lang.en) "English" in it else "Tiếng Việt" in it) }
    if (start < 0) return all
    val rest = all.drop(start + 1)
    val end = rest.indexOfFirst { it.startsWith("## ") && ("English" in it || "Tiếng Việt" in it) }
    return if (end < 0) rest else rest.take(end)
}
