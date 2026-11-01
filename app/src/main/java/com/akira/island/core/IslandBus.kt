package com.akira.island.core

import androidx.compose.ui.graphics.Color
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.CoroutineScope
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

enum class CardPreviewMode { NONE, MESSAGE, CALL }

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
    
    val gamingActive = MutableStateFlow(false)
    
    val ramPercent = MutableStateFlow(0)
    val ramGbText = MutableStateFlow("")
    
    val unreadLedColor = MutableStateFlow<Color?>(null)
    val unreadLedPkg = MutableStateFlow<String?>(null)
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
                val duration = SettingsRepo.current.noticeDurationMs + n.extraMs
                
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
