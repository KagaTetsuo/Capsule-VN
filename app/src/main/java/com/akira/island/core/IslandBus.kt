package com.akira.island.core

import androidx.compose.ui.graphics.Color
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class CardPreviewMode { NONE, MESSAGE, CALL, UPDATE, EXPANDED, NOTICE }

object IslandBus {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val serviceRunning = MutableStateFlow(false)
    val expanded = MutableStateFlow(false)
    
    val focus = MutableStateFlow<String?>(null)

    val media = MutableStateFlow<MediaInfo?>(null)
    val lyrics = MutableStateFlow<Lyrics?>(null)
    
    val lyricsStatus = MutableStateFlow<Pair<String, LyricsStatus>?>(null)
    val progress = MutableStateFlow<Map<String, ProgressInfo>>(emptyMap())
    val chrono = MutableStateFlow<Map<String, ChronoInfo>>(emptyMap())
    val calls = MutableStateFlow<Map<String, CallInfo>>(emptyMap())
    
    val callHidden = MutableStateFlow(false)
    
    val commands = MutableSharedFlow<ServiceCommand>(extraBufferCapacity = 4)
    val live = MutableStateFlow<Map<String, LiveInfo>>(emptyMap())
    
    val payReady = MutableStateFlow<PayReady?>(null)
    
    val nextAlarm = MutableStateFlow<AlarmInfo?>(null)
    
    val ringingAlarms = MutableStateFlow<Set<String>>(emptySet())
    val torch = MutableStateFlow(TorchState())
    val timer = MutableStateFlow<TimerState?>(null)
    val stopwatch = MutableStateFlow<StopwatchState?>(null)
    val notice = MutableStateFlow<Notice?>(null)
    
    val reply = MutableStateFlow<ReplyState?>(null)
    
    val glassTilt = MutableStateFlow(0f)
    
    val widthPreviewUntil = MutableStateFlow(0L)
    
    val cardPreviewMode = MutableStateFlow(CardPreviewMode.NONE)
    /** Màn hình cài đặt của app đang hiển thị — xem trước chỉ hiện khi app đang mở, rời app là tự ẩn. */
    val appVisible = MutableStateFlow(false)

    /** Ngón tay vừa chạm Viên thuốc: phóng sẵn cửa sổ để bung mở rộng không bị khựng. */
    val touchDown = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun previewing(): Boolean = appVisible.value && cardPreviewMode.value != CardPreviewMode.NONE
    /** Loại thông báo đang xem trước trong tab "Khác" (null = mẫu chung). */
    val previewNoticeKind = MutableStateFlow<com.akira.island.data.NoticeKind?>(null)

    private var motionDemoJob: Job? = null

    /**
     * Xem trước trực tiếp hiệu ứng mở rộng trên Viên thuốc thật ở đỉnh màn hình:
     * thu gọn (nếu đang mở) -> bung mở rộng theo cấu hình hiện tại -> giữ -> thu gọn lại.
     * Gọi liên tục (khi kéo thanh trượt) sẽ được gộp lại (debounce) để chỉ chạy bản mới nhất.
     */
    fun previewMotion(debounceMs: Long = 260) {
        motionDemoJob?.cancel()
        motionDemoJob = scope.launch {
            delay(debounceMs)
            val s = SettingsRepo.current
            // Thời gian ổn định của lò xo: ~0,25 s (nhanh nhất) .. ~1,15 s (chậm nhất)
            val settle = 250L + s.durationPercent.coerceIn(0, 100) * 9L
            if (cardPreviewMode.value == CardPreviewMode.EXPANDED) {
                cardPreviewMode.value = CardPreviewMode.NONE
                delay(settle + 150)
            }
            cardPreviewMode.value = CardPreviewMode.EXPANDED
            delay(settle + 1_100)
            cardPreviewMode.value = CardPreviewMode.NONE
        }
    }

    fun stopMotionPreview() {
        motionDemoJob?.cancel()
        motionDemoJob = null
        if (cardPreviewMode.value == CardPreviewMode.EXPANDED) cardPreviewMode.value = CardPreviewMode.NONE
    }
    
    val gamingActive = MutableStateFlow(false)
    
    val ramPercent = MutableStateFlow(0)
    val ramGbText = MutableStateFlow("")
    
