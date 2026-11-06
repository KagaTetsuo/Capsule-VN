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
