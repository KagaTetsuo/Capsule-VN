package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.data.TapAction
import com.akira.island.ui.ChoiceDialog
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Tile
import com.akira.island.ui.SectionHeader

private enum class Which { TAP, DOUBLE, LONG }

@Composable
fun GesturesScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var dialog by remember { mutableStateOf<Which?>(null) }

    Page(tr("Cử chỉ", "Gestures"), onBack,
        subtitle = tr("Viên thuốc mở rộng sẽ thu nhỏ lại khi chạm vào hoặc chạm ra ngoài", "The expanded island collapses with a tap or a touch outside it")) {
        SectionHeader(tr("Chạm", "Taps"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.TouchApp, tr("Chạm một lần", "Single tap"), s.tapAction.title,
                onClick = { dialog = Which.TAP }, color = Tile.Blue)
            GroupItem(Pos.Middle, Icons.Rounded.TouchApp, tr("Chạm đúp", "Double tap"), s.doubleTapAction.title,
                onClick = { dialog = Which.DOUBLE }, color = Tile.Indigo)
            GroupItem(Pos.Last, Icons.Rounded.PanTool, tr("Nhấn giữ", "Long press"), s.longPressAction.title,
                onClick = { dialog = Which.LONG }, color = Tile.Orange)
        }
        SectionHeader(tr("Vuốt", "Swipes"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.KeyboardArrowDown, tr("Kéo xuống", "Pull down"),
                tr("Vuốt xuống mở rộng viên thuốc, vuốt lên — thu nhỏ", "Expand the capsule, up — collapse"),
                s.swipeExpand, color = Tile.Teal) { v ->
                SettingsRepo.update { copy(swipeExpand = v) }
            }
            GroupToggle(Pos.Last, Icons.Rounded.SwapHoriz, tr("Đẩy sang bên", "Push sideways"),
                tr("Trái — bài kế tiếp, phải — bài trước", "Left — next track, right — previous"),
                s.swipeTracks, color = Tile.Purple) { v ->
                SettingsRepo.update { copy(swipeTracks = v) }
            }
        }
    }

    when (dialog) {
        Which.TAP -> ChoiceDialog(tr("Chạm một lần", "Single tap"), TapAction.entries, s.tapAction, { it.title },
            { v -> SettingsRepo.update { copy(tapAction = v) } }) { dialog = null }
        Which.DOUBLE -> ChoiceDialog(tr("Chạm đúp", "Double tap"), TapAction.entries, s.doubleTapAction, { it.title },
            { v -> SettingsRepo.update { copy(doubleTapAction = v) } }) { dialog = null }
        Which.LONG -> ChoiceDialog(tr("Nhấn giữ", "Long press"), TapAction.entries, s.longPressAction, { it.title },
            { v -> SettingsRepo.update { copy(longPressAction = v) } }) { dialog = null }
        null -> Unit
    }
}
