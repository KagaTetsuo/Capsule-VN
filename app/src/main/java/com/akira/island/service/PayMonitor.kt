package com.akira.island.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.SystemClock
import android.provider.Telephony
import android.service.notification.StatusBarNotification
import com.akira.island.core.IslandBus
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.PayReady
import com.akira.island.core.Tints
import com.akira.island.R
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Ios
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PayMonitor {
    
    private val known = setOf(
        "com.google.android.apps.walletnfcrel", 
        "com.samsung.android.spay", // Samsung Wallet
        "ru.nspk.mirpay", // Mir Pay
        "com.idamob.tinkoff.android", 
        "ru.alfabank.mobile.android", 
        "ru.sberbankmobile", 
        "ru.vtb24.mobilebanking.android", 
        "ru.raiffeisennews", 
        "ru.gazprombank.android.mobilebank.app", 
        "ru.sovcomcard.halva.v1", 
        "ru.mts.money", 
        "com.paypal.android.p2pmobile",
        "com.revolut.revolut",
    )

    private val wallets = setOf(
        "com.google.android.apps.walletnfcrel",
        "com.samsung.android.spay",
        "ru.nspk.mirpay",
    )

    const val PAY_WINDOW_MS = 180_000L

    const val GMS = "com.google.android.gms"

    fun overlay(pkg: String) = pkg == GMS || pkg == "com.android.nfc"

    private val bankish = Regex(
        "bank|banking|sber|tinkoff|tbank|vtb|alfa|raiff|sovcom|halva|gazprom|psbank|otkritie|pochta|rshb|uralsib|" +
            "rosbank|akbars|mkb|yoomoney|qiwi|wallet|\\.pay|pay\\.|mirpay|spay|fintech|" +
            "kaspi|halyk|revolut|paypal|gopay|paytm|phonepe|gcash",
        RegexOption.IGNORE_CASE,
    )

    private val bankSender = Regex(
        "^\\d{3,6}$|bank|ngân hàng|vietcombank|vcb|techcombank|tcb|mbbank|mb|vietinbank|bidv|acb|vpbank|tpbank|sacombank|" +
            "hdbank|vib|shb|ocb|msb|seabank|agribank|lpbank|eximbank|momo|zalopay|vnpay|shopeepay|viettelpay|timo|cake",
        RegexOption.IGNORE_CASE,
    )

    private var readyPkg: String? = null
    
    private var readyShownAt = 0L
    private var readyShownPkg: String? = null
    private var readyTimeout: Job? = null
    private var paidKey: String? = null
    private var paidAt = 0L

    private var nfcApps: Set<String> = emptySet()
    private var nfcCheckedAt = 0L

    fun isPayApp(context: Context, pkg: String): Boolean = pkg in known || pkg in nfcApps(context)

    fun isWallet(pkg: String) = pkg in wallets || pkg == GMS

    private fun nfcApps(context: Context): Set<String> {
        val now = SystemClock.elapsedRealtime()
        if (nfcCheckedAt == 0L || now - nfcCheckedAt > 10 * 60_000) {
            nfcCheckedAt = now
            nfcApps = runCatching {
                context.packageManager.queryIntentServices(Intent(HostApduService.SERVICE_INTERFACE), 0)
                    .mapNotNull { it.serviceInfo?.packageName }
                    .toSet() - context.packageName - "com.android.nfc"
            }.getOrDefault(emptySet())
        }
        return nfcApps
    }

    private fun appFor(pkg: String) = if (pkg == GMS) "com.google.android.apps.walletnfcrel" else pkg

    fun onPayScreen(context: Context, pkg: String) {
        val s = SettingsRepo.current
        if (!s.enabled || !s.evPayReady || IslandBus.payReady.value?.pkg == pkg) return
        readyPkg = pkg
        readyShownAt = SystemClock.elapsedRealtime()
        readyShownPkg = appFor(pkg)
        PayDebug.add(tr("Màn hình thanh toán", "Payment screen"), pkg)
        IslandBus.payReady.value = PayReady(pkg, NotificationParser.icon(context, appFor(pkg)))
        readyTimeout?.cancel()
        readyTimeout = IslandBus.scope.launch {
            delay(PAY_WINDOW_MS)
            clearReady()
        }
    }

    fun onTapAndPay(context: Context) = onPayScreen(context, GMS)

    fun onAppOpened(context: Context, pkg: String) {
        if (SettingsRepo.current.payHintOnOpen && isPayApp(context, pkg)) onPayScreen(context, pkg)
    }

    fun onForeground(pkg: String) {
        val ready = readyPkg ?: return
        if (pkg == ready || overlay(pkg)) return
        clearReady()
    }

    fun clearReady() {
        readyPkg = null
        readyTimeout?.cancel()
        readyTimeout = null
        IslandBus.payReady.value = null
    }

    private val success = Regex(
        "thanh toán|giao dịch|trừ tiền|chi tiêu|paid|payment|purchase|charged|spent|" +
            "pembayaran|transaksi|pago|compra|pagamento|zahlung|achat|paiement|ödeme|satın",
        RegexOption.IGNORE_CASE,
    )
    
    private val skip = Regex(
        "nạp tiền|nhận tiền|hoàn tiền|cashback|refund|từ chối|declined|lỗi|thất bại|" +
            "không thành công|không đủ|nhắc nhở|đến hạn|due|remind|failed|unsuccessful|insufficient|request|received",
        RegexOption.IGNORE_CASE,
    )

    private fun key(sum: String) = sum.filter { it.isDigit() }.trimEnd('0')

    private fun alreadyShown(sum: String) = key(sum) == paidKey && SystemClock.elapsedRealtime() - paidAt < PAY_WINDOW_MS

    private fun showPaid(context: Context, sum: String, caption: String, pkg: String) {
        paidKey = key(sum)
        paidAt = SystemClock.elapsedRealtime()
        readyShownAt = 0L
        clearReady()
        IslandBus.post(
            Notice(
                title = tr("Đã thanh toán", "Paid"),
                state = sum,
                icon = Ios.load(context, R.drawable.ios_checkmark_circle),
                tint = Tints.Green,
                caption = caption,
                style = NoticeStyle.PAY_DONE,
                appIcon = NotificationParser.icon(context, pkg),
                extraMs = 900,
            ),
        )
    }

    fun onScreenPaid(context: Context, pkg: String, sum: String) {
        val s = SettingsRepo.current
        PayDebug.add(tr("Từ màn hình: đã thanh toán $sum", "From the screen: paid $sum"), pkg)
        if (!s.enabled || !s.evPaySuccess || alreadyShown(sum)) return
        val app = appFor(pkg)
        showPaid(context, sum, NotificationParser.appLabel(context, app), app)
    }

    fun onNotification(context: Context, sbn: StatusBarNotification): Boolean {
        val s = SettingsRepo.current
        if (!s.enabled || !s.evPaySuccess || sbn.isOngoing) return false
        val pkg = sbn.packageName
        val n = sbn.notification ?: return false
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return false
        val ex = n.extras ?: return false
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val sms = pkg == defaultSms(context)
        if (sms) {
            if (!bankSender.containsMatchIn(title.trim())) return false
        } else if (!isPayApp(context, pkg) && !bankish.containsMatchIn(pkg)) {
            return false
        }
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        val all = PayScreenParser.normalize("$title $text")
        
        val sum = PayScreenParser.amount(all)
        if (sum == null) {
            PayDebug.add(tr("Không có số tiền — không phải thanh toán", "No amount — not a payment"), pkg, all)
            return false
        }

        val recent = readyShownAt != 0L && SystemClock.elapsedRealtime() - readyShownAt < PAY_WINDOW_MS &&
            (pkg == readyShownPkg || sms || isPayApp(context, pkg))
        val paid = !skip.containsMatchIn(all) && (success.containsMatchIn(all) || pkg in wallets || recent)
        PayDebug.add(if (paid) tr("Đã thanh toán $sum", "Paid $sum") else tr("Không giống một giao dịch thanh toán", "Doesn't look like a payment"), pkg, all)
        if (!paid) return false
        if (alreadyShown(sum)) return true
        showPaid(context, sum, if (sms) title else NotificationParser.appLabel(context, pkg), pkg)
        return true
    }

    private var smsPkg: String? = null
    private var smsCheckedAt = 0L

    private fun defaultSms(context: Context): String? {
        val now = SystemClock.elapsedRealtime()
        if (smsCheckedAt == 0L || now - smsCheckedAt > 60_000) {
            smsCheckedAt = now
            smsPkg = runCatching { Telephony.Sms.getDefaultSmsPackage(context) }.getOrNull()
        }
        return smsPkg
    }
}

object PayDebug {
    val log = MutableStateFlow<List<String>>(emptyList())

    private val clock = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    private var last = ""

    fun add(result: String, pkg: String, text: String = "") {
        
        val what = buildString {
            append(result).append('\n').append(pkg)
            if (text.isNotBlank()) append('\n').append(text.trim().take(200))
        }
        if (what == last) return
        last = what
        log.value = (listOf("${clock.format(Date())} · $what") + log.value).take(15)
    }
}
