package com.akira.island.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.service.SystemBanners
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.IslandSwitch
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import com.akira.island.ui.posOf

@Composable
fun BannersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val seen by SystemBanners.seen.collectAsState()
    val calls = remember(seen) { SystemBanners.callApps(context, seen) }
    val clocks = remember(seen) { SystemBanners.clockApps(context, seen) }
    val s by SettingsRepo.state.collectAsState()
    val popUp by SystemBanners.popUp.collectAsState()
    val messengers = remember(s.msgChannels) {
        s.msgChannels.keys.filter { it != SystemBanners.SYSTEM_UI && it != SystemBanners.ANDROID }
            .sortedBy { SystemBanners.label(context, it).lowercase() }
    }

    Page(tr("Biểu ngữ hệ thống", "System banners"), onBack) {
        Box(
            Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(24.dp)).background(Ui.Card)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                tr("Android tự hiện biểu ngữ (pop-up) của ứng dụng đè lên Viên thuốc và không cho ứng dụng khác chặn. Chạm vào ứng dụng bên dưới để mở trang thông báo của nó:\n• Samsung: bỏ chọn ô “Pop-up thông báo” (giữ “M.hình khóa” và “Huy hiệu”).\n• Máy khác: tắt “Bật lên trên màn hình” / “Hiển thị biểu ngữ”.\nThông báo vẫn nằm trong thanh thông báo, chỉ Viên thuốc hiện trên màn hình.", "Android shows its own pop-up banners on top of the island, and the system doesn't let a third-party app " +
                        "block them. Tap an app and turn off “Pop on screen” (on some phones — “Show on top” or “Banners”). The " +
                        "notification stays in the shade, and only the island shows the event on screen."),
                color = Ui.Sub, fontSize = 15.sp, lineHeight = 20.sp,
            )
        }

        SectionHeader(tr("Ứng dụng nhắn tin", "Messengers"))
        if (messengers.isEmpty()) {
            GroupItem(Pos.Single, Icons.Rounded.ChatBubble, tr("Chưa có tin nhắn nào gửi tới Viên thuốc", "No message has reached the island yet"),
                color = Tile.Gray)
        } else {
            Group {
                messengers.forEachIndexed { i, pkg ->
                    // One UI không cho app khác đọc ô "Pop-up thông báo" -> người dùng tự xác nhận "Đã tắt"
                    val confirmed = pkg in s.popupOffApps
                    val ok = popUp[pkg] == false || confirmed
                    GroupItem(
                        posOf(i, messengers.size), Icons.Rounded.ChatBubble, SystemBanners.label(context, pkg),
                        when {
                            ok -> tr("Không bị hiện thông báo nổi trùng lặp ✓", "Its own banner doesn't pop up ✓")
                            SystemBanners.isSamsung -> tr("Chạm để mở, bỏ chọn “Pop-up thông báo”, rồi gạt “Đã tắt” bên phải", "Tap, untick “Pop-up notifications”, then flip “Done” on the right")
                            popUp[pkg] == true -> tr("Bị hiện đè lên Viên thuốc — chạm vào và tắt “Bật lên trên màn hình”", "Pops up over the island — tap and turn off “Pop on screen”")
                            else -> tr("Chạm vào và tắt “Bật lên trên màn hình”", "Tap and turn off “Pop on screen”")
                        },
                        color = if (ok) Tile.Green else Tile.Blue,
                        onClick = { SystemBanners.openSettings(context, pkg) },
                    ) {
                        if (SystemBanners.isSamsung) {
                            IslandSwitch(confirmed, { v ->
                                SettingsRepo.update { copy(popupOffApps = if (v) popupOffApps + pkg else popupOffApps - pkg) }
                            })
                        } else {
                            Arrow()
                        }
                    }
                }
            }
        }
        Text(
            tr("Nếu bạn tắt thông báo nổi của ứng dụng nhắn tin mà sau đó lại tắt hoặc ẩn Viên thuốc, tin nhắn sẽ chỉ nằm trong thanh thông báo.", "If you turn off a messenger's pop-ups and then turn the island off or hide it, messages will only be in the shade."),
            color = Ui.Sub, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        )

        SectionHeader(tr("Cuộc gọi", "Calls"))
        AppList(calls, Icons.Rounded.Call, Tile.Green,
            tr("Kênh cuộc gọi đến. Viên thuốc sẽ tự sáng màn hình khi có cuộc gọi", "The incoming calls channel. The island turns the screen on for calls itself"),
            tr("Không tìm thấy trình gọi điện — hãy gọi thử để xem hiển thị ở đây", "No dialer found — call yourself and it will show up here")) { SystemBanners.openSettings(context, it) }

        SectionHeader(tr("Hẹn giờ & báo thức", "Timers and alarms"))
        AppList(clocks, Icons.Rounded.Alarm, Tile.Orange,
            tr("Cảnh báo hẹn giờ và báo thức", "Timer and alarm alerts"),
            tr("Không tìm thấy ứng dụng Đồng hồ", "No Clock app found")) { SystemBanners.openSettings(context, it) }
        Text(
            tr("Mẹo: đừng tắt hoàn toàn thông báo của Đồng hồ — hãy chọn “Im lặng” và “Thu nhỏ” (hoặc chỉ tắt “Bật lên trên màn hình”). Khi đó hẹn giờ và bấm giờ sẽ hiện ngay trên Viên thuốc, và các nút điều khiển sẽ hoạt động mà không cần mở Đồng hồ.", "Tip: don't turn off Clock's notifications completely — choose “Silent” and “Minimize” (or turn off only " +
                    "“Pop on screen”). Then the timer and stopwatch show up in the island instantly, and its buttons work " +
                    "without opening Clock. If the notifications are fully off, the island reads the timer from the Clock " +
                    "screen and presses the buttons itself, opening the app for a moment."),
            color = Ui.Sub, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        )

        SectionHeader(tr("Pin & đang sạc", "Battery and charging"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.BatteryAlert, SystemBanners.label(context, SystemBanners.SYSTEM_UI),
                tr("“Pin yếu” — kênh Thông báo Pin. Một số giao diện hệ thống không cho phép sửa đổi", "“Battery low” — the Battery channel. Some firmwares don't let you change it"),
                color = Tile.Red, onClick = { SystemBanners.openSettings(context, SystemBanners.SYSTEM_UI) }) { Arrow() }
            GroupItem(Pos.Last, Icons.Rounded.Usb, SystemBanners.label(context, SystemBanners.ANDROID),
                tr("Sạc USB và các thông báo hệ thống khác", "USB charging and other system messages"),
                color = Tile.Gray, onClick = { SystemBanners.openSettings(context, SystemBanners.ANDROID) }) { Arrow() }
        }
        Text(
            tr("Hiệu ứng sạc pin mặc định do hệ điều hành vẽ (Samsung, Xiaomi...) có thể tắt trong cài đặt màn hình khóa hoặc cài đặt pin của điện thoại, nếu nhà sản xuất cho phép.", "The charging animation drawn by the firmware itself (Xiaomi, Samsung and others) is turned off in the " +
                    "phone's lock screen or battery settings, if the manufacturer allows it."),
            color = Ui.Sub, fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun AppList(
    packages: List<String>,
    icon: ImageVector,
    color: Color,
    hint: String,
    empty: String,
    onOpen: (String) -> Unit,
) {
    val context = LocalContext.current
    if (packages.isEmpty()) {
        GroupItem(Pos.Single, icon, empty, color = Tile.Gray)
        return
    }
    Group {
        packages.forEachIndexed { i, pkg ->
            GroupItem(posOf(i, packages.size), icon, SystemBanners.label(context, pkg), hint, color = color,
                onClick = { onOpen(pkg) }) { Arrow() }
        }
    }
}
