package com.akira.island.island

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.Tints
import com.akira.island.core.Turn
import com.akira.island.data.AccentMode
import com.akira.island.data.ExpandStyle
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import com.akira.island.core.IslandBus
import kotlin.math.pow
import kotlin.math.sqrt
import kotlinx.coroutines.launch

fun <T> islandSpec(s: IslandSettings): FiniteAnimationSpec<T> {
    val damping = if (s.reduceEffects) 1f else s.elasticity.damping
    return when (s.expandStyle) {
        ExpandStyle.INSTANT -> snap()
        ExpandStyle.SMOOTH -> tween(animDurationMs(s), easing = FastOutSlowInEasing)
        ExpandStyle.SPRING -> spring(dampingRatio = (damping + 0.1f).coerceAtMost(1f), stiffness = animStiffness(s))
        ExpandStyle.FRUIT -> spring(dampingRatio = damping, stiffness = animStiffness(s))
    }
}

fun <T> fadeSpec(s: IslandSettings): FiniteAnimationSpec<T> = when (s.expandStyle) {
    ExpandStyle.INSTANT -> snap()
    ExpandStyle.SMOOTH -> tween(animDurationMs(s) / 2)
    else -> spring(stiffness = animStiffness(s) * 6f)
}

private const val VIDEO_STIFFNESS = 223f

fun animStiffness(s: IslandSettings): Float {
    val t = s.durationPercent.coerceIn(0, 100) / 100f
    return if (t <= 0.35f) {
        1200f * (VIDEO_STIFFNESS / 1200f).pow(t / 0.35f)
    } else {
        VIDEO_STIFFNESS * (60f / VIDEO_STIFFNESS).pow((t - 0.35f) / 0.65f)
    }
}

fun motionScale(s: IslandSettings): Float = if (s.expandStyle == ExpandStyle.INSTANT) 0.5f else sqrt(VIDEO_STIFFNESS / animStiffness(s))

val ExpoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

fun animDurationMs(s: IslandSettings): Int = 160 + s.durationPercent.coerceIn(0, 100) * 6

@Composable
fun rememberNow(periodMs: Long, active: Boolean = true): Long {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(periodMs, active) {
        while (active) {
            if (!IslandBus.screenOn.value) IslandBus.screenOn.first { it }
            now = SystemClock.elapsedRealtime()
            delay(periodMs)
        }
    }
    return now
}

@Composable
fun rememberFxClock(active: Boolean): State<Long> {
    val time = remember { mutableLongStateOf(0L) }
    LaunchedEffect(active) {
        var last = 0L
        while (active) {
            if (!IslandBus.screenOn.value) IslandBus.screenOn.first { it }
            val fps = SettingsRepo.current.fxRate.fps
            val step = if (fps > 0) 1000L / fps else 0L
            withFrameMillis { t ->
                if (t - last >= step - 3) {
                    time.longValue = t
                    last = t
                }
            }
            if (step > 0) delay(step - 6)
        }
    }
    return time
}

fun pingPong(t: Long, halfMs: Int): Float {
    val p = (t % (2L * halfMs)).toFloat() / halfMs
    return FastOutSlowInEasing.transform(if (p < 1f) p else 2f - p)
}

@Composable
fun rememberWallNow(periodMs: Long): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(periodMs) {
        while (true) {
            if (!IslandBus.screenOn.value) IslandBus.screenOn.first { it }
            now = System.currentTimeMillis()
            delay(periodMs)
        }
    }
    return now
}

@Composable
fun rememberHaptic(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        { if (SettingsRepo.current.haptics) view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    }
}

@Composable
fun Equalizer(color: Color, playing: Boolean, animate: Boolean, modifier: Modifier = Modifier, bars: Int = 5) {
    val moving = playing && animate
    val time = rememberFxClock(moving)
    Canvas(modifier) {
        val w = size.width / (bars + (bars - 1) * 1.6f)
        for (i in 0 until bars) {
            val h = when {
                moving -> {
                    val t = time.value
                    0.25f + 0.75f * (0.6f * pingPong(t, 420 + (i * 173) % 380) + 0.4f * pingPong(t + i * 211L, 700 + (i * 97) % 400))
                }
                playing -> 0.3f + 0.45f * ((i * 37) % 10) / 10f
                else -> 0.15f
            }
            val bh = (size.height * h).coerceAtLeast(w)
            drawRoundRect(
                color = color,
                topLeft = Offset(i * 2.6f * w, (size.height - bh) / 2),
                size = Size(w, bh),
                cornerRadius = CornerRadius(w / 2),
            )
        }
    }
}

