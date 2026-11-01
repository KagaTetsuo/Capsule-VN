package com.akira.island.island

import android.content.Context
import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.akira.island.data.LedStyle
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akira.island.R
import com.akira.island.core.AppMessage
import com.akira.island.core.BatteryInfo
import com.akira.island.core.CallInfo
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.ChronoInfo
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.MsgAction
import com.akira.island.core.Notice
import com.akira.island.service.NotificationParser
import com.akira.island.core.Lyrics
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.Elasticity
import com.akira.island.data.ExpandStyle
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.data.TapAction
import com.akira.island.service.CallMonitor
import com.akira.island.service.NavScreen
import com.akira.island.service.PayMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

private val WidthDemo = com.akira.island.core.LiveInfo(
    key = "width-demo", pkg = "", kind = com.akira.island.core.LiveKind.NAV, title = "", text = "", sub = "",
    icon = null, art = null, postTime = 0L, open = null, distance = tr("300 m", "300 m"), turn = com.akira.island.core.Turn.RIGHT,
)

private fun buildPreviewMessage(context: Context): Notice {
    val flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) android.app.PendingIntent.FLAG_MUTABLE else 0)
    val dummy = android.app.PendingIntent.getBroadcast(
        context, 0, android.content.Intent("com.akira.island.TEST").setPackage(context.packageName), flags
    )
    return Notice(
        title = "Nguyễn Lê Lý Huỳnh",
        icon = Ios.load(context, R.drawable.ios_chatbubble_ellipses),
        tint = Tints.Blue,
        style = NoticeStyle.MESSAGE,
        message = AppMessage(
            key = "preview-msg",
            pkg = context.packageName,
            appName = "Zalo · Capsule VN",
            text = tr("Mô phỏng thẻ tin nhắn trực tiếp: điều chỉnh vị trí trên/dưới, bề ngang và bề dọc", "Mô phỏng thẻ tin nhắn trực tiếp: điều chỉnh vị trí trên/dưới, bề ngang và bề dọc"),
            avatar = null,
            appIcon = NotificationParser.icon(context, context.packageName),
            intent = null,
            actions = listOf(
                MsgAction(tr("Trả lời", "Reply"), dummy),
                MsgAction(tr("Đánh dấu đã đọc", "Mark as read"), dummy),
            ),
        ),
    )
}

private fun buildPreviewCall(): CallInfo = CallInfo(
    key = "preview-call",
    pkg = "",
    name = "Nguyễn Lê Lý Huỳnh",
    avatar = null,
    incoming = true,
    base = 0L,
    postTime = 0L,
    answer = null,
    decline = null,
    hangUp = null,
    open = null,
)

private fun buildPreviewUpdate(context: Context): Notice = Notice(
    title = tr("Đã có phiên bản 1.1.0", "Version 1.1.0 is available"),
    icon = Ios.load(context, R.drawable.ios_download),
    tint = Tints.Lavender,
    caption = tr("Tùy biến bo góc khung thông báo & kích thước thẻ cập nhật", "Customizable card corner radius & update size"),
    style = NoticeStyle.UPDATE,
    appIcon = NotificationParser.icon(context, context.packageName),
    update = com.akira.island.update.Release(
        version = "1.1.0",
        notes = tr("Tùy biến bo góc khung thông báo & kích thước thẻ cập nhật", "Customizable card corner radius & update size"),
        apkUrl = "https://github.com/KagaTetsuo/Capsule-VN/releases/download/v1.1.0/Capsule_VN.apk",
        pageUrl = "https://github.com/KagaTetsuo/Capsule-VN/releases",
        sizeBytes = 25_000_000L,
    ),
    openPkg = context.packageName,
    extraMs = 3_500,
)

val CalibrationRed = Color(0xFFFF6B6B)

const val ISLAND_PAD_DP = 12

private data class Frame(val act: Act, val expanded: Boolean) {
    val key: String get() = "${act.id}|$expanded"
}

