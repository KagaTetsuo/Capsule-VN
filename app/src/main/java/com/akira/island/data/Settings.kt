package com.akira.island.data

import android.content.Context
import android.content.SharedPreferences
import com.akira.island.core.tr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class FxRate(val fps: Int, private val ru: String, private val en: String) {
    ECO(30, "30 khung hình — tiết kiệm pin", "30 fps — saves power"),
    SMOOTH(60, "60 khung hình — mượt mà", "60 fps — smooth"),
    MAX(0, "Theo tần số màn hình — tới 120", "Screen rate — up to 120"),
    ;

    val title: String get() = tr(ru, en)
}

/** Kiểu nền cho các thẻ thông báo (tin nhắn, cuộc gọi đến, cập nhật). */
@Serializable
enum class CardBg(private val ru: String, private val en: String) {
    DEFAULT("Như Viên thuốc", "Same as the island"),
    COLOR("Màu tuỳ chọn", "Custom color"),
    IMAGE("Ảnh nền", "Background image"),
    GLASS("Liquid Glass (iOS)", "Liquid Glass (iOS)"),
    ;

    val title: String get() = tr(ru, en)
}

@Serializable
enum class ExpandStyle(private val ru: String, private val en: String) {
    FRUIT("Như Táo khuyết (iOS)", "Like a Fruit"),
    SPRING("Lò xo nảy", "Spring"),
    SMOOTH("Mượt mà", "Smooth"),
    INSTANT("Không hiệu ứng", "No animation"),
    ;

    val title: String get() = tr(ru, en)
}

@Serializable
enum class Elasticity(private val ru: String, private val en: String, val damping: Float) {
    NONE("Không đàn hồi", "No bounce", 1f),
    LOW("Đàn hồi nhẹ", "Light bounce", 0.8f),
    MEDIUM("Đàn hồi vừa", "Medium bounce", 0.62f),
    HIGH("Đàn hồi mạnh", "Strong bounce", 0.48f),
    ;

    val title: String get() = tr(ru, en)
}

@Serializable
enum class TapAction(private val ru: String, private val en: String, private val hintRu: String, private val hintEn: String) {
    EXPAND("Mở rộng viên thuốc", "Expand the capsule", "Xem chi tiết hoạt động; thông báo thì mở ứng dụng", "See details; for alerts opens the app"),
    OPEN_APP("Mở ứng dụng", "Open the app", "Mở app của hoạt động / thông báo; Viên thuốc trống mở Capsule VN", "Opens the app of the activity / alert; empty island opens Capsule VN"),
    PLAY_PAUSE("Phát / Tạm dừng", "Play / pause", "Điều khiển nhạc đang phát; không có nhạc thì mở rộng", "Controls music; without music it expands"),
    NEXT_TRACK("Bài tiếp theo", "Next track", "Chuyển bài; không có nhạc thì mở rộng", "Skips the track; without music it expands"),
    SCREENSHOT("Chụp màn hình (ẩn Viên thuốc)", "Screenshot without the island", "Viên thuốc tự ẩn để không dính vào ảnh", "The island hides so it's not in the shot"),
    NOTHING("Không làm gì", "Nothing", "Bỏ qua cử chỉ này", "Ignore this gesture"),
    ;

    val title: String get() = tr(ru, en)
    val hint: String get() = tr(hintRu, hintEn)
}

@Serializable
enum class QuickAction(private val ru: String, private val en: String, private val shortRu: String, private val shortEn: String) {
    TIMER("Hẹn giờ", "Timer", "Hẹn giờ", "Timer"),
    STOPWATCH("Đồng hồ bấm giờ", "Stopwatch", "Bấm giờ", "Stopwatch"),
    FLASHLIGHT("Đèn pin", "Flashlight", "Đèn pin", "Flashlight"),
    SCREENSHOT("Chụp màn hình (ẩn Viên thuốc)", "Screenshot without the island", "Chụp MH", "Screenshot"),
    CAMERA("Máy ảnh", "Camera", "Máy ảnh", "Camera"),
    LOCK("Khóa màn hình", "Lock the screen", "Khóa", "Lock"),
    ;

    val title: String get() = tr(ru, en)
    val short: String get() = tr(shortRu, shortEn)
}

