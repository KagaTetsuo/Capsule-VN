package com.akira.island.service

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.akira.island.R
import com.akira.island.core.LiveInfo
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Turn
import com.akira.island.island.Ios
import com.akira.island.island.liveTint
import android.os.SystemClock
import com.akira.island.core.IslandBus
import com.akira.island.core.Lang
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object LiveParser {
    
    private class Remote(val texts: List<String>, val image: Bitmap?, val note: String)

    private val distance = Regex("""(\d{1,3}(?:[.,]\d{1,2})?)\s?(km|mét|mi|ft|m)\.?(?!\p{L})""", RegexOption.IGNORE_CASE)
    private val clock = Regex("""\b\d{1,2}:\d{2}\b""")
    private val minutes = Regex("""\b\d{1,3}\s?(phút|giờ|min|h)\b""", RegexOption.IGNORE_CASE)

    fun parse(context: Context, sbn: StatusBarNotification, fallbackTitle: String): LiveInfo? {
        val n = sbn.notification
        val ex = n.extras
        val head = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = ex.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        val big = ex.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val sub = (ex.getCharSequence(Notification.EXTRA_SUB_TEXT) ?: ex.getCharSequence(Notification.EXTRA_INFO_TEXT))
            ?.toString()?.trim().orEmpty()
        val kind = LiveApps.kind(sbn.packageName, n.category, n.flags, "$head $text $big $sub") ?: return null
        val fields = listOf(head, text, big, sub)
        
        val remote = if (!sbn.isOngoing) {
            Remote(emptyList(), null, tr("chưa phân tích: thông báo không phải dạng liên tục", "not parsed: the notification isn't ongoing"))
        } else if (findDistance(fields) != null && findEta(fields) != null) {
            Remote(emptyList(), null, tr("chưa phân tích: khoảng cách và thời gian nằm trong các trường dữ liệu", "not parsed: distance and time are in the fields"))
        } else {
            remoteFor(context, sbn.key, n)
        }
        LiveDebug.record(context, sbn, head, text, big, sub, remote.texts, remote.note)
        if (!sbn.isOngoing) return null
        if (head.isEmpty() && text.isEmpty() && remote.texts.isEmpty()) return null

        val all = fields + remote.texts
        val hint = instructionOf(all)
        val turn = hint?.let { detectTurn(listOf(it)) }
        val art = if (turn == null) {
            runCatching { n.getLargeIcon()?.loadDrawable(context)?.toBitmap(128, 128) }.getOrNull() ?: remote.image
        } else {
            null
        }
        val instruction = hint ?: (listOf(text, big) + remote.texts)
            .firstOrNull { it.isNotBlank() && it != head && it.length > 8 && distance.matchEntire(it.trim()) == null }
            .orEmpty()
        return LiveInfo(
            key = sbn.key,
            pkg = sbn.packageName,
            kind = kind,
            title = head.ifEmpty { fallbackTitle },
            text = instruction,
            sub = sub,
            icon = NotificationParser.icon(context, sbn.packageName),
            art = art?.asImageBitmap(),
            postTime = sbn.postTime,
            open = n.contentIntent,
            distance = findDistance(all),
            eta = findEta(all),
            turn = turn,
        ).let { info ->
            val (p, stage) = progressOf(sbn.key, kind, n, all)
            info.copy(progress = p, stage = stage)
        }
    }

    // ---------- Thanh tiến trình (giao hàng, gọi xe, dẫn đường) ----------

    /** Số phút còn lại lớn nhất từng thấy của mỗi hoạt động: tiến trình = phần đã đi được. */
    private val startMinutes = HashMap<String, Float>()
    private val minutesRx = Regex("""(\d{1,3})\s?(phút|ph|min|mins|minutes|giờ|h|hr|hrs)\b""", RegexOption.IGNORE_CASE)

    private class Stage(val at: Float, val vi: String, val en: String, val words: List<String>)

    private val deliveryStages = listOf(
        Stage(1f, "Đã giao", "Delivered", listOf("đã giao", "giao thành công", "giao hàng thành công", "hoàn thành", "hoàn tất", "delivered", "completed")),
        Stage(0.85f, "Sắp đến", "Arriving", listOf("sắp đến", "sắp giao", "đã đến", "gần đến", "arriving", "nearby", "arrived")),
        Stage(0.6f, "Đang giao", "On the way", listOf("đang giao", "đang trên đường", "đang đến", "đã lấy", "đã nhận món", "on the way", "out for delivery", "picked up")),
        Stage(0.35f, "Đang chuẩn bị", "Preparing", listOf("đang chuẩn bị", "chuẩn bị", "tìm tài xế", "tìm shipper", "đang lấy hàng", "đến lấy", "preparing", "finding a driver", "looking for")),
        Stage(0.15f, "Đã đặt", "Ordered", listOf("đã đặt", "đã xác nhận", "xác nhận", "chờ xác nhận", "đặt hàng thành công", "confirmed", "order placed")),
    )
    private val rideStages = listOf(
        Stage(1f, "Hoàn thành", "Completed", listOf("hoàn thành", "hoàn tất", "kết thúc chuyến", "đã đến điểm đến", "completed", "arrived at destination")),
        Stage(0.7f, "Đang di chuyển", "On trip", listOf("đang trong chuyến", "đang trên chuyến", "chuyến đi đã bắt đầu", "bắt đầu chuyến", "đang di chuyển", "đang đến điểm đến", "on trip", "on the way to")),
        Stage(0.45f, "Tài xế đã đến", "Driver arrived", listOf("tài xế đã đến", "đã đến điểm đón", "driver has arrived", "driver arrived")),
        Stage(0.25f, "Tài xế đang đến", "Driver on the way", listOf("tài xế đang đến", "đang đến đón", "driver is on the way", "driver is coming")),
        Stage(0.1f, "Đang tìm tài xế", "Finding a driver", listOf("đang tìm", "tìm tài xế", "tìm xe", "finding", "searching")),
    )

    private fun minutesLeft(texts: List<String>): Float? {
        val m = minutesRx.find(texts.joinToString(" ")) ?: return null
        val v = m.groupValues[1].toFloatOrNull() ?: return null
        val unit = m.groupValues[2].lowercase()
        return if (unit.startsWith("g") || unit.startsWith("h")) v * 60f else v
    }

    /** Ưu tiên: tiến trình app tự báo > giai đoạn (đã đặt / đang giao…) > thời gian còn lại so với lúc bắt đầu. */
    private fun progressOf(key: String, kind: com.akira.island.core.LiveKind, n: Notification, texts: List<String>): Pair<Float?, String?> {
        val ex = n.extras
        val max = ex.getInt(Notification.EXTRA_PROGRESS_MAX, 0)
        val explicit = if (max > 0) (ex.getInt(Notification.EXTRA_PROGRESS, 0).toFloat() / max).coerceIn(0f, 1f) else null
        val joined = texts.joinToString(" ").lowercase()
        val stages = when (kind) {
            com.akira.island.core.LiveKind.DELIVERY -> deliveryStages
            com.akira.island.core.LiveKind.RIDE -> rideStages
            else -> emptyList()
        }
        val stage = stages.firstOrNull { st -> st.words.any { it in joined } }
        val left = minutesLeft(texts)
        val byTime = left?.let { cur ->
            val start = maxOf(startMinutes[key] ?: cur, cur).also { startMinutes[key] = it }
            if (start <= 0f) null else (1f - cur / start).coerceIn(0.03f, 1f)
        }
        val p = explicit ?: listOfNotNull(stage?.at, byTime).maxOrNull()
        return p to stage?.let { tr(it.vi, it.en) }
    }

    private val lastBanner = HashMap<String, Long>()

    fun postBanner(context: Context, l: LiveInfo) {
        if (!SettingsRepo.current.cardLive) return
        val now = SystemClock.elapsedRealtime()
        if (now - (lastBanner[l.pkg] ?: -120_000L) < 60_000) return
        lastBanner[l.pkg] = now
        IslandBus.post(banner(context, l))
    }

    fun postBannerLater(context: Context, key: String) {
        IslandBus.scope.launch {
            delay(1600)
            val l = IslandBus.live.value[key] ?: return@launch
            postBanner(context, NavScreen.merge(l, NavScreen.data.value[l.pkg]))
        }
    }

    fun banner(context: Context, l: LiveInfo): Notice = Notice(
        title = l.title,
        icon = Ios.load(context, R.drawable.ios_navigate),
        tint = liveTint(l.kind),
        style = NoticeStyle.LIVE,
        live = l,
        extraMs = 2_000,
    )

    internal fun findDistance(texts: List<String>): String? {
        val cleaned = texts.map { it.trim() }.filter { it.isNotEmpty() }
        cleaned.firstOrNull { it.length <= 12 && distance.matchEntire(it) != null }?.let { return normalize(it) }
        for (t in cleaned) distance.find(t)?.let { return normalize(it.value) }
        return null
    }

    private fun normalize(s: String) = units(s.trim().trimEnd('.').replace(Regex("""\s+"""), " "))

    private val viUnit = Regex("""(\d+)(?:,(\d+))?\s?(km|mét|phút|giờ|m)\.?(?!\p{L})""")

    fun units(s: String): String {
        if (!Lang.en) return s
        return viUnit.replace(s) { m ->
            val (whole, frac, unit) = m.destructured
            val n = if (frac.isEmpty()) whole else "$whole.$frac"
            val u = when (unit) {
                "km" -> "km"
                "phút" -> "min"
                "giờ" -> "h"
                else -> "m"
            }
            "$n $u"
        }
    }

    internal fun findEta(texts: List<String>): String? {
        val joined = texts.joinToString(" ")
        val time = clock.find(joined)?.value
        val min = minutes.find(joined)?.value
        return listOfNotNull(time?.let { tr("Đến nơi lúc $it", "Arrive $it") }, min?.replace(Regex("""\s+"""), " ")?.let(::units))
            .takeIf { it.isNotEmpty() }?.joinToString(" · ")
    }

    private val maneuverWords = listOf(
        "rẽ", "đi thẳng", "quay đầu", "chếch", "lối ra", "vòng xuyến", "bên trái", "bên phải",
        "turn", "keep", "exit", "u-turn", "continue",
    )
    private val notInstruction = listOf("theo từng bước", "theo gps", "bước ")

    internal fun instructionOf(texts: List<String>): String? = texts.map { it.trim() }.firstOrNull { t ->
        val l = t.lowercase()
        t.length in 6..90 && maneuverWords.any { it in l } && notInstruction.none { it in l }
    }

    internal fun detectTurn(texts: List<String>): Turn? {
        val t = texts.joinToString(" ").lowercase()
        return when {
            listOf("quay đầu", "u-turn", "uturn", "make a u").any { it in t } -> Turn.UTURN
            listOf("rẽ trái", "bên trái", "chếch sang trái", "left").any { it in t } -> Turn.LEFT
            listOf("rẽ phải", "bên phải", "chếch sang phải", "right").any { it in t } -> Turn.RIGHT
            listOf("đi thẳng", "tiếp tục", "straight", "continue").any { it in t } -> Turn.STRAIGHT
            else -> null
        }
    }

    @Suppress("DEPRECATION")
    
    private val remoteCache = HashMap<String, Pair<Long, Remote>>()

    private fun remoteFor(context: Context, key: String, n: Notification): Remote {
        val now = SystemClock.elapsedRealtime()
        val cached = remoteCache[key]
        if (cached != null && (now - cached.first < 2000 || !IslandBus.screenOn.value)) return cached.second
        return remoteViews(context, n).also { remoteCache[key] = now to it }
    }

    fun forget(key: String) {
        remoteCache.remove(key)
        startMinutes.remove(key)
    }

    private fun remoteViews(context: Context, n: Notification): Remote {
        val rv = n.bigContentView ?: n.contentView ?: n.headsUpContentView
            ?: return Remote(emptyList(), null, tr("không có bố cục tùy chỉnh", "no custom layout"))
        return runCatching {
            val root = rv.apply(context, FrameLayout(context))
            val texts = mutableListOf<String>()
            var image: Bitmap? = null
            fun walk(v: View) {
                when (v) {
                    is TextView -> v.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(texts::add)
                    is ImageView -> {
                        v.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let(texts::add)
                        val d = v.drawable
                        if (image == null && d != null && d.intrinsicWidth in 1..220) image = d.toBitmap(96, 96)
                    }
                }
                if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
            }
            walk(root)
            Remote(texts, image, tr("Bố cục tùy chỉnh, số văn bản: ${texts.size}", "custom layout, texts: ${texts.size}"))
        }.getOrElse {
            Remote(emptyList(), null, tr("Không thể đọc bố cục: ${it.javaClass.simpleName}", "couldn't read the layout: ${it.javaClass.simpleName}"))
        }
    }
}

