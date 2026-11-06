@file:OptIn(ExperimentalLayoutApi::class)

package com.akira.island.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import com.akira.island.data.BorderEffect
import com.akira.island.data.BorderPalette
import com.akira.island.data.BorderSpec
import com.akira.island.data.NoticeKind
import com.akira.island.data.NoticeTweak
import com.akira.island.data.SettingsRepo
import com.akira.island.data.Surface
import com.akira.island.data.border
import com.akira.island.island.drawBorder
import com.akira.island.island.drawBorderGlow
import com.akira.island.island.moving
import com.akira.island.island.paletteColors
import com.akira.island.island.rememberFxClock
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.GroupStepSlider
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui

/** Tham số điều hướng đơn giản giữa các màn hình (màn hình không nhận đối số). */
object SurfaceNav {
    /** Nơi được chọn sẵn khi mở "Tùy chỉnh theo từng nơi". */
    var initial: Surface = Surface.IDLE
    /** Tab mở sẵn của "Vị trí & kích thước" (0 = Viên thuốc, 1 = Bảng mở rộng). */
    var sizeTab: Int = 0
}

private val borderSwatches = listOf(
    0xFFE6C068, 0xFFC8102E, 0xFFFFFFFF, 0xFF8E8E93, 0xFF0A84FF, 0xFF00E5FF,
    0xFF34C759, 0xFF39FF14, 0xFFFFD60A, 0xFFFF9F0A, 0xFFFF2D55, 0xFFFF2D92,
    0xFFBF5AF2, 0xFF7F00FF, 0xFF5E5CE6, 0xFF3A3A40,
)

private fun gradientOf(colors: List<Color>): Brush =
    Brush.horizontalGradient(if (colors.size == 1) listOf(colors[0], colors[0]) else colors)

/** Viên thuốc mẫu nhỏ trong app: xem ngay màu & hiệu ứng viền đang chọn. */
@Composable
fun BorderPreview(spec: BorderSpec, adaptive: Color?, label: String = "Capsule VN") {
    val live = spec.copy(on = true)
    val clock = rememberFxClock(spec.on && spec.moving)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1C1416), Color(0xFF0E0B0C))))
            .border(1.dp, Color(0xFF33262A), RoundedCornerShape(26.dp))
            .padding(vertical = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .width(196.dp)
                    .height(42.dp)
                    .drawBehind { if (spec.on) drawBorderGlow(live, adaptive, clock.value, 21.dp.toPx(), false) }
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color.Black)
                    .drawWithContent {
                        drawContent()
                        if (spec.on) drawBorder(live, adaptive, clock.value, 21.dp.toPx(), false)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = Color(0xFFE0D6CC), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (spec.on) "${spec.palette.title} · ${spec.effect.title} · %.1f dp".format(spec.width)
                else tr("Không viền (mặc định)", "No border (default)"),
                color = if (spec.on) Ui.Primary else Ui.Sub, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun Chip(selected: Boolean, text: String, onClick: () -> Unit, lead: (@Composable () -> Unit)? = null) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Ui.Primary else Ui.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (lead != null) {
            lead()
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text, color = if (selected) Ui.OnPrimary else Ui.Text, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1,
        )
    }
}

/**
 * Bộ chỉnh đường viền dùng chung: bảng màu, hiệu ứng, độ dày, tốc độ, quầng sáng, độ đậm.
 * [toggleTitle] = null: không hiện công tắc bật / tắt (vd. Vòng sáng LED có công tắc riêng).
 */
