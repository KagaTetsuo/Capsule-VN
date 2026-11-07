package com.akira.island.data

import com.akira.island.core.tr
import kotlinx.serialization.Serializable

/** Những "nơi" của Viên thuốc có thể tuỳ chỉnh riêng (kích thước, vị trí, nền, đường viền…). */
@Serializable
enum class Surface(private val ru: String, private val en: String, private val hintRu: String, private val hintEn: String) {
    IDLE("Viên thuốc bình thường", "Idle island", "Khi không có hoạt động nào", "When nothing is happening"),
    ACTIVE("Khi có hoạt động", "With activity", "Nhạc, hẹn giờ, điều hướng, cuộc gọi đang diễn ra…", "Music, timers, navigation, ongoing calls…"),
    PILL("Thông báo viên nhỏ", "Small pill alerts", "Chế độ chuông, sạc, tai nghe, thanh toán, đã gửi…", "Ring mode, charging, headphones, payments, sent…"),
    CARD("Khung thẻ thông báo", "Notification cards", "Tin nhắn, cuộc gọi đến, cập nhật, tin chưa đọc…", "Messages, incoming calls, updates, unread…"),
    EXPANDED("Bảng mở rộng", "Expanded board", "Giờ – Pin, trình phát nhạc, hẹn giờ…", "Clock & battery, music player, timers…"),
    ;

    val title: String get() = tr(ru, en)
    val hint: String get() = tr(hintRu, hintEn)
}

/** Bảng màu của đường viền. */
@Serializable
enum class BorderPalette(private val ru: String, private val en: String) {
    RAINBOW("LED RGB cầu vồng", "RGB rainbow"),
    APP("Theo màu ứng dụng / ảnh bìa", "App / artwork color"),
    SINGLE("Đơn sắc tự chọn", "Single color"),
    MULTI("Đa sắc tự chọn (2–4 màu)", "Custom 2–4 colors"),
    ROYAL("Đen – Đỏ – Vàng", "Black – Red – Gold"),
    GOLD("Vàng hoàng gia", "Royal gold"),
    NEON("Neon Cyber", "Cyber neon"),
    FIRE("Lửa", "Fire"),
    OCEAN("Đại dương", "Ocean"),
    AURORA("Cực quang", "Aurora"),
    SUNSET("Hoàng hôn", "Sunset"),
    SAKURA("Hoa anh đào", "Sakura"),
    PASTEL("Pastel dịu", "Soft pastel"),
    ICE("Băng tuyết", "Ice"),
    TOXIC("Dạ quang", "Toxic"),
    VN("Cờ đỏ sao vàng", "Red & gold star"),
    ;

    val title: String get() = tr(ru, en)
}

/** Hiệu ứng chuyển động của đường viền. */
@Serializable
enum class BorderEffect(private val ru: String, private val en: String) {
    STATIC("Đứng yên", "Static"),
    ROTATE("Xoay 360°", "Rotate 360°"),
    ROTATE_REV("Xoay đảo chiều", "Rotate reverse"),
    PINGPONG("Xoay qua lại", "Swing back & forth"),
    COMET("Sao chổi", "Comet"),
    DUAL_COMET("Hai sao chổi đuổi nhau", "Twin comets"),
    SPREAD("Loang màu từ đỉnh", "Color spread"),
    COLOR_CYCLE("Đổi màu toàn viền", "Color cycle"),
    WAVE("Gợn sóng", "Wave"),
    BREATHE("Nhịp thở", "Breathing"),
    HEARTBEAT("Nhịp tim", "Heartbeat"),
    BLINK("Nhấp nháy", "Blink"),
    FLICKER("Đèn neon chập chờn", "Neon flicker"),
    SPARKLE("Lấp lánh", "Sparkle"),
    ;

    val title: String get() = tr(ru, en)
}