@Composable
fun IslandOverlay(
    onTransition: () -> Unit = {},
    onBodyBounds: (Rect) -> Unit = {},
    onBubbleBounds: (Rect) -> Unit = {},
) {
    val saved by SettingsRepo.state.collectAsState()
    val powerSave by IslandBus.powerSave.collectAsState()
    
    val s = remember(saved, powerSave) { saved.forPowerSave(powerSave) }
    val media by IslandBus.media.collectAsState()
    val lyrics by IslandBus.lyrics.collectAsState()
    val progress by IslandBus.progress.collectAsState()
    val chrono by IslandBus.chrono.collectAsState()
    val calls by IslandBus.calls.collectAsState()
    val live by IslandBus.live.collectAsState()
    val torch by IslandBus.torch.collectAsState()
    val timer by IslandBus.timer.collectAsState()
    val stopwatch by IslandBus.stopwatch.collectAsState()
    val notice by IslandBus.notice.collectAsState()
    val battery by IslandBus.battery.collectAsState()
    val foreground by IslandBus.foregroundPackage.collectAsState()
    val expanded by IslandBus.expanded.collectAsState()
    val focus by IslandBus.focus.collectAsState()
    val calibrating by IslandBus.calibrating.collectAsState()
    val nextAlarm by IslandBus.nextAlarm.collectAsState()
    val egg by IslandBus.egg.collectAsState()
    val payReady by IslandBus.payReady.collectAsState()

    val screenOn by IslandBus.screenOn.collectAsState()
    var alarmNow by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(nextAlarm, screenOn) {
        val at = nextAlarm?.triggerAt ?: return@LaunchedEffect
        while (true) {
            val now = System.currentTimeMillis()
            alarmNow = now
            val next = when {
                now < at - ALARM_LEAD_MS -> at - ALARM_LEAD_MS
                now < at -> at
                else -> break
            }
            delay(next - now + 50)
        }
    }

    
    var chronoNow by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(chrono, screenOn) {
        while (true) {
            val now = System.currentTimeMillis()
            chronoNow = now
            val next = chrono.values.filter { it.countDown && it.pausedMs == null }
                .map { it.base + ChronoInfo.DONE_MS }.filter { it >= now }.minOrNull() ?: break
            delay(next - now + 50)
        }
    }
    val chronoActive = chrono.values.filterNot { it.finished(chronoNow) }

    val hasExpiring = live.values.any { it.expiresAt > 0 }
    var wallNow by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(hasExpiring) {
        while (hasExpiring) {
            wallNow = System.currentTimeMillis()
            delay(1000)
        }
    }
    
    val screenNav by NavScreen.data.collectAsState()
    val liveActive = live.values.filter { it.expiresAt == 0L || wallNow < it.expiresAt }
        .map { NavScreen.merge(it, screenNav[it.pkg]) }
        .let { list -> list + screenNav.values.filter { sn -> sn.distance != null && list.none { it.pkg == sn.pkg } }.map(NavScreen::synthetic) }

    val call = if (!s.cardCall) null else {
        val tel = calls[CallMonitor.KEY]
        val fromApp = calls.filterKeys { it != CallMonitor.KEY }.values.maxByOrNull { it.postTime }
        when {
            fromApp == null -> tel
            tel == null -> fromApp
            
            else -> fromApp.copy(
                incoming = tel.incoming,
                base = if (!tel.incoming && fromApp.incoming) tel.base else fromApp.base,
                avatar = fromApp.avatar ?: tel.avatar,
            )
        }
    }
    
    val callHidden by IslandBus.callHidden.collectAsState()
    val acts = buildList {
        
        if (call != null && call.incoming && !callHidden) add(Act.CallA(call))
        notice?.let { add(Act.NoticeA(it)) }

        val persistent = mutableListOf<Act>()
        val t = timer
        if (t != null && t.ringing) persistent += Act.TimerA(t)
        
        payReady?.let { persistent += Act.PayA(it) }
        if (call != null && !call.incoming) persistent += Act.CallA(call)
        if (torch.on && s.cardTorch) persistent += Act.TorchA(torch)
        if (t != null && !t.ringing) persistent += Act.TimerA(t)
        if (s.cardLive) liveActive.maxByOrNull { it.postTime }?.let { persistent += Act.LiveA(it) }
        stopwatch?.let { persistent += Act.StopwatchA(it) }
        if (s.cardTimer) chronoActive.maxByOrNull { it.postTime }?.let { persistent += Act.ChronoA(it) }
        val m = media
        if (s.cardMedia && m != null && !(s.hideWhenPlayerOpen && foreground == m.pkg)) persistent += Act.MediaA(m)
        if (s.cardProgress) progress.values.maxByOrNull { it.postTime }?.let { persistent += Act.ProgressA(it) }
        if (s.cardCharging && battery.charging) persistent += Act.ChargingA(battery)
        
        val alarm = nextAlarm
        if (s.cardAlarm && alarm != null && alarm.triggerAt - alarmNow in 1..ALARM_LEAD_MS) persistent += Act.AlarmA(alarm)

        val ringing = (persistent.firstOrNull() as? Act.TimerA)?.t?.ringing == true || call?.incoming == true
        val i = persistent.indexOfFirst { it.id == focus }
        if (i > 0 && !ringing) persistent.add(0, persistent.removeAt(i))
        addAll(persistent)
    }

    val previewUntil by IslandBus.widthPreviewUntil.collectAsState()
    var previewNow by remember { mutableLongStateOf(0L) }
    LaunchedEffect(previewUntil) {
        while (true) {
            previewNow = android.os.SystemClock.elapsedRealtime()
            if (previewNow >= previewUntil) break
            delay(250)
        }
    }
    val previewing = previewNow < previewUntil

    val context = LocalContext.current
    val cardPreview by IslandBus.cardPreviewMode.collectAsState()
    val previewMsgNotice = remember(context) { buildPreviewMessage(context) }
    val previewCallInfo = remember { buildPreviewCall() }
    val previewUpdateNotice = remember(context) { buildPreviewUpdate(context) }

    val primary = when {
        calibrating -> Act.Idle
        cardPreview == CardPreviewMode.MESSAGE -> Act.NoticeA(previewMsgNotice)
        cardPreview == CardPreviewMode.CALL -> Act.CallA(previewCallInfo)
        cardPreview == CardPreviewMode.UPDATE -> Act.NoticeA(previewUpdateNotice)
        previewing -> Act.LiveA(WidthDemo)
        else -> acts.firstOrNull() ?: Act.Idle
    }
    val incoming = (primary as? Act.CallA)?.c?.incoming == true
    
    val secondary = if (!s.bubble || primary.transient || incoming || calibrating || cardPreview != CardPreviewMode.NONE) null
    else acts.drop(1).firstOrNull { !it.transient && it !is Act.AlarmA }
    val showExpanded = (expanded || cardPreview == CardPreviewMode.EXPANDED) && !primary.transient && !incoming && !calibrating
    val h = s.height.dp

    val lastSecondary = remember { arrayOfNulls<Act>(1) }
    if (secondary != null) lastSecondary[0] = secondary
    val bubbleVisible = secondary != null && !showExpanded
    
    val slot = animateDpAsState(if (bubbleVisible) activeHeight(s) + 6.dp else 0.dp, islandSpec(s), label = "slot")

    val focusGone = expanded && focus != null && focus != Act.Idle.id && acts.none { it.id == focus }
    LaunchedEffect(focusGone) { if (focusGone) Controls.collapse() }

    LaunchedEffect(showExpanded, s.autoCollapseSec) {
        val limit = s.autoCollapseSec * 1000L
        if (!showExpanded || limit <= 0) return@LaunchedEffect
        val opened = SystemClock.elapsedRealtime()
        while (true) {
            val idle = SystemClock.elapsedRealtime() - maxOf(opened, IslandBus.lastTouch)
            if (idle >= limit) break
            delay(limit - idle)
        }
        Controls.collapse()
    }

    val hidden = s.hideIdle && primary is Act.Idle && !showExpanded && !calibrating

    val transitionKey = "${primary.id}|$showExpanded|${secondary?.id}|$hidden"
    val lastKey = remember { arrayOfNulls<String>(1) }
    SideEffect {
        if (lastKey[0] != transitionKey) {
            lastKey[0] = transitionKey
            onTransition()
        }
    }

    if (hidden) {
        SideEffect {
            onBodyBounds(Rect.Zero)
            onBubbleBounds(Rect.Zero)
        }
    }
    val hiddenNow = rememberUpdatedState(hidden)

    val body = remember { mutableStateOf(Rect.Zero) }
    val onBody = remember(onBodyBounds) {
        { r: Rect ->
            body.value = r
            onBodyBounds(if (hiddenNow.value) Rect.Zero else r)
        }
    }
    val onBubble = remember(onBubbleBounds) { { r: Rect -> onBubbleBounds(if (hiddenNow.value) Rect.Zero else r) } }
    val eggOn = egg && !calibrating

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        
        if (!calibrating) PayEffects(primary, { body.value }, s)
        
        if (eggOn) EggGlow { body.value }
        
        val top = TransformOrigin(0.5f, 0f)
        AnimatedVisibility(
            !hidden,
            enter = if (s.reduceEffects) fadeIn() else scaleIn(islandSpec(s), initialScale = 0.3f, transformOrigin = top) + fadeIn(tween(120)),
            exit = if (s.reduceEffects) fadeOut() else scaleOut(islandSpec(s), targetScale = 0.3f, transformOrigin = top) + fadeOut(tween(160)),
        ) {
            IslandRow(slot, s, primary, showExpanded, lyrics, battery, bubbleVisible, lastSecondary[0], calibrating, onBody, onBubble)
        }
        if (eggOn) {
            EggSparks { body.value }
            EggDriver({ body.value }, s)
        }
    }
}

