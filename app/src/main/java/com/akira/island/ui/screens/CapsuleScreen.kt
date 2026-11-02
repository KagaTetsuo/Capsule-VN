package com.akira.island.ui.screens

import android.os.SystemClock
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.akira.island.ui.GroupStepSlider
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LensBlur
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.IslandBus
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.Elasticity
import com.akira.island.data.ExpandStyle
import com.akira.island.data.FxRate
import com.akira.island.data.IslandSettings
import com.akira.island.data.LedStyle
import com.akira.island.data.SettingsRepo
import com.akira.island.island.drawLedRing
import com.akira.island.island.ledFx
import com.akira.island.data.LedMotion
import com.akira.island.island.glassSurface
import com.akira.island.island.islandSpec
import com.akira.island.island.ledOuterGlow
import com.akira.island.island.rememberHaptic
import com.akira.island.ui.ChoiceDialog
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import androidx.compose.ui.platform.LocalContext
import com.akira.island.R
import com.akira.island.core.Notice
import com.akira.island.island.Ios
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun CapsuleScreen(open: (Screen) -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    Page(tr("Giao diện", "Appearance")) {
        IslandPreview()

        SectionHeader(tr("Bố cục", "Layout"))
        Group {
            GroupItem(
                Pos.First,
                Icons.Rounded.OpenWith,
                tr("Vị trí & kích thước", "Position & size"),
                tr("Viên thuốc ${s.width}×${s.height} dp · Bảng mở rộng", "Capsule ${s.width}×${s.height} dp · Expanded board"),
                color = Tile.Indigo,
                onClick = { open(Screen.SIZE) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle,
                Icons.Rounded.SwapHoriz,
                tr("Khi có hoạt động", "With activity"),
                if (s.activeWidth == 0) tr("Độ dày +${s.activeExtra} dp, chiều rộng giữ nguyên", "Thickness +${s.activeExtra} dp, width as without it")
                else tr("Độ dày +${s.activeExtra}, chiều rộng +${s.activeWidth} dp", "Thickness +${s.activeExtra}, width +${s.activeWidth} dp"),
                color = Tile.Purple,
                onClick = { open(Screen.LOOK) },
            ) { Arrow() }
            GroupItem(
                Pos.Last,
                Icons.Rounded.Tune,
                tr("Khung thẻ thông báo", "Notification cards"),
                tr("Tin nhắn, cuộc gọi, cập nhật: vị trí, kích thước, số dòng, bo góc", "Message, call, update: position, size, lines, corners"),
                color = Tile.Green,
                onClick = { open(Screen.MSG_CALL_SIZE) },
            ) { Arrow() }
        }

        SectionHeader(tr("Màu sắc & ánh sáng", "Color and lighting"))
        Group {
            GroupItem(
                Pos.First,
                Icons.Rounded.FormatColorFill,
                tr("Màu sắc & viền", "Color & border"),
                tr("Màu sắc nền, viền ngoài và bóng đổ", "Color, outline and shadow"),
                color = Tile.Orange,
                onClick = { open(Screen.COLORS) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle,
                Icons.Rounded.RadioButtonChecked,
                tr("Vòng sáng LED", "LED ring"),
                if (s.ledEnabled) "${s.ledStyle.title} · %.1f dp".format(s.ledWidth) else tr("Tắt", "Off"),
                color = Tile.Purple,
                onClick = { open(Screen.LED_RING) },
            ) { Arrow() }
            GroupToggle(
                if (s.liquidGlass) Pos.Middle else Pos.Last,
                Icons.Rounded.WaterDrop,
                "Liquid Glass (iOS)",
                tr("Thấu kính chất lỏng 3D chuẩn iOS Liquid Glass với khúc xạ viền và phản quang sống động khi nghiêng máy", "iOS optical 3D liquid glass lens with live refraction and specular highlights when tilting"),
                s.liquidGlass,
                color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(liquidGlass = v) } }
            if (s.liquidGlass) {
                GroupSlider(
                    Pos.Middle, Icons.Rounded.Opacity, tr("Độ trong suốt chất lỏng", "Glass density"),
                    value = s.glassOpacity.toFloat(), range = 25f..95f, label = "${s.glassOpacity}%", color = Tile.Teal,
                ) { v -> SettingsRepo.update { copy(glassOpacity = v.toInt()) } }
                GroupSlider(
                    Pos.Middle, Icons.Rounded.LensBlur, tr("Độ cong khúc xạ viền 3D", "Lens refraction"),
                    value = s.glassRefraction.toFloat(), range = 20f..100f, label = "${s.glassRefraction}%", color = Tile.Blue,
                ) { v -> SettingsRepo.update { copy(glassRefraction = v.toInt()) } }
                GroupSlider(
                    Pos.Last, Icons.Rounded.Flare, tr("Độ bóng gương & tán sắc", "Specular & dispersion"),
                    value = s.glassSpecular.toFloat(), range = 20f..100f, label = "${s.glassSpecular}%", color = Tile.Indigo,
                ) { v -> SettingsRepo.update { copy(glassSpecular = v.toInt()) } }
            }
        }

        SectionHeader(tr("Chuyển động", "Motion"))
        Group {
            GroupItem(
                Pos.First,
                Icons.Rounded.Animation,
                tr("Mở rộng & độ đàn hồi", "Expansion & elasticity"),
                "${s.expandStyle.title} · ${s.elasticity.title}" + if (s.liquidExpand) " · " + tr("Chất lỏng", "Liquid") else "",
                color = Tile.Blue,
                onClick = { open(Screen.ANIM) },
            ) { Arrow() }
            GroupItem(
                Pos.Last,
                Icons.Rounded.AutoAwesome,
                tr("Hiệu ứng đặc biệt", "Special effects"),
                effectsSummary(s),
                color = Tile.Purple,
                onClick = { open(Screen.EFFECTS) },
            ) { Arrow() }
        }
    }
}

@Composable
fun LookScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    Page(tr("Khi có hoạt động", "With activity"), onBack, subtitle = tr("Kích thước & vị trí Viên thuốc khi đang có nhạc, hẹn giờ, điều hướng… — xem trước trực tiếp trên đỉnh màn hình", "Size & position of the island while music, timers, navigation… are running — live preview at the top")) {
        IslandPreview()
        Group {
            GroupSlider(
                Pos.First, Icons.Rounded.LineWeight, tr("Độ dày", "Thickness"),
                value = s.activeExtra.toFloat(), range = 0f..14f, label = "+${s.activeExtra} dp", steps = 13, color = Tile.Purple,
            ) { v ->
                previewActivity()
                SettingsRepo.update { copy(activeExtra = v.toInt()) }
            }
            GroupSlider(
                Pos.Last, Icons.Rounded.SwapHoriz, tr("Chiều rộng", "Width"),
                value = s.activeWidth.toFloat(), range = 0f..120f, steps = 23,
                label = if (s.activeWidth == 0) tr("Như khi không có", "As without it") else "+${s.activeWidth} dp", color = Tile.Blue,
            ) { v ->
                previewActivity()
                SettingsRepo.update { copy(activeWidth = (v / 5).toInt() * 5) }
            }
        }

        SectionHeader(tr("Vị trí khi có hoạt động", "Position with activity"))
        Group {
            GroupStepSlider(
                Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"), s.activeOffsetY, -30..60,
                action = tr("Mặc định 0 dp", "Default 0 dp") to { SettingsRepo.update { copy(activeOffsetY = 0) } }, color = Tile.Indigo,
            ) { v ->
                previewActivity()
                SettingsRepo.update { copy(activeOffsetY = v) }
            }
            GroupStepSlider(
                Pos.Last, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), s.activeOffsetX, -120..120,
                action = tr("Chính giữa", "Center") to { SettingsRepo.update { copy(activeOffsetX = 0) } }, color = Tile.Teal,
            ) { v ->
                previewActivity()
                SettingsRepo.update { copy(activeOffsetX = v) }
            }
        }
    }
}

