@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package com.akira.island.island

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.akira.island.core.AppMessage
import com.akira.island.core.CallInfo
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.kind
import com.akira.island.core.LiveInfo
import com.akira.island.core.LiveKind
import com.akira.island.core.MsgAction
import com.akira.island.core.ReplyState
import com.akira.island.core.MediaInfo
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.formatDuration
import com.akira.island.data.IslandSettings
import com.akira.island.core.CardPreviewMode
import com.akira.island.data.NoticeKind
import com.akira.island.data.NoticeTweak
import com.akira.island.data.SettingsRepo
import com.akira.island.service.Messages
import com.akira.island.service.OtpHelper
import androidx.compose.runtime.collectAsState
import com.akira.island.core.tr
import com.akira.island.ui.Inter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val Digits = TextStyle(fontFamily = Inter, fontFeatureSettings = "tnum", fontWeight = FontWeight.SemiBold)

fun activeHeight(s: IslandSettings): Dp = (s.height + s.activeExtra).dp

fun compactGap(s: IslandSettings): Dp = s.cameraGap.dp.coerceAtLeast(8.dp)

fun sideMax(s: IslandSettings): Dp = activeHeight(s) * 2.6f

fun noticeHeight(s: IslandSettings): Dp = ((s.height.dp * 1.68f).coerceAtLeast(56.dp) + s.noticeHeightExtra.dp).coerceAtLeast(36.dp)

/** Phân loại "thông báo khác" để áp dụng vị trí / kích thước riêng. */
fun noticeKindOf(n: Notice): NoticeKind = n.kind()

fun callHeight(s: IslandSettings): Dp = s.callHeight.dp.coerceIn(50.dp, 150.dp)

fun cameraInset(s: IslandSettings): Dp = s.height.dp * 0.5f + 11.5.dp