@Composable
private fun IslandRow(
    slot: State<Dp>,
    s: IslandSettings,
    primary: Act,
    showExpanded: Boolean,
    lyrics: Lyrics?,
    battery: BatteryInfo,
    bubbleVisible: Boolean,
    bubbleAct: Act?,
    calibrating: Boolean,
    onBodyBounds: (Rect) -> Unit,
    onBubbleBounds: (Rect) -> Unit,
) {
    val cardPreview by IslandBus.cardPreviewMode.collectAsState()
    val extraYTarget = when {
        !calibrating && primary is Act.CallA && primary.c.incoming -> (s.topOffset + s.callOffsetY - s.windowBaseY).dp
        !calibrating && primary is Act.NoticeA && primary.n.style == NoticeStyle.MESSAGE -> (s.topOffset + s.msgOffsetY - s.windowBaseY).dp
        !calibrating && primary is Act.NoticeA && primary.n.style == NoticeStyle.UPDATE -> (s.topOffset + s.updateOffsetY - s.windowBaseY).dp
        // Bảng mở rộng: áp dụng vị trí trên / dưới riêng (expandedOffsetY)
        !calibrating && showExpanded -> (s.topOffset + s.expandedOffsetY - s.windowBaseY).dp
        else -> (s.topOffset - s.windowBaseY).dp
    }
    val extraY = animateDpAsState(
        extraYTarget,
        if (cardPreview != CardPreviewMode.NONE && cardPreview != CardPreviewMode.EXPANDED) androidx.compose.animation.core.snap() else islandSpec(s),
        label = "extraY",
    )
    Row(
        Modifier
            
            .layout { measurable, constraints ->
                val yOff = extraY.value.roundToPx().coerceAtLeast(0)
                val p = measurable.measure(constraints)
                layout(p.width, p.height + yOff) {
                    val room = ((constraints.maxWidth - p.width) / 2).coerceAtLeast(0)
                    p.place(s.offsetX.dp.roundToPx().coerceIn(-room, room), yOff)
                }
            }
            .wrapContentSize(Alignment.TopCenter, unbounded = true)
            .padding(ISLAND_PAD_DP.dp),
        verticalAlignment = Alignment.Top,
    ) {
        
        Spacer(Modifier.animatedWidth(slot))
        IslandBody(primary, showExpanded, s, lyrics, battery, calibrating, onBodyBounds)
        BubbleSlot(slot, bubbleVisible, bubbleAct, s, onBubbleBounds)
    }
}

private fun Modifier.animatedWidth(width: State<Dp>) = layout { measurable, constraints ->
    val w = width.value.roundToPx().coerceAtLeast(0)
    val p = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
    layout(w, p.height) { p.place(0, 0) }
}