@Composable
fun AnimScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current
    val haptic = rememberHaptic()
    val scope = rememberCoroutineScope()
    var styleDialog by remember { mutableStateOf(false) }
    var elasticityDialog by remember { mutableStateOf(false) }
    var noticeJob by remember { mutableStateOf<Job?>(null) }
    var demoNoticeId by remember { mutableStateOf(-1L) }

    // Mọi thay đổi đều được xem trước trực tiếp trên Viên thuốc thật ở đỉnh màn hình.
    DisposableEffect(Unit) {
        onDispose { IslandBus.stopMotionPreview() }
    }

    fun previewNotice() {
        noticeJob?.cancel()
        noticeJob = scope.launch {
            delay(350)
            IslandBus.stopMotionPreview()
            // Chỉ thay thế thông báo mẫu trước đó; thông báo thật của ứng dụng khác luôn được ưu tiên giữ nguyên
            val current = IslandBus.notice.value
            if (current != null && current.id == demoNoticeId) IslandBus.notice.value = null
            val sec = SettingsRepo.current.noticeDurationMs / 1000f
            val demo = Notice(
                title = tr("Thông báo mẫu", "Sample event"),
                state = tr("%.1f giây", "%.1f s").format(sec),
                icon = Ios.load(context, R.drawable.ios_hourglass),
                tint = Tints.Orange,
            )
            demoNoticeId = demo.id
            IslandBus.post(demo)
        }
    }

    Page(tr("Mở rộng & độ đàn hồi", "Expansion & elasticity"), onBack, action = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Ui.Primary.copy(alpha = 0.22f)).clickable {
                    haptic()
                    IslandBus.previewMotion(0)
                },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.PlayArrow, tr("Xem thử trên Viên thuốc", "Preview on the island"), tint = Ui.Primary, modifier = Modifier.size(22.dp))
            }
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Ui.Card).clickable {
                    haptic()
                    SettingsRepo.resetAnimations()
                    IslandBus.previewMotion()
                },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Restore, tr("Đặt lại", "Reset"), tint = Ui.Text, modifier = Modifier.size(20.dp))
            }
        }
    }) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF261A1C))
                .border(1.dp, Color(0xFF4A3528), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(Tints.Green))
            Spacer(Modifier.width(10.dp))
            Text(
                tr(
                    "Mỗi thay đổi sẽ được bung thử trực tiếp trên Viên thuốc ở đỉnh màn hình. Nhấn ▶ để xem lại.",
                    "Every change is played live on the island at the top of the screen. Tap ▶ to replay.",
                ),
                color = Color(0xFFCDBFB4), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f),
            )
        }

        SectionHeader(tr("Kiểu chuyển động", "Motion style"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.Animation, tr("Kiểu mở rộng", "Expansion style"), s.expandStyle.title, color = Tile.Purple,
                onClick = { styleDialog = true },
            ) { Arrow() }
            GroupItem(
                Pos.Middle, Icons.Rounded.Waves, tr("Độ đàn hồi", "Elasticity"), s.elasticity.title, color = Tile.Teal,
                onClick = { elasticityDialog = true },
            ) { Arrow() }
            GroupToggle(
                Pos.Last, Icons.Rounded.BubbleChart, tr("Mở rộng dạng chất lỏng", "Liquid expansion"),
                tr("Nội dung co giãn cùng Viên thuốc và hiện rõ dần từ hiệu ứng mờ", "The content stretches with the island and emerges from a blur"),
                s.liquidExpand, color = Tile.Indigo, badge = tr("Mới", "New"),
            ) { v ->
                SettingsRepo.update { copy(liquidExpand = v) }
                IslandBus.previewMotion()
            }
        }

        SectionHeader(tr("Thời gian", "Timing"))
        Group {
            GroupSlider(
                Pos.First, Icons.Rounded.Speed, tr("Tốc độ chuyển động", "Animation speed"),
                value = s.durationPercent.toFloat(), range = 0f..100f,
                label = speedLabel(s.durationPercent), color = Tile.Indigo,
            ) { v ->
                SettingsRepo.update { copy(durationPercent = v.toInt()) }
                IslandBus.previewMotion(450)
            }
            GroupSlider(
                Pos.Last, Icons.Rounded.Schedule, tr("Thời gian hiển thị sự kiện", "Event display time"),
                value = s.noticeDurationMs / 1000f, range = 1.5f..6f, steps = 8,
                label = tr("%.1f giây", "%.1f s").format(s.noticeDurationMs / 1000f), color = Tile.Orange,
            ) { v ->
                SettingsRepo.update { copy(noticeDurationMs = (v * 1000).toInt()) }
                previewNotice()
            }
        }

        SectionHeader(tr("Hiệu năng", "Performance"))
        Group {
            GroupToggle(
                Pos.Single, Icons.Rounded.BlurOn, tr("Giảm hiệu ứng", "Reduce effects"),
                tr("Tắt nảy, mờ và co giãn — hữu ích trên thiết bị cấu hình thấp", "Turns off bounce, blur and stretch — helps on less powerful devices"),
                s.reduceEffects, color = Tile.Gray,
            ) { v ->
                SettingsRepo.update { copy(reduceEffects = v) }
                IslandBus.previewMotion()
            }
        }
    }

    if (styleDialog) {
        ChoiceDialog(
            tr("Kiểu mở rộng", "Expansion style"), ExpandStyle.entries, s.expandStyle, { it.title },
            { v ->
                SettingsRepo.update { copy(expandStyle = v) }
                IslandBus.previewMotion()
            },
        ) { styleDialog = false }
    }

    if (elasticityDialog) {
        ChoiceDialog(
            tr("Độ đàn hồi", "Elasticity"), Elasticity.entries, s.elasticity, { it.title },
            { v ->
                SettingsRepo.update { copy(elasticity = v) }
                IslandBus.previewMotion()
            },
        ) { elasticityDialog = false }
    }
}