@Composable
fun CompactContent(act: Act, s: IslandSettings, wideWidth: Dp) {
    val h = activeHeight(s)
    val gap = compactGap(s)
    val side = sideMax(s)
    val icon = h * 0.5f
    when (act) {
        
        is Act.Idle -> Box(Modifier.size(s.width.dp, s.height.dp), contentAlignment = Alignment.CenterStart) {
            val keyguard by IslandBus.keyguard.collectAsState()
            AnimatedVisibility(s.evLock && keyguard, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                Icon(Ios.lock, null, tint = Color.White, modifier = Modifier.padding(start = s.height.dp * 0.38f).size(s.height.dp * 0.46f))
            }
        }

        is Act.MediaA -> MediaCompact(act.m, s)

        is Act.TimerA -> {
            val now = rememberNow(250)
            if (act.t.ringing) {
                val wobble = rememberInfiniteTransition(label = "bell")
                val angle = if (s.reduceEffects || !s.fxShake) null
                else wobble.animateFloat(-18f, 18f, infiniteRepeatable(tween(120), RepeatMode.Reverse), label = "a")
                CompactRow(h, gap, side * 1.4f,
                    left = {
                        Icon(Ios.bell, null, tint = Tints.Orange, modifier = Modifier.size(icon).graphicsLayer {
                            rotationZ = angle?.value ?: 0f
                            transformOrigin = TransformOrigin(0.5f, 0.1f)
                        })
                    },
                    right = { Text(tr("Hẹn giờ", "Timer"), color = Tints.Orange, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                )
            } else {
                val left = act.t.left(now)
                val color = if (left <= 10_000 && !act.t.paused) Tints.Red else Tints.Orange
                CompactRow(h, gap, side * 1.4f,
                    left = { Icon(if (act.t.paused) Ios.pause else Ios.timer, null, tint = color, modifier = Modifier.size(icon)) },
                    right = { TimeText(formatDuration(left + 999), color, 18, FontWeight.Bold) },
                )
            }
        }

        is Act.StopwatchA -> {
            val now = rememberNow(250, active = act.w.running)
            CompactRow(h, gap, side * 1.4f,
                left = { Icon(Ios.stopwatch, null, tint = Tints.Orange, modifier = Modifier.size(icon)) },
                right = { TimeText(formatDuration(act.w.elapsed(now)), Tints.Orange, 18, FontWeight.Bold) },
            )
        }

        is Act.ChronoA -> {
            val now = rememberWallNow(500)
            
            val recording = act.c.isRecording
            val tint = if (recording) Tints.Red else Tints.Orange
            CompactRow(h, gap, side * 1.4f,
                left = {
                    when {
                        recording -> Equalizer(tint, true, s.liveEqualizer, Modifier.size(h * 0.62f, h * 0.42f), bars = 5)
                        act.c.pausedMs != null -> Icon(Ios.pause, null, tint = tint, modifier = Modifier.size(icon))
                        else -> AppIcon(act.c.icon, h * 0.56f, Ios.timer, tint)
                    }
                },
                right = { TimeText(formatDuration(act.c.elapsed(now)), tint, 18, FontWeight.Bold) },
            )
        }

        is Act.CallA -> if (act.c.incoming) {
            IncomingCall(act.c, s, wideWidth)
        } else {
            val now = rememberWallNow(500)
            CompactRow(h, gap, side * 2f,
                left = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Ios.call, null, tint = Tints.Green, modifier = Modifier.size(h * 0.42f))
                        Spacer(Modifier.width(6.dp))
                        TimeText(formatDuration(now - act.c.base), Tints.Green, 15)
                    }
                },
                right = { Equalizer(Tints.Green, true, s.liveEqualizer, Modifier.size(h * 0.7f, h * 0.42f), bars = 6) },
            )
        }

        is Act.TorchA -> CompactRow(h, gap, side,
            left = { Icon(Ios.flashlight, null, tint = Tints.Yellow, modifier = Modifier.size(icon)) },
            right = { GlowDot(Tints.Yellow, h * 0.26f, !s.reduceEffects) },
        )

        is Act.ProgressA -> CompactRow(h, gap, side,
            left = { AppIcon(act.p.icon, h * 0.56f, Ios.download, Tints.Blue) },
            right = {
                if (act.p.indeterminate) {
                    Icon(Ios.download, null, tint = Tints.Blue, modifier = Modifier.size(icon))
                } else {
                    
                    val fraction by animateFloatAsState(act.p.fraction, tween(450, easing = FastOutSlowInEasing), label = "progress")
                    ProgressRing(fraction, Tints.Blue, Modifier.size(h * 0.52f), stroke = 3.dp)
                }
            },
        )

        is Act.ChargingA -> CompactRow(h, gap, side * 1.6f,
            left = { Icon(Ios.flash, null, tint = Tints.Green, modifier = Modifier.size(icon)) },
            right = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeText("${act.b.level}%", Tints.Green, 15)
                    Spacer(Modifier.width(5.dp))
                    BatteryGlyph(act.b.level, Tints.Green, Modifier.size(h * 0.6f, h * 0.3f))
                }
            },
        )

        is Act.LiveA -> LiveCompact(act.l, s)

        is Act.AlarmA -> AlarmCompact(act.a, s)

        is Act.NoticeA -> NoticeCompact(act.n, s, wideWidth)

        is Act.UnreadA -> NoticeCompact(act.n, s, wideWidth)

        is Act.PayA -> PayReady(tr("Thanh toán", "Pay"), tr("Chạm vào đầu đọc thẻ", "Hold near reader"), act.p.icon, s, wideWidth)

    }
}

@Composable
private fun TimeText(text: String, color: Color, size: Int = 16, weight: FontWeight = FontWeight.SemiBold) {
    Text(text, color = color, fontSize = size.sp, style = Digits.copy(fontWeight = weight), maxLines = 1, softWrap = false)
}

@Composable
private fun GlowDot(color: Color, size: Dp, animate: Boolean) {
    
    val time = rememberFxClock(animate)
    Canvas(Modifier.size(size * 1.8f)) {
        val k = if (animate) 0.55f + 0.45f * pingPong(time.value, 1100) else 1f
        val r = this.size.minDimension / 2
        drawCircle(color.copy(alpha = 0.25f * k), radius = r * k)
        drawCircle(color, radius = r / 1.8f)
    }
}

