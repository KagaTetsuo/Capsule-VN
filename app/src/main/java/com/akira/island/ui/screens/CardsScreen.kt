package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.OfflineBolt
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import androidx.compose.material.icons.rounded.Tune
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.IconAction
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile

@Composable
fun CardsScreen(onBack: () -> Unit, onInstruction: () -> Unit, open: (Screen) -> Unit = {}) {
    val s by SettingsRepo.state.collectAsState()
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    Page(tr("Thẻ hoạt động", "Live activity cards"), onBack,
        action = { IconAction(Icons.Rounded.Lightbulb, tr("Hướng dẫn", "Instructions"), onInstruction) }) {
        SectionHeader(tr("Nhạc & cuộc gọi", "Music and calls"))
        Group {
            GroupToggle(Pos.First, Icons.Outlined.PlayCircle, tr("Điều khiển trình phát", "Player controls"),
                tr("Khi đang phát nhạc và trình phát chạy ngầm", "When music is playing and the player is in the background"),
                s.cardMedia, color = Tile.Pink) { set { copy(cardMedia = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.Call, tr("Cuộc gọi", "Calls"),
                tr("Cuộc gọi đến kèm nút trả lời và thời lượng cuộc gọi", "Incoming calls with answer buttons, and the call time"),
                s.cardCall, color = Tile.Green) { set { copy(cardCall = it) } }
            GroupItem(
                Pos.Last,
                Icons.Rounded.Tune,
                tr("Kích thước & bo góc khung thẻ", "Cards size & corner radius"),
                tr("Tùy chỉnh riêng vị trí, kích thước & độ bo góc kèm mô phỏng trực tiếp", "Position, size & corner radius controls with live simulation"),
                color = Tile.Indigo,
                onClick = { open(Screen.MSG_CALL_SIZE) },
            ) { Arrow() }
        }

        SectionHeader(tr("Hẹn giờ & báo thức", "Timers and alarm"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.HourglassEmpty, tr("Hẹn giờ & đồng hồ bấm giờ", "Timer and stopwatch"),
                tr("Hẹn giờ và bấm giờ Đồng hồ chạy ngầm — kể cả khi tắt thông báo của nó", "Clock's timer and stopwatch in the background — even with its notifications off"),
                s.cardTimer, color = Tile.Orange) { set { copy(cardTimer = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.TouchApp, tr("Các nút Đồng hồ", "Clock buttons"),
                tr("Nút tạm dừng và đặt lại trên Viên thuốc sẽ bấm trực tiếp nút của Đồng hồ. Tắt — chỉ mở Đồng hồ", "Pause and reset in the island press Clock's own buttons. Off — they just open Clock"),
                s.clockAutoTap, color = Tile.Orange) { set { copy(clockAutoTap = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.KeyboardVoice, tr("Hẹn giờ từ trợ lý ảo", "Timer from assistant"),
                tr("“Đặt hẹn giờ 5 phút” từ trợ lý ảo hoặc tiện ích có thể chuyển thẳng lên Viên thuốc", "“Set a timer for 5 minutes” from an assistant or widget can go to the island"),
                s.timerFromAssistant, color = Tile.Pink) { set { copy(timerFromAssistant = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.Bedtime, tr("Báo thức", "Alarm"),
                tr("12 giờ trước báo thức — thời gian thức dậy và vòng quay giấc ngủ, như trên iPhone", "12 hours before an alarm — the wake-up time and a sleep dial, like on iPhone"),
                s.cardAlarm, color = Tile.Indigo) { set { copy(cardAlarm = it) } }
        }

        SectionHeader(tr("Hệ thống", "System"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.FlashlightOn, tr("Điều khiển đèn pin", "Flashlight controls"),
                tr("Khi bật đèn pin", "When the flashlight is on"),
                s.cardTorch, color = Tile.Yellow) { set { copy(cardTorch = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.OfflineBolt, tr("Chỉ báo đang sạc", "Charging indicator"),
                tr("Khi thiết bị đang sạc", "When the device is charging"),
                s.cardCharging, color = Tile.Green) { set { copy(cardCharging = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.Downloading, tr("Chỉ báo tiến trình", "Progress indicator"),
                tr("Tải xuống và tải lên trong Google Play, Telegram và các ứng dụng khác", "Downloads and uploads in Google Play, Telegram and other apps"),
                s.cardProgress, color = Tile.Teal) { set { copy(cardProgress = it) } }
        }

        SectionHeader(tr("Trên đường đi", "On the road"))
        GroupToggle(Pos.Single, Icons.Rounded.Navigation, tr("Điều hướng, gọi xe và giao hàng", "Navigation, taxi and delivery"),
            tr("Google Maps, bản đồ điều hướng, gọi xe... — khoảng cách và thời gian trên viên thuốc, chi tiết khi mở rộng", "Google and Yandex Maps, Navigator, 2GIS, Yandex Go, Uber and others — distance and time in the capsule, details when expanded"),
            s.cardLive, color = Tile.Teal) { set { copy(cardLive = it) } }

        SectionHeader(tr("Điều bất ngờ", "Surprises"))
        GroupToggle(Pos.Single, Icons.Rounded.AutoAwesome, tr("Trứng phục sinh (Bí mật)", "Easter eggs"),
            tr("Viên thuốc nhận diện bài hát đặc biệt và biểu diễn hiệu ứng cờ sao rực rỡ theo nhịp điệu. Gợi ý: Hãy mở một ca khúc hào hùng của ca sĩ Tùng Dương!", "The island recognizes a special song and puts on a festive star visualizer to its beat. Hint: Try playing an epic song by Tung Duong!"),
            s.easterEggs, color = Tile.Red) { set { copy(easterEggs = it) } }
    }
}