@Serializable
enum class AccentMode(private val ru: String, private val en: String, private val hintRu: String, private val hintEn: String) {
    DEFAULT("Tím oải hương", "Lavender", "Màu mặc định của ứng dụng", "The app's color"),
    SYSTEM("Theo hệ thống", "System", "Lấy màu từ hình nền Android 12+ (Material You)", "From your Android 12+ wallpaper (Material You)"),
    ADAPTIVE("Từ ảnh đại diện", "From the avatar", "Tự động phối theo ảnh đại diện hoặc biểu tượng ứng dụng", "Matched to the avatar or app icon"),
    CUSTOM("Tùy chỉnh", "Custom", "Chọn bất kỳ màu nào từ bảng màu", "Any color from the palette"),
    ;

    val title: String get() = tr(ru, en)
    val hint: String get() = tr(hintRu, hintEn)
}

@Serializable
enum class LedStyle(private val ru: String, private val en: String) {
    ROTATE_RGB("Cầu vồng RGB xoay 360°", "Rainbow RGB 360°"),
    AUTO_APP("Theo màu ứng dụng / sự kiện", "App / Event adaptive"),
    CYBER_NEON("Neon Cyber (Xanh - Tím - Đỏ)", "Cyber Neon"),
    BREATHE_GOLD("Nhịp thở Vàng Hoàng Gia", "Breathing Gold"),
    CUSTOM("Màu đơn tự chọn", "Single custom color"),
    MULTI("2–3 màu tự chọn", "2–3 custom colors"),
    ;

    val title: String get() = tr(ru, en)
}

/** Nhóm "thông báo khác" để chỉnh vị trí / kích thước riêng từng loại. */
@Serializable
enum class NoticeKind(private val ru: String, private val en: String) {
    CHARGING("Sạc pin", "Charging"),
    AUDIO("Tai nghe & Bluetooth", "Headphones & Bluetooth"),
    PAYMENT("Thanh toán", "Payments"),
    NAV("Điều hướng", "Navigation"),
    ALARM("Báo thức", "Alarm"),
    MODE("Viên nhỏ (chế độ chuông, im lặng, rung…)", "Small pills (ring mode, silent, vibrate…)"),
    SENT("Đã gửi / Đã sao chép", "Sent / Copied"),
    OTHER("Sự kiện khác", "Other events"),
    ;

    val title: String get() = tr(ru, en)
}

/** Vị trí / kích thước riêng của một nhóm thông báo (width 0 = tự động, corner -1 = tự động). */
@Serializable
data class NoticeTweak(
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val width: Int = 0,
    val heightExtra: Int = 0,
    val corner: Int = -1,
)

/** Kiểu chuyển động của viền sáng. */
@Serializable
enum class LedMotion(private val ru: String, private val en: String) {
    ROTATE("Xoay 360°", "Rotate 360°"),
    COMET("Sao chổi chạy quanh viền", "Comet chase"),
    BREATHE("Nhịp thở", "Breathing"),
    BLINK("Nhấp nháy", "Blink"),
    STATIC("Đứng yên", "Static"),
    ;

    val title: String get() = tr(ru, en)
}

@Serializable
enum class AppLanguage {
    SYSTEM, VI, EN;

    val title: String
        get() = when (this) {
            SYSTEM -> tr("Theo hệ thống", "System default")
            VI -> "Tiếng Việt"
            EN -> "English"
        }
}