@Composable
private fun BubbleSlot(slot: State<Dp>, visible: Boolean, act: Act?, s: IslandSettings, onBounds: (Rect) -> Unit) {
    Box(
        Modifier
            .zIndex(-1f)
            .layout { measurable, constraints ->
                val w = slot.value.roundToPx().coerceAtLeast(0)
                val p = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                layout(w, p.height) { p.place(w - p.width, 0) }
            }
            .onGloballyPositioned { onBounds(it.boundsInWindow()) },
    ) {
        val drop = TransformOrigin(0f, 0.5f)
        AnimatedVisibility(
            visible && act != null,
            enter = if (s.reduceEffects) fadeIn() else scaleIn(islandSpec(s), initialScale = 0.4f, transformOrigin = drop) + fadeIn(tween(90)),
            exit = if (s.reduceEffects) fadeOut() else scaleOut(islandSpec(s), targetScale = 0.4f, transformOrigin = drop) + fadeOut(tween(120, delayMillis = 60)),
        ) {
            if (act != null) Bubble(act, s)
        }
    }
}

@Composable
private fun IslandBody(
    primary: Act,
    expanded: Boolean,
    s: IslandSettings,
    lyrics: Lyrics?,
    battery: BatteryInfo,
    calibrating: Boolean,
    onBounds: (Rect) -> Unit,
) {
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    val conf = LocalConfiguration.current
    
    val expandedWidth = if (s.expandedWidth > 0) s.expandedWidth.dp.coerceAtMost(conf.screenWidthDp.dp) else (conf.screenWidthDp.dp - 22.dp).coerceAtMost(460.dp)
    val h = s.height.dp

    val cornerTarget = when {
        expanded -> s.expandedCornerRadius.dp
        primary is Act.NoticeA && (primary.n.style == NoticeStyle.MESSAGE || primary.n.style == NoticeStyle.EGG) -> s.cardCornerRadius.dp
        primary is Act.NoticeA && primary.n.style == NoticeStyle.UPDATE -> s.updateCornerRadius.dp
        primary is Act.CallA && primary.c.incoming -> s.callCornerRadius.dp.coerceAtMost(callHeight(s) / 2)
        primary is Act.NoticeA && primary.n.style == NoticeStyle.WIDE && primary.n.title.isNotBlank() -> {
            if (s.capsuleCornerRadius >= 0) s.capsuleCornerRadius.dp else noticeHeight(s) / 2
        }
        primary is Act.NoticeA && (primary.n.style == NoticeStyle.PAY_DONE ||
            primary.n.style == NoticeStyle.LIVE || primary.n.style == NoticeStyle.ALARM || primary.n.style == NoticeStyle.SENT) -> {
            if (s.capsuleCornerRadius >= 0) s.capsuleCornerRadius.dp else noticeHeight(s) / 2
        }
        primary is Act.PayA -> {
            if (s.capsuleCornerRadius >= 0) s.capsuleCornerRadius.dp else noticeHeight(s) / 2
        }
        primary is Act.Idle -> {
            if (s.capsuleCornerRadius >= 0) s.capsuleCornerRadius.dp else h / 2
        }
        else -> {
            if (s.capsuleCornerRadius >= 0) s.capsuleCornerRadius.dp else activeHeight(s) / 2
        }
    }
    
    val corner = animateDpAsState(cornerTarget, islandSpec(s), label = "corner")
    val shape = remember(corner, s.notch) { AnimatedIslandShape(corner, s.notch) }
    val accent = (primary as? Act.MediaA)?.m?.accent
    val outlineColor = if (s.outlineFromArt && accent != null) accent else Color(s.outlineColor)

    val playingArt = (primary as? Act.MediaA)?.m?.takeIf { it.playing }?.accent
    val lastArt = remember { arrayOf(Color.White) }
    if (playingArt != null) lastArt[0] = playingArt
    val glowOn = s.fxGlow && !s.reduceEffects && !calibrating && playingArt != null
    val glowAlpha by animateFloatAsState(if (glowOn) 1f else 0f, tween(500), label = "glowAlpha")
    val glowTint by animateColorAsState(lastArt[0], tween(500), label = "glowTint")

    val unreadLedColor by IslandBus.unreadLedColor.collectAsState()
    val previewLed by IslandBus.previewLed.collectAsState()
    val eventLed = s.ledOnEvent && primary is Act.NoticeA
    val unreadLed = s.ledOnUnread && unreadLedColor != null
    val chargeLed = s.ledOnCharge && battery.charging
    val ledActive = (s.ledEnabled || previewLed) && !calibrating && (s.ledAlwaysOn || eventLed || unreadLed || chargeLed || previewLed)
    val ledAlpha by animateFloatAsState(if (ledActive) 1f else 0f, tween(320), label = "ledAlpha")
    val ledClock = rememberFxClock(ledActive && !s.reduceEffects)
    val adaptiveLedColor = when {
        previewLed -> Color(0xFF0068FF)
        primary is Act.NoticeA -> primary.n.tint
        unreadLedColor != null -> unreadLedColor!!
        battery.charging -> if (battery.superFastCharging) Tints.Teal else Tints.Green
        playingArt != null -> playingArt
        else -> Color(s.ledColor)
    }

    val jellyX = remember { Animatable(1f) }
    val jellyY = remember { Animatable(1f) }
    var firstRun by remember { mutableStateOf(true) }
    LaunchedEffect(expanded) {
        if (firstRun) {
            firstRun = false
            return@LaunchedEffect
        }
        if (s.expandStyle != ExpandStyle.FRUIT || s.reduceEffects || s.elasticity == Elasticity.NONE) return@LaunchedEffect
        val amp = 0.07f * (1.25f - s.elasticity.damping)
        
        val rebound = spring<Float>(dampingRatio = (s.elasticity.damping * 0.62f).coerceAtLeast(0.3f), stiffness = 260f)
        val dir = if (expanded) 1f else -1f
        launch {
            jellyX.animateTo(1f - amp * dir, tween(110))
            jellyX.animateTo(1f, rebound)
        }
        launch {
            jellyY.animateTo(1f + amp * dir, tween(110))
            jellyY.animateTo(1f, rebound)
        }
    }
    
    val pull = remember { Animatable(0f) }

    val tilt = IslandBus.glassTilt.collectAsState()
    val sheen = remember { Animatable(-1f) }
    LaunchedEffect(primary.id, expanded, s.liquidGlass) {
        if (!s.liquidGlass || s.reduceEffects) return@LaunchedEffect
        sheen.snapTo(0f)
        sheen.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
        sheen.snapTo(-1f)
    }

    val shake = remember { Animatable(0f) }
    val pillId = (primary as? Act.NoticeA)?.n?.takeIf { it.style == NoticeStyle.PILL }?.id
    LaunchedEffect(pillId) {
        if (pillId == null || s.reduceEffects || !s.fxShake) return@LaunchedEffect
        for (x in floatArrayOf(-8f, 7f, -5f, 3.5f, -2f, 0f)) shake.animateTo(x, tween(60))
    }

    val current by rememberUpdatedState(primary)
    val isExpanded by rememberUpdatedState(expanded)
    val settings by rememberUpdatedState(s)
    val egg by IslandBus.egg.collectAsState()

    Box(
        Modifier
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .graphicsLayer {
                
                val bump = Egg.bump() * PayFx.pop.floatValue
                scaleX = jellyX.value * (1f + 0.03f * pull.value) * bump
                scaleY = jellyY.value * (1f + 0.08f * pull.value) * bump
                translationX = shake.value.dp.toPx()
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .then(
                if (glowAlpha > 0f || ledAlpha > 0f) {
                    Modifier.drawBehind {
                        if (glowAlpha > 0f) islandGlow(glowTint, glowAlpha, corner.value.toPx(), s.notch)
                        if (ledAlpha > 0f) ledOuterGlow(s.ledStyle, adaptiveLedColor, Color(s.ledColor), ledAlpha, ledClock.value, corner.value.toPx(), s.notch)
                    }
                } else Modifier
            )
            
            .graphicsLayer {
                this.shape = islandShape(corner.value, s.notch)
                clip = true
                when {
                    s.liquidGlass && !calibrating && !s.reduceEffects -> {
                        shadowElevation = 10.dp.toPx()
                        ambientShadowColor = Color.Black.copy(alpha = 0.5f)
                        spotShadowColor = Color.Black.copy(alpha = 0.6f)
                    }
                    s.shadow && !s.reduceEffects -> shadowElevation = 10.dp.toPx()
                }
            }
            .then(
                if (s.liquidGlass && !calibrating) {
                    Modifier.glassSurface(shape, Color(s.color), s.glassOpacity / 100f, s.glassRefraction / 100f, s.glassSpecular / 100f, tilt = { tilt.value }) { sheen.value }
                } else {
                    Modifier.background(if (calibrating) CalibrationRed else Color(s.color))
                },
            )
            .then(
                when {
                    calibrating -> Modifier.border(2.dp, Color.White, shape)
                    s.outline -> Modifier.border(s.outlineWidth.dp, outlineColor, shape)
                    else -> Modifier
                },
            )
            .then(
                if ((egg || ledAlpha > 0f) && !calibrating) {
                    Modifier.drawWithContent {
                        drawContent()
                        if (ledAlpha > 0f) {
                            drawLedRing(
                                style = s.ledStyle,
                                adaptive = adaptiveLedColor,
                                custom = Color(s.ledColor),
                                strokeDp = s.ledWidth,
                                alpha = ledAlpha,
                                timeMs = ledClock.value,
                                corner = corner.value.toPx(),
                                notch = s.notch,
                            )
                        }
                        if (egg) eggRim(corner.value.toPx(), s.notch)
                    }
                } else {
                    Modifier
                },
            )
            
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        IslandBus.lastTouch = SystemClock.elapsedRealtime()
                    }
                }
            }
            .pointerInput(s.doubleTapAction != TapAction.NOTHING, s.longPressAction != TapAction.NOTHING) {
                val doubleOn = settings.doubleTapAction != TapAction.NOTHING
                val longOn = settings.longPressAction != TapAction.NOTHING
                val onDouble: ((Offset) -> Unit)? = if (!doubleOn) null else {
                    { _: Offset ->
                        haptic()
                        perform(context, settings.doubleTapAction, current)
                    }
                }
                val onLong: ((Offset) -> Unit)? = if (!longOn) null else {
                    { _: Offset ->
                        haptic()
                        perform(context, settings.longPressAction, current)
                    }
                }
                detectTapGestures(
                    onTap = {
                        haptic()
                        onTap(context, current, isExpanded, settings)
                    },
                    onDoubleTap = onDouble,
                    onLongPress = onLong,
                )
            }
            .pointerInput(Unit) {
                val threshold = 36.dp.toPx()
                val side = 56.dp.toPx()
                var dx = 0f
                var dy = 0f
                detectDragGestures(
                    onDragStart = {
                        dx = 0f
                        dy = 0f
                    },
                    onDragEnd = {
                        scope.launch { pull.animateTo(0f, spring(dampingRatio = 0.45f)) }
                        val vertical = abs(dy) > abs(dx)
                        val act = current
                        when {
                            !isExpanded && vertical && dy > threshold && settings.swipeExpand && !act.transient &&
                                (act !is Act.Idle || settings.idleExpand) -> {
                                haptic()
                                Controls.expand(act.id)
                            }
                            isExpanded && vertical && dy < -threshold -> {
                                haptic()
                                Controls.collapse()
                            }

                            !isExpanded && vertical && dy < -threshold && act is Act.NoticeA -> {
                                IslandBus.notice.value = null
                            }
                            
                            !isExpanded && vertical && dy < -threshold && act is Act.PayA -> PayMonitor.clearReady()
                            
                            !isExpanded && vertical && dy < -threshold && act is Act.CallA && act.c.incoming -> {
                                haptic()
                                Controls.hideIncomingCall()
                            }
                            !isExpanded && !vertical && abs(dx) > side && act is Act.MediaA && settings.swipeTracks -> {
                                haptic()
                                
                                if (dx < 0) Controls.next() else Controls.prev()
                            }
                        }
                    },
                    onDragCancel = { scope.launch { pull.animateTo(0f) } },
                    onDrag = { change, amount ->
                        change.consume()
                        dx += amount.x
                        dy += amount.y
                        if (!isExpanded && settings.swipeExpand) {
                            scope.launch { pull.snapTo((dy / (threshold * 3)).coerceIn(0f, 1f)) }
                        }
                    },
                )
            },
    ) {

        val widths = remember { HashMap<String, Int>() }
        val frames = updateTransition(Frame(primary, expanded), label = "island")
        frames.AnimatedContent(
            contentAlignment = Alignment.TopCenter,
            contentKey = { it.key },
            transitionSpec = {
                val change = if (s.liquidExpand) EnterTransition.None togetherWith ExitTransition.None else classicChange(s)
                change.using(SizeTransform(clip = true) { _, _ -> islandSpec(s) })
            },
        ) { f ->
            Box(if (s.liquidExpand) liquidFlow(f.key, frames, widths, s) else classicFlow(s)) {
                if (f.expanded) {
                    ExpandedContent(f.act, s, lyrics, battery, expandedWidth)
                } else {
                    CompactContent(f.act, s, expandedWidth)
                }
            }
        }
    }
}

