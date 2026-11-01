package com.akira.island.ui.screens

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import com.akira.island.island.drawLedRing
import com.akira.island.island.ledOuterGlow
import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LensBlur
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.IslandBus
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.Elasticity
import com.akira.island.data.ExpandStyle
import com.akira.island.data.FxRate
import com.akira.island.data.LedStyle
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RadioButtonChecked
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
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

@Composable
fun CapsuleScreen(open: (Screen) -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    Page(tr("Giao diện", "Appearance")) {
        IslandPreview()

        SectionHeader(tr("Vị trí & kích thước", "Position and size"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.OpenWith, tr("Vị trí & kích thước", "Position and size"),
                tr("Viên thuốc ${s.width}×${s.height} dp — căn chỉnh vừa camera", "Capsule ${s.width}×${s.height} dp — fit it to the camera"),
                color = Tile.Indigo, onClick = { open(Screen.SIZE) }) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.SwapHoriz, tr("Kích thước khi có hoạt động", "Size with activity"),
                if (s.activeWidth == 0) tr("Độ dày +${s.activeExtra} dp, chiều rộng giữ nguyên", "Thickness +${s.activeExtra} dp, width as without it")
                else tr("Độ dày +${s.activeExtra}, chiều rộng +${s.activeWidth} dp", "Thickness +${s.activeExtra}, width +${s.activeWidth} dp"),
                color = Tile.Purple, onClick = { open(Screen.LOOK) }) { Arrow() }
            GroupItem(
                Pos.Last,
                Icons.Rounded.Tune,
                tr("Kích thước & bo góc khung thẻ", "Cards size & corner radius"),
                tr("Tin nhắn, cuộc gọi, cập nhật · Bo góc ${s.cardCornerRadius} dp", "Messages, calls, updates · Corner ${s.cardCornerRadius} dp"),
                color = Tile.Green,
                onClick = { open(Screen.MSG_CALL_SIZE) },
            ) { Arrow() }
        }

        SectionHeader(tr("Màu sắc & hiệu ứng kính", "Color and glass"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.FormatColorFill, tr("Màu viên thuốc", "Capsule color"), tr("Màu sắc, đường viền và bóng đổ", "Color, outline and shadow"),
                color = Tile.Orange, onClick = { open(Screen.COLORS) }) { Arrow() }
            GroupItem(
                Pos.Middle, Icons.Rounded.RadioButtonChecked, tr("Vòng sáng LED Viên thuốc", "Capsule LED Ring"),
                if (s.ledEnabled) "${s.ledStyle.title} · %.1f dp".format(s.ledWidth) else tr("Tắt", "Off"),
                color = Tile.Purple, onClick = { open(Screen.LED_RING) },
            ) { Arrow() }
            GroupToggle(
                if (s.liquidGlass) Pos.Middle else Pos.Last, Icons.Rounded.WaterDrop, "Liquid Glass (iOS)",
                tr("Thấu kính chất lỏng 3D chuẩn iOS Liquid Glass với khúc xạ viền và phản quang sống động khi nghiêng máy", "iOS optical 3D liquid glass lens with live refraction and specular highlights when tilting"),
                s.liquidGlass, color = Tile.Teal,
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

        SectionHeader(tr("Hiệu ứng chuyển động", "Animations"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.Animation, tr("Mở rộng Viên thuốc", "Island expansion"),
                "${s.expandStyle.title} · ${s.durationPercent}%",
                color = Tile.Blue, onClick = { open(Screen.ANIM) }) { Arrow() }
            
            GroupToggle(Pos.Middle, Icons.Rounded.BubbleChart, tr("Mở rộng dạng chất lỏng", "Liquid expansion"),
                tr("Nội dung mở rộng cùng với Viên thuốc và hiện rõ từ hiệu ứng mờ", "The content grows together with the island and emerges from a blur"),
                s.liquidExpand, color = Tile.Indigo, badge = tr("Mới", "New"),
            ) { v -> SettingsRepo.update { copy(liquidExpand = v) } }
            
            ElasticityItem(Pos.Middle, s, Tile.Teal)
            GroupItem(Pos.Last, Icons.Rounded.AutoAwesome, tr("Hiệu ứng", "Effects"), effectsSummary(s),
                color = Tile.Purple, onClick = { open(Screen.EFFECTS) }) { Arrow() }
        }
    }
}

@Composable
private fun ElasticityItem(pos: Pos, s: IslandSettings, color: Color) {
    var dialog by remember { mutableStateOf(false) }
    GroupItem(pos, Icons.Rounded.Waves, tr("Độ đàn hồi", "Elasticity"), s.elasticity.title, color = color, onClick = { dialog = true })
    if (dialog) {
        ChoiceDialog(tr("Độ đàn hồi của hiệu ứng", "Animation elasticity"), Elasticity.entries, s.elasticity, { it.title },
            { v -> SettingsRepo.update { copy(elasticity = v) } }) { dialog = false }
    }
}

private fun previewActivity() {
    IslandBus.widthPreviewUntil.value = SystemClock.elapsedRealtime() + 2_500
}

@Composable
fun LookScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    Page(tr("Kích thước khi có hoạt động", "Size with activity"), onBack) {
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
    }
}