@Composable
fun BatteryGlyph(level: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val nub = size.height * 0.16f
        val bodyW = size.width - nub - 1.5.dp.toPx()
        val r = size.height * 0.32f
        drawRoundRect(color.copy(alpha = 0.3f), size = Size(bodyW, size.height), cornerRadius = CornerRadius(r))
        val inset = size.height * 0.14f
        val fillW = (bodyW - inset * 2) * (level.coerceIn(0, 100) / 100f)
        if (fillW > 0f) {
            drawRoundRect(
                color,
                topLeft = Offset(inset, inset),
                size = Size(fillW.coerceAtLeast(r * 0.5f), size.height - inset * 2),
                cornerRadius = CornerRadius(r * 0.7f),
            )
        }
        drawRoundRect(
            color.copy(alpha = 0.5f),
            topLeft = Offset(bodyW + 1.5.dp.toPx(), size.height * 0.29f),
            size = Size(nub, size.height * 0.42f),
            cornerRadius = CornerRadius(nub / 2),
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    stroke: Dp = 3.dp,
    track: Color = color.copy(alpha = 0.22f),
) {
    Canvas(modifier) {
        val px = stroke.toPx()
        val inset = px / 2
        val arcSize = Size(size.width - px, size.height - px)
        drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(px))
        drawArc(color, -90f, 360f * progress.coerceIn(0f, 1f), false, Offset(inset, inset), arcSize,
            style = Stroke(px, cap = StrokeCap.Round))
    }
}

@Composable
fun Art(art: ImageBitmap?, size: Dp, corner: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Color(0xFF2C2C2E)),
        contentAlignment = Alignment.Center,
    ) {
        if (art != null) {
            Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Ios.music, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(size * 0.5f))
        }
    }
}

@Composable
fun AppIcon(icon: ImageBitmap?, size: Dp, fallback: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    if (icon != null) {
        Image(icon, null, modifier.size(size).clip(CircleShape))
    } else {
        Icon(fallback, null, tint = tint, modifier = modifier.size(size))
    }
}

@Composable
fun RoundButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    bg: Color = Color.White.copy(alpha = 0.14f),
    fg: Color = Color.White,
    iconScale: Float = 0.52f,
    iconRotation: Float = 0f,
    onClick: () -> Unit,
) {
    val haptic = rememberHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, spring(dampingRatio = 0.5f, stiffness = 600f), label = "press")
    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bg)
            .clickable(interaction, indication = null) { haptic(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(size * iconScale).graphicsLayer { rotationZ = iconRotation })
    }
}

