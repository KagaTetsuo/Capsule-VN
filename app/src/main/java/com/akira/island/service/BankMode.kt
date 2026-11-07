package com.akira.island.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

/**
 * Chế độ ngân hàng tự động: ngân hàng / VNeID chặn mọi app đang bật Dịch vụ hỗ trợ tiếp cận.
 * Khi mở các app này, Capsule VN tự tắt dịch vụ của mình; khi rời app, tự bật lại.
 *
 * - Tự bật lại cần quyền WRITE_SECURE_SETTINGS (cấp 1 lần qua Shizuku hoặc ADB).
 * - Biết lúc rời app ngân hàng (khi dịch vụ đã tắt) cần quyền Truy cập dữ liệu sử dụng.
 */
object BankMode {

    /** Ứng dụng ngân hàng, ví điện tử & định danh phổ biến tại Việt Nam (được tự chọn sẵn nếu đã cài). */
    private val knownVn = setOf(
        "com.VCB", "com.vietinbank.ipay", "com.bidv.smartbanking", "com.vnpay.Agribank3g", "vn.com.techcombank.bb.app",
        "com.mbmobile", "mobile.acb.com.vn", "com.vnpay.vpbankonline", "com.vpbank.neo", "com.tpb.mb.gprsandroid",
        "com.sacombank.ewallet", "com.hdbank.mobilebanking", "com.vib.myvib2", "vn.shb.mbanking", "com.ocb.omni",
        "vn.com.msb.smartBanking", "vn.seabank.digitalbanking", "com.lpb.lienviet24h", "com.eximbank.ebmobile",
        "com.namabank.mobilebanking", "com.cake.cakebank", "io.lifestyle.plus", "vn.timo.android", "com.vnid",
        "com.mservice.momotransfer", "vn.com.vng.zalopay", "com.vnpay.wallet", "vn.viettelpay.viettelmoney",
        "com.shopeepay.vn", "com.bplus.vtpay", "com.vnpt.vnptmoney", "com.vietbank.mobilebanking",
        "com.vnpay.bidv", "com.vnpay.hdbank", "vn.com.ocb.awe", "vn.com.seabank.mb1", "vn.com.lpb.lienviet24h",
        "com.vnpay.EximBankOmni", "com.vnpay.namabank", "xyz.be.cake", "com.pvcombank.retail", "vn.shb.saha.mbanking",
        "com.vss.vssid", "vn.gov.gdt.etaxmobile", "vn.payoo.wallet",
    )

    /** Mã ngân hàng / ví thường gặp trong tên gói (so khớp đúng từng đoạn, tránh nhầm "vib" trong "vibrate"…). */
    private val tokens = setOf(
        "vcb", "bidv", "agribank", "agribank3g", "tpb", "vib", "shb", "ocb", "msb", "lpb", "acb", "hdbank", "vpbank", "mbbank",
        "mbmobile", "techcombank", "sacombank", "seabank", "eximbank", "namabank", "pvcombank", "abbank", "baovietbank",
        "kienlongbank", "bacabank", "vietabank", "vietbank", "scb", "ncb", "saigonbank", "oceanbank", "gpbank", "pgbank",
        "bvbank", "cake", "timo", "vnid", "vneid", "vssid", "momotransfer", "zalopay", "viettelpay", "viettelmoney", "vtpay",
        "vnptmoney", "vnptpay", "shopeepay", "payoo", "vietqr", "napas",
    )

    /** Từ khoá trong tên gói hoặc tên hiển thị của ngân hàng, ví điện tử, thanh toán, định danh. */
    private val bankish = Regex(
        "bank|banking|ewallet|wallet|vnpay|momo|zalopay|viettel ?money|vneid|vnid|vssid|ngân hàng|ví điện tử|smart ?banking|e-?banking|m-?banking",
        RegexOption.IGNORE_CASE,
    )

    private fun isBankLike(context: Context, pkg: String, label: String): Boolean {
        if (pkg in knownVn) return true
        val parts = pkg.lowercase().split('.', '_')
        if (parts.any { it in tokens }) return true
        if (bankish.containsMatchIn(pkg) || bankish.containsMatchIn(label)) return true
        // Ví / ngân hàng có thanh toán chạm NFC (khai báo dịch vụ HCE)
        return PayMonitor.isPayApp(context, pkg)
    }

    /** Danh sách app áp dụng: người dùng đã chỉnh thì theo người dùng, chưa thì tự nhận diện. */
    fun apps(context: Context): Set<String> {
        val s = SettingsRepo.current
        return if (s.bankAppsEdited) s.bankApps.toSet() else detected(context)
    }

    @Volatile private var detectedCache: Set<String>? = null

    fun detected(context: Context): Set<String> = detectedCache ?: launchable(context)
        .filter { (pkg, label) -> pkg != context.packageName && isBankLike(context, pkg, label) }
        .map { it.first }
        .toSet()
        .also { detectedCache = it }