@Serializable
data class IslandSettings(
    val enabled: Boolean = true,
    
    val language: AppLanguage = AppLanguage.SYSTEM,

    val cardMedia: Boolean = true,
    val cardTorch: Boolean = true,
    val cardProgress: Boolean = true,
    val cardTimer: Boolean = true,
    val cardCharging: Boolean = true,
    val cardCall: Boolean = true,
    val cardMessages: Boolean = true,
    
    val msgActions: Boolean = true,
    
    val cardLive: Boolean = true,
    
    val cardAlarm: Boolean = true,
    
    val clockAutoTap: Boolean = true,
    
    val timerFromAssistant: Boolean = true,
    
    val wakeForAlerts: Boolean = true,
    /** Màn hình đang tắt mà có thông báo: sáng lên hiện trên Viên thuốc rồi tự tắt lại. */
    val wakeOnMessage: Boolean = false,
    /** Giờ ngủ: không tự sáng màn hình vì thông báo trong khoảng này. */
    val wakeQuietOn: Boolean = true,
    val wakeQuietFrom: Int = 23,
    val wakeQuietTo: Int = 6,
    
    val easterEggs: Boolean = true,

    val evChargeStart: Boolean = true,
    val evChargeStop: Boolean = true,
    val evLowBattery: Boolean = true,
    val evWifiOn: Boolean = true,
    val evWifiOff: Boolean = true,
    val evHeadsetOn: Boolean = true,
    val evHeadsetOff: Boolean = true,
    val evAirplaneOn: Boolean = true,
    val evAirplaneOff: Boolean = true,
    val evRinger: Boolean = true,
    val evBrightness: Boolean = true,
    val evUsbOn: Boolean = true,
    val evUsbOff: Boolean = true,
    val evUnlock: Boolean = false,
    
    val evLock: Boolean = true,
    val evDnd: Boolean = true,
    
    val evPayReady: Boolean = true,
    
    val payHintOnOpen: Boolean = false,
    
    val evPaySuccess: Boolean = true,
    
    val evAlarmSet: Boolean = true,

    
    val width: Int = 104,
    val height: Int = 31,
    val offsetX: Int = 0,
    val offsetY: Int = 11,
    
    val notch: Boolean = false,
    
    val cameraGap: Int = 34,
    
    val activeExtra: Int = 6,
    
    val activeWidth: Int = 40,
    
    val layoutVersion: Int = 1,

    val msgOffsetY: Int = 0,
    val msgWidth: Int = 380,
    val msgHeightExtra: Int = 0,

    val callOffsetY: Int = 0,
    val callWidth: Int = 370,
    val callHeight: Int = 82,

    val updateOffsetY: Int = 0,
    val updateWidth: Int = 380,
    val updateHeightExtra: Int = 0,

    val expandedOffsetY: Int = 0,
    // Viên thuốc khi có hoạt động (nhạc, hẹn giờ…): dịch vị trí riêng
    val activeOffsetX: Int = 0,
    val activeOffsetY: Int = 0,
    // Các thông báo khác (sạc, tai nghe, thanh toán, sự kiện…): vị trí, kích thước, bo góc riêng
    val noticeOffsetX: Int = 0,
    val noticeOffsetY: Int = 0,
    val noticeWidth: Int = 0,
    val noticeHeightExtra: Int = 0,
    val noticeCornerRadius: Int = -1,
    /** Chỉnh riêng từng loại (chưa có thì dùng chung các giá trị notice* ở trên). */
    val noticeTweaks: Map<NoticeKind, NoticeTweak> = emptyMap(),
    val expandedWidth: Int = 0,
    val expandedHeightExtra: Int = 0,

    val capsuleCornerRadius: Int = -1,
    val expandedCornerRadius: Int = 44,
    val cardCornerRadius: Int = 32,
    val callCornerRadius: Int = 32,
    val updateCornerRadius: Int = 32,

    // Nền & viền của các thẻ thông báo
    val cardBg: CardBg = CardBg.DEFAULT,
    val cardBgColor: Long = 0xFF1F1719,
    val cardBgOpacity: Int = 100,
    val cardBgImage: String = "",
    val cardImageDim: Int = 40,
    val cardBorder: Boolean = false,
    val cardBorderColor: Long = 0xFFE6C068,
    val cardBorderWidth: Float = 1.5f,
    /** Áp dụng nền & viền thẻ cho cả bảng mở rộng (Giờ, Pin, nhạc, hẹn giờ…). */
    val cardStyleExpanded: Boolean = true,
    /** Áp dụng cho cả thông báo dạng viên nhỏ (sạc, tai nghe, chế độ chuông, thanh toán…). Mặc định giữ nền đen như cũ. */
    val cardStylePills: Boolean = false,
    /** Áp dụng nền & viền cho cả Viên thuốc khi không có hoạt động. */
    val cardStyleIdle: Boolean = false,

    val color: Long = 0xFF000000,
    val outline: Boolean = false,
    val outlineColor: Long = 0xFF3A3A40,
    val outlineWidth: Float = 1f,
    val outlineFromArt: Boolean = true,
    val shadow: Boolean = false,

    val accentMode: AccentMode = AccentMode.DEFAULT,
    val accentColor: Long = 0xFF0A84FF,

    val autoUpdateCheck: Boolean = true,
    
    val skippedVersion: String = "",
    
    val lastSeenVersion: String = "",
    val lastUpdateCheck: Long = 0,
    
    val islandVersion: String = "",

    val liquidGlass: Boolean = false,
    val glassOpacity: Int = 62,
    val glassRefraction: Int = 80,
    val glassSpecular: Int = 85,

    val expandStyle: ExpandStyle = ExpandStyle.FRUIT,
    val durationPercent: Int = 35,
    
    val liquidExpand: Boolean = false,
    
    val elasticity: Elasticity = Elasticity.LOW,
    val reduceEffects: Boolean = false,
    val haptics: Boolean = true,

    
    val fxBlur: Boolean = true,
    
    val fxEqualizer: Boolean = true,
    
    val fxRate: FxRate = FxRate.SMOOTH,
    
    val fxGlow: Boolean = false,
    
    val fxPay: Boolean = true,
    
    val fxShake: Boolean = true,
    
    val batterySaver: Boolean = false,

    val tapAction: TapAction = TapAction.EXPAND,
    val doubleTapAction: TapAction = TapAction.NOTHING,
    val longPressAction: TapAction = TapAction.OPEN_APP,
    val swipeTracks: Boolean = true,
    /** Chế độ ngân hàng: tự tắt Viên thuốc khi mở app ngân hàng / VNeID, tự bật lại khi rời app. */
    val bankMode: Boolean = false,
    val bankApps: List<String> = emptyList(),
    val bankAppsEdited: Boolean = false,
    /** Vuốt ngang trên Viên thuốc thu gọn để chuyển giữa các hoạt động đang chạy. */
    val swipeSwitch: Boolean = false,
    val swipeExpand: Boolean = true,

    val lyricsEnabled: Boolean = true,
    /** Nguồn lời dự phòng NetEase (máy chủ ở Trung Quốc) — người dùng có thể tắt để chỉ dùng LRCLIB. */
    val lyricsNetease: Boolean = true,

    val hideIdle: Boolean = false,
    val hideInLandscape: Boolean = true,
    val hideWhenPlayerOpen: Boolean = true,
    
    val scheduleOn: Boolean = false,
    val scheduleFrom: Int = 10,
    val scheduleTo: Int = 23,
    val noticeDurationMs: Int = 2500,
    /** Thời gian hiển thị riêng từng loại (khoá: MESSAGE, UPDATE hoặc tên NoticeKind); chưa đặt thì dùng noticeDurationMs. */
    val noticeDurations: Map<String, Int> = emptyMap(),
    val timerMinutes: Int = 5,
    
    val autoCollapseSec: Int = 0,
    
    val bubble: Boolean = true,
    
    val idleExpand: Boolean = true,
    
    val quickActions: List<QuickAction> = listOf(QuickAction.TIMER, QuickAction.STOPWATCH, QuickAction.FLASHLIGHT),
    
    val keepAlive: Boolean = false,
    
    val msgLines: Int = 3,
    
    val msgInApp: Boolean = false,
    /** Không hiện thẻ cuộc gọi đến khi đang mở sẵn màn hình cuộc gọi của chính app đó (tránh trùng). */
    val hideCallInCallApp: Boolean = true,
    
    val msgChannels: Map<String, String> = emptyMap(),
    
    val bannerHintHidden: Boolean = false,

    val gameModeEnabled: Boolean = true,
    val gameForceOn: Boolean = false,
    val gameSuppressPopups: Boolean = true,
    val gameShowHud: Boolean = true,
    val gameBypassLandscapeHide: Boolean = true,

    val ledEnabled: Boolean = false,
    val ledOnUnread: Boolean = true,
    /** Giữ tin nhắn chưa đọc trên Viên thuốc (chạm để xem lại) — độc lập với Vòng sáng LED, có chấm nhỏ báo hiệu. */
    val unreadKeep: Boolean = true,
    val ledOnEvent: Boolean = true,
    val ledOnCharge: Boolean = true,
    val ledAlwaysOn: Boolean = false,
    val ledStyle: LedStyle = LedStyle.ROTATE_RGB,
    val ledWidth: Float = 2.5f,
    val ledColor: Long = 0xFF00E5FF,
    val ledMotion: LedMotion = LedMotion.ROTATE,
    /** Tốc độ chuyển động 0 (chậm) .. 100 (nhanh). */
    val ledSpeed: Int = 50,
    val ledColors: List<Long> = listOf(0xFFE6C068, 0xFFC8102E, 0xFF0A84FF),
    val ledColorCount: Int = 2,
    /** Viền thẻ thông báo dùng hiệu ứng như Vòng sáng LED thay cho màu đơn. */
    val cardBorderLed: Boolean = false,
    /** Đường viền riêng của từng nơi (bình thường, hoạt động, viên nhỏ, thẻ, mở rộng). Mặc định: không viền. */
    val borders: Map<Surface, BorderSpec> = emptyMap(),
    /** Màu & hiệu ứng của Vòng sáng LED báo sự kiện / tin chưa đọc / sạc. */
    val ledBorder: BorderSpec = BorderSpec(on = true, width = 2.5f, glow = 60),

    val ecoSpPen: Boolean = true,
    val ecoBudsWatch: Boolean = true,
    val ecoFastCharge: Boolean = true,
    val ecoBatteryProtectLevel: Int = 80,
    val ecoDexCast: Boolean = true,

    /** Ứng dụng tự bật pop-up của nó: Viên thuốc chỉ báo dạng viên nhỏ để không hiện 2 lần. */
    val msgCompactWhenPopup: Boolean = false,
    /** Samsung: những app người dùng xác nhận đã bỏ chọn "Pop-up thông báo" (One UI không cho app khác đọc ô này). */
    val popupOffApps: Set<String> = emptySet(),
    /** Tự động sao lưu cấu hình ra Download/CapsuleVN (còn lại sau khi gỡ app). */
    val autoBackup: Boolean = true,
    /** Đã hỏi khôi phục cài đặt cũ sau khi cài mới. */
    val restoreAsked: Boolean = false,
    val otpEnabled: Boolean = true,
    val otpAutoCopy: Boolean = false,
    val otpAutoFill: Boolean = true,

    val ttsEnabled: Boolean = false,
    val ttsOnlyHeadset: Boolean = true,
    val ttsReadContent: Boolean = true,
    val ttsReadCalls: Boolean = true,
) {
    
    fun forPowerSave(on: Boolean): IslandSettings = if (on && batterySaver && !reduceEffects) copy(reduceEffects = true) else this

    fun inWakeQuiet(hour: Int): Boolean = wakeQuietOn && wakeQuietFrom != wakeQuietTo && when {
        wakeQuietFrom < wakeQuietTo -> hour in wakeQuietFrom until wakeQuietTo
        else -> hour >= wakeQuietFrom || hour < wakeQuietTo
    }

    fun inSchedule(hour: Int): Boolean = when {
        !scheduleOn || scheduleFrom == scheduleTo -> true
        scheduleFrom < scheduleTo -> hour in scheduleFrom until scheduleTo
        else -> hour >= scheduleFrom || hour < scheduleTo
    }

    val topOffset: Int get() = if (notch) 0 else offsetY

    val windowBaseY: Int get() = minOf(
        minOf(topOffset, topOffset + msgOffsetY, topOffset + callOffsetY),
        minOf(topOffset + updateOffsetY, topOffset + expandedOffsetY),
        minOf(topOffset + activeOffsetY, topOffset + noticeOffsetY),
        topOffset + (noticeTweaks.values.minOfOrNull { it.offsetY } ?: 0),
    )

    /** Cài đặt chung cho mọi "thông báo khác". */
    val noticeGeneral: NoticeTweak get() = NoticeTweak(noticeOffsetX, noticeOffsetY, noticeWidth, noticeHeightExtra, noticeCornerRadius)

    fun tweakFor(kind: NoticeKind?): NoticeTweak = kind?.let { noticeTweaks[it] } ?: noticeGeneral

    fun durationFor(key: String): Int = noticeDurations[key] ?: noticeDurationMs

    val liveEqualizer: Boolean get() = fxEqualizer && !reduceEffects
}