private fun speedLabel(p: Int): String = when {
    p <= 15 -> tr("Rất nhanh", "Very fast")
    p <= 40 -> tr("Nhanh", "Fast")
    p <= 65 -> tr("Vừa", "Medium")
    p <= 85 -> tr("Chậm", "Slow")
    else -> tr("Rất chậm", "Very slow")
} + " · $p%"

private fun previewActivity() {
    IslandBus.widthPreviewUntil.value = SystemClock.elapsedRealtime() + 2_500
}

private fun effectsSummary(s: IslandSettings): String {
    if (s.reduceEffects) return tr("Tắt: Do tính năng Giảm hiệu ứng đang bật", "Off: Reduce effects is on")
    val on = listOfNotNull(
        tr("mờ", "blur").takeIf { s.fxBlur },
        tr("sóng nhạc", "equalizer").takeIf { s.fxEqualizer },
        tr("phát sáng", "glow").takeIf { s.fxGlow },
        tr("sóng thanh toán", "payment waves").takeIf { s.fxPay },
        tr("rung", "shake").takeIf { s.fxShake },
    )
    return if (on.isEmpty()) tr("Tắt tất cả", "All off") else on.joinToString(", ").replaceFirstChar { it.uppercase() }
}

@Composable
fun EffectsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var rateDialog by remember { mutableStateOf(false) }
    Page(tr("Hiệu ứng đặc biệt", "Special effects"), onBack, action = {
        Text(tr("Đặt lại", "Reset"), color = Ui.Sub, fontSize = 16.sp, modifier = Modifier.clickable { SettingsRepo.resetEffects() })
    }) {
        IslandPreview()
        if (s.reduceEffects) {
            Text(
                tr("Giảm hiệu ứng đang bật trong Mở rộng Viên thuốc — khi bật, các hiệu ứng sẽ tắt", "Reduce effects is on in Island expansion — while it's on, there are no effects"),
                color = Tints.Orange, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        SectionHeader(tr("Chuyển động", "Motion"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.BlurCircular, tr("Làm mờ khi thay đổi", "Blur on change"),
                tr("Nội dung của Viên thuốc chuyển động mượt mà kèm hiệu ứng mờ, như trên iPhone (Android 12+)", "The island's content flows with a blur, like on iPhone (Android 12+)"),
                s.fxBlur, color = Tile.Blue) { v -> SettingsRepo.update { copy(fxBlur = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.GraphicEq, tr("Bộ cân bằng sóng nhạc động", "Live equalizer"),
                tr("Cột sóng nhạc và cuộc gọi nhún nhảy. Tắt — đứng yên để tiết kiệm pin", "The music and call bars dance. Off — they stand still, which saves power"),
                s.fxEqualizer, color = Tile.Pink) { v -> SettingsRepo.update { copy(fxEqualizer = v) } }
            GroupItem(Pos.Middle, Icons.Rounded.Speed, tr("Tốc độ khung hình bộ cân bằng nhạc", "Equalizer frame rate"),
                tr("${s.fxRate.title}. Thêm chấm phát sáng của đèn pin và điều hướng", "${s.fxRate.title}. Also the glowing dot of the flashlight and navigation"),
                color = Tile.Indigo, onClick = { rateDialog = true })
            GroupToggle(Pos.Last, Icons.Rounded.NotificationsActive, tr("Rung lắc", "Shake"),
                tr("Viên thuốc rung lên khi đổi chế độ chuông, và chuông hẹn giờ lắc lư", "The capsule shakes when the ring mode changes, and a ringing timer's bell swings"),
                s.fxShake, color = Tile.Orange) { v -> SettingsRepo.update { copy(fxShake = v) } }
        }

        SectionHeader(tr("Phát sáng", "Glow"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Flare, tr("Phát sáng theo ảnh bìa", "Artwork glow"),
                tr("Vầng sáng dịu theo màu ảnh bìa xung quanh Viên thuốc khi phát nhạc", "A soft halo in the artwork's color around the island while music plays"),
                s.fxGlow, color = Tile.Purple) { v -> SettingsRepo.update { copy(fxGlow = v) } }
            GroupToggle(Pos.Last, Icons.Rounded.Contactless, tr("Sóng thanh toán", "Payment waves"),
                tr("Sóng NFC quanh Viên thuốc khi chờ đầu đọc thẻ, và hiệu ứng pháo hoa khi Đã thanh toán", "NFC waves around the island while waiting for the reader, and a burst of sparks on Paid"),
                s.fxPay, color = Tile.Teal) { v -> SettingsRepo.update { copy(fxPay = v) } }
        }

        SectionHeader(tr("Pin", "Battery"))
        GroupToggle(Pos.Single, Icons.Rounded.BatterySaver, tr("Tiết kiệm pin", "Battery saving"),
            tr("Ở chế độ tiết kiệm pin của Android, Viên thuốc sẽ tự động bỏ qua các hiệu ứng động", "In Android's battery saver mode the island skips live effects by itself"),
            s.batterySaver, color = Tile.Green) { v -> SettingsRepo.update { copy(batterySaver = v) } }
    }

    if (rateDialog) {
        ChoiceDialog(tr("Tốc độ khung hình bộ cân bằng nhạc", "Equalizer frame rate"), FxRate.entries, s.fxRate, { it.title },
            { v -> SettingsRepo.update { copy(fxRate = v) } }) { rateDialog = false }
    }
}

private val ledPalette = listOf(
    0xFF00E5FF, 0xFF0A84FF, 0xFF34C759, 0xFFFFD60A, 0xFFFF9F0A,
    0xFFFF2D55, 0xFFBF5AF2, 0xFF7F00FF, 0xFF00F5D4, 0xFFFFFFFF,
)

@Composable
private fun LedRingPreviewCard(s: IslandSettings) {
    val infiniteTransition = rememberInfiniteTransition(label = "ledPreviewLoop")
    val animatedMs by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2400f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
        ),
        label = "ledClock",
    )

    val cornerDp = 19.dp
    val ledAlpha = if (s.ledEnabled) 1f else 0.55f
    val adaptiveColor = Color(0xFF0084FF)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1C1416),
                        Color(0xFF0E0B0C),
                    )
                )
            )
            .border(1.dp, Color(0xFF33262A), RoundedCornerShape(28.dp))
            .padding(top = 16.dp, bottom = 14.dp, start = 14.dp, end = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("09:41", color = Color(0xFF9A8A84), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("5G", color = Color(0xFF9A8A84), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                Box(
                    Modifier
                        .size(14.dp, 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF9A8A84))
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .width(152.dp)
                .height(38.dp)
                .drawBehind {
                    ledOuterGlow(
                        style = s.ledStyle,
                        adaptive = adaptiveColor,
                        custom = Color(s.ledColor),
                        alpha = ledAlpha,
                        timeMs = animatedMs.toLong(),
                        corner = cornerDp.toPx(),
                        notch = false,
                        fx = s.ledFx(),
                    )
                }
                .clip(RoundedCornerShape(cornerDp))
                .background(Color(0xFF08090D))
                .drawWithContent {
                    drawContent()
                    drawLedRing(
                        style = s.ledStyle,
                        adaptive = adaptiveColor,
                        custom = Color(s.ledColor),
                        strokeDp = s.ledWidth,
                        alpha = ledAlpha,
                        timeMs = animatedMs.toLong(),
                        corner = cornerDp.toPx(),
                        notch = false,
                        fx = s.ledFx(),
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF030305))
                        .border(1.dp, Color(0xFF2E2224), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A0E10))
                    )
                }

                Text(
                    "Capsule VN",
                    color = Color(0xFFE0D6CC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (s.ledEnabled) Color(0xFF34C759) else Color(0xFFA2948D))
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            if (s.ledEnabled) {
                tr(
                    "● Vòng sáng LED đang bật • ${s.ledStyle.title} (%.1f dp)".format(s.ledWidth),
                    "● LED Ring Active • ${s.ledStyle.title} (%.1f dp)".format(s.ledWidth),
                )
            } else {
                tr(
                    "○ Mô phỏng xem trước (Đang tắt công tắc bên dưới)",
                    "○ Preview simulation (Switch below is off)",
                )
            },
            color = if (s.ledEnabled) Tints.Green else Tints.Orange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            LedStyle.entries.forEach { style ->
                val selected = s.ledStyle == style
                val badgeColor = when (style) {
                    LedStyle.ROTATE_RGB -> Color(0xFFFF2D55)
                    LedStyle.AUTO_APP -> Color(0xFF0068FF)
                    LedStyle.CYBER_NEON -> Color(0xFF00F2FE)
                    LedStyle.BREATHE_GOLD -> Color(0xFFFFD700)
                    LedStyle.CUSTOM -> Color(s.ledColor)
                    LedStyle.MULTI -> Color(s.ledColors.first())
                }

                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Ui.Primary.copy(alpha = 0.22f) else Color(0xFF261A1C))
                        .border(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) Ui.Primary else Color(0xFF4A3528),
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { SettingsRepo.update { copy(ledStyle = style) } }
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            style.title,
                            color = if (selected) Color.White else Color(0xFFB3A69F),
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LedRingScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var styleDialog by remember { mutableStateOf(false) }
    var motionDialog by remember { mutableStateOf(false) }
    var slot by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        IslandBus.previewLed.value = true
        onDispose {
            IslandBus.previewLed.value = false
        }
    }

    Page(tr("Vòng sáng LED", "LED ring"), onBack) {
        LedRingPreviewCard(s)

        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.RadioButtonChecked, tr("Bật Vòng sáng LED quanh Viên thuốc", "Enable Capsule LED Ring"),
                tr("Viền ánh sáng LED chuyển động mượt mà bao quanh camera và Viên thuốc", "Animated glowing LED border around the camera cutout & capsule"),
                s.ledEnabled, color = Tile.Purple,
            ) { v -> SettingsRepo.update { copy(ledEnabled = v) } }
            GroupItem(
                Pos.Middle, Icons.Rounded.Palette, tr("Màu viền", "Colors"),
                s.ledStyle.title, color = Tile.Blue, onClick = { styleDialog = true },
            ) { Arrow() }
            GroupItem(
                Pos.Middle, Icons.Rounded.Animation, tr("Hiệu ứng chuyển động", "Motion"),
                s.ledMotion.title, color = Tile.Indigo, onClick = { motionDialog = true },
            ) { Arrow() }
            if (s.ledMotion != LedMotion.STATIC) {
                GroupSlider(
                    Pos.Middle, Icons.Rounded.Speed, tr("Tốc độ", "Speed"),
                    value = s.ledSpeed.toFloat(), range = 0f..100f, label = "${s.ledSpeed}%", color = Tile.Orange,
                ) { v -> SettingsRepo.update { copy(ledSpeed = v.toInt()) } }
            }
            GroupSlider(
                Pos.Last, Icons.Rounded.LineWeight, tr("Độ dày viền sáng LED", "LED Ring Thickness"),
                value = s.ledWidth, range = 1f..6f, label = "%.1f dp".format(s.ledWidth), color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(ledWidth = (v * 2).toInt() / 2f) } }
        }

        if (s.ledStyle == LedStyle.CUSTOM) {
            SectionHeader(tr("Màu viền LED tùy chọn", "Custom LED Color"))
            Swatches(ledPalette, s.ledColor) { c -> SettingsRepo.update { copy(ledColor = c) } }
        }
        if (s.ledStyle == LedStyle.MULTI) {
            SectionHeader(tr("Chọn 2–3 màu (xoay / chạy quanh viền)", "Pick 2–3 colors"))
            // Chọn ô màu cần đổi, rồi chạm vào bảng màu bên dưới
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    val on = i < s.ledColorCount
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (on) Color(s.ledColors.getOrElse(i) { s.ledColor }) else Ui.Card)
                            .border(if (slot == i) 3.dp else 1.dp, if (slot == i) Ui.Primary else Ui.Divider, CircleShape)
                            .clickable {
                                slot = i
                                if (!on) SettingsRepo.update { copy(ledColorCount = i + 1) }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (on) "${i + 1}" else "+", color = if (on) Color.Black.copy(alpha = 0.6f) else Ui.Sub, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (s.ledColorCount == 3) {
                    Text(
                        tr("Bỏ màu 3", "Remove 3rd"), color = Ui.Primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(CircleShape).clickable { slot = 0; SettingsRepo.update { copy(ledColorCount = 2) } }.padding(8.dp),
                    )
                }
            }
            Swatches(ledPalette, s.ledColors.getOrElse(slot) { s.ledColor }) { c ->
                SettingsRepo.update {
                    val list = ledColors.toMutableList()
                    while (list.size < 3) list += ledColor
                    list[slot] = c
                    copy(ledColors = list)
                }
            }
        }

        SectionHeader(tr("Khi nào Vòng sáng LED bật?", "LED Triggers"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.MarkChatUnread, tr("Khi có thông báo / tin nhắn chưa đọc", "Unread notifications"),
                tr("Sáng viền theo màu ứng dụng (Zalo xanh dương, Messenger, Telegram…) cho đến khi bạn mở app hoặc chạm vào Viên thuốc", "Glows in the app's brand color (Zalo, Messenger, Telegram…) until you open the app or tap the capsule"),
                s.ledOnUnread, color = Tile.Blue,
            ) { v -> SettingsRepo.update { copy(ledOnUnread = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.NotificationsActive, tr("Khi có sự kiện & thông báo mới", "During events & notifications"),
                tr("Làm rực sáng viền Viên thuốc mỗi khi có thẻ thông báo hoặc sự kiện xuất hiện", "Illuminate capsule edges whenever an event or notification banner appears"),
                s.ledOnEvent, color = Tile.Orange,
            ) { v -> SettingsRepo.update { copy(ledOnEvent = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.Bolt, tr("Khi đang cắm sạc pin", "While charging battery"),
                tr("Vòng sáng xoay quanh camera trong suốt thời gian cắm sạc", "Rotating glow around the camera while the phone is plugged in"),
                s.ledOnCharge, color = Tile.Green,
            ) { v -> SettingsRepo.update { copy(ledOnCharge = v) } }
            GroupToggle(
                Pos.Last, Icons.Rounded.Flare, tr("Luôn sáng trang trí (Always-On LED)", "Always-On decorative LED"),
                tr("Giữ vòng sáng LED luôn lung linh quanh Viên thuốc khi màn hình bật", "Keep the LED ring glowing continuously around the capsule while screen is on"),
                s.ledAlwaysOn, color = Tile.Pink,
            ) { v -> SettingsRepo.update { copy(ledAlwaysOn = v) } }
        }

        SectionHeader(tr("Kiểm tra thử", "Test"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.MarkChatUnread, tr("Bật thử Vòng sáng LED báo tin chưa đọc", "Test Unread LED Ring (Zalo Blue)"),
                tr("Thắp sáng vòng LED quanh Viên thuốc ngay bây giờ (chạm vào Viên thuốc để tắt)", "Light up the unread LED ring around the capsule right now (tap capsule to clear)"),
                color = Tile.Blue,
                onClick = {
                    SettingsRepo.update { copy(ledEnabled = true, ledOnUnread = true) }
                    IslandBus.unreadLedColor.value = Color(0xFF0068FF)
                },
            )
            GroupItem(
                Pos.Last, Icons.Rounded.RadioButtonChecked, tr("Tắt đèn LED thử nghiệm", "Clear Unread LED indicator"),
                tr("Tắt hiệu ứng đèn báo chưa đọc đang sáng", "Turn off the active unread LED indicator"),
                color = Tile.Gray,
                onClick = {
                    IslandBus.clearUnread()
                },
            )
        }
    }

    if (styleDialog) {
        ChoiceDialog(
            tr("Kiểu hiệu ứng ánh sáng LED", "LED Lighting Style"),
            LedStyle.entries,
            s.ledStyle,
            { it.title },
            { v -> SettingsRepo.update { copy(ledStyle = v) } },
        ) { styleDialog = false }
    }
    if (motionDialog) {
        ChoiceDialog(
            tr("Hiệu ứng chuyển động", "Motion"), LedMotion.entries, s.ledMotion, { it.title },
            { v -> SettingsRepo.update { copy(ledMotion = v) } },
        ) { motionDialog = false }
    }
}