private fun classicChange(s: IslandSettings): ContentTransform {
    val m = motionScale(s)
    val origin = TransformOrigin(0.5f, 0.3f)
    val inSpec = tween<Float>((300 * m).toInt(), delayMillis = (100 * m).toInt(), easing = ExpoOut)
    val outSpec = tween<Float>((167 * m).toInt(), easing = LinearEasing)
    val enter = if (s.reduceEffects) {
        fadeIn(tween(150))
    } else {
        fadeIn(inSpec) + scaleIn(inSpec, initialScale = 0.9f, transformOrigin = origin)
    }
    val exit = if (s.reduceEffects) {
        fadeOut(tween(90))
    } else {
        fadeOut(outSpec) + scaleOut(outSpec, targetScale = 0.95f, transformOrigin = origin)
    }
    return enter togetherWith exit
}

@Composable
private fun AnimatedVisibilityScope.classicFlow(s: IslandSettings): Modifier {
    val m = motionScale(s)
    val blur = transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) tween((300 * m).toInt(), delayMillis = (100 * m).toInt(), easing = ExpoOut)
            else tween((167 * m).toInt())
        },
        label = "blur",
    ) { state ->
        when {
            s.reduceEffects || !s.fxBlur -> 0f
            state == EnterExitState.PreEnter -> 7f
            state == EnterExitState.PostExit -> 5f
            else -> 0f
        }
    }
    return Modifier.graphicsLayer { blurLayer(blur.value) }
}