/** Thiết lập đường viền của một nơi (mặc định: tắt, không màu). */
@Serializable
data class BorderSpec(
    val on: Boolean = false,
    val palette: BorderPalette = BorderPalette.RAINBOW,
    /** Màu đơn sắc (và màu dự phòng khi ứng dụng không có màu riêng). */
    val color: Long = 0xFFE6C068,
    val colors: List<Long> = listOf(0xFFE6C068, 0xFFC8102E, 0xFF0A84FF, 0xFF34C759),
    val colorCount: Int = 2,
    val effect: BorderEffect = BorderEffect.ROTATE,
    /** Độ dày viền (dp). */
    val width: Float = 1.5f,
    /** Tốc độ 0 (chậm) .. 100 (nhanh). */
    val speed: Int = 50,
    /** Quầng sáng toả ra ngoài 0..100. */
    val glow: Int = 0,
    /** Độ đậm 20..100. */
    val opacity: Int = 100,
)

fun IslandSettings.border(surface: Surface): BorderSpec = borders[surface] ?: BorderSpec()

private fun LedStyle.toPalette(): BorderPalette = when (this) {
    LedStyle.ROTATE_RGB -> BorderPalette.RAINBOW
    LedStyle.AUTO_APP -> BorderPalette.APP
    LedStyle.CYBER_NEON -> BorderPalette.NEON
    LedStyle.BREATHE_GOLD -> BorderPalette.GOLD
    LedStyle.CUSTOM -> BorderPalette.SINGLE
    LedStyle.MULTI -> BorderPalette.MULTI
}

private fun LedMotion.toEffect(): BorderEffect = when (this) {
    LedMotion.ROTATE -> BorderEffect.ROTATE
    LedMotion.COMET -> BorderEffect.COMET
    LedMotion.BREATHE -> BorderEffect.BREATHE
    LedMotion.BLINK -> BorderEffect.BLINK
    LedMotion.STATIC -> BorderEffect.STATIC
}

/** Chuyển các thiết lập viền đời cũ (viền ngoài, viền thẻ, Vòng sáng LED) sang hệ thống viền mới. */
internal fun IslandSettings.migrateBorders(): IslandSettings {
    val ledColorsNew = (ledColors + BorderSpec().colors).take(4)
    val led = BorderSpec(
        on = true, palette = ledStyle.toPalette(), color = ledColor, colors = ledColorsNew,
        colorCount = ledColorCount.coerceIn(2, 4), effect = ledMotion.toEffect(), width = ledWidth, speed = ledSpeed, glow = 60,
    )
    val map = borders.toMutableMap()
    if (outline) {
        val o = BorderSpec(
            on = true, palette = if (outlineFromArt) BorderPalette.APP else BorderPalette.SINGLE,
            color = outlineColor, effect = BorderEffect.STATIC, width = outlineWidth,
        )
        // Viền ngoài cũ áp dụng cho mọi nơi chưa có viền thẻ
        Surface.entries.forEach { map.putIfAbsent(it, o) }
    }
    if (cardBorder) {
        val c = if (cardBorderLed) led.copy(width = cardBorderWidth, glow = 0)
        else BorderSpec(on = true, palette = BorderPalette.SINGLE, color = cardBorderColor, effect = BorderEffect.STATIC, width = cardBorderWidth)
        map[Surface.CARD] = c
        if (cardStyleExpanded) map[Surface.EXPANDED] = c
        if (cardStylePills) map[Surface.PILL] = c
        if (cardStyleIdle) map[Surface.IDLE] = c
    }
    return copy(borders = map, ledBorder = led)
}

/**
 * Từng nơi / từng loại thông báo của Viên thuốc — mỗi loại một trang tuỳ chỉnh riêng,
 * có kích thước & vị trí, nền, thời gian hiển thị và đường viền riêng (không dùng chung).
 */
