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
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DashboardCustomize
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
import com.akira.island.data.SettingsRepo
import com.akira.island.island.glassSurface
import com.akira.island.island.islandSpec
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
import com.akira.island.data.Place
import kotlin.math.roundToInt
import androidx.compose.material.icons.rounded.Wallpaper
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
        FeatureSearch(open)
        IslandPreview()

        // Mỗi nơi / mỗi loại thông báo một trang riêng: kích thước & vị trí, nền, thời gian hiển thị, đường viền
        val sections = listOf(
            tr("Viên thuốc", "Island") to listOf(Place.IDLE, Place.ACTIVE, Place.EXPANDED),
            tr("Khung thẻ thông báo", "Notification cards") to listOf(Place.MESSAGE, Place.CALL, Place.UPDATE),
            tr("Thông báo sự kiện", "Event alerts") to listOf(Place.CHARGING, Place.AUDIO, Place.PAYMENT, Place.NAV, Place.ALARM, Place.SENT, Place.OTHER),
            tr("Thông báo viên nhỏ", "Small pills") to listOf(Place.MODE, Place.UNLOCK, Place.COMPACT),
        )
        sections.forEach { (header, list) ->
            SectionHeader(header)
            Group {
                list.forEachIndexed { i, p ->
                    GroupItem(
                        com.akira.island.ui.posOf(i, list.size), p.icon(), p.title, placeSummary(s, p),
                        color = p.tile(), onClick = { openPlace(open, p) },
                    ) { Arrow() }
                }
            }
        }

        SectionHeader(tr("Chung", "General"))
        Group {
            GroupItem(
                Pos.First,
                Icons.Rounded.FormatColorFill,
                tr("Màu nền & bóng đổ", "Color & shadow"),
                tr("Màu nền đen / màu của Viên thuốc khi không dùng nền thẻ, bóng đổ", "Island color when not using the card background, shadow"),
                color = Tile.Orange,
                onClick = { open(Screen.COLORS) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle,
                Icons.Rounded.Wallpaper,
                tr("Nền thẻ thông báo", "Card background"),
                tr("Kiểu nền (màu / ảnh / Liquid Glass) cho các loại bật \"Dùng nền thẻ\"", "Style used by places with \"Use card background\""),
                color = Tile.Pink,
                onClick = { open(Screen.CARD_BG) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle,
                Icons.Rounded.RadioButtonChecked,
                tr("Vòng sáng LED", "LED ring"),
                if (s.ledEnabled) "${s.ledBorder.palette.title} · ${s.ledBorder.effect.title}" else tr("Tắt", "Off"),
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
fun AnimScreen(onBack: () -> Unit, open: (Screen) -> Unit = {}) {
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
            // Mặc định cho mọi loại; loại nào cần nhanh / chậm riêng thì chỉnh trong trang riêng của loại đó
            GroupSlider(
                Pos.Last, Icons.Rounded.Schedule, tr("Thời gian hiển thị mặc định", "Default display time"),
                value = s.noticeDurationMs / 1000f, range = 1f..12f, steps = 21,
                label = tr("%.1f giây", "%.1f s").format(s.noticeDurationMs / 1000f), color = Tile.Orange,
            ) { v ->
                SettingsRepo.update { copy(noticeDurationMs = (v * 2).roundToInt() * 500) }
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

@Composable
fun LedRingScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()

    DisposableEffect(Unit) {
        IslandBus.previewLed.value = true
        onDispose {
            IslandBus.previewLed.value = false
        }
    }

    Page(tr("Vòng sáng LED", "LED ring"), onBack) {
        BorderPreview(s.ledBorder.copy(on = true), Color(0xFF0068FF), tr("Vòng sáng LED", "LED ring"))

        Group {
            GroupToggle(
                Pos.Single, Icons.Rounded.RadioButtonChecked, tr("Bật Vòng sáng LED quanh Viên thuốc", "Enable Capsule LED Ring"),
                tr("Viền ánh sáng báo sự kiện, tin chưa đọc, đang sạc… (khác với Đường viền trang trí của từng nơi)", "A light ring for events, unread messages, charging… (separate from each place's decorative border)"),
                s.ledEnabled, color = Tile.Purple,
            ) { v -> SettingsRepo.update { copy(ledEnabled = v) } }
        }
        BorderEditor(s.ledBorder.copy(on = true), Color(0xFF0068FF), null) { b -> SettingsRepo.update { copy(ledBorder = b) } }

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

}