@Composable
private fun AnimatedVisibilityScope.liquidFlow(key: String, frames: Transition<Frame>, widths: HashMap<String, Int>, s: IslandSettings): Modifier {

    val target = frames.targetState.key
    val other = if (key == target) frames.currentState.key else target
    val from = remember { arrayOfNulls<String>(1) }
    if (other != key) from[0] = other
    
    val presence = transition.animateFloat({ islandSpec(s) }, label = "presence") { if (it == EnterExitState.Visible) 1f else 0f }
    
    val fade = transition.animateFloat({ fadeSpec(s) }, label = "fade") { if (it == EnterExitState.Visible) 1f else 0f }
    DisposableEffect(key) { onDispose { widths.remove(key) } }
    val zoom = !s.reduceEffects
    val blurDp = if (s.reduceEffects || !s.fxBlur) 0f else 14f
    return Modifier.layout { measurable, constraints ->
        val content = measurable.measure(constraints)
        widths[key] = content.width
        layout(content.width, content.height) {
            content.placeWithLayer(0, 0) {
                val p = presence.value
                alpha = fade.value.coerceIn(0f, 1f)
                val ow = from[0]?.let { widths[it] }
                if (zoom && ow != null && content.width > 0) {
                    val k = (ow.toFloat() / content.width).coerceIn(0.25f, 3f)
                    scaleX = k + (1f - k) * p
                    scaleY = scaleX
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                
                val rest = (1f - p).coerceAtLeast(0f)
                blurLayer(blurDp * rest * sqrt(rest))
            }
        }
    }
}

private fun GraphicsLayerScope.blurLayer(dp: Float) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dp > 0.3f) {
        renderEffect = BlurEffect(dp.dp.toPx(), dp.dp.toPx(), TileMode.Decal)
    }
}