@Composable
fun CompactRow(
    h: Dp,
    gap: Dp,
    side: Dp,
    left: @Composable BoxScope.() -> Unit,
    right: @Composable BoxScope.() -> Unit,
    shrink: Boolean = true,
    /** Chiều rộng cố định riêng (vd. thông báo viên nhỏ); null = theo "Khi có hoạt động". */
    widthOverride: Dp? = null,
) {
    
    val st = SettingsRepo.state.collectAsState().value
    // activeWidth = 0: tự động theo nội dung; > 0: đúng chiều rộng người dùng chọn (nội dung tự thu nhỏ cho vừa)
    val auto = widthOverride == null && st.activeWidth == 0
    val baseWidth = widthOverride ?: (st.width.dp + st.activeWidth.dp)
    val density = LocalDensity.current
    SubcomposeLayout { _ ->
        val hPx = h.roundToPx()
        val pad = (h * 0.3f).roundToPx()
        val gapPx = gap.roundToPx()
        val base = baseWidth.roundToPx()
        // Khoảng trống mỗi bên camera theo chiều rộng đã chọn (luôn >= 1 để không bị bỏ qua khi rất hẹp)
        val sideRoom = ((base - gapPx) / 2 - pad).coerceAtLeast(1)
        val maxSide = if (auto) side.roundToPx().coerceAtLeast(1) else minOf(side.roundToPx().coerceAtLeast(1), sideRoom)
        val loose = Constraints(maxWidth = maxSide, maxHeight = hPx)
        var l = subcompose("l") { Box(contentAlignment = Alignment.Center, content = left) }.first().measure(loose)
        var r = subcompose("r") { Box(contentAlignment = Alignment.Center, content = right) }.first().measure(loose)
        val sideNeed = maxOf(l.width, r.width)
        val k = if (shrink && sideNeed > sideRoom) (sideRoom.toFloat() / sideNeed).coerceIn(if (auto) 0.7f else 0.55f, 1f) else 1f
        if (k < 1f) {
            val small = Density(density.density * k, density.fontScale)
            l = subcompose("ls") {
                CompositionLocalProvider(LocalDensity provides small) { Box(contentAlignment = Alignment.Center, content = left) }
            }.first().measure(loose)
            r = subcompose("rs") {
                CompositionLocalProvider(LocalDensity provides small) { Box(contentAlignment = Alignment.Center, content = right) }
            }.first().measure(loose)
        }
        val needed = 2 * (pad + maxOf(l.width, r.width)) + gapPx
        val w = if (auto) maxOf(needed, base) else maxOf(base, gapPx + 2 * pad + 2)
        layout(w, hPx) {
            l.place(pad, (hPx - l.height) / 2)
            r.place(w - pad - r.width, (hPx - r.height) / 2)
        }
    }
}

@Composable
private fun MediaCompact(m: MediaInfo, s: IslandSettings) {
    val h = activeHeight(s)
    val art = h * 0.58f
    val egg by IslandBus.egg.collectAsState()
    
    val accent by animateColorAsState(m.accent, tween(400), label = "accent")
    CompactRow(h, compactGap(s), sideMax(s),
        left = { Crossfade(m.art, animationSpec = tween(300), label = "art") { Art(it, art, art * 0.24f) } },
        right = {
            
            if (egg) SoulHeart(Modifier.size(h * 0.5f)) else Equalizer(accent, m.playing, s.liveEqualizer, Modifier.size(h * 0.6f, h * 0.42f))
        },
    )
}