@Composable
fun BorderEditor(
    spec: BorderSpec,
    adaptive: Color?,
    toggleTitle: String?,
    onChange: (BorderSpec) -> Unit,
) {
    var slot by remember { mutableIntStateOf(0) }
    val on = spec.on || toggleTitle == null

    if (toggleTitle != null) {
        Group {
            GroupToggle(
                Pos.Single, Icons.Rounded.BorderStyle, toggleTitle,
                tr("Mặc định không có viền. Bật để chọn màu, hiệu ứng và độ dày riêng cho nơi này", "Off by default. Turn on to pick colors, effects and thickness for this place"),
                spec.on, color = Tile.Yellow,
            ) { v -> onChange(spec.copy(on = v)) }
        }
    }
    if (!on) return

    SectionHeader(tr("Màu viền", "Border colors"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BorderPalette.entries.forEach { p ->
            val colors = paletteColors(spec.copy(palette = p), adaptive ?: Color(0xFF0A84FF))
            Chip(spec.palette == p, p.title, { onChange(spec.copy(palette = p)) }) {
                Box(Modifier.size(width = 22.dp, height = 10.dp).clip(CircleShape).background(gradientOf(colors)))
            }
        }
    }
    when (spec.palette) {
        BorderPalette.SINGLE, BorderPalette.APP -> {
            if (spec.palette == BorderPalette.APP) {
                Text(
                    tr("Màu tự đổi theo ứng dụng / ảnh bìa bài hát. Màu dưới đây dùng khi không có màu riêng", "Follows the app / album art. The color below is used when there is none"),
                    color = Ui.Sub, fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
            Swatches(borderSwatches, spec.color) { c -> onChange(spec.copy(color = c)) }
        }
        BorderPalette.MULTI -> {
            val count = spec.colorCount.coerceIn(2, 4)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { i ->
                    val used = i < count
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (used) Color(spec.colors.getOrElse(i) { spec.color }) else Ui.Card)
                            .border(if (slot == i) 3.dp else 1.dp, if (slot == i) Ui.Primary else Ui.Divider, CircleShape)
                            .clickable {
                                slot = i
                                if (!used) onChange(spec.copy(colorCount = i + 1))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (used) "${i + 1}" else "+", color = if (used) Color.Black.copy(alpha = 0.6f) else Ui.Sub, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (count > 2) {
                    Text(
                        tr("Bớt 1 màu", "Remove one"), color = Ui.Primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clip(CircleShape).clickable {
                            slot = 0
                            onChange(spec.copy(colorCount = count - 1))
                        }.padding(8.dp),
                    )
                }
            }
            Swatches(borderSwatches, spec.colors.getOrElse(slot) { spec.color }) { c ->
                val list = spec.colors.toMutableList()
                while (list.size < 4) list += spec.color
                list[slot.coerceIn(0, 3)] = c
                onChange(spec.copy(colors = list))
            }
        }
        else -> Unit
    }

    SectionHeader(tr("Hiệu ứng", "Effect"))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BorderEffect.entries.forEach { e ->
            Chip(spec.effect == e, e.title, { onChange(spec.copy(effect = e)) })
        }
    }

    Group {
        GroupSlider(
            Pos.First, Icons.Rounded.LineWeight, tr("Độ dày", "Thickness"),
            value = spec.width, range = 0.5f..6f, label = "%.1f dp".format(spec.width), color = Tile.Yellow,
        ) { v -> onChange(spec.copy(width = (v * 2).toInt() / 2f)) }
        if (spec.effect != BorderEffect.STATIC) {
            GroupSlider(
                Pos.Middle, Icons.Rounded.Speed, tr("Tốc độ hiệu ứng", "Effect speed"),
                value = spec.speed.toFloat(), range = 0f..100f, label = "${spec.speed}%", color = Tile.Orange,
            ) { v -> onChange(spec.copy(speed = v.toInt())) }
        }
        GroupSlider(
            Pos.Middle, Icons.Rounded.Flare, tr("Quầng sáng toả ra", "Outer glow"),
            value = spec.glow.toFloat(), range = 0f..100f, label = if (spec.glow == 0) tr("Tắt", "Off") else "${spec.glow}%", color = Tile.Purple,
        ) { v -> onChange(spec.copy(glow = v.toInt())) }
        GroupSlider(
            Pos.Last, Icons.Rounded.Opacity, tr("Độ đậm", "Opacity"),
            value = spec.opacity.toFloat(), range = 20f..100f, label = "${spec.opacity}%", color = Tile.Teal,
        ) { v -> onChange(spec.copy(opacity = v.toInt())) }
    }
}

/** Kích thước & vị trí riêng của một loại viên nhỏ: chế độ chuông (MODE) hoặc Đã gửi / Đã sao chép (SENT). */
@Composable
private fun PillSizeGroup(kind: NoticeKind) {
    val s by SettingsRepo.state.collectAsState()
    val own = s.noticeTweaks[kind]
    val cur = own ?: s.noticeGeneral.copy(width = 0, heightExtra = 0, corner = -1)
    val widths = if (kind == NoticeKind.SENT) 120..460 else 60..360
    fun put(tw: NoticeTweak) {
        IslandBus.cardPreviewMode.value = CardPreviewMode.NOTICE
        IslandBus.previewNoticeKind.value = kind
        SettingsRepo.update { copy(noticeTweaks = noticeTweaks + (kind to tw)) }
    }
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), cur.offsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { put(cur.copy(offsetY = 0)) }, color = Tile.Indigo,
        ) { v -> put(cur.copy(offsetY = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), cur.offsetX, -120..120,
            action = tr("Chính giữa", "Center") to { put(cur.copy(offsetX = 0)) }, color = Tile.Teal,
        ) { v -> put(cur.copy(offsetX = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"),
            if (cur.width == 0) widths.first + 60 else cur.width.coerceIn(widths.first, widths.last), widths,
            action = (if (kind == NoticeKind.SENT) tr("Tự động theo màn hình", "Auto (screen width)") else tr("Tự động theo nội dung", "Auto (fits content)")) to { put(cur.copy(width = 0)) },
            color = Tile.Blue,
            valueText = if (cur.width == 0) tr("Tự động", "Auto") else null,
        ) { v -> put(cur.copy(width = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), cur.heightExtra, -12..30,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { put(cur.copy(heightExtra = 0)) }, color = Tile.Purple,
        ) { v -> put(cur.copy(heightExtra = v)) }
        GroupStepSlider(
            if (own != null) Pos.Middle else Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"),
            if (cur.corner < 0) 20 else cur.corner, 0..30,
            action = tr("Tự động (tròn đều)", "Auto (fully round)") to { put(cur.copy(corner = -1)) }, color = Tile.Yellow,
            valueText = if (cur.corner < 0) tr("Tự động", "Auto") else null,
        ) { v -> put(cur.copy(corner = v)) }
        if (own != null) {
            GroupItem(
                Pos.Last, Icons.Rounded.Restore, tr("Đặt lại kích thước", "Reset size"),
                tr("Dùng lại kích thước tự động như ban đầu", "Back to the automatic size"),
                color = Tile.Gray, onClick = { SettingsRepo.update { copy(noticeTweaks = noticeTweaks - kind) } },
            )
        }
    }
}

/**
 * Trung tâm tuỳ chỉnh theo từng nơi: Viên thuốc bình thường, khi có hoạt động, thông báo viên nhỏ,
 * khung thẻ thông báo và bảng mở rộng — mỗi nơi có kích thước & vị trí, nền, đường viền và tính năng riêng.
 */
@Composable
fun SurfaceScreen(onBack: () -> Unit, open: (Screen) -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var surface by remember { mutableStateOf(SurfaceNav.initial) }
    // Thông báo viên nhỏ chia 2 loại chỉnh riêng: chế độ chuông / im lặng / rung và Đã gửi / Đã sao chép
    var pillKind by remember { mutableStateOf(NoticeKind.MODE) }

    // Xem trước trực tiếp đúng nơi đang chỉnh trên Viên thuốc thật (tự ẩn khi rời màn hình / rời app)
    DisposableEffect(surface, pillKind) {
        IslandBus.previewNoticeKind.value = if (surface == Surface.PILL) pillKind else null
        IslandBus.cardPreviewMode.value = when (surface) {
            Surface.PILL -> CardPreviewMode.NOTICE
            Surface.CARD -> CardPreviewMode.MESSAGE
            Surface.EXPANDED -> CardPreviewMode.EXPANDED
            else -> CardPreviewMode.NONE
        }
        IslandBus.widthPreviewUntil.value = if (surface == Surface.ACTIVE) SystemClock.elapsedRealtime() + 10 * 60_000L else 0L
        onDispose {
            IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
            IslandBus.previewNoticeKind.value = null
            IslandBus.widthPreviewUntil.value = 0L
        }
    }

    val spec = s.border(surface)
    fun setSpec(b: BorderSpec) = SettingsRepo.update { copy(borders = borders + (surface to b)) }

    Page(tr("Tùy chỉnh theo từng nơi", "Customize each place"), onBack, subtitle = surface.hint) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface.entries.forEach { sf ->
                Chip(sf == surface, sf.title, { surface = sf }) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape).background(
                            if (s.border(sf).on) gradientOf(paletteColors(s.border(sf), Ui.Primary)) else Brush.linearGradient(listOf(Ui.Divider, Ui.Divider)),
                        ),
                    )
                }
            }
        }

        SectionHeader(tr("Kích thước & vị trí", "Size & position"))
        when (surface) {
            Surface.IDLE -> Group {
                GroupItem(
                    Pos.Single, Icons.Rounded.OpenWith, tr("Vị trí & kích thước Viên thuốc", "Island position & size"),
                    tr("${s.width}×${s.height} dp · căn quanh camera · bo góc", "${s.width}×${s.height} dp · around the camera · corners"),
                    color = Tile.Indigo, onClick = { SurfaceNav.sizeTab = 0; open(Screen.SIZE) },
                ) { Arrow() }
            }
            Surface.ACTIVE -> Group {
                GroupItem(
                    Pos.Single, Icons.Rounded.SwapHoriz, tr("Độ dày, chiều rộng & vị trí khi có hoạt động", "Thickness, width & position"),
                    if (s.activeWidth == 0) tr("+${s.activeExtra} dp · rộng tự động theo nội dung", "+${s.activeExtra} dp · width fits content")
                    else tr("+${s.activeExtra} dp · rộng +${s.activeWidth} dp", "+${s.activeExtra} dp · width +${s.activeWidth} dp"),
                    color = Tile.Purple, onClick = { open(Screen.LOOK) },
                ) { Arrow() }
            }
            Surface.PILL -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(NoticeKind.MODE to tr("Chế độ chuông, im lặng…", "Ring mode, silent…"), NoticeKind.SENT to tr("Đã gửi / Đã sao chép", "Sent / Copied")).forEach { (k, label) ->
                        Chip(pillKind == k, label, { pillKind = k })
                    }
                }
                PillSizeGroup(pillKind)
            }
            Surface.CARD -> Group {
                GroupItem(
                    Pos.Single, Icons.Rounded.OpenWith, tr("Tin nhắn, cuộc gọi, cập nhật, thông báo khác", "Messages, calls, updates, other alerts"),
                    tr("Vị trí, chiều rộng, chiều cao, số dòng, bo góc từng loại", "Position, width, height, lines, corners per kind"),
                    color = Tile.Green, onClick = { open(Screen.MSG_CALL_SIZE) },
                ) { Arrow() }
            }
            Surface.EXPANDED -> Group {
                GroupItem(
                    Pos.Single, Icons.Rounded.DashboardCustomize, tr("Vị trí & kích thước bảng mở rộng", "Expanded board position & size"),
                    if (s.expandedWidth == 0) tr("Rộng tự động · bo góc ${s.expandedCornerRadius} dp", "Auto width · ${s.expandedCornerRadius} dp corners")
                    else tr("Rộng ${s.expandedWidth} dp · bo góc ${s.expandedCornerRadius} dp", "${s.expandedWidth} dp wide · ${s.expandedCornerRadius} dp corners"),
                    color = Tile.Indigo, onClick = { SurfaceNav.sizeTab = 1; open(Screen.SIZE) },
                ) { Arrow() }
            }
        }

        SectionHeader(tr("Nền & giao diện", "Background & look"))
        Group {
            when (surface) {
                Surface.IDLE -> {
                    GroupItem(
                        Pos.First, Icons.Rounded.FormatColorFill, tr("Màu nền & bóng đổ", "Color & shadow"), null,
                        color = Tile.Orange, onClick = { open(Screen.COLORS) },
                    ) { Arrow() }
                    GroupToggle(
                        Pos.Last, Icons.Rounded.AspectRatio, tr("Dùng nền thẻ (ảnh / màu / kính)", "Use the card background"),
                        tr("Viên thuốc trống dùng chung nền với khung thẻ thông báo", "The idle island uses the card background"),
                        s.cardStyleIdle, color = Tile.Blue,
                    ) { v -> SettingsRepo.update { copy(cardStyleIdle = v) } }
                }
                Surface.ACTIVE -> {
                    GroupItem(
                        Pos.Single, Icons.Rounded.AutoAwesome, tr("Hiệu ứng đặc biệt", "Special effects"),
                        tr("Sóng nhạc, phát sáng theo ảnh bìa, rung…", "Equalizer, artwork glow, shake…"),
                        color = Tile.Purple, onClick = { open(Screen.EFFECTS) },
                    ) { Arrow() }
                }
                Surface.PILL -> {
                    GroupToggle(
                        Pos.First, Icons.Rounded.Wallpaper, tr("Dùng nền thẻ (ảnh / màu / kính)", "Use the card background"),
                        tr("Tắt: giữ nền đen như cũ", "Off: keep the classic black"),
                        s.cardStylePills, color = Tile.Gray,
                    ) { v -> SettingsRepo.update { copy(cardStylePills = v) } }
                    GroupItem(
                        Pos.Last, Icons.Rounded.Schedule, tr("Thời gian hiển thị", "Display time"),
                        tr("%.1f giây · chỉnh nhanh / chậm từng loại", "%.1f s · set per kind").format(s.durationFor(pillKind.name) / 1000f),
                        color = Tile.Orange, onClick = { open(Screen.DURATIONS) },
                    ) { Arrow() }
                }
                Surface.CARD -> {
                    GroupItem(
                        Pos.First, Icons.Rounded.Wallpaper, tr("Nền thẻ (ảnh / màu / Liquid Glass)", "Card background"),
                        s.cardBg.title, color = Tile.Orange, onClick = { open(Screen.MSG_CALL_SIZE) },
                    ) { Arrow() }
                    GroupItem(
                        Pos.Middle, Icons.Rounded.Schedule, tr("Thời gian hiển thị", "Display time"),
                        tr("Tin nhắn %.1f giây · chỉnh nhanh / chậm từng loại", "Messages %.1f s · set per kind").format(s.durationFor("MESSAGE") / 1000f),
                        color = Tile.Orange, onClick = { open(Screen.DURATIONS) },
                    ) { Arrow() }
                    GroupItem(
                        Pos.Last, Icons.Rounded.NotificationsActive, tr("Thông báo & tin nhắn", "Notifications & messages"),
                        tr("Chọn ứng dụng, trả lời nhanh, chống hiện 2 lần…", "Apps, quick reply, no duplicates…"),
                        color = Tile.Blue, onClick = { open(Screen.NOTIFICATIONS) },
                    ) { Arrow() }
                }
                Surface.EXPANDED -> {
                    GroupToggle(
                        Pos.First, Icons.Rounded.Wallpaper, tr("Dùng nền thẻ (ảnh / màu / kính)", "Use the card background"),
                        tr("Bảng Giờ – Pin, nhạc, hẹn giờ… dùng chung nền với khung thẻ", "Clock, music, timers… use the card background"),
                        s.cardStyleExpanded, color = Tile.Indigo,
                    ) { v -> SettingsRepo.update { copy(cardStyleExpanded = v) } }
                    GroupItem(
                        Pos.Last, Icons.Rounded.Widgets, tr("Thêm / bớt thẻ trong bảng mở rộng", "Add / remove panels"),
                        tr("Giờ, pin, âm lịch, RAM/ROM, phím tắt…", "Clock, battery, lunar date, RAM/ROM, shortcuts…"),
                        color = Tile.Green, onClick = { open(Screen.CARDS) },
                    ) { Arrow() }
                }
            }
        }

        SectionHeader(tr("Đường viền", "Border"))
        BorderPreview(spec, null, surface.title)
        BorderEditor(spec, null, tr("Đường viền cho ${surface.title.lowercase()}", "Border for ${surface.title.lowercase()}"), ::setSpec)

        if (spec.on) {
            Group {
                GroupItem(
                    Pos.First, Icons.Rounded.ContentCopy, tr("Dùng viền này cho mọi nơi", "Use this border everywhere"),
                    tr("Chép sang cả 5 nơi của Viên thuốc", "Copy to all 5 places of the island"),
                    color = Tile.Blue,
                    onClick = { SettingsRepo.update { copy(borders = Surface.entries.associateWith { spec }) } },
                )
                GroupItem(
                    Pos.Last, Icons.Rounded.Restore, tr("Bỏ viền ở mọi nơi", "Remove borders everywhere"),
                    tr("Về mặc định: không viền", "Back to default: no border"),
                    color = Tile.Gray,
                    onClick = { SettingsRepo.update { copy(borders = emptyMap()) } },
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