@Serializable
enum class Place(private val ru: String, private val en: String, private val hintRu: String, private val hintEn: String) {
    IDLE("Viên thuốc bình thường", "Idle island", "Khi không có hoạt động nào", "When nothing is happening"),
    ACTIVE("Khi có hoạt động", "With activity", "Nhạc, hẹn giờ, cuộc gọi đang diễn ra, dẫn đường…", "Music, timers, ongoing calls, navigation…"),
    EXPANDED("Bảng mở rộng", "Expanded board", "Giờ – Pin, trình phát nhạc, hẹn giờ…", "Clock & battery, music player, timers…"),
    MESSAGE("Tin nhắn", "Messages", "Thẻ tin nhắn & tin chưa đọc", "Message cards & unread"),
    CALL("Cuộc gọi đến", "Incoming call", "Thẻ cuộc gọi đến có nút nghe / từ chối", "Card with answer / decline"),
    UPDATE("Cập nhật ứng dụng", "App update", "Thẻ báo có phiên bản mới", "New version card"),
    CHARGING("Sạc pin", "Charging", "Cắm / rút sạc, mức pin", "Plug in / unplug, battery level"),
    AUDIO("Tai nghe & Bluetooth", "Headphones & Bluetooth", "Kết nối tai nghe, loa, pin tai nghe", "Headphones, speakers, battery"),
    PAYMENT("Thanh toán", "Payments", "Chạm thanh toán NFC, đã thanh toán", "NFC tap to pay, paid"),
    NAV("Điều hướng & gọi xe", "Navigation & rides", "Rẽ, lộ trình, giao hàng, gọi xe", "Turns, routes, deliveries, rides"),
    ALARM("Báo thức", "Alarm", "Báo thức sắp reo", "Upcoming alarm"),
    MODE("Chế độ chuông", "Ring mode", "Chuông, rung, im lặng", "Ring, vibrate, silent"),
    UNLOCK("Mở khoá máy", "Unlocked", "Biểu tượng ổ khoá khi mở khoá màn hình", "Lock icon when you unlock"),
    COMPACT("Tin nhắn thu gọn & Gaming", "Compact message & gaming", "Tin nhắn dạng viên nhỏ (khi app tự hiện pop-up, khi chơi game) và Gaming Mode", "Messages as a pill (app pop-up, in games) and Gaming Mode"),
    SENT("Đã gửi / Đã sao chép", "Sent / Copied", "Đã gửi tin trả lời, đã chép mã OTP", "Reply sent, OTP copied"),
    OTHER("Sự kiện khác", "Other events", "Wi-Fi, chế độ máy bay, đèn pin…", "Wi-Fi, airplane mode, flashlight…"),
    ;

    val title: String get() = tr(ru, en)
    val hint: String get() = tr(hintRu, hintEn)

    /** Loại thông báo tương ứng (để chỉnh vị trí / kích thước, thời gian hiển thị). */
    val kind: NoticeKind?
        get() = when (this) {
            CHARGING -> NoticeKind.CHARGING
            AUDIO -> NoticeKind.AUDIO
            PAYMENT -> NoticeKind.PAYMENT
            NAV -> NoticeKind.NAV
            ALARM -> NoticeKind.ALARM
            MODE -> NoticeKind.MODE
            UNLOCK -> NoticeKind.UNLOCK
            COMPACT -> NoticeKind.COMPACT
            SENT -> NoticeKind.SENT
            OTHER -> NoticeKind.OTHER
            else -> null
        }

    /** Khoá thời gian hiển thị (null = không có thời gian hiển thị riêng). */
    val durationKey: String?
        get() = when (this) {
            MESSAGE -> "MESSAGE"
            UPDATE -> "UPDATE"
            else -> kind?.name
        }
}

fun NoticeKind.place(): Place = when (this) {
    NoticeKind.CHARGING -> Place.CHARGING
    NoticeKind.AUDIO -> Place.AUDIO
    NoticeKind.PAYMENT -> Place.PAYMENT
    NoticeKind.NAV -> Place.NAV
    NoticeKind.ALARM -> Place.ALARM
    NoticeKind.MODE -> Place.MODE
    NoticeKind.UNLOCK -> Place.UNLOCK
    NoticeKind.COMPACT -> Place.COMPACT
    NoticeKind.SENT -> Place.SENT
    NoticeKind.OTHER -> Place.OTHER
}