@Composable
private fun NoticeCompact(n: Notice, base: IslandSettings, cardWidth: Dp) {
    // Các thông báo khác dùng vị trí / kích thước riêng theo từng loại (chưa chỉnh thì dùng chung)
    val previewKind by IslandBus.previewNoticeKind.collectAsState()
    val preview = previewMode()
    val kind = if (preview == CardPreviewMode.NOTICE) previewKind else noticeKindOf(n)
    val tweak = base.tweakFor(kind)
    val s = remember(base, tweak) { base.copy(noticeWidth = tweak.width, noticeHeightExtra = tweak.heightExtra) }
    val wideWidth = if (s.noticeWidth > 0) s.noticeWidth.dp.coerceAtMost(LocalConfiguration.current.screenWidthDp.dp) else cardWidth
    when {
        n.style == NoticeStyle.MESSAGE && n.message != null -> MessageCard(n, n.message, s, cardWidth)
        n.style == NoticeStyle.PAY_DONE -> PayDone(n, s, wideWidth)
        n.style == NoticeStyle.LIVE && n.live != null -> LiveBanner(n, n.live, s, wideWidth)
        n.style == NoticeStyle.ALARM && n.alarm != null -> AlarmBanner(n, n.alarm, s, wideWidth)
        n.style == NoticeStyle.EGG -> EggBanner(n, s)
        n.style == NoticeStyle.SENT -> SentBanner(n, s, wideWidth)
        n.style == NoticeStyle.UPDATE -> UpdateCard(n, s, cardWidth)
        n.style == NoticeStyle.PILL || n.title.isBlank() && n.state == null -> PillNotice(n, base, base.noticeTweaks[NoticeKind.MODE])
        else -> WideNotice(n, s, wideWidth)
    }
}

@Composable
private fun WideNotice(n: Notice, s: IslandSettings, wideWidth: Dp) {
    
    val pop = remember(n.id) { Animatable(if (s.reduceEffects) 1f else 0.3f) }
    val fill = remember(n.id) { Animatable(if (s.reduceEffects) 1f else 0f) }
    LaunchedEffect(n.id) {
        launch { pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 320f)) }
        fill.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    val height = noticeHeight(s)

    val value = when {
        n.ring != null -> "${(n.ring * 100).toInt()}%"
        else -> n.state ?: n.caption
    }
    Row(
        Modifier.width(wideWidth).height(height).padding(horizontal = height * 0.32f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(n.icon, null, tint = n.tint, modifier = Modifier.size(height * 0.355f).scale(pop.value))
            Spacer(Modifier.width(9.dp))
            
            FitText(n.title, Color.White, 17.sp, 13.sp, FontWeight.SemiBold, Modifier.weight(1f, fill = false))
        }
        Spacer(Modifier.width(compactGap(s)))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                FitText(value, n.tint, if (value.length <= 5) 17.sp else 16.sp, 12.sp, FontWeight.SemiBold,
                    Modifier.weight(1f, fill = false), style = Digits)
            }
            if (n.battery != null) {
                Spacer(Modifier.width(8.dp))
                BatteryGlyph((n.battery * fill.value).toInt(), n.tint, Modifier.size(30.dp, 14.dp))
            }
            if (n.ring != null) {
                Spacer(Modifier.width(8.dp))
                ProgressRing(n.ring * fill.value, n.tint, Modifier.size(20.dp), stroke = 3.dp)
            }
        }
    }
}

