package com.akira.island.service

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object PayScreenParser {
    private val spaces = Regex("[\\u00A0\\u202F\\u2007\\u2009]")

    private const val NUMBER = """\d{1,3}(?:[ .,]?\d{3})*(?:[.,]\d{1,2})?"""
    
    private const val CURRENCY = """đ|vnđ|vnd|USD|EUR|GBP|JPY|KRW|CNY|SGD|THB|MYR|IDR|PHP|INR|AUD|CAD|CHF|BRL|""" +
        """AMD|GEL|UZS|AZN|KGS|€|£|₸|₴|₹|₱|₩|¥|฿|₫|₺|\$"""
    
    private const val PREFIX = """[$€£₹₱₩¥฿₫₺₸]|R\$|Rp|RM|RUB|USD|EUR|INR|IDR|PHP|MYR|THB|KZT"""

    private val charge = Regex(
        """(?<![\p{L}\p{N}])[−–-]\s?(?:($NUMBER)\s?($CURRENCY)?|($PREFIX)\s?($NUMBER))""",
        RegexOption.IGNORE_CASE,
    )

    private val amount = Regex(
        """(?<![\d*])$NUMBER\s?(?:$CURRENCY)|(?<!\p{L})(?:$PREFIX)\s?$NUMBER""",
        RegexOption.IGNORE_CASE,
    )

    private val readyWords = Regex(
        "(đưa|chạm|đặt)\\S*\\s+(\\S+\\s+){0,4}?(gần|vào)\\s+(\\S+\\s+)?(máy|đầu đọc|thiết bị)|" +
            "hold\\s+(\\S+\\s+){0,4}?(to|near|close to|against)\\s+(the\\s+)?(payment\\s+)?(terminal|reader)|" +
            "tap\\s+(\\S+\\s+){0,3}?(to|on)\\s+(the\\s+)?(terminal|reader)|ready to pay|sẵn sàng thanh toán",
        RegexOption.IGNORE_CASE,
    )

    private val paidWords = Regex(
        "đã thanh toán|thanh toán thành công|giao dịch thành công|payment successful|payment complete|" +
            "payment approved|\\bpaid\\b|pembayaran berhasil|pago realizado|pagamento aprovado",
        RegexOption.IGNORE_CASE,
    )

    private val failureWords = Regex(
        "không thành công|thất bại|từ chối|lỗi|không đủ số dư|declined|failed|error|insufficient|unsuccess|try again|thử lại",
        RegexOption.IGNORE_CASE,
    )

    fun charges(text: String): List<String> {
        val t = normalize(text)
        return charge.findAll(t).mapNotNull { m ->
            val (num, cur, preCur, preNum) = m.destructured
            when {
                preNum.isNotEmpty() -> "$preCur$preNum"
                cur.isNotEmpty() -> "$num ${cur.trimEnd('.')}"
                num.contains(',') || num.contains('.') -> num
                else -> null
            }
        }.toList()
    }

    fun normalize(text: String) = text.replace(spaces, " ")

    fun amount(text: String): String? = amount.find(normalize(text))?.value?.trim()

    fun ready(text: String) = readyWords.containsMatchIn(normalize(text))

    fun paid(text: String) = paidWords.containsMatchIn(text) && !failureWords.containsMatchIn(text)

    fun failure(text: String) = failureWords.containsMatchIn(text)
}

class PayScreenReader(private val service: AccessibilityService, private val scope: CoroutineScope) {
    val active: Boolean get() = current != null

    private var current: String? = null
    private var since = 0L
    private var readyAt = 0L
    private var baseline: MutableSet<String>? = null
    private var done = false
    
    private var lastFresh: List<String>? = null
    private var lastScan = 0L
    private var scanJob: Job? = null
    private var stopJob: Job? = null

    fun onForeground(pkg: String, cls: String) {
        
        if (current != null && PayMonitor.overlay(pkg)) return
        if (pkg == current) {
            if (payWindow(cls)) markReady(pkg)
            schedule(150)
            return
        }
        stop()
        if (!PayMonitor.isPayApp(service, pkg)) return
        start(pkg)
        if (payWindow(cls)) markReady(pkg)
    }

    fun onTapAndPay() {
        if (current != PayMonitor.GMS) {
            stop()
            start(PayMonitor.GMS)
        }
        markReady(PayMonitor.GMS)
        stopJob?.cancel()
        stopJob = scope.launch {
            delay(60_000)
            if (current == PayMonitor.GMS) stop()
        }
    }

