package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.service.KeepAliveService
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Perms
import com.akira.island.ui.Perms.Vendor
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile

@Composable
fun KeepAliveScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vendor = Perms.vendor()
    val (battery, _) = rememberOnResume { Perms.batteryUnrestricted(context) }
    val s by SettingsRepo.state.collectAsState()

    Page(tr("Giữ Viên thuốc hoạt động ổn định", "Keep the island running"), onBack,
        subtitle = tr("Hệ điều hành của máy có thể dừng Viên thuốc khi chạy ngầm — khi đó Viên thuốc sẽ biến mất cho đến khi bạn bật lại", "Your phone's firmware may stop the island in the background — then it disappears until you turn it on again")) {
        
        GroupToggle(
            Pos.Single, Icons.Rounded.PushPin, tr("Giữ Viên thuốc luôn chạy", "Keep the island loaded"),
            tr("Viên thuốc vẫn chạy ngay cả khi bạn vuốt đóng app trong danh sách gần đây. Android có thể hiện một thông báo yên lặng", "The island stays even if you swipe the app away from recent apps. Android may show a quiet notification"),
            s.keepAlive, color = Tile.Purple,
        ) { v ->
            SettingsRepo.update { copy(keepAlive = v) }
            KeepAliveService.sync(context)
        }

        Group {
            GroupItem(
                if (Perms.hasAutostart()) Pos.First else Pos.Single, Icons.Rounded.BatteryChargingFull,
                tr("Pin không hạn chế", "Unrestricted battery"),
                batteryHint(vendor), onClick = { Perms.openBattery(context) }, color = Tile.Green,
            ) { Status(battery) }
            if (Perms.hasAutostart()) {
                GroupItem(
                    Pos.Last, Icons.Rounded.RocketLaunch, tr("Tự khởi chạy", "Autostart"),
                    tr("Bật quyền cho Capsule VN — để app tự khởi động lại sau khi khởi động máy hoặc bị tắt", "Turn it on for Dynamic Island — then it starts by itself after a reboot or being closed"),
                    onClick = { Perms.openAutostart(context) }, color = Tile.Orange,
                ) { Arrow() }
            }
        }

        SectionHeader(tr("Khóa trong ứng dụng gần đây", "Lock it in recent apps"))
        InfoText(lockHint(vendor))

        SectionHeader(tr("Nếu Viên thuốc vẫn bị biến mất", "If the island still disappears"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.Lock, tr("Hỗ trợ tiếp cận", "Accessibility"),
                tr("Tắt và bật lại dịch vụ Capsule VN", "Turn the Dynamic Island service off and on again"),
                onClick = { Perms.openAccessibility(context) }, color = Tile.Blue,
            ) { Arrow() }
            AppDetailsItem(Pos.Last)
        }
    }
}

@Composable
internal fun KeepAliveItem(pos: Pos, open: (Screen) -> Unit) {
    GroupItem(pos, Icons.Rounded.BatteryChargingFull, tr("Giữ Viên thuốc hoạt động ổn định", "Keep the island running"),
        tr("Pin, tự khởi chạy và khóa trong ứng dụng gần đây", "Battery, autostart and locking in recent apps"),
        onClick = { open(Screen.KEEPALIVE) }, color = Tile.Green) { Arrow() }
}

@Composable
internal fun AppDetailsItem(pos: Pos) {
    val context = LocalContext.current
    GroupItem(pos, Icons.Rounded.Security, tr("Thông tin ứng dụng trong Android", "App info in Android"),
        tr("Cài đặt bị hạn chế, quyền hạn, pin", "Restricted settings, permissions, battery"),
        onClick = { Perms.openAppDetails(context) }, color = Tile.Gray)
}

private fun batteryHint(v: Vendor): String = when (v) {
    Vendor.XIAOMI -> tr("Tiết kiệm pin hoặc “Kiểm soát hoạt động” → “Không hạn chế”", "Battery saver or “Activity control” → “No restrictions”")
    Vendor.SAMSUNG -> tr("Pin → Không hạn chế, và xóa Viên thuốc khỏi danh sách Ứng dụng ở chế độ ngủ", "Battery → Unrestricted, and remove the island from Sleeping apps")
    Vendor.OPPO -> tr("Mức sử dụng pin → Cho phép hoạt động ngầm", "Battery usage → allow background activity")
    else -> tr("Tìm Capsule VN và chọn “Không tối ưu hóa” hoặc “Không hạn chế”", "Find Dynamic Island and choose “Don't optimize” or “Unrestricted”")
}

private fun lockHint(v: Vendor): String = when (v) {
    Vendor.XIAOMI -> tr("Mở ứng dụng gần đây, nhấn giữ thẻ Capsule VN và bấm vào biểu tượng ổ khóa để không bị đóng khi dọn dẹp bộ nhớ.", "Open recent apps, long-press the Dynamic Island card and tap the lock. HyperOS and MIUI don't close a " +
            "locked app, even when you clear recent apps.")
    Vendor.OPPO -> tr("Mở ứng dụng gần đây, bấm vào dấu “⋮” phía trên thẻ Capsule VN (hoặc kéo thẻ xuống) và chọn “Khóa”.", "Open recent apps, tap “⋮” above the Dynamic Island card (or pull it down) and choose “Lock”.")
    Vendor.SAMSUNG -> tr("Mở ứng dụng gần đây, chạm vào biểu tượng ứng dụng phía trên thẻ Capsule VN và chọn “Giữ mở” (Keep open).", "Open recent apps, tap the icon above the Dynamic Island card and choose “Keep open”.")
    else -> tr("Mở ứng dụng gần đây và kéo thẻ Capsule VN xuống hoặc nhấn giữ — biểu tượng ổ khóa sẽ xuất hiện để không bị tắt khi xóa danh sách gần đây.", "Open recent apps and pull the Dynamic Island card down or long-press it — a lock appears. A locked app " +
            "isn't closed when you clear recent apps.")
}