    /** Các app có biểu tượng ở màn hình chính: (gói, tên hiển thị). */
    fun launchable(context: Context): List<Pair<String, String>> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return runCatching {
            pm.queryIntentActivities(intent, 0)
                .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
                .distinctBy { it.first }
                .filter { it.first != context.packageName }
                .sortedBy { it.second.lowercase() }
        }.getOrDefault(emptyList())
    }

    /** Tên (đã bỏ dấu) → gói: của MỌI app có biểu tượng, và riêng các app trong danh sách ngân hàng. */
    private class Labels(val all: Map<String, String>, val banks: Map<String, String>)

    @Volatile private var labelCache: Labels? = null
    @Volatile private var homeCache: String? = null

    fun invalidate() {
        detectedCache = null
        labelCache = null
        homesCache = null
    }

    /** Ứng dụng màn hình chính (launcher) hiện tại. */
    fun homePackage(context: Context): String? = homeCache ?: runCatching {
        context.packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)?.activityInfo?.packageName
    }.getOrNull().also { homeCache = it }

    /** Chạm vào biểu tượng ở màn hình chính: đoán gói từ tên hiển thị (để tắt Viên thuốc TRƯỚC khi app ngân hàng kịp kiểm tra). */
    fun pkgForLabel(context: Context, label: String): String? {
        val c = labelCache ?: run {
            val list = apps(context)
            val all = HashMap<String, String>()
            launchable(context).forEach { (pkg, name) -> all.putIfAbsent(fold(name), pkg) }
            Labels(all, all.filterValues { it in list })
        }.also { labelCache = it }
        val l = fold(label.substringBefore('\n').substringBefore(','))
        if (l.isEmpty()) return null
        // 1) Trùng đúng tên một app: chỉ tạm tắt nếu CHÍNH app đó nằm trong danh sách ngân hàng
        //    (chạm "Zalo" là Zalo, không bao giờ bị nhầm thành "Zalopay")
        c.all[l]?.let { pkg -> return pkg.takeIf { it in c.banks.values } }
        // 2) Chữ trên biểu tượng ngắn hơn / dài hơn tên app ("Tammi" ↔ "Viettel Tammi", "Xanh SM 2 thông báo"):
        //    phải khớp nguyên từ, khớp đúng MỘT app ngân hàng và không trùng nguyên từ với app thường nào
        fun wordMatch(name: String) = " $name ".contains(" $l ") || " $l ".contains(" $name ")
        val bankHits = c.banks.filterKeys(::wordMatch).values.distinct()
        if (bankHits.size != 1) return null
        val otherHit = c.all.any { (name, pkg) -> pkg !in c.banks.values && wordMatch(name) }
        return if (otherHit) null else bankHits.first()
    }

    /** Chữ thường, bỏ dấu, gộp khoảng trắng — để so tên app trên biểu tượng. */
    private fun fold(s: String): String =
        java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "").replace('đ', 'd').replace(Regex("\\s+"), " ").trim()

    @Volatile private var homesCache: Set<String>? = null

    /** Mọi launcher đã cài (màn hình chính mặc định, launcher phụ, khay ứng dụng). */
    fun homePackages(context: Context): Set<String> = homesCache ?: runCatching {
        context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
            .map { it.activityInfo.packageName }.toSet() + listOfNotNull(homePackage(context))
    }.getOrDefault(emptySet()).also { homesCache = it }

    fun canWriteSecure(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun hasUsageAccess(context: Context): Boolean = runCatching {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        mode == AppOpsManager.MODE_ALLOWED
    }.getOrDefault(false)

    fun shouldPause(context: Context, pkg: String): Boolean {
        val s = SettingsRepo.current
        return s.enabled && s.bankMode && pkg != context.packageName && pkg in apps(context)
    }

    /** Tắt Viên thuốc ngay (trước / khi app ngân hàng mở) và bật dịch vụ canh để tự bật lại. */
    /** Lần chạm biểu tượng gần nhất ở màn hình chính (để chẩn đoán trong trang Ngân hàng & VNeID). */
    val lastClick = kotlinx.coroutines.flow.MutableStateFlow("")

    fun pause(service: AccessibilityService, pkg: String, source: Int = BankWatchService.SOURCE_WINDOW) {
        // Tắt trước tiên (việc gấp nhất), rồi mới bật dịch vụ canh để tự bật lại
        if (canWriteSecure(service)) setAccessibility(service, false)
        BankWatchService.start(service, pkg, source)
        service.disableSelf()
    }

    /** Bật lại Dịch vụ hỗ trợ tiếp cận của Capsule VN (cần WRITE_SECURE_SETTINGS). */
    fun resume(context: Context): Boolean = canWriteSecure(context) && setAccessibility(context, true)

    private fun setAccessibility(context: Context, on: Boolean): Boolean = runCatching {
        val cr = context.contentResolver
        val me = ComponentName(context, IslandService::class.java)
        val current = Settings.Secure.getString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        val others = current.split(':').filter { it.isNotBlank() && ComponentName.unflattenFromString(it) != me }
        val next = if (on) others + me.flattenToString() else others
        Settings.Secure.putString(cr, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, next.joinToString(":"))
        if (on) Settings.Secure.putInt(cr, Settings.Secure.ACCESSIBILITY_ENABLED, 1)
        true
    }.getOrDefault(false)

    // ---------- Cấp quyền 1 chạm qua Shizuku ----------

    fun shizukuRunning(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    fun shizukuGranted(): Boolean = runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }.getOrDefault(false)

    fun requestShizuku(code: Int) {
        runCatching { Shizuku.requestPermission(code) }
    }

    /** Lệnh ADB tương đương, cho người dùng có máy tính. */
    fun adbCommands(context: Context): String =
        "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS\n" +
            "adb shell appops set ${context.packageName} GET_USAGE_STATS allow"

    /** Dùng Shizuku để tự cấp WRITE_SECURE_SETTINGS và quyền truy cập dữ liệu sử dụng. */
    suspend fun grantWithShizuku(context: Context): Boolean = withContext(Dispatchers.IO) {
        val pkg = context.packageName
        val a = shell("pm grant $pkg android.permission.WRITE_SECURE_SETTINGS")
        val b = shell("appops set $pkg GET_USAGE_STATS allow")
        a && b || canWriteSecure(context)
    }

    private fun shell(cmd: String): Boolean = runCatching {
        // Shizuku.newProcess bị ẩn ở API 13 nhưng vẫn hoạt động — gọi qua phản chiếu
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java,
        )
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as java.lang.Process
        p.waitFor() == 0
    }.getOrDefault(false)
}
