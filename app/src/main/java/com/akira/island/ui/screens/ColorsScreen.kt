@file:OptIn(ExperimentalLayoutApi::class)

package com.akira.island.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.data.Surface
import com.akira.island.data.border
import com.akira.island.ui.Group
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui

private val capsuleColors = listOf(
    0xFF000000, 0xFF111114, 0xFF1C1C22, 0xFF26233A, 0xFF1A2238, 0xFF102A2A,
    0xFF2B1730, 0xFF3A1A1A, 0xFF2E2A1A, 0xFF3D3D45, 0xFFF2F2F7, 0xFFC6C3F7,
)

@Composable
fun ColorsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    Page(tr("Màu sắc & viền", "Color & border"), onBack) {
        IslandPreview()

        SectionHeader(tr("Màu sắc", "Color"))
        Swatches(capsuleColors, s.color) { c -> SettingsRepo.update { copy(color = c) } }

        // Đường viền Viên thuốc bình thường (các nơi khác chỉnh ở Giao diện → Tùy chỉnh theo từng nơi)
        SectionHeader(tr("Đường viền Viên thuốc bình thường", "Idle island border"))
        val idle = s.border(Surface.IDLE)
        BorderPreview(idle, null, tr("Viên thuốc", "Island"))
        BorderEditor(idle, null, tr("Đường viền", "Border")) { b ->
            SettingsRepo.update { copy(borders = borders + (Surface.IDLE to b)) }
        }
        Text(
            tr("Viền khi có hoạt động, thông báo viên nhỏ, khung thẻ và bảng mở rộng: Giao diện → Tùy chỉnh theo từng nơi.", "Borders for activities, pills, cards and the expanded board: Appearance → Customize each place."),
            color = Ui.Sub, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 8.dp),
        )

        SectionHeader(tr("Bóng đổ", "Shadow"))
        GroupToggle(Pos.Single, Icons.Rounded.Layers, tr("Bóng đổ", "Shadow"),
            tr("Bóng mờ dưới viên thuốc — dễ nhìn thấy trên nền màn hình sáng", "A soft shadow under the capsule — visible on light screens"),
            s.shadow, color = Tile.Indigo) { v ->
            SettingsRepo.update { copy(shadow = v) }
        }

        Text(
            tr("Màu của nút Trả lời và Đánh dấu đã đọc nằm ở Tính năng → Thông báo.", "The color of the Reply and Mark as read buttons is in Features → Notifications."),
            color = Ui.Sub, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
fun Swatches(colors: List<Long>, selected: Long, onPick: (Long) -> Unit) {
    FlowRow(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Ui.Card)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        colors.forEach { c ->
            val color = Color(c)
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, if (c == selected) Ui.Primary else Ui.Divider, CircleShape)
                    .clickable { onPick(c) },
                contentAlignment = Alignment.Center,
            ) {
                if (c == selected) {
                    Icon(Icons.Rounded.Check, null, tint = if (color.luminance() > 0.5f) Color.Black else Color.White)
                }
            }
        }
    }
}