object SettingsRepo {
    private const val KEY = "settings_v1"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private var prefs: SharedPreferences? = null

    var hadSavedSettings = false
        private set
    private val _state = MutableStateFlow(IslandSettings())
    val state: StateFlow<IslandSettings> = _state.asStateFlow()
    val current: IslandSettings get() = _state.value

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences("island", Context.MODE_PRIVATE)
        prefs = p
        hadSavedSettings = p.contains(KEY)
        p.getString(KEY, null)?.let { raw ->
            runCatching { json.decodeFromString<IslandSettings>(raw) }.onSuccess { _state.value = it }
        }
        
        val cur = _state.value
        
        if (cur.layoutVersion in 2..2) update { copy(cameraGap = IslandSettings().cameraGap, layoutVersion = 3) }
        if (cur.layoutVersion < 2) {
            update {
                copy(
                    width = if (width == 96) 126 else width,
                    height = if (height == 34) 37 else height,
                    offsetY = if (offsetY == 8) 11 else offsetY,
                    layoutVersion = 3,
                )
            }
        }
        
        if (current.layoutVersion < 4) {
            val d = IslandSettings()
            update {
                val old = width == 126 && height == 37
                copy(
                    width = if (old) d.width else width,
                    height = if (old) d.height else height,
                    activeWidth = if (old && activeWidth == 48) d.activeWidth else activeWidth,
                    
                    elasticity = if (elasticity == Elasticity.HIGH) d.elasticity else elasticity,
                    layoutVersion = 4,
                )
            }
        }
        // v1.5: màn hình tắt chỉ để AOD của máy hiển thị (không tự bật sáng); bỏ chờ chạm đúp làm chạm chậm
        if (current.layoutVersion < 5) {
            update {
                val oldPreset = tapAction == TapAction.EXPAND && doubleTapAction == TapAction.OPEN_APP && longPressAction == TapAction.PLAY_PAUSE
                copy(
                    wakeOnMessage = false,
                    doubleTapAction = if (oldPreset) TapAction.NOTHING else doubleTapAction,
                    longPressAction = if (oldPreset) TapAction.OPEN_APP else longPressAction,
                    layoutVersion = 5,
                )
            }
        }
        // v1.5: hệ thống đường viền mới theo từng nơi — chuyển thiết lập viền cũ sang, không mất gì
        if (current.layoutVersion < 6) update { migrateBorders().copy(layoutVersion = 6) }
        // v1.5.1: Vòng sáng LED mặc định tắt (giống đường viền) — bật lại được trong Giao diện → Vòng sáng LED
        if (current.layoutVersion < 7) update { copy(ledEnabled = false, layoutVersion = 7) }
    }

    fun update(block: IslandSettings.() -> IslandSettings) {
        val next = _state.value.block()
        if (next == _state.value) return
        _state.value = next
        prefs?.edit()?.putString(KEY, json.encodeToString(next))?.apply()
    }

    fun reset() = update { IslandSettings() }

    /** Sao lưu toàn bộ cài đặt ra chuỗi JSON (để lưu thành file). */
    fun exportJson(): String = json.encodeToString(current)

    /** Khôi phục cài đặt từ file JSON đã sao lưu; giữ nguyên các thông tin cập nhật của bản đang cài. */
    fun importJson(raw: String): Boolean = runCatching {
        val decoded = json.decodeFromString<IslandSettings>(raw)
        // File sao lưu từ bản cũ: chuyển thiết lập viền cũ sang hệ thống viền mới
        val restored = if (decoded.layoutVersion in 1..5) decoded.migrateBorders().copy(layoutVersion = 6) else decoded
        val c = current
        update {
            restored.copy(
                skippedVersion = c.skippedVersion,
                lastSeenVersion = c.lastSeenVersion,
                lastUpdateCheck = c.lastUpdateCheck,
                islandVersion = c.islandVersion,
            )
        }
        true
    }.getOrDefault(false)

    fun resetAnimations() = update {
        val d = IslandSettings()
        copy(
            expandStyle = d.expandStyle,
            durationPercent = d.durationPercent,
            liquidExpand = d.liquidExpand,
            elasticity = d.elasticity,
            reduceEffects = d.reduceEffects,
            noticeDurationMs = d.noticeDurationMs,
        )
    }

    fun resetEffects() = update {
        val d = IslandSettings()
        copy(
            fxBlur = d.fxBlur, fxEqualizer = d.fxEqualizer, fxRate = d.fxRate, fxGlow = d.fxGlow, fxPay = d.fxPay,
            fxShake = d.fxShake, batterySaver = d.batterySaver,
        )
    }
}
