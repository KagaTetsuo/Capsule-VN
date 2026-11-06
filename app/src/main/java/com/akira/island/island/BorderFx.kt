package com.akira.island.island

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.akira.island.data.BorderEffect
import com.akira.island.data.BorderPalette
import com.akira.island.data.BorderSpec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/*
 * Bộ vẽ đường viền chung cho mọi nơi của Viên thuốc (bình thường, hoạt động, viên nhỏ, thẻ, mở rộng)
 * và cho Vòng sáng LED. Một bảng màu + một hiệu ứng chuyển động, vẽ bằng một nét sweep-gradient duy nhất
 * mỗi khung hình (rẻ: không tạo bitmap, không shader tuỳ biến).
 */

private const val TAU = (2.0 * Math.PI).toFloat()

private fun hex(vararg c: Long) = c.map { Color(it) }

/** Các màu gốc của bảng màu (APP: màu ứng dụng / ảnh bìa, rơi về màu tự chọn nếu không có). */
fun paletteColors(spec: BorderSpec, adaptive: Color? = null): List<Color> = when (spec.palette) {
    BorderPalette.RAINBOW -> List(8) { i -> Color.hsv(i * 45f, 0.88f, 1f) }
    BorderPalette.APP -> listOf(adaptive ?: Color(spec.color))
    BorderPalette.SINGLE -> listOf(Color(spec.color))
    BorderPalette.MULTI -> spec.colors.take(spec.colorCount.coerceIn(2, 4)).map { Color(it) }.ifEmpty { listOf(Color(spec.color)) }
    BorderPalette.ROYAL -> hex(0xFFC8102E, 0xFFE6C068, 0xFF7A0A18, 0xFFE6C068)
    BorderPalette.GOLD -> hex(0xFFFF8C00, 0xFFFFD700, 0xFFFFF8DC, 0xFFFFD700)
    BorderPalette.NEON -> hex(0xFF00F2FE, 0xFF7F00FF, 0xFFFF0844)
    BorderPalette.FIRE -> hex(0xFFFF1E00, 0xFFFF6A00, 0xFFFFC300, 0xFFFF6A00)
    BorderPalette.OCEAN -> hex(0xFF0033A0, 0xFF0A84FF, 0xFF00E5FF, 0xFF30B0C7)
    BorderPalette.AURORA -> hex(0xFF00FFA3, 0xFF00C2FF, 0xFF7B61FF, 0xFFFF5CF0)
    BorderPalette.SUNSET -> hex(0xFFFF5E62, 0xFFFF9966, 0xFFFFC371, 0xFFB24592)
    BorderPalette.SAKURA -> hex(0xFFFFB7C5, 0xFFFF8FAB, 0xFFFFFFFF, 0xFFFF5D8F)
    BorderPalette.PASTEL -> hex(0xFFFFB3BA, 0xFFFFDFBA, 0xFFBAFFC9, 0xFFBAE1FF, 0xFFD7BAFF)
    BorderPalette.ICE -> hex(0xFFFFFFFF, 0xFFBDEFFF, 0xFF7FDBFF, 0xFFD0E8F2)
    BorderPalette.TOXIC -> hex(0xFF39FF14, 0xFFCCFF00, 0xFF00FF9C)
    BorderPalette.VN -> hex(0xFFDA251D, 0xFFFFCD00, 0xFFDA251D, 0xFFFFCD00)
}

/** Hiệu ứng có chuyển động (cần đồng hồ khung hình) hay đứng yên. */
val BorderSpec.moving: Boolean get() = effect != BorderEffect.STATIC

private fun period(spec: BorderSpec): Long = (4200 - spec.speed.coerceIn(0, 100) * 36).toLong().coerceIn(600L, 4200L)

/** Nội suy vòng tròn qua danh sách màu (t trong [0,1)). */
private fun cycle(colors: List<Color>, t: Float): Color {
    val n = colors.size
    if (n == 1) return colors[0]
    val x = t.mod(1f) * n
    val i = x.toInt() % n
    return lerp(colors[i], colors[(i + 1) % n], x - x.toInt())
}

private fun hash(n: Int): Float {
    var x = n * 374761393 + 668265263
    x = (x xor (x ushr 13)) * 1274126177
    return ((x xor (x ushr 16)) and 0xFFFFFF) / 16777216f
}

private fun bump(x: Float, c: Float, w: Float) = (1f - abs(x - c) / w).coerceIn(0f, 1f)

/** Độ sáng chung của cả viền theo hiệu ứng (nhịp thở, nhấp nháy, nhịp tim, chập chờn). */
private fun globalAlpha(spec: BorderSpec, phase: Float, timeMs: Long): Float = when (spec.effect) {
    BorderEffect.BREATHE -> 0.18f + 0.82f * (0.5f + 0.5f * sin(phase * TAU))
    BorderEffect.BLINK -> if (phase < 0.5f) 1f else 0.04f
    BorderEffect.HEARTBEAT -> 0.12f + 0.88f * max(bump(phase, 0.08f, 0.09f), 0.7f * bump(phase, 0.3f, 0.09f))
    BorderEffect.FLICKER -> {
        val r = hash((timeMs / 70).toInt())
        when {
            r < 0.10f -> 0.12f
            r < 0.18f -> 0.55f
            else -> 1f
        }
    }
    else -> 1f
}