    val unreadLedColor = MutableStateFlow<Color?>(null)
    val unreadLedPkg = MutableStateFlow<String?>(null)
    /** Tin nhắn chưa đọc — mỗi cuộc trò chuyện một thẻ, mới nhất đứng đầu (chạm Viên thuốc để xem, vuốt qua lại). */
    val unreadNotices = MutableStateFlow<List<Notice>>(emptyList())
    private const val MAX_UNREAD = 6

    fun setUnread(n: Notice) {
        val key = n.message?.key
        unreadNotices.value = (listOf(n) + unreadNotices.value.filter { it.message?.key != key }).take(MAX_UNREAD)
        syncUnreadLed()
    }

    /** Bỏ một cuộc trò chuyện khỏi danh sách chưa đọc (đã mở, đã trả lời hoặc thông báo đã bị xoá). */
    fun removeUnread(key: String) {
        unreadNotices.value = unreadNotices.value.filter { it.message?.key != key }
        syncUnreadLed()
    }

    fun removeUnreadPkg(pkg: String) {
        if (unreadNotices.value.none { it.message?.pkg == pkg } && unreadLedPkg.value != pkg) return
        unreadNotices.value = unreadNotices.value.filter { it.message?.pkg != pkg }
        syncUnreadLed()
    }

    fun clearUnread() {
        unreadNotices.value = emptyList()
        syncUnreadLed()
    }

    // Viền LED lấy màu của cuộc trò chuyện chưa đọc mới nhất
    private fun syncUnreadLed() {
        val top = unreadNotices.value.firstOrNull()
        unreadLedColor.value = top?.tint
        unreadLedPkg.value = top?.message?.pkg
    }
    val previewLed = MutableStateFlow(false)
    
    val otpFillRequests = MutableSharedFlow<String>(extraBufferCapacity = 4)
    
    val noticeHold = MutableStateFlow(false)
    val battery = MutableStateFlow(BatteryInfo())
    val foregroundPackage = MutableStateFlow<String?>(null)
    val landscape = MutableStateFlow(false)
    
    val offSchedule = MutableStateFlow(false)
    
    val screenOn = MutableStateFlow(true)
    
    val powerSave = MutableStateFlow(false)
    
    val locked = MutableStateFlow(false)
    
    val keyguard = MutableStateFlow(false)
    
    val calibrating = MutableStateFlow(false)
    
    val egg = MutableStateFlow(false)
    
    @Volatile
    var lastTouch = 0L

    private val queue = Channel<Notice>(capacity = 6, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        scope.launch {
            for (n in queue) {
                notice.value = n
                val duration = SettingsRepo.current.durationFor(n.durationKey()) + n.extraMs
                
                withTimeoutOrNull(duration) { notice.first { it?.id != n.id } }
                
                if (notice.value?.id == n.id && noticeHold.value) {
                    while (notice.value?.id == n.id && noticeHold.value) delay(200)
                    if (notice.value?.id == n.id) withTimeoutOrNull(1500) { notice.first { it?.id != n.id } }
                }
                if (notice.value?.id == n.id) notice.value = null
                
                delay(110)
            }
        }
    }

    fun post(n: Notice) {
        queue.trySend(n)
    }
}

enum class ServiceCommand { SCREENSHOT, LOCK, TURN_OFF }

/** Phân loại thông báo để áp dụng vị trí / kích thước / thời gian hiển thị riêng. */
fun Notice.kind(): com.akira.island.data.NoticeKind = when {
    style == NoticeStyle.PAY_DONE -> com.akira.island.data.NoticeKind.PAYMENT
    style == NoticeStyle.LIVE -> com.akira.island.data.NoticeKind.NAV
    style == NoticeStyle.ALARM -> com.akira.island.data.NoticeKind.ALARM
    style == NoticeStyle.PILL -> com.akira.island.data.NoticeKind.MODE
    style == NoticeStyle.SENT -> com.akira.island.data.NoticeKind.SENT
    battery != null -> com.akira.island.data.NoticeKind.CHARGING
    ring != null -> com.akira.island.data.NoticeKind.AUDIO
    else -> com.akira.island.data.NoticeKind.OTHER
}

/** Khoá thời gian hiển thị: tin nhắn, cập nhật hoặc theo loại. */
fun Notice.durationKey(): String = when (style) {
    NoticeStyle.MESSAGE -> "MESSAGE"
    NoticeStyle.UPDATE -> "UPDATE"
    else -> kind().name
}