@Composable
private fun PillNotice(n: Notice, s: IslandSettings, own: NoticeTweak?) {
    // Kích thước riêng của thông báo viên nhỏ (Giao diện → Thông báo viên nhỏ); chưa chỉnh thì như cũ
    val h = (activeHeight(s) + (own?.heightExtra ?: 0).dp).coerceAtLeast(22.dp)
    val fixedW = own?.width?.takeIf { it > 0 }?.dp
    val swing = remember(n.id) { Animatable(0f) }
    LaunchedEffect(n.id) {
        if (s.reduceEffects || !s.fxShake) return@LaunchedEffect
        
        var amp = 22f
        repeat(6) {
            swing.animateTo(amp, tween(70))
            amp = -amp * 0.75f
        }
        swing.animateTo(0f, spring(dampingRatio = 0.5f))
    }
    CompactRow(h, compactGap(s), h * 2.4f,
        left = {
            Icon(
                n.icon, null, tint = n.tint,
                modifier = Modifier.size(h * 0.5f).graphicsLayer {
                    rotationZ = swing.value
                    transformOrigin = TransformOrigin(0.5f, 0.1f)
                },
            )
        },
        right = {
            if (n.state != null) {
                Text(n.state, color = n.tint, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
            }
        },
        
        shrink = fixedW != null,
        widthOverride = fixedW,
    )
}

@Composable
internal fun MessageCard(n: Notice, m: AppMessage, s: IslandSettings, @Suppress("UNUSED_PARAMETER") wideWidth: Dp) {
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val iconSize = 44.dp
    val reply by IslandBus.reply.collectAsState()
    val locked by IslandBus.locked.collectAsState()
    
    val hidden = locked && m.sensitive
    val replyAction = reply?.takeIf { it.noticeId == n.id && !locked }?.action
    val accent = rememberAccent(s, m.accent)
    var open by remember(n.id) { mutableStateOf(false) }
    val dim = Color.White.copy(alpha = 0.55f)

    DisposableEffect(open, replyAction != null) {
        IslandBus.noticeHold.value = open || replyAction != null
        onDispose { IslandBus.noticeHold.value = false }
    }
    
    DisposableEffect(n.id) {
        onDispose { if (IslandBus.reply.value?.noticeId == n.id) IslandBus.reply.value = null }
    }

    // Khoảng cách từ dòng tên app / thời gian xuống nội dung: vừa đủ, không để trống quá nhiều
    val topGap = (6.dp + (s.msgHeightExtra * 0.45f).dp).coerceAtLeast(2.dp)
    val bottomPad = (16.dp + (s.msgHeightExtra * 0.55f).dp).coerceAtLeast(4.dp)

    Column(
        Modifier
            .animateContentSize(islandSpec(s))
            .width(messageWidth(s))
            .padding(bottom = bottomPad),
    ) {
        
        // Dòng tên app (trái) & thời gian (phải) hai bên camera: chữ rõ hơn, thụt vào khỏi góc bo để không bị cắt
        val headerInset = (s.cardCornerRadius * 0.55f).dp.coerceIn(20.dp, 30.dp)
        Row(
            Modifier.fillMaxWidth().height(s.height.dp + 4.dp).padding(start = headerInset, end = headerInset, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (m.appIcon != null) {
                    Image(m.appIcon, null, Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)))
                    Spacer(Modifier.width(7.dp))
                }
                Text(m.appName, color = Color.White.copy(alpha = 0.72f), fontSize = 13.5.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(compactGap(s)))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(tr("bây giờ", "now"), color = Color.White.copy(alpha = 0.72f), fontSize = 13.5.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                Spacer(Modifier.width(5.dp))
                Icon(Ios.bell, null, tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(15.dp))
            }
        }
        Spacer(Modifier.height(topGap))
        Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.Top) {
            NotifIcon(if (hidden) m.copy(avatar = null) else m, n, iconSize, Color(s.color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 22.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (hidden) m.appName else n.title, color = Color.White, fontSize = 16.sp, lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    if (m.history.size > 1 && !hidden) {
                        Spacer(Modifier.width(8.dp))
                        CountPill(m.history.size, open, accent) {
                            haptic()
                            open = !open
                        }
                    }
                }
                if (hidden) {
                    Text(tr("Nội dung đã ẩn", "Content hidden"), color = dim, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 1)
                } else if (open && m.history.size > 1) {
                    m.history.forEachIndexed { i, line ->
                        Text(
                            line, color = Color.White.copy(alpha = if (i == m.history.lastIndex) 0.9f else 0.6f),
                            fontSize = 14.sp, lineHeight = 19.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                } else if (m.text.isNotEmpty()) {
                    Text(
                        m.text, color = Color.White.copy(alpha = 0.85f), fontSize = 14.5.sp, lineHeight = 20.sp, maxLines = s.msgLines.coerceIn(1, 8),
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }
        }
        if (m.otpCode != null && !locked && s.otpEnabled) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp + iconSize + 12.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Tints.Green.copy(alpha = 0.22f))
                        .clickable {
                            haptic()
                            OtpHelper.copyToClipboard(context, m.otpCode)
                            if (s.otpAutoFill) {
                                IslandBus.otpFillRequests.tryEmit(m.otpCode)
                            }
                            IslandBus.notice.value = null
                            IslandBus.post(
                                Notice(
                                    title = if (s.otpAutoFill) tr("Đã chép & điền OTP", "OTP copied & filled")
                                    else tr("Đã sao chép OTP", "OTP copied"),
                                    state = m.otpCode,
                                    icon = n.icon,
                                    tint = Tints.Green,
                                    style = NoticeStyle.SENT,
                                    extraMs = -SettingsRepo.current.noticeDurationMs / 2L,
                                ),
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (s.otpAutoFill) tr("Chép & Điền OTP: ${m.otpCode}", "Copy & Fill OTP: ${m.otpCode}")
                        else tr("Sao chép OTP: ${m.otpCode}", "Copy OTP: ${m.otpCode}"),
                        color = Tints.Green,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
        if (replyAction != null) {
            
            Box(Modifier.padding(start = 18.dp + iconSize + 12.dp, end = 16.dp)) {
                ReplyField(replyAction, accent) { text ->
                    if (text.isBlank()) return@ReplyField
                    if (Controls.sendReply(context, replyAction, text)) {
                        Messages.onReplied(m.key)
                        IslandBus.reply.value = null
                        IslandBus.notice.value = null
                        
                        IslandBus.post(
                            Notice(
                                title = tr("Đã gửi", "Sent"),
                                state = n.title,
                                icon = n.icon,
                                tint = accent,
                                style = NoticeStyle.SENT,
                                extraMs = -SettingsRepo.current.noticeDurationMs / 2L,
                            ),
                        )
                    } else {
                        
                        Toast.makeText(context, tr("Không thể gửi phản hồi", "Couldn't send the reply"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (m.actions.isNotEmpty() && !locked) {
            
            // Nút thao tác dạng viên tròn, chia đều bề ngang & thẳng hàng với ảnh đại diện
            Row(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                m.actions.forEach { a ->
                    ActionPill(a.title, accent, Modifier.weight(1f)) {
                        if (a.isReply) {
                            IslandBus.reply.value = ReplyState(n.id, a)
                        } else {
                            Controls.send(context, a.intent)
                            IslandBus.notice.value = null
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun messageWidth(s: IslandSettings = SettingsRepo.current): Dp =
    s.msgWidth.dp.coerceIn(200.dp, LocalConfiguration.current.screenWidthDp.dp)

@Composable
fun messageTop(s: IslandSettings): Dp {
    val status = statusBarHeight()
    return maxOf(s.height.dp + 6.dp, status + 24.dp - s.topOffset.dp)
}

@Composable
private fun statusBarHeight(): Dp {
    val res = LocalContext.current.resources
    val density = LocalDensity.current
    return remember(res, density) {
        @Suppress("DiscouragedApi", "InternalInsetResource")
        val id = res.getIdentifier("status_bar_height", "dimen", "android")
        if (id > 0) with(density) { res.getDimensionPixelSize(id).toDp() } else 24.dp
    }
}

@Composable
private fun NotifIcon(m: AppMessage, n: Notice, size: Dp, ring: Color) {
    Box(Modifier.size(size + 4.dp)) {
        val raw = m.avatar ?: m.appIcon
        val big = remember(raw) { raw?.let { runCatching { circleCrop(it) }.getOrDefault(it) } }
        if (big != null) {
            Image(big, null, Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.size(size).clip(CircleShape).background(Color(0xFF2C2C2E)), contentAlignment = Alignment.Center) {
                Icon(n.icon, null, tint = Color.White, modifier = Modifier.size(size * 0.5f))
            }
        }
        if (m.avatar != null && m.appIcon != null) {
            Image(
                m.appIcon, null,
                Modifier.align(Alignment.BottomEnd).size(size * 0.42f).clip(CircleShape).background(Color.Black).padding(2.dp).clip(CircleShape),
            )
        }
    }
}

@Composable
private fun CountPill(count: Int, open: Boolean, accent: Color, onClick: () -> Unit) {
    val ink = onAccent(accent)
    val rotation by animateFloatAsState(if (open) 180f else 0f, spring(dampingRatio = 0.6f, stiffness = 400f), label = "chevron")
    Row(
        Modifier.height(24.dp).clip(CircleShape).background(accent).clickable(onClick = onClick).padding(start = 9.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$count", color = ink, fontSize = 13.sp, style = Digits, maxLines = 1)
        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = ink, modifier = Modifier.size(17.dp).graphicsLayer { rotationZ = rotation })
    }
}

@Composable
private fun ActionPill(title: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val haptic = rememberHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .height(38.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = if (pressed) 0.28f else 0.16f))
            .clickable(interaction, indication = null) {
                haptic()
                onClick()
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Cắt ảnh đại diện thành hình tròn ngay trên bitmap — luôn tròn kể cả khi lớp vẽ không hỗ trợ clip. */
private fun circleCrop(src: ImageBitmap): ImageBitmap {
    val b = src.asAndroidBitmap()
    val d = minOf(b.width, b.height)
    val out = android.graphics.Bitmap.createBitmap(d, d, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(out)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
    paint.shader = android.graphics.BitmapShader(b, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP).apply {
        setLocalMatrix(android.graphics.Matrix().apply { setTranslate(-(b.width - d) / 2f, -(b.height - d) / 2f) })
    }
    canvas.drawCircle(d / 2f, d / 2f, d / 2f, paint)
    return out.asImageBitmap()
}

@Composable
private fun TextAction(title: String, accent: Color, modifier: Modifier = Modifier, size: TextUnit = 16.sp, onClick: () -> Unit) {
    val haptic = rememberHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(interaction, indication = null) {
                haptic()
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            title, color = accent.copy(alpha = if (pressed) 0.55f else 1f), fontSize = size, fontWeight = FontWeight.SemiBold,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReplyField(action: MsgAction, accent: Color, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        
        repeat(3) {
            delay(180)
            runCatching { focus.requestFocus() }
            keyboard?.show()
        }
    }
    val ready = text.isNotBlank()
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontFamily = Inter),
                cursorBrush = SolidColor(accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend(text) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text("${action.title.ifBlank { tr("Trả lời", "Reply") }}…", color = Tints.Gray, fontSize = 15.sp, maxLines = 1)
                        }
                        inner()
                    }
                },
            )
        }
        Spacer(Modifier.width(8.dp))
        RoundButton(
            Ios.arrowUp, size = 40.dp,
            bg = if (ready) accent else accent.copy(alpha = 0.35f), fg = onAccent(accent), iconScale = 0.5f,
        ) { onSend(text) }
    }
}

@Composable
private fun PayReady(title: String, caption: String?, appIcon: ImageBitmap?, s: IslandSettings, wideWidth: Dp) {
    val height = noticeHeight(s)
    Row(
        Modifier.width(wideWidth).height(height).padding(horizontal = height * 0.29f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            ContactlessWaves(Tints.Lavender, !s.reduceEffects, Modifier.size(height * 0.42f))
            Spacer(Modifier.width(10.dp))
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.width(compactGap(s)))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (caption != null) {
                
                FitText(caption, Tints.Gray, 14.sp, 11.sp, FontWeight.Normal, Modifier.weight(1f, fill = false))
            }
            if (appIcon != null) {
                Spacer(Modifier.width(9.dp))
                Image(appIcon, null, Modifier.size(24.dp).clip(RoundedCornerShape(7.dp)))
            }
        }
    }
}

@Composable
private fun PayDone(n: Notice, s: IslandSettings, wideWidth: Dp) {
    val haptic = rememberHaptic()
    LaunchedEffect(n.id) { haptic() }
    val height = noticeHeight(s)
    Row(
        Modifier.width(wideWidth).height(height).padding(horizontal = height * 0.29f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            CheckBadge(n.tint, !s.reduceEffects, Modifier.size(height * 0.42f))
            Spacer(Modifier.width(10.dp))
            Text(n.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Spacer(Modifier.width(compactGap(s)))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (n.state != null) {
                Text(n.state, color = n.tint, fontSize = 18.sp, style = Digits.copy(fontWeight = FontWeight.Bold), maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

fun liveTint(kind: LiveKind): Color = when (kind) {
    LiveKind.NAV -> Tints.Green
    LiveKind.RIDE -> Tints.Yellow
    LiveKind.DELIVERY -> Tints.Orange
}

fun liveShort(l: LiveInfo): String? = l.distance
    ?: if (l.kind == LiveKind.NAV) {
        l.eta?.substringAfterLast(" · ")?.takeIf { it.isNotBlank() }
    } else {
        l.title.ifBlank { l.text }.take(18).takeIf { it.isNotBlank() }
    }

@Composable
fun LiveGlyph(l: LiveInfo, size: Dp, tint: Color) {
    when {
        l.turn != null -> TurnArrow(l.turn, tint, Modifier.size(size))
        l.art != null -> Image(l.art, null, Modifier.size(size).clip(CircleShape))
        else -> AppIcon(l.icon, size, Ios.navigate, tint)
    }
}

@Composable
private fun LiveCompact(l: LiveInfo, s: IslandSettings) {
    val h = activeHeight(s)
    val tint = liveTint(l.kind)
    CompactRow(h, compactGap(s), sideMax(s) * 1.5f,
        left = { LiveGlyph(l, if (l.turn != null) h * 0.51f else h * 0.56f, tint) },
        right = {
            val short = liveShort(l)
            if (short != null) {
                Text(short, color = tint, fontSize = 17.sp, style = Digits.copy(fontWeight = FontWeight.Bold), maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
            } else {
                GlowDot(tint, h * 0.22f, !s.reduceEffects)
            }
        },
    )
}

@Composable
private fun LiveBanner(n: Notice, l: LiveInfo, s: IslandSettings, wideWidth: Dp) {
    val height = noticeHeight(s)
    val tint = liveTint(l.kind)
    val pop = remember(n.id) { Animatable(if (s.reduceEffects) 1f else 0.3f) }
    LaunchedEffect(n.id) { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 320f)) }
    Row(
        Modifier.width(wideWidth).height(height).padding(horizontal = height * 0.32f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.scale(pop.value)) { LiveGlyph(l, height * 0.39f, tint) }
            Spacer(Modifier.width(10.dp))
            Text(
                l.distance ?: l.title, color = Color.White, fontSize = if (l.distance != null) 19.sp else 16.sp,
                style = Digits.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(compactGap(s)))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
            if (l.text.isNotBlank()) {
                Text(l.text, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, lineHeight = 16.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            }
            
            val shortEta = l.eta?.removePrefix("Đến nơi lúc ")?.removePrefix("Arrive ")?.split(" · ")?.take(2)?.joinToString(" · ")
            if (shortEta != null) {
                Text(shortEta, color = tint, fontSize = 13.sp, style = Digits, maxLines = 1)
            }
        }
    }
}

@Composable
private fun IncomingCall(c: CallInfo, s: IslandSettings, @Suppress("UNUSED_PARAMETER") wideWidth: Dp) {
    val context = LocalContext.current
    val breath = rememberInfiniteTransition(label = "answer")
    val wave = if (s.reduceEffects) null else breath.animateFloat(0f, 1f, infiniteRepeatable(tween(500, easing = LinearEasing)), label = "w")
    val pulse = Modifier.graphicsLayer {
        val k = wave?.let { 1f + 0.06f * kotlin.math.sin(it.value * 2 * Math.PI).toFloat().coerceAtLeast(0f) } ?: 1f
        scaleX = k
        scaleY = k
    }
    val cardW = s.callWidth.dp.coerceIn(200.dp, LocalConfiguration.current.screenWidthDp.dp)
    val cardH = callHeight(s)
    val avatarSize = (cardH * 0.60f).coerceIn(32.dp, 64.dp)
    val btnSize = (cardH * 0.55f).coerceIn(32.dp, 56.dp)
    val hPad = (cardH * 0.21f).coerceIn(10.dp, 22.dp)
    Row(
        Modifier.width(cardW).height(cardH).padding(horizontal = hPad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFDBA74), Color(0xFFF472B6)))),
            contentAlignment = Alignment.Center,
        ) {
            if (c.avatar != null) {
                Image(c.avatar, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(c.name.take(1).uppercase(), color = Color.White, fontSize = (avatarSize.value * 0.42f).sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(tr("Cuộc gọi đến", "Incoming call"), color = Tints.Gray, fontSize = 12.5.sp, lineHeight = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(c.name, color = Color.White, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        RoundButton(Ios.call, size = btnSize, bg = Tints.Red, iconScale = 0.52f, iconRotation = 135f) {
            Controls.endCall(context, c)
        }
        Spacer(Modifier.width(10.dp))
        RoundButton(Ios.call, pulse, size = btnSize, bg = Tints.Green, iconScale = 0.52f) {
            Controls.answerCall(context, c)
        }
    }
}
