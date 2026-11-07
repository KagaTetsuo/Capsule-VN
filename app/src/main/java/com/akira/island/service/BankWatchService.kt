package com.akira.island.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.akira.island.R
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Chạy trong lúc Viên thuốc tạm nghỉ vì đang dùng app ngân hàng / VNeID.
 * Theo dõi ứng dụng đang mở (Truy cập dữ liệu sử dụng); khi người dùng rời các app đó thì bật lại Viên thuốc.
 */
class BankWatchService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var watch: Job? = null
    private var bankPkg: String? = null
    private var pausedAt = 0L
    private var source = SOURCE_WINDOW

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SettingsRepo.init(this)
        if (intent?.action == ACTION_RESUME) {
            resumeIsland()
            return START_NOT_STICKY
        }
        bankPkg = intent?.getStringExtra(EXTRA_PKG) ?: bankPkg
        source = intent?.getIntExtra(EXTRA_SOURCE, SOURCE_WINDOW) ?: SOURCE_WINDOW
        pausedAt = System.currentTimeMillis()
        val shown = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(ID, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(ID, notification())
            }
        }.isSuccess
        if (!shown) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (watch?.isActive != true) {
            watch = scope.launch {
                // App ngân hàng có thể kịp tự đóng lần đầu (phát hiện Hỗ trợ tiếp cận trước khi Viên thuốc tắt):
                // lúc này dịch vụ đã tắt -> mở lại app ngân hàng cho người dùng
                // Kiểm tra 2 lần (app tự thoát nhanh hoặc chậm sau hộp thoại cảnh báo); mở lại tối đa 1 lần
                delay(1_500)
                if (!relaunchIfClosed()) {
                    delay(1_700)
                    relaunchIfClosed()
                }
                if (BankMode.hasUsageAccess(this@BankWatchService)) watchForeground()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun watchForeground() {
        val usage = getSystemService(UsageStatsManager::class.java) ?: return
        val power = getSystemService(PowerManager::class.java)
        val apps = BankMode.apps(this)
        // Chờ app ngân hàng thật sự lên màn hình rồi mới bắt đầu canh
        delay(2_200)
        var since = System.currentTimeMillis() - 1_500
        var lastPkg: String? = bankPkg
        while (scope.isActive) {
            if (power?.isInteractive != false) {
                val now = System.currentTimeMillis()
                val events = usage.queryEvents(since, now)
                val e = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(e)
                    @Suppress("DEPRECATION")
                    if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) lastPkg = e.packageName
                }
                since = now - 1_000
                val pkg = lastPkg
                if (pkg != null && pkg !in apps && pkg != packageName && pkg != "com.android.systemui" && pkg !in TRANSIENT) {
                    resumeIsland()
                    return
                }
            }
            delay(1_200)
        }
    }

    /** Ứng dụng đang ở trên cùng (theo Truy cập dữ liệu sử dụng), null nếu không đọc được. */
    private fun foregroundPkg(): String? {
        if (!BankMode.hasUsageAccess(this)) return null
        val usage = getSystemService(UsageStatsManager::class.java) ?: return null
        val now = System.currentTimeMillis()
        val events = usage.queryEvents(now - 10_000, now)
        val e = UsageEvents.Event()
        var last: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            @Suppress("DEPRECATION")
            if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) last = e.packageName
        }
        return last
    }

    /**
     * Mở lại app ngân hàng nếu nó tự thoát lần đầu (kịp thấy Hỗ trợ tiếp cận trước khi Viên thuốc tắt).
     * Chỉ khi chắc người dùng đang mở ĐÚNG app này (vừa chạm biểu tượng của nó / cửa sổ của nó vừa hiện)
     * và giờ nó không còn trên màn hình — đang ở app khác thì không làm gì. Trả về true nếu đã mở lại.
     */
    private fun relaunchIfClosed(): Boolean {
        val pkg = bankPkg ?: return false
        if (source == SOURCE_SAFE) return false
        if (BankMode.hasUsageAccess(this)) {
            val usage = getSystemService(UsageStatsManager::class.java) ?: return false
            val events = usage.queryEvents(pausedAt - 500, System.currentTimeMillis())
            val e = UsageEvents.Event()
            var last: String? = null
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                @Suppress("DEPRECATION")
                if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) last = e.packageName
            }
            // Còn trên màn hình -> ổn; đang ở app khác -> người dùng đã đi nơi khác
            if (last == pkg) return false
            if (last != null && last !in BankMode.homePackages(this) && last !in TRANSIENT) return false
        }
        val launch = packageManager.getLaunchIntentForPackage(pkg)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) ?: return false
        if (android.provider.Settings.canDrawOverlays(this)) {
            // Có quyền "Hiển thị trên ứng dụng khác": tự mở lại ngay (app còn chạy thì chỉ đưa lên trước, không sao)
            runCatching { startActivity(launch) }
            return true
        } else {
            // Không tự mở được -> nhắc 1 chạm để mở lại (chỉ khi biết chắc app đã thoát)
            if (!BankMode.hasUsageAccess(this)) return false
            val nm = getSystemService(NotificationManager::class.java) ?: return false
            ensureRemindChannel(nm)
            val label = NotificationParser.appLabel(this, pkg)
            val pi = PendingIntent.getActivity(this, 3, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val n = Notification.Builder(this, REMIND_CHANNEL)
                .setSmallIcon(R.drawable.ic_stat_capsule)
                .setContentTitle(tr("Mở lại $label", "Open $label again"))
                .setContentText(tr("Viên thuốc đã tạm nghỉ — chạm để mở lại $label", "The island is paused — tap to open $label again"))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setTimeoutAfter(15_000)
                .build()
            runCatching { nm.notify(RELAUNCH_ID, n) }
            return true
        }
    }

    private fun ensureRemindChannel(nm: NotificationManager) {
        nm.createNotificationChannel(
            NotificationChannel(REMIND_CHANNEL, tr("Nhắc bật lại Viên thuốc", "Turn the island back on"), NotificationManager.IMPORTANCE_HIGH).apply {
                description = tr("Hiện khi bạn rời ứng dụng ngân hàng / VNeID", "Shown when you leave a bank / ID app")
                setShowBadge(false)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 90, 70, 90)
                setSound(null, null)
            },
        )
    }

    private fun resumeIsland() {
        if (!BankMode.resume(this)) {
            // Chưa có quyền tự bật lại -> hiện nhắc nhở nổi: chạm 1 lần là tới đúng chỗ bật Viên thuốc
            remind()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun remind() {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(
            NotificationChannel(REMIND_CHANNEL, tr("Nhắc bật lại Viên thuốc", "Turn the island back on"), NotificationManager.IMPORTANCE_HIGH).apply {
                description = tr("Hiện khi bạn rời ứng dụng ngân hàng / VNeID", "Shown when you leave a bank / ID app")
                setShowBadge(false)
                // Rung nhẹ 2 nhịp ngắn để dễ để ý mà không gây phiền
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 90, 70, 90)
                setSound(null, null)
            },
        )
        val n = Notification.Builder(this, REMIND_CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_capsule)
            .setContentTitle(tr("Bật lại Viên thuốc?", "Turn the island back on?"))
            .setContentText(tr("Bạn đã rời ứng dụng — chạm để bật lại trong Hỗ trợ tiếp cận", "You left the app — tap to turn it back on in Accessibility"))
            .setContentIntent(accessibilityIntent())
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()
        runCatching { nm.notify(REMIND_ID, n) }
    }

    /** Mở thẳng trang Capsule VN trong Hỗ trợ tiếp cận (Android 11+), máy cũ mở danh sách chung. */
    private fun accessibilityIntent(): PendingIntent {
        val me = android.content.ComponentName(this, IslandService::class.java).flattenToString()
        val detail = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
            .putExtra(Intent.EXTRA_COMPONENT_NAME, me)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && detail.resolveActivity(packageManager) != null) detail
        else Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun notification(manual: Boolean = false): Notification {
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            NotificationChannel(CHANNEL, tr("Chế độ ngân hàng", "Banking mode"), NotificationManager.IMPORTANCE_LOW).apply {
                description = tr("Viên thuốc tạm nghỉ khi dùng ngân hàng / VNeID", "The island pauses while you use a bank / ID app")
                setShowBadge(false)
            },
        )
        val label = bankPkg?.let { NotificationParser.appLabel(this, it) } ?: tr("ngân hàng", "a bank app")
        val resume = PendingIntent.getService(
            this, 1, Intent(this, BankWatchService::class.java).setAction(ACTION_RESUME), PendingIntent.FLAG_IMMUTABLE,
        )
        val openA11y = accessibilityIntent()
        val auto = BankMode.canWriteSecure(this)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_capsule)
            .setContentTitle(tr("Viên thuốc tạm nghỉ khi dùng $label", "The island is paused for $label"))
            .setContentText(
                when {
                    manual -> tr("Chạm để bật lại Viên thuốc trong Hỗ trợ tiếp cận", "Tap to turn the island back on in Accessibility")
                    auto && BankMode.hasUsageAccess(this) -> tr("Tự bật lại khi bạn rời ứng dụng", "Turns back on when you leave the app")
                    auto -> tr("Bấm “Bật lại ngay” khi dùng xong", "Tap “Turn on now” when you're done")
                    BankMode.hasUsageAccess(this) -> tr("Rời ứng dụng sẽ có nhắc bật lại — hoặc chạm vào đây", "You'll get a reminder when you leave — or tap here")
                    else -> tr("Dùng xong, chạm vào đây để bật lại Viên thuốc", "When you're done, tap here to turn the island back on")
                },
            )
            .setContentIntent(if (auto && !manual) resume else openA11y)
            .apply { if (auto) addAction(Notification.Action.Builder(null, tr("Bật lại ngay", "Turn on now"), resume).build()) }
            .setOngoing(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ID = 11
        private const val REMIND_ID = 12
        private const val RELAUNCH_ID = 13
        private const val REMIND_CHANNEL = "bank_mode_remind_v2"
        private const val CHANNEL = "bank_mode"
        private const val ACTION_RESUME = "com.akira.island.BANK_RESUME"
        private const val EXTRA_PKG = "pkg"
        private const val EXTRA_SOURCE = "source"
        /** Tạm tắt vì: cửa sổ app ngân hàng hiện lên / chạm biểu tượng ở màn hình chính / lối tắt mở an toàn. */
        const val SOURCE_WINDOW = 0
        const val SOURCE_CLICK = 1
        const val SOURCE_SAFE = 2

        // Màn hình chồng tạm thời không tính là "rời app ngân hàng" (bàn phím, xác thực sinh trắc, NFC…)
        private val TRANSIENT = setOf(
            "com.google.android.inputmethod.latin", "com.samsung.android.honeyboard", "com.google.android.gms",
            "com.android.nfc", "com.samsung.android.biometrics.app.setting", "com.android.permissioncontroller",
            "com.google.android.permissioncontroller",
        )

        fun start(context: Context, pkg: String, source: Int = SOURCE_WINDOW) {
            runCatching {
                context.startForegroundService(
                    Intent(context, BankWatchService::class.java).putExtra(EXTRA_PKG, pkg).putExtra(EXTRA_SOURCE, source),
                )
            }
        }
    }
}
