package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.ViewCarousel
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

    Page(tr("Cử chỉ chạm & vuốt", "Taps & gestures"), onBack,
        subtitle = tr("Khi đang mở rộng: chạm để thu nhỏ, vuốt trái / phải để chuyển giữa các hoạt động đang chạy", "When expanded: tap to collapse, swipe left / right to switch between running activities")) {
        SectionHeader(tr("Chạm", "Taps"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.TouchApp, tr("Chạm một lần", "Single tap"), "${s.tapAction.title} — ${s.tapAction.hint}",
                onClick = { dialog = Which.TAP }, color = Tile.Blue) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.TouchApp, tr("Chạm đúp", "Double tap"),
                "${s.doubleTapAction.title} — ${s.doubleTapAction.hint}" + if (s.doubleTapAction != TapAction.NOTHING) tr(". Lưu ý: bật chạm đúp làm chạm một lần chậm ~0,3 giây", ". Note: double tap makes a single tap ~0.3 s slower") else "",
                onClick = { dialog = Which.DOUBLE }, color = Tile.Indigo) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.PanTool, tr("Nhấn giữ", "Long press"), "${s.longPressAction.title} — ${s.longPressAction.hint}",
                onClick = { dialog = Which.LONG }, color = Tile.Orange) { Arrow() }
            val recommended = s.tapAction == TapAction.EXPAND && s.doubleTapAction == TapAction.NOTHING && s.longPressAction == TapAction.OPEN_APP
            GroupItem(Pos.Last, Icons.Rounded.AutoFixHigh, tr("Dùng cài đặt khuyên dùng", "Use recommended"),
                if (recommended) tr("Đang dùng: Chạm = Mở rộng (tức thì) · Giữ = Mở ứng dụng · Chạm đúp = Tắt", "In use: Tap = Expand (instant) · Hold = Open app · Double tap = Off")
                else tr("Chạm = Mở rộng (tức thì) · Giữ = Mở ứng dụng · Chạm đúp = Tắt", "Tap = Expand (instant) · Hold = Open app · Double tap = Off"),
                color = if (recommended) Tile.Green else Tile.Gray,
                onClick = {
                    SettingsRepo.update { copy(tapAction = TapAction.EXPAND, doubleTapAction = TapAction.NOTHING, longPressAction = TapAction.OPEN_APP) }
                },
            )
        }
        SectionHeader(tr("Vuốt", "Swipes"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.KeyboardArrowDown, tr("Vuốt xuống để mở rộng", "Pull down to expand"),
                tr("Vuốt xuống để mở rộng Viên thuốc, vuốt lên để thu nhỏ", "Pull down to expand the island, swipe up to collapse"),
                s.swipeExpand, color = Tile.Teal) { v ->
                SettingsRepo.update { copy(swipeExpand = v) }
            }
            GroupToggle(Pos.Middle, Icons.Rounded.SwapHoriz, tr("Vuốt ngang chuyển bài", "Swipe to change track"),
                tr("Khi đang phát nhạc (Viên thuốc thu gọn): vuốt trái — bài kế tiếp, vuốt phải — bài trước", "While music plays (compact island): left — next track, right — previous"),
                s.swipeTracks, color = Tile.Purple) { v ->
                SettingsRepo.update { copy(swipeTracks = v) }
            }
            GroupToggle(Pos.Last, Icons.Rounded.ViewCarousel, tr("Vuốt ngang chuyển hoạt động", "Swipe to switch activity"),
                tr("Khi Viên thuốc thu gọn có nhiều hoạt động (hẹn giờ, đèn pin, điều hướng…): vuốt trái / phải để đổi hoạt động hiển thị. Cần Viên thuốc đủ rộng để vuốt; khi đang phát nhạc, vuốt vẫn ưu tiên chuyển bài", "With several activities on the compact island (timer, flashlight, navigation…): swipe left / right to change which one shows. Needs a wide enough island; while music plays, swiping still changes tracks first"),
                s.swipeSwitch, color = Tile.Blue) { v ->
                SettingsRepo.update { copy(swipeSwitch = v) }
            }
        }
    }

    when (dialog) {
        Which.TAP -> ChoiceDialog(tr("Chạm một lần", "Single tap"), TapAction.entries, s.tapAction, { "${it.title} — ${it.hint}" },
            { v -> SettingsRepo.update { copy(tapAction = v) } }) { dialog = null }
        Which.DOUBLE -> ChoiceDialog(tr("Chạm đúp", "Double tap"), TapAction.entries, s.doubleTapAction, { "${it.title} — ${it.hint}" },
            { v -> SettingsRepo.update { copy(doubleTapAction = v) } }) { dialog = null }
        Which.LONG -> ChoiceDialog(tr("Nhấn giữ", "Long press"), TapAction.entries, s.longPressAction, { "${it.title} — ${it.hint}" },
            { v -> SettingsRepo.update { copy(longPressAction = v) } }) { dialog = null }
        null -> Unit
    }
}