object LiveDebug {
    val dumps = MutableStateFlow<Map<String, String>>(emptyMap())

    fun record(
        context: Context,
        sbn: StatusBarNotification,
        title: String,
        text: String,
        bigText: String,
        sub: String,
        remote: List<String>,
        remoteNote: String,
    ) {
        val n = sbn.notification
        val body = buildString {
            appendLine(NotificationParser.appLabel(context, sbn.packageName) + " (" + sbn.packageName + ")")
            val category = n.category ?: "—"
            appendLine(tr("Đang diễn ra: ${sbn.isOngoing}, danh mục: $category", "ongoing: ${sbn.isOngoing}, category: $category"))
            appendLine(tr("tiêu đề: ", "title: ") + title.ifEmpty { "—" })
            appendLine(tr("nội dung: ", "text: ") + text.ifEmpty { "—" })
            if (bigText.isNotEmpty() && bigText != text) appendLine(tr("văn bản lớn: ", "big text: ") + bigText)
            appendLine(tr("văn bản phụ: ", "subtext: ") + sub.ifEmpty { "—" })
            appendLine(tr("bố cục: ", "layout: ") + remoteNote)
            remote.forEachIndexed { i, t -> appendLine("  [$i] $t") }
            append(tr("các nút: ", "buttons: ") + (n.actions?.joinToString { it.title?.toString().orEmpty() } ?: "—"))
        }
        put(sbn.key, body)
    }