@Composable
fun PillButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = Color.White.copy(alpha = 0.14f),
    fg: Color = Color.White,
    onClick: () -> Unit,
) {
    val haptic = rememberHaptic()
    Row(
        modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { haptic(); onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

/**
 * iOS 26 Authentic Liquid Glass (Thấu kính giọt chất lỏng 3D trong suốt, khúc xạ viền & phản quang gương):
 * Không dùng lớp phủ sương mờ (frosted glass) làm đục phần giữa viên thuốc, mà tái tạo vật lý quang học
 * của giọt thủy tinh lỏng iOS:
 *  - Lõi giữa trong trẻo, giữ độ sâu quang học và độ tương phản cao;
 *  - Rãnh khúc xạ tối (Meniscus Dark Trough) + đường tụ sáng Caustic bên trong viền cong 3D;
 *  - Vệt bóng gương sắc nét (Specular Crest) ở vòm trên và phản xạ đáy chuyển động mượt theo góc nghiêng [tilt];
 *  - Viền thấu kính Fresnel sắc nét kèm tán sắc lăng kính (Cyan / Indigo / Rose) ở các góc cong.
 */
fun Modifier.glassSurface(
    shape: Shape,
    base: Color,
    opacity: Float,
    refraction: Float = 0.80f,
    specular: Float = 0.85f,
    tilt: () -> Float = { 0f },
    sheen: () -> Float = { -1f },
): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    val a = opacity.coerceIn(0.20f, 0.96f)
    val refr = refraction.coerceIn(0.15f, 1f)
    val spec = specular.coerceIn(0.15f, 1f)
    val outline = shape.createOutline(size, layoutDirection, this)
    val dp = 1.dp.toPx()

    // Lõi chất lỏng trong trẻo: không phủ sương trắng ở giữa, chỉ tạo độ cong meniscus ở sát đỉnh và đáy
    val liquidDepth = Brush.verticalGradient(
        0.00f to Color(0xFFE0F2FE).copy(alpha = 0.16f * spec),
        0.10f to Color.Transparent,
        0.48f to Color.Black.copy(alpha = 0.18f * refr),
        0.86f to Color.Transparent,
        1.00f to Color(0xFFBAE6FD).copy(alpha = 0.14f * refr),
    )

    // Rãnh khúc xạ lồi sát mép viền (Meniscus Trough) tạo chiều sâu thấu kính 3D
    val troughStroke = Stroke(width = 4.6f * dp)
    val innerBevelStroke = Stroke(width = 2.4f * dp)
    val causticStroke = Stroke(width = 1.35f * dp)
    val outerLipStroke = Stroke(width = 3.2f * dp)
    val fresnelRimStroke = Stroke(width = 1.45f * dp)
    val prismStroke = Stroke(width = 1.55f * dp)

    // Chiều cao vệt phản quang gương vòm trên (sắc nét như mặt nước căng, không lan mờ toàn bộ)
    val crestH = minOf(h * 0.24f, 9.5f * dp).coerceAtLeast(2f * dp)
    val floorH = minOf(h * 0.18f, 7f * dp).coerceAtLeast(2f * dp)

    onDrawWithContent {
        val t = tilt().coerceIn(-1f, 1f)
        val shift = t * w * 0.36f

        // 1. Thân giọt chất lỏng trong suốt & độ sâu quang học
        drawRect(base.copy(alpha = a * 0.80f))
        drawRect(liquidDepth)

        // 2. Vệt bóng gương sắc nét ở vòm trên (Glossy Wet Specular Crest)
        val crestCenter = (w * (0.36f + 0.28f * t)).coerceIn(w * 0.12f, w * 0.88f)
        drawRect(
            brush = Brush.radialGradient(
                0.00f to Color.White.copy(alpha = 0.52f * spec),
                0.38f to Color(0xFFE0F2FE).copy(alpha = 0.22f * spec),
                0.78f to Color.White.copy(alpha = 0.04f * spec),
                1.00f to Color.Transparent,
                center = Offset(crestCenter, crestH * 0.15f),
                radius = (w * 0.46f).coerceAtLeast(crestH * 2f),
            ),
            topLeft = Offset(0f, 0f),
            size = Size(w, crestH),
        )

        // 3. Đường tụ sáng Caustic ở đáy đối diện (nơi thấu kính lồi hội tụ ánh sáng)
        val causticCenter = (w * (0.68f - 0.26f * t)).coerceIn(w * 0.15f, w * 0.85f)
        drawRect(
            brush = Brush.radialGradient(
                0.00f to Color(0xFFBAE6FD).copy(alpha = 0.36f * refr),
                0.45f to Color.White.copy(alpha = 0.14f * refr),
                1.00f to Color.Transparent,
                center = Offset(causticCenter, h),
                radius = (w * 0.42f).coerceAtLeast(floorH * 2f),
            ),
            topLeft = Offset(0f, h - floorH),
            size = Size(w, floorH),
        )

        // 4. Rãnh khúc xạ tối sát mép (Meniscus Dark Trough) tạo độ lồi 3D tách biệt phần viền và phần tâm
        drawOutline(outline, Color.Black.copy(alpha = 0.30f * refr), style = troughStroke)

        // 5. Vòng tụ sáng Caustic nội viền (nằm dịch vào trong 1.8dp)
        val innerCausticBrush = Brush.linearGradient(
            0.00f to Color.Transparent,
            0.25f to Color(0xFFE0F2FE).copy(alpha = 0.28f * refr),
            0.52f to Color.Transparent,
            0.82f to Color.White.copy(alpha = 0.42f * refr),
            1.00f to Color(0xFFBAE6FD).copy(alpha = 0.50f * refr),
            start = Offset(shift * 0.5f, 0f),
            end = Offset(w + shift * 0.5f, h),
        )
        translate(0f, 1.4f * dp) {
            drawOutline(outline, innerCausticBrush, style = causticStroke)
        }
        translate(0f, -1.2f * dp) {
            drawOutline(outline, innerCausticBrush, alpha = 0.65f, style = causticStroke)
        }

        // 6. Sóng ánh sáng lướt qua mặt chất lỏng khi Viên thuốc co giãn / đổi nội dung
        val shine = sheen()
        if (shine in 0f..1f) {
            val x = -w * 0.35f + w * 1.7f * shine
            // Lưỡi sáng phản quang chính sắc nét
            drawRect(
                Brush.linearGradient(
                    0.00f to Color.Transparent,
                    0.42f to Color(0xFFE0F2FE).copy(alpha = 0.12f * spec),
                    0.50f to Color.White.copy(alpha = 0.44f * spec),
                    0.58f to Color(0xFFBAE6FD).copy(alpha = 0.14f * spec),
                    1.00f to Color.Transparent,
                    start = Offset(x - w * 0.16f, 0f),
                    end = Offset(x + w * 0.16f, h * 0.75f),
                ),
            )
        }

        // 7. Vẽ nội dung bên trong Viên thuốc (chữ, icon, sóng nhạc sắc nét)
        drawContent()

        // 8. Tán sắc lăng kính quang học (Chromatic Prism Dispersion) ở góc cong thấu kính
        val disp = 0.85f * dp * refr
        translate(-disp, -disp * 0.8f) {
            drawOutline(outline, Color(0xFF38BDF8).copy(alpha = 0.28f * spec * refr), style = prismStroke)
        }
        translate(disp, disp * 0.8f) {
            drawOutline(outline, Color(0xFFF472B6).copy(alpha = 0.22f * spec * refr), style = prismStroke)
        }
        translate(disp * 0.6f, -disp * 0.6f) {
            drawOutline(outline, Color(0xFF818CF8).copy(alpha = 0.18f * spec * refr), style = causticStroke)
        }

        // 9. Viền thấu kính Fresnel 3D chuẩn iOS Liquid Glass: sắc nét ở góc trên-trái & dưới-phải, trong suốt ở đoạn giữa
        val fresnelRim = Brush.linearGradient(
            0.00f to Color.White.copy(alpha = 0.98f),
            0.14f to Color(0xFFE0F2FE).copy(alpha = 0.62f),
            0.34f to Color.White.copy(alpha = 0.08f),
            0.52f to Color.White.copy(alpha = 0.03f),
            0.70f to Color(0xFFBAE6FD).copy(alpha = 0.32f),
            0.88f to Color(0xFFE0F2FE).copy(alpha = 0.78f),
            1.00f to Color.White.copy(alpha = 0.96f),
            start = Offset(-shift, 0f),
            end = Offset(w - shift, h),
        )
        // Độ dày mép vát 3D
        drawOutline(outline, fresnelRim, alpha = 0.24f * refr, style = outerLipStroke)
        drawOutline(outline, fresnelRim, alpha = 0.42f * refr, style = innerBevelStroke)
        // Cạnh phản quang gương sắc nét ngoài cùng
        drawOutline(outline, fresnelRim, alpha = (0.50f + 0.50f * spec).coerceAtMost(1f), style = fresnelRimStroke)
    }
}

@Composable
fun ContactlessWaves(color: Color, animate: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "waves")
    val moving = if (animate) transition.animateFloat(0f, 1f, infiniteRepeatable(tween(800, easing = LinearEasing)), label = "phase") else null
    Canvas(modifier) {
        val phase = moving?.value ?: 0.6f
        val k = size.minDimension / 24f
        val c = Offset(4f * k, size.height / 2)
        for (i in 0..3) {
            val r = (4f + 4.2f * i) * k
            val bright = 0.35f + 0.65f * (1f - kotlin.math.abs(phase - (i + 1) / 5f) * 4f).coerceIn(0f, 1f)
            drawArc(
                color.copy(alpha = if (animate) bright else 1f),
                startAngle = -43f,
                sweepAngle = 86f,
                useCenter = false,
                topLeft = Offset(c.x - r, c.y - r),
                size = Size(2 * r, 2 * r),
                style = Stroke(width = 2.2f * k, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun CheckBadge(color: Color, animate: Boolean, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(if (animate) 0f else 1f) }
    val draw = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!animate) return@LaunchedEffect
        launch { pop.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = 220f)) }
        delay(100)
        draw.animateTo(1f, tween(300, easing = LinearEasing))
    }
    Canvas(modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value }) {
        val w = size.width
        drawCircle(color)
        val tick = Path().apply {
            moveTo(w * 0.31f, w * 0.513f)
            lineTo(w * 0.445f, w * 0.65f)
            lineTo(w * 0.704f, w * 0.364f)
        }
        val part = Path()
        val measure = PathMeasure()
        measure.setPath(tick, false)
        measure.getSegment(0f, measure.length * draw.value, part, true)
        drawPath(part, Color.White, style = Stroke(width = w * 0.093f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun FitText(
    text: String,
    color: Color,
    max: TextUnit,
    min: TextUnit,
    weight: FontWeight,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
) {
    val measurer = rememberTextMeasurer()
    val base = LocalTextStyle.current.merge(style)
    BoxWithConstraints(modifier) {
        val room = constraints.maxWidth
        val size = remember(text, room, max, min, base) {
            var sz = max.value
            while (sz > min.value &&
                measurer.measure(text, base.copy(fontSize = sz.sp, fontWeight = weight), maxLines = 1).size.width > room
            ) {
                sz -= 0.5f
            }
            sz.sp
        }
        Text(text, color = color, fontSize = size, fontWeight = weight, maxLines = 1, softWrap = false,
            overflow = TextOverflow.Ellipsis, style = base)
    }
}

@Composable
fun rememberAccent(s: IslandSettings, adaptive: Color? = null): Color {
    val context = LocalContext.current
    return remember(s.accentMode, s.accentColor, adaptive) { resolveAccent(context, s, adaptive) }
}

fun resolveAccent(context: android.content.Context, s: IslandSettings, adaptive: Color?): Color = when (s.accentMode) {
    AccentMode.DEFAULT -> Tints.Lavender
    AccentMode.CUSTOM -> Color(s.accentColor)
    AccentMode.ADAPTIVE -> adaptive ?: Tints.Lavender
    AccentMode.SYSTEM -> systemAccent(context) ?: Tints.Lavender
}

fun systemAccent(context: android.content.Context): Color? =
    if (android.os.Build.VERSION.SDK_INT >= 31) {
        runCatching { Color(context.getColor(android.R.color.system_accent1_200)) }.getOrNull()
    } else {
        null
    }

fun onAccent(accent: Color): Color = if (accent.luminance() > 0.5f) Color(0xFF1B1830) else Color.White

@Composable
fun TurnArrow(turn: Turn, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val k = size.minDimension / 100f
        val flip = turn == Turn.LEFT
        fun x(v: Float) = if (flip) 100f - v else v
        val body = Path()
        val head = Path()
        when (turn) {
            Turn.STRAIGHT -> {
                body.moveTo(50 * k, 86 * k); body.lineTo(50 * k, 20 * k)
                head.moveTo(30 * k, 40 * k); head.lineTo(50 * k, 18 * k); head.lineTo(70 * k, 40 * k)
            }
            Turn.RIGHT, Turn.LEFT -> {
                body.moveTo(x(25f) * k, 92 * k); body.lineTo(x(25f) * k, 50 * k)
                body.quadraticBezierTo(x(25f) * k, 33.3f * k, x(41.7f) * k, 33.3f * k); body.lineTo(x(79f) * k, 33.3f * k)
                head.moveTo(x(62.5f) * k, 16.7f * k); head.lineTo(x(79f) * k, 33.3f * k); head.lineTo(x(62.5f) * k, 50 * k)
            }
            Turn.UTURN -> {
                body.moveTo(30 * k, 88 * k); body.lineTo(30 * k, 40 * k)
                body.quadraticBezierTo(30 * k, 14 * k, 54 * k, 14 * k)
                body.quadraticBezierTo(76 * k, 14 * k, 76 * k, 40 * k); body.lineTo(76 * k, 62 * k)
                head.moveTo(60 * k, 48 * k); head.lineTo(76 * k, 66 * k); head.lineTo(92 * k, 48 * k)
            }
        }
        val stroke = Stroke(width = 11.7f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(body, color, style = stroke)
        drawPath(head, color, style = stroke)
    }
}

/** Chế độ xem trước đang yêu cầu, chỉ có hiệu lực khi app Capsule VN đang mở (rời app là tự tắt). */
@Composable
fun previewMode(): com.akira.island.core.CardPreviewMode {
    val mode by IslandBus.cardPreviewMode.collectAsState()
    val visible by IslandBus.appVisible.collectAsState()
    return if (visible) mode else com.akira.island.core.CardPreviewMode.NONE
}

@Composable
fun calibratingNow(): Boolean {
    val on by IslandBus.calibrating.collectAsState()
    val visible by IslandBus.appVisible.collectAsState()
    return on && visible
}

@Composable
fun previewLedNow(): Boolean {
    val on by IslandBus.previewLed.collectAsState()
    val visible by IslandBus.appVisible.collectAsState()
    return on && visible
}