@Composable
fun AnimScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var styleDialog by remember { mutableStateOf(false) }

    Page(tr("Mở rộng Viên thuốc", "Island expansion"), onBack, action = {
        Text(tr("Đặt lại", "Reset"), color = Ui.Sub, fontSize = 16.sp, modifier = Modifier.clickable { SettingsRepo.resetAnimations() })
    }) {
        Group {
            GroupItem(Pos.First, Icons.Rounded.Animation, tr("Kiểu mở rộng", "Expansion style"), s.expandStyle.title, color = Tile.Purple,
                onClick = { styleDialog = true })
            GroupSlider(
                Pos.Middle, Icons.Rounded.Speed, tr("Thời lượng", "Duration"),
                value = s.durationPercent.toFloat(), range = 0f..100f, label = "${s.durationPercent}%", color = Tile.Indigo,
            ) { v -> SettingsRepo.update { copy(durationPercent = v.toInt()) } }
            GroupSlider(
                Pos.Middle, Icons.Rounded.Schedule, tr("Hiển thị sự kiện", "Event display"),
                value = s.noticeDurationMs / 1000f, range = 1.5f..6f, label = tr("%.1f giây", "%.1f s").format(s.noticeDurationMs / 1000f), color = Tile.Orange,
            ) { v -> SettingsRepo.update { copy(noticeDurationMs = (v * 1000).toInt()) } }
            GroupToggle(Pos.Last, Icons.Rounded.BlurOn, tr("Giảm hiệu ứng", "Reduce effects"),
                tr("Có thể hữu ích trên thiết bị cấu hình thấp", "Can help on less powerful devices"),
                s.reduceEffects, color = Tile.Gray) { v -> SettingsRepo.update { copy(reduceEffects = v) } }
        }
    }

    if (styleDialog) {
        ChoiceDialog(tr("Kiểu mở rộng", "Expansion style"), ExpandStyle.entries, s.expandStyle, { it.title },
            { v -> SettingsRepo.update { copy(expandStyle = v) } }) { styleDialog = false }
    }
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
    Page(tr("Hiệu ứng", "Effects"), onBack, action = {
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
                        Color(0xFF141724),
                        Color(0xFF0D0E15),
                    )
                )
            )
            .border(1.dp, Color(0xFF262A3E), RoundedCornerShape(28.dp))
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
            Text("09:41", color = Color(0xFF7A8099), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("5G", color = Color(0xFF7A8099), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                Box(
                    Modifier
                        .size(14.dp, 8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF7A8099))
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
                        .border(1.dp, Color(0xFF222738), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0B1428))
                    )
                }

                Text(
                    "Capsule VN",
                    color = Color(0xFFC7CBD9),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (s.ledEnabled) Color(0xFF34C759) else Color(0xFF8E92A8))
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
                }

                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Ui.Primary.copy(alpha = 0.22f) else Color(0xFF1E2130))
                        .border(
                            if (selected) 1.5.dp else 1.dp,
                            if (selected) Ui.Primary else Color(0xFF2E3348),
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
                            color = if (selected) Color.White else Color(0xFF9AA0B6),
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

    DisposableEffect(Unit) {
        IslandBus.previewLed.value = true
        onDispose {
            IslandBus.previewLed.value = false
        }
    }

    Page(tr("Vòng sáng LED Viên thuốc", "Capsule LED Ring"), onBack) {
        LedRingPreviewCard(s)

        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.RadioButtonChecked, tr("Bật Vòng sáng LED quanh Viên thuốc", "Enable Capsule LED Ring"),
                tr("Viền ánh sáng LED chuyển động mượt mà bao quanh camera và Viên thuốc", "Animated glowing LED border around the camera cutout & capsule"),
                s.ledEnabled, color = Tile.Purple,
            ) { v -> SettingsRepo.update { copy(ledEnabled = v) } }
            GroupItem(
                Pos.Middle, Icons.Rounded.Palette, tr("Kiểu hiệu ứng ánh sáng LED", "LED Lighting Style"),
                s.ledStyle.title, color = Tile.Blue, onClick = { styleDialog = true },
            )
            GroupSlider(
                Pos.Last, Icons.Rounded.LineWeight, tr("Độ dày viền sáng LED", "LED Ring Thickness"),
                value = s.ledWidth, range = 1f..6f, label = "%.1f dp".format(s.ledWidth), color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(ledWidth = (v * 2).toInt() / 2f) } }
        }

        if (s.ledStyle == LedStyle.CUSTOM) {
            SectionHeader(tr("Màu viền LED tùy chọn", "Custom LED Color"))
            Swatches(ledPalette, s.ledColor) { c -> SettingsRepo.update { copy(ledColor = c) } }
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
                    IslandBus.unreadLedColor.value = null
                    IslandBus.unreadLedPkg.value = null
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
}
