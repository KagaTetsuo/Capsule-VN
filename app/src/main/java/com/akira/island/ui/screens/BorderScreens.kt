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