/** Mặc định: nơi nào dùng nền đen thường (không dùng nền thẻ) — giống giao diện trước đây. */
val DEFAULT_PLAIN_BG: Set<Place> = setOf(
    Place.IDLE, Place.ACTIVE, Place.CHARGING, Place.AUDIO, Place.PAYMENT, Place.OTHER, Place.MODE, Place.UNLOCK, Place.COMPACT, Place.SENT,
)

fun IslandSettings.placeBorder(p: Place): BorderSpec = placeBorders[p] ?: BorderSpec()

/** Nơi này dùng nền thẻ (màu / ảnh / Liquid Glass ở "Nền thẻ thông báo") hay nền đen thường. */
fun IslandSettings.cardBgOn(p: Place): Boolean = p !in plainBgPlaces

/** v1.6: viền & nền tách riêng từng loại — chép thiết lập theo nhóm cũ sang từng loại, giữ nguyên giao diện. */
internal fun IslandSettings.migratePlaces(): IslandSettings {
    val map = placeBorders.toMutableMap()
    fun put(spec: BorderSpec?, vararg ps: Place) {
        if (spec != null) ps.forEach { map.putIfAbsent(it, spec) }
    }
    put(borders[Surface.IDLE], Place.IDLE)
    put(borders[Surface.ACTIVE], Place.ACTIVE)
    put(borders[Surface.EXPANDED], Place.EXPANDED)
    put(borders[Surface.CARD], Place.MESSAGE, Place.CALL, Place.UPDATE, Place.NAV, Place.ALARM)
    put(borders[Surface.PILL], Place.CHARGING, Place.AUDIO, Place.PAYMENT, Place.OTHER, Place.MODE, Place.SENT)
    val plain = buildSet {
        if (!cardStyleIdle) add(Place.IDLE)
        add(Place.ACTIVE)
        if (!cardStyleExpanded) add(Place.EXPANDED)
        if (!cardStylePills) addAll(listOf(Place.CHARGING, Place.AUDIO, Place.PAYMENT, Place.OTHER, Place.MODE, Place.SENT))
    }
    return copy(placeBorders = map, plainBgPlaces = plain)
}

/** Thông báo viên nhỏ (biểu tượng + chữ ngắn quanh camera). */
val NoticeKind.isPill: Boolean get() = this == NoticeKind.MODE || this == NoticeKind.UNLOCK || this == NoticeKind.COMPACT

/** v1.6.1: trước đây mọi viên nhỏ dùng chung thiết lập "chế độ chuông" — chép sang 2 loại mới để giữ nguyên giao diện. */
internal fun IslandSettings.migratePills(): IslandSettings {
    val newKinds = listOf(NoticeKind.UNLOCK, NoticeKind.COMPACT)
    val tw = noticeTweaks[NoticeKind.MODE]
    val dur = noticeDurations[NoticeKind.MODE.name]
    val b = placeBorders[Place.MODE]
    return copy(
        noticeTweaks = if (tw == null) noticeTweaks else noticeTweaks + newKinds.filter { it !in noticeTweaks }.associateWith { tw },
        noticeDurations = if (dur == null) noticeDurations else noticeDurations + newKinds.map { it.name }.filter { it !in noticeDurations }.associateWith { dur },
        placeBorders = if (b == null) placeBorders else placeBorders + listOf(Place.UNLOCK, Place.COMPACT).filter { it !in placeBorders }.associateWith { b },
        plainBgPlaces = if (Place.MODE in plainBgPlaces) plainBgPlaces + Place.UNLOCK + Place.COMPACT else plainBgPlaces - Place.UNLOCK - Place.COMPACT,
    )
}