/** Màu tại vị trí [pos] (0..1 theo vòng quanh viền, 0 = bên phải, 0.75 = chính giữa đỉnh). */
private fun colorAt(spec: BorderSpec, base: List<Color>, pos: Float, phase: Float, k: Int): Color = when (spec.effect) {
    BorderEffect.ROTATE -> cycle(base, pos - phase)
    BorderEffect.ROTATE_REV -> cycle(base, pos + phase)
    BorderEffect.PINGPONG -> cycle(base, pos - 0.35f * sin(phase * TAU))
    BorderEffect.COMET, BorderEffect.DUAL_COMET -> {
        val c = cycle(base, pos - phase)
        fun tail(head: Float): Float {
            val d = (head - pos).mod(1f)
            return (1f - d / 0.32f).coerceIn(0f, 1f)
        }
        val t = if (spec.effect == BorderEffect.DUAL_COMET) max(tail(phase), tail(phase + 0.5f)) else tail(phase)
        // Đầu sao chổi trắng rực, đuôi mờ dần
        lerp(c, Color.White, (t - 0.92f).coerceAtLeast(0f) * 6f).copy(alpha = c.alpha * (0.06f + 0.94f * t * t))
    }
    BorderEffect.SPREAD -> {
        // Màu loang ra từ chính giữa đỉnh xuống hai bên rồi tan dần
        val d = min((pos - 0.75f).mod(1f), (0.75f - pos).mod(1f)) / 0.5f
        val front = (phase / 0.7f) * 1.25f
        val reveal = ((front - d) / 0.18f).coerceIn(0f, 1f)
        val fade = if (phase > 0.8f) 1f - (phase - 0.8f) / 0.2f else 1f
        val c = cycle(base, d * 0.8f - phase * 0.5f)
        c.copy(alpha = c.alpha * reveal * fade)
    }
    BorderEffect.COLOR_CYCLE -> cycle(base, phase)
    BorderEffect.WAVE -> {
        val c = cycle(base, pos - phase * 0.25f)
        c.copy(alpha = c.alpha * (0.25f + 0.75f * (0.5f + 0.5f * sin((pos * 4f - phase) * TAU))))
    }
    BorderEffect.SPARKLE -> {
        val c = cycle(base, pos)
        val tw = (0.5f + 0.5f * sin((phase * 2f + hash(k * 7919)) * TAU)).pow(14)
        lerp(c, Color.White, tw * 0.85f).copy(alpha = c.alpha * (0.38f + 0.62f * tw))
    }
    else -> cycle(base, pos)
}

private const val SAMPLES = 64

/** Vẽ đường viền theo [spec] sát mép trong của Viên thuốc (gọi trong drawWithContent, sau khi đã clip). */
fun DrawScope.drawBorder(
    spec: BorderSpec,
    adaptive: Color?,
    timeMs: Long,
    corner: Float,
    notch: Boolean,
    alpha: Float = 1f,
) {
    if (alpha <= 0f) return
    val w = spec.width.coerceIn(0.5f, 8f).dp.toPx()
    val p = period(spec)
    val phase = (timeMs % p).toFloat() / p
    var base = paletteColors(spec, adaptive)
    // Viền một màu mà có hiệu ứng xoay: thêm vệt sáng để thấy được chuyển động
    if (base.size == 1 && spec.effect in setOf(BorderEffect.ROTATE, BorderEffect.ROTATE_REV, BorderEffect.PINGPONG, BorderEffect.WAVE)) {
        val c = base[0]
        base = listOf(c, lerp(c, Color.White, 0.65f), c, c.copy(alpha = c.alpha * 0.3f))
    }
    val a = (alpha * globalAlpha(spec, phase, timeMs) * spec.opacity.coerceIn(20, 100) / 100f).coerceIn(0f, 1f)
    val brush = if (base.size == 1 && spec.effect !in setOf(BorderEffect.COMET, BorderEffect.DUAL_COMET, BorderEffect.SPREAD, BorderEffect.SPARKLE)) {
        null
    } else {
        Brush.sweepGradient(List(SAMPLES + 1) { k -> colorAt(spec, base, k.toFloat() / SAMPLES, phase, k) }, center)
    }
    val topLeft = Offset(w / 2f, w / 2f)
    val sz = Size(size.width - w, size.height - w)
    val r = (corner - w / 2f).coerceAtLeast(0f)
    if (brush == null) drawIslandRect(base[0], topLeft, sz, r, notch, Stroke(w), a)
    else drawIslandRect(brush, topLeft, sz, r, notch, Stroke(w), a)
}

/** Quầng sáng toả ra ngoài viền (gọi trong drawBehind, trước khi clip). */
fun DrawScope.drawBorderGlow(
    spec: BorderSpec,
    adaptive: Color?,
    timeMs: Long,
    corner: Float,
    notch: Boolean,
    alpha: Float = 1f,
) {
    if (spec.glow <= 0 || alpha <= 0f) return
    val p = period(spec)
    val phase = (timeMs % p).toFloat() / p
    val base = paletteColors(spec, adaptive)
    val color = when (spec.effect) {
        BorderEffect.STATIC, BorderEffect.BREATHE, BorderEffect.BLINK, BorderEffect.HEARTBEAT, BorderEffect.FLICKER -> base.first()
        else -> cycle(base, phase)
    }
    val g = spec.glow.coerceIn(0, 100) / 100f
    val pulse = globalAlpha(spec, phase, timeMs) * spec.opacity.coerceIn(20, 100) / 100f
    val maxSpread = (3f + 7f * g).dp.toPx()
    for (i in 1..5) {
        val d = maxSpread * i / 5f
        drawIslandRect(
            color = color,
            topLeft = Offset(-d, -d),
            size = Size(size.width + 2 * d, size.height + 2 * d),
            corner = corner + d,
            notch = notch,
            alpha = (0.11f * g * alpha * pulse * (1f - i / 6f)).coerceIn(0f, 1f),
        )
    }
}