private fun onTap(context: Context, act: Act, expanded: Boolean, s: IslandSettings) {
    if (expanded) {
        Controls.collapse()
        return
    }
    when (act) {
        is Act.NoticeA -> {
            
            if (IslandBus.reply.value != null) return
            
            act.n.message?.let { m ->
                if (!Controls.send(context, m.intent)) Controls.openApp(context, m.pkg)
            }
            act.n.openPkg?.let { Controls.openApp(context, it) }
            act.n.alarm?.let { openAlarm(context, it) }
            IslandBus.notice.value = null
        }
        
        is Act.Idle -> {
            val unreadPkg = IslandBus.unreadLedPkg.value
            if (unreadPkg != null && s.ledEnabled && s.ledOnUnread && !s.idleExpand) {
                IslandBus.unreadLedColor.value = null
                IslandBus.unreadLedPkg.value = null
                Controls.openApp(context, unreadPkg)
            } else {
                IslandBus.unreadLedColor.value = null
                IslandBus.unreadLedPkg.value = null
                if (s.idleExpand) Controls.expand(act.id)
            }
        }
        
        is Act.PayA -> if (act.pkg != PayMonitor.GMS) Controls.openApp(context, act.pkg)
        is Act.TimerA -> if (act.t.ringing) Controls.cancelTimer() else perform(context, s.tapAction, act)
        is Act.CallA -> if (act.c.incoming) {
            Controls.openCall(context, act.c)
        } else {
            perform(context, s.tapAction, act)
        }
        else -> perform(context, s.tapAction, act)
    }
}

private fun perform(context: Context, action: TapAction, act: Act) {
    if (act.transient) return
    val hasMedia = IslandBus.media.value != null
    when (action) {
        TapAction.EXPAND -> Controls.expand(act.id)
        TapAction.OPEN_APP -> act.pkg?.let { Controls.openApp(context, it) } ?: Controls.expand(act.id)
        TapAction.PLAY_PAUSE -> if (hasMedia) Controls.playPause() else Controls.expand(act.id)
        TapAction.NEXT_TRACK -> if (hasMedia) Controls.next() else Controls.expand(act.id)
        TapAction.SCREENSHOT -> Controls.screenshot()
        TapAction.NOTHING -> Unit
    }
}

@Composable
private fun Bubble(act: Act, s: IslandSettings) {
    val h = activeHeight(s)
    val haptic = rememberHaptic()
    val tilt = IslandBus.glassTilt.collectAsState()
    val now = rememberNow(500, active = act is Act.TimerA)
    Box(
        Modifier
            .size(h)
            .then(if (s.shadow && !s.reduceEffects) Modifier.shadow(8.dp, CircleShape) else Modifier)
            .clip(CircleShape)
            .then(
                if (s.liquidGlass) Modifier.glassSurface(CircleShape, Color(s.color), s.glassOpacity / 100f, s.glassRefraction / 100f, s.glassSpecular / 100f, tilt = { tilt.value })
                else Modifier.background(Color(s.color)),
            )
            .then(if (s.outline) Modifier.border(s.outlineWidth.dp, Color(s.outlineColor), CircleShape) else Modifier)
            .clickable {
                haptic()
                Controls.expand(act.id)
            },
        contentAlignment = Alignment.Center,
    ) {
        val inner: Dp = h * 0.62f
        when (act) {
            is Act.MediaA -> Art(act.m.art, h * 0.72f, h / 2)
            is Act.TimerA -> {
                ProgressRing(
                    progress = if (act.t.totalMs > 0) act.t.left(now).toFloat() / act.t.totalMs else 0f,
                    color = Tints.Orange,
                    modifier = Modifier.size(inner),
                    stroke = 2.5.dp,
                )
                Icon(Ios.timer, null, tint = Tints.Orange, modifier = Modifier.size(inner * 0.5f))
            }
            is Act.StopwatchA -> Icon(Ios.stopwatch, null, tint = Tints.Orange, modifier = Modifier.size(inner * 0.8f))
            is Act.TorchA -> Icon(Ios.flashlight, null, tint = Tints.Yellow, modifier = Modifier.size(inner * 0.75f))
            is Act.ChronoA -> AppIcon(act.c.icon, inner, Ios.timer, Tints.Orange)
            is Act.ProgressA -> {
                ProgressRing(act.p.fraction, Tints.Blue, Modifier.size(inner), stroke = 2.5.dp)
                Icon(Ios.download, null, tint = Tints.Blue, modifier = Modifier.size(inner * 0.5f))
            }
            is Act.CallA -> Icon(Ios.call, null, tint = Tints.Green, modifier = Modifier.size(inner * 0.7f))
            is Act.LiveA -> LiveGlyph(act.l, inner, liveTint(act.l.kind))
            is Act.ChargingA -> Icon(Ios.flash, null, tint = Tints.Green, modifier = Modifier.size(inner * 0.7f))
            else -> Unit
        }
    }
}

fun DrawScope.islandGlow(color: Color, alpha: Float, corner: Float, notch: Boolean = false) {
    val spread = 10.dp.toPx()
    for (i in 1..10) {
        val d = spread * i / 10f
        drawIslandRect(color, Offset(-d, -d), Size(size.width + 2 * d, size.height + 2 * d), corner + d, notch, alpha = 0.06f * alpha)
    }
}