    fun forget(key: String) {
        if (key in dumps.value) dumps.value = dumps.value - key
    }

    private fun put(key: String, body: String) {
        val next = (dumps.value - key) + (key to body)
        dumps.value = if (next.size > MAX_DUMPS) next.entries.drop(next.size - MAX_DUMPS).associate { it.toPair() } else next
    }

    private const val MAX_DUMPS = 20

    fun recordScreen(context: Context, pkg: String, items: List<ScreenItem>, r: NavScreenParser.Result) {
        val body = buildString {
            appendLine(tr("Màn hình: ", "Screen: ") + NotificationParser.appLabel(context, pkg) + " (" + pkg + ")")
            val dist = r.distance ?: "—"
            val street = r.street ?: "—"
            val eta = r.eta ?: "—"
            val turn = r.turn?.toString() ?: tr("từ hình ảnh", "from the picture")
            appendLine(tr("Tìm thấy: khoảng cách $dist, đường $street, đến nơi $eta, rẽ $turn", "found: distance $dist, street $street, arrival $eta, turn $turn"))
            items.filter { it.text.isNotEmpty() }.sortedWith(compareBy({ it.top }, { it.left })).take(40).forEach {
                appendLine("  «${it.text}» ${it.left},${it.top}–${it.right},${it.bottom}")
            }
        }.trimEnd()
        val key = "screen:$pkg"
        if (dumps.value[key] != body) put(key, body)
    }
}