    fun onContentChanged(event: AccessibilityEvent) {
        if (done || event.packageName?.toString() != current) return
        schedule(if (readyAt != 0L) 150 else 400)
    }

    fun stop() {
        scanJob?.cancel()
        stopJob?.cancel()
        scanJob = null
        current = null
        baseline = null
    }

    private fun start(pkg: String) {
        val s = SettingsRepo.current
        if (!s.enabled || (!s.evPayReady && !s.evPaySuccess)) return
        current = pkg
        since = SystemClock.elapsedRealtime()
        readyAt = 0L
        baseline = null
        lastFresh = null
        done = false
        schedule(300)
    }

    private fun payWindow(cls: String) = PayWindow.containsMatchIn(cls)

    private fun markReady(pkg: String, texts: Set<String>? = null) {
        if (readyAt == 0L) {
            readyAt = SystemClock.elapsedRealtime()
            baseline = texts?.toMutableSet() ?: HashSet()
        }
        PayMonitor.onPayScreen(service, pkg)
    }

    private fun schedule(wait: Long) {
        if (scanJob?.isActive == true) return
        
        val gap = if (readyAt != 0L) 0L else (400 - (SystemClock.elapsedRealtime() - lastScan)).coerceAtLeast(0)
        scanJob = scope.launch {
            delay(maxOf(wait, gap))
            runCatching { scan() }
        }
    }

    private fun scan() {
        val pkg = current ?: return
        if (done) return
        val now = SystemClock.elapsedRealtime()
        lastScan = now
        
        if (readyAt != 0L && now - readyAt > PayMonitor.PAY_WINDOW_MS) {
            done = true
            return
        }
        val root = rootFor(pkg) ?: return
        val texts = LinkedHashSet<String>()
        collect(root, texts, 0)

        if (readyAt == 0L && texts.any { PayScreenParser.ready(it) }) {
            markReady(pkg, texts)
            return
        }
        val all = texts.flatMap { PayScreenParser.charges(it) }.distinct()
        if (readyAt != 0L) {
            val base = baseline ?: return
            
            if (now - readyAt < 500) {
                base += texts
                return
            }
            val fresh = texts.filter { it !in base }
            
            if (fresh == lastFresh) return
            lastFresh = fresh
            if (fresh.any { PayScreenParser.failure(it) }) return
            val freshCharges = fresh.flatMap { PayScreenParser.charges(it) }.distinct()

            if (all.size == 1 && freshCharges.size == 1 &&
                (PayMonitor.isWallet(pkg) || texts.any { PayScreenParser.paid(it) || PayScreenParser.ready(it) })
            ) {
                return paid(pkg, freshCharges[0])
            }
            
            if (fresh.any { PayScreenParser.paid(it) }) {
                val sums = fresh.mapNotNull { PayScreenParser.amount(it) }.distinct()
                if (sums.size == 1) return paid(pkg, sums[0])
                PayDebug.add(tr("“Đã thanh toán” trên màn hình, nhưng số tiền: ${sums.size}", "“Paid” on screen, but amounts: ${sums.size}"), pkg, fresh.joinToString(" | "))
            }
        } else if (now - since < 4_000 && all.size == 1 && texts.any { PayScreenParser.paid(it) } && texts.none { PayScreenParser.failure(it) }) {
            
            paid(pkg, all[0])
        }
    }

    private fun paid(pkg: String, sum: String) {
        done = true
        PayMonitor.onScreenPaid(service, pkg, sum)
    }

    private fun rootFor(pkg: String): AccessibilityNodeInfo? =
        service.rootInActiveWindow?.takeIf { it.packageName?.toString() == pkg }
            ?: runCatching { service.windows.firstNotNullOfOrNull { w -> w.root?.takeIf { it.packageName?.toString() == pkg } } }.getOrNull()

    private fun collect(node: AccessibilityNodeInfo, out: MutableSet<String>, depth: Int) {
        if (depth > 30 || out.size > 300) return
        if (node.isVisibleToUser) {
            node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() && it.length < 200 }?.let { out += it }
            node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() && it.length < 200 }?.let { out += it }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collect(child, out, depth + 1)
        }
    }

    private companion object {
        val PayWindow = Regex("nfc|hce|contactless|tapandpay|tap_and_pay|tappay|tpay|sberpay|mirpay", RegexOption.IGNORE_CASE)
    }
}