private fun DrawScope.islandOutline(topLeft: Offset, size: Size, corner: Float, notch: Boolean, draw: DrawScope.(Outline) -> Unit) {
    val outline = islandShape(corner.coerceAtLeast(0f).toDp(), notch).createOutline(size, layoutDirection, this)
    translate(topLeft.x, topLeft.y) { draw(outline) }
}

fun DrawScope.drawIslandRect(
    color: Color,
    topLeft: Offset,
    size: Size,
    corner: Float,
    notch: Boolean,
    style: DrawStyle = Fill,
    alpha: Float = 1f,
) = islandOutline(topLeft, size, corner, notch) { drawOutline(it, color, alpha, style) }

fun DrawScope.drawIslandRect(
    brush: Brush,
    topLeft: Offset,
    size: Size,
    corner: Float,
    notch: Boolean,
    style: DrawStyle = Fill,
    alpha: Float = 1f,
) = islandOutline(topLeft, size, corner, notch) { drawOutline(it, brush, alpha, style) }

fun islandShape(corner: Dp, s: IslandSettings): RoundedCornerShape = islandShape(corner, s.notch)

fun islandShape(corner: Dp, notch: Boolean): RoundedCornerShape =
    if (notch) RoundedCornerShape(0.dp, 0.dp, corner, corner) else RoundedCornerShape(corner)

private class AnimatedIslandShape(private val corner: State<Dp>, private val notch: Boolean) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        islandShape(corner.value, notch).createOutline(size, layoutDirection, density)
}

internal fun DrawScope.ledOuterGlow(
    style: LedStyle,
    adaptive: Color,
    custom: Color,
    alpha: Float,
    timeMs: Long,
    corner: Float,
    notch: Boolean,
) {
    val phase = (timeMs % 2400L).toFloat() / 2400f
    val pulse = 0.65f + 0.35f * sin(phase * 2f * Math.PI.toFloat())
    val glowColor = when (style) {
        LedStyle.ROTATE_RGB -> Color.hsv((phase * 360f) % 360f, 0.85f, 1f)
        LedStyle.AUTO_APP -> adaptive
        LedStyle.CYBER_NEON -> if (phase < 0.5f) Color(0xFF00F2FE) else Color(0xFFFF0844)
        LedStyle.BREATHE_GOLD -> Color(0xFFFFD700)
        LedStyle.CUSTOM -> custom
    }
    val maxSpread = 7.dp.toPx()
    for (i in 1..5) {
        val d = maxSpread * i / 5f
        drawIslandRect(
            color = glowColor,
            topLeft = Offset(-d, -d),
            size = Size(size.width + 2 * d, size.height + 2 * d),
            corner = corner + d,
            notch = notch,
            alpha = (0.075f * alpha * pulse * (1f - i / 6f)).coerceIn(0f, 1f),
        )
    }
}

internal fun DrawScope.drawLedRing(
    style: LedStyle,
    adaptive: Color,
    custom: Color,
    strokeDp: Float,
    alpha: Float,
    timeMs: Long,
    corner: Float,
    notch: Boolean,
) {
    val w = strokeDp.coerceIn(1f, 6f).dp.toPx()
    val phase = (timeMs % 2200L).toFloat() / 2200f
    val angle = phase * 360f
    val pulse = 0.72f + 0.28f * sin(phase * 2f * Math.PI.toFloat())

    val colors = when (style) {
        LedStyle.ROTATE_RGB -> List(9) { i ->
            Color.hsv((angle + i * 45f) % 360f, 0.88f, 1f)
        }
        LedStyle.AUTO_APP -> {
            val dim = adaptive.copy(alpha = 0.22f)
            val bright = Color.White.copy(alpha = 0.92f)
            shiftRing(listOf(dim, adaptive, bright, adaptive, dim, adaptive.copy(alpha = 0.5f), dim), phase)
        }
        LedStyle.CYBER_NEON -> {
            val c1 = Color(0xFF00F2FE)
            val c2 = Color(0xFF7F00FF)
            val c3 = Color(0xFFFF0844)
            shiftRing(listOf(c1, c2, c3, Color.White, c1, c2, c1), phase)
        }
        LedStyle.BREATHE_GOLD -> {
            val g1 = Color(0xFFFFD700)
            val g2 = Color(0xFFFF8C00)
            val g3 = Color(0xFFFFF8DC)
            shiftRing(listOf(g2, g1, g3, g1, g2, g1, g2), phase)
        }
        LedStyle.CUSTOM -> {
            val dim = custom.copy(alpha = 0.25f)
            val crest = Color.White.copy(alpha = 0.9f)
            shiftRing(listOf(dim, custom, crest, custom, dim, custom.copy(alpha = 0.55f), dim), phase)
        }
    }

    drawIslandRect(
        brush = Brush.sweepGradient(colors, center),
        topLeft = Offset(w / 2f, w / 2f),
        size = Size(size.width - w, size.height - w),
        corner = (corner - w / 2f).coerceAtLeast(0f),
        notch = notch,
        style = Stroke(w),
        alpha = (alpha * pulse).coerceIn(0f, 1f),
    )
}

internal fun shiftRing(base: List<Color>, phase: Float): List<Color> {
    if (base.size < 2) return base
    val core = base.dropLast(1)
    val n = core.size
    val shift = ((phase * n).toInt()) % n
    val rotated = List(n) { i -> core[(i - shift + n) % n] }
    return rotated + rotated.first()
}
