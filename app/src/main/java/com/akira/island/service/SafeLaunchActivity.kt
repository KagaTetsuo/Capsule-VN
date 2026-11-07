package com.akira.island.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityManager
import com.akira.island.data.SettingsRepo

/**
 * "Lối tắt mở an toàn" cho app ngân hàng / app hay tự thoát (Xanh SM, Tammi…):
 * tắt Viên thuốc TRƯỚC, chờ Android xác nhận Dịch vụ hỗ trợ tiếp cận đã tắt hẳn, rồi mới mở app.
 * Không còn "cuộc đua" với app kiểm tra Hỗ trợ tiếp cận ngay lúc khởi động -> không bị tự thoát lần đầu.
 */
class SafeLaunchActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsRepo.init(this)
        val pkg = intent?.getStringExtra(EXTRA_PKG)
        val launch = pkg?.let { packageManager.getLaunchIntentForPackage(it) }
        if (pkg == null || launch == null) {
            finish()
            return
        }
        IslandService.instance?.let { BankMode.pause(it, pkg, BankWatchService.SOURCE_SAFE) }
        val am = getSystemService(AccessibilityManager::class.java)
        val started = SystemClock.uptimeMillis()
        var offSince = 0L
        val check = object : Runnable {
            override fun run() {
                val on = runCatching {
                    am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                        ?.any { it.resolveInfo?.serviceInfo?.packageName == packageName } == true
                }.getOrDefault(false)
                val now = SystemClock.uptimeMillis()
                if (!on && offSince == 0L) offSince = now
                // Đã tắt hẳn + chờ thêm một nhịp để hệ thống báo cho mọi app; quá 2,5 giây thì cứ mở
                if ((offSince > 0 && now - offSince >= 300) || now - started > 2_500) {
                    runCatching { startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)) }
                    finish()
                } else {
                    handler.postDelayed(this, 60)
                }
            }
        }
        handler.post(check)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PKG = "pkg"
    }
}
