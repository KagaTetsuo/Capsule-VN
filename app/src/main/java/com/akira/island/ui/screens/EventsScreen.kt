package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.AirplanemodeInactive
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.UsbOff
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.Group
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile

@Composable
fun EventsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    Page(tr("Sự kiện hệ thống", "System events"), onBack) {
        SectionHeader(tr("Pin", "Battery"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Bolt, tr("Bắt đầu sạc pin", "Charging started"), null,
                s.evChargeStart, color = Tile.Green) { set { copy(evChargeStart = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.PowerOff, tr("Đã ngắt sạc pin", "Charging stopped"), null,
                s.evChargeStop, color = Tile.Gray) { set { copy(evChargeStop = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.BatteryAlert, tr("Pin yếu", "Low battery"),
                tr("Khi pin giảm xuống 5%", "When the battery drops to 5%"),
                s.evLowBattery, color = Tile.Red) { set { copy(evLowBattery = it) } }
        }

        SectionHeader(tr("Kết nối", "Connections"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Wifi, tr("Đã kết nối Wi-Fi", "Wi-Fi connected"), null,
                s.evWifiOn, color = Tile.Blue) { set { copy(evWifiOn = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.WifiOff, tr("Đã ngắt kết nối Wi-Fi", "Wi-Fi disconnected"), null,
                s.evWifiOff, color = Tile.Gray) { set { copy(evWifiOff = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.BluetoothConnected, tr("Đã kết nối tai nghe", "Headphones connected"),
                tr("Qua Bluetooth, kèm % pin nếu thiết bị hỗ trợ", "Over Bluetooth, with the charge if it's known"),
                s.evHeadsetOn, color = Tile.Indigo) { set { copy(evHeadsetOn = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.BluetoothDisabled, tr("Đã ngắt kết nối tai nghe", "Headphones disconnected"), null,
                s.evHeadsetOff, color = Tile.Gray) { set { copy(evHeadsetOff = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.Usb, tr("Đã kết nối thiết bị USB", "USB device connected"), null,
                s.evUsbOn, color = Tile.Teal) { set { copy(evUsbOn = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.UsbOff, tr("Đã ngắt kết nối thiết bị USB", "USB device disconnected"), null,
                s.evUsbOff, color = Tile.Gray) { set { copy(evUsbOff = it) } }
        }

        SectionHeader(tr("Chế độ", "Modes"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.AirplanemodeActive, tr("Đã bật chế độ máy bay", "Airplane mode on"), null,
                s.evAirplaneOn, color = Tile.Orange) { set { copy(evAirplaneOn = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.AirplanemodeInactive, tr("Đã tắt chế độ máy bay", "Airplane mode off"), null,
                s.evAirplaneOff, color = Tile.Gray) { set { copy(evAirplaneOff = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.DoNotDisturbOn, tr("Không làm phiền", "Do Not Disturb"),
                tr("Mặt trăng và trạng thái khi thay đổi", "A moon and the state when it changes"),
                s.evDnd, color = Tile.Indigo) { set { copy(evDnd = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.Vibration, tr("Chế độ chuông", "Ring mode"),
                tr("Chuông, rung hoặc im lặng", "Ring, vibrate or silent"),
                s.evRinger, color = Tile.Pink) { set { copy(evRinger = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.BrightnessAuto, tr("Chế độ độ sáng", "Brightness mode"),
                tr("Khi bật hoặc tắt độ sáng thích ứng", "When adaptive brightness is turned on or off"),
                s.evBrightness, color = Tile.Yellow) { set { copy(evBrightness = it) } }
        }

        SectionHeader(tr("Thanh toán", "Payments"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Nfc, tr("Chạm vào đầu đọc thẻ", "Hold near reader"),
                tr("Khi màn hình thanh toán chạm xuất hiện (Google Wallet, Samsung Pay...). Giữ nguyên cho đến khi bạn thanh toán hoặc rời app", "When a phone payment is on screen — Mir Pay, T-Pay, SberPay, Yandex Pay and others. Stays until you pay or leave the app"),
                s.evPayReady, color = Tile.Blue) { set { copy(evPayReady = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.AccountBalance, tr("Gợi ý khi mở ứng dụng ngân hàng", "Hint when opening a bank"),
                tr("Hiện chạm vào đầu đọc ngay khi mở ví điện tử/ngân hàng, không chỉ trên màn hình thanh toán", "Hold near reader as soon as you open a bank or wallet app, not only on the payment screen"),
                s.payHintOnOpen, color = Tile.Gray) { set { copy(payHintOnOpen = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.CheckCircle, tr("Đã thanh toán", "Paid"),
                tr("Dấu tích và số tiền — từ màn hình ví, thông báo hoặc tin nhắn SMS ngân hàng", "A check mark and the amount — from the wallet screen, a notification or a bank SMS"),
                s.evPaySuccess, color = Tile.Green) { set { copy(evPaySuccess = it) } }
        }

        SectionHeader(tr("Khác", "Other"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Alarm, tr("Đã đặt báo thức", "Alarm set"),
                tr("Thời gian thức dậy và thời gian bạn có thể ngủ", "Wake-up time and how long you can sleep"),
                s.evAlarmSet, color = Tile.Orange) { set { copy(evAlarmSet = it) } }
            GroupToggle(Pos.Middle, Icons.Rounded.Lock, tr("Khóa trên màn hình khóa", "Lock on the lock screen"),
                tr("Khi điện thoại đang khóa, viên thuốc hiển thị biểu tượng ổ khóa", "While the phone is locked, the capsule shows a lock"),
                s.evLock, color = Tile.Indigo) { set { copy(evLock = it) } }
            GroupToggle(Pos.Last, Icons.Rounded.LockOpen, tr("Mở khóa thiết bị", "Device unlock"),
                tr("Ổ khóa mở ra khi bạn mở khóa điện thoại", "A lock opens when you unlock the phone"),
                s.evUnlock, color = Tile.Purple) { set { copy(evUnlock = it) } }
        }
    }
}
