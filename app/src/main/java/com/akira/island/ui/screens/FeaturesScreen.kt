package com.akira.island.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.MarkChatUnread
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Language
import com.akira.island.data.SettingsBackup
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Backup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.widget.Toast
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.R
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.Lang
import com.akira.island.core.Notice
import com.akira.island.core.AppMessage
import com.akira.island.core.NoticeStyle
import com.akira.island.service.NotificationParser
import com.akira.island.service.OtpHelper
import com.akira.island.service.VoiceAnnouncer
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.VolumeUp
import com.akira.island.island.Ios
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.AccentMode
import com.akira.island.data.AppLanguage
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.island.FitText
import com.akira.island.island.onAccent
import com.akira.island.island.resolveAccent
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
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import com.akira.island.data.QuickAction

@Composable
fun FeaturesScreen(open: (Screen) -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var confirmReset by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(SettingsRepo.exportJson().toByteArray()) } != null
        }.getOrDefault(false)
        Toast.makeText(context, if (ok) tr("Đã sao lưu cài đặt", "Settings backed up") else tr("Không lưu được file sao lưu", "Couldn't save the backup"), Toast.LENGTH_SHORT).show()
    }
    val restoreLauncher = rememberLauncherForActivityResult(OpenBackup) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val raw = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } }.getOrNull()
        val ok = raw != null && SettingsRepo.importJson(raw)
        Toast.makeText(context, if (ok) tr("Đã khôi phục cài đặt", "Settings restored") else tr("File không hợp lệ", "Invalid backup file"), Toast.LENGTH_SHORT).show()
    }

    Page(tr("Tính năng", "Features")) {
        FeatureSearch(open)

        SectionHeader(tr("Nội dung hiển thị", "Display content"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.Dashboard, tr("Thẻ hoạt động", "Live activity cards"),
                tr("Nhạc, cuộc gọi, hẹn giờ, đèn pin, điều hướng, tải xuống", "Music, calls, timers, flashlight, navigation, downloads"),
                color = Tile.Purple, onClick = { open(Screen.CARDS) }) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.ChatBubble, tr("Tin nhắn & thông báo", "Messages & notifications"),
                tr("Tin nhắn kèm nút trả lời nhanh ngay trên Viên thuốc", "App messages with replies right in the island"),
                color = Tile.Blue, onClick = { open(Screen.NOTIFICATIONS) }) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.NotificationsActive, tr("Sự kiện hệ thống", "System events"),
                tr("Sạc pin, Wi-Fi, tai nghe, thanh toán, báo thức", "Charging, Wi-Fi, headphones, payments, alarm"),
                color = Tile.Orange, onClick = { open(Screen.EVENTS) }) { Arrow() }
            GroupItem(Pos.Last, Icons.Rounded.LibraryMusic, tr("Lời bài hát (Karaoke)", "Synced lyrics"),
                tr("Lời bài hát đồng bộ trong trình phát mở rộng", "Synced lyrics in the expanded player"),
                color = Tile.Pink, onClick = { open(Screen.LYRICS) }) { Arrow() }
        }

        SectionHeader(tr("Tiện ích thông minh", "Smart utilities"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.Key, tr("Mã OTP 1 chạm", "1-tap OTP"),
                tr("Tự chép và điền mã xác thực từ SMS, ngân hàng, Zalo… chỉ với 1 chạm", "Copy & fill verification codes from SMS, banks, messengers in one tap"),
                color = Tile.Teal, onClick = { open(Screen.OTP_SETTINGS) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle, Icons.Rounded.RecordVoiceOver, tr("Đọc thông báo qua tai nghe", "Read notifications aloud"),
                tr("Đọc to tin nhắn và tên người gọi khi đang đeo tai nghe", "Reads messages and caller names aloud while wearing headphones"),
                color = Tile.Orange, onClick = { open(Screen.TTS_SETTINGS) },
            ) { Arrow() }
            GroupItem(
                Pos.Middle, Icons.Rounded.SportsEsports, tr("Chế độ Gaming & HUD", "Gaming Mode & HUD"),
                tr("Tự nhận diện game, thu gọn thông báo, bảng HUD °C & RAM", "Detects games, compact notifications, °C & RAM HUD"),
                color = Tile.Green, onClick = { open(Screen.GAMING) },
            ) { Arrow() }
            GroupItem(
                Pos.Last, Icons.Rounded.Bolt, tr("Hệ sinh thái Samsung & Android", "Samsung & Android Ecosystem"),
                tr("S Pen, Galaxy Buds/Watch, sạc siêu nhanh, bảo vệ pin, DeX / truyền màn hình", "S Pen, Galaxy Buds/Watch, super fast charging, battery protection, DeX / cast"),
                color = Tile.Blue, onClick = { open(Screen.ECOSYSTEM) },
            ) { Arrow() }
        }

        SectionHeader(tr("Điều khiển & hiển thị", "Controls & display"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.Gesture, tr("Cử chỉ chạm & vuốt", "Taps & gestures"), tr("Chạm và vuốt trên viên thuốc", "Taps and swipes on the capsule"),
                color = Tile.Teal, onClick = { open(Screen.GESTURES) }) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.Tune, tr("Hành vi hiển thị", "Display behavior"),
                tr("Khi nào hiện / ẩn, lịch trình, tự thu nhỏ, rung phản hồi", "When to show / hide, schedule, auto-collapse, haptics"),
                color = Tile.Indigo, onClick = { open(Screen.BEHAVIOR) }) { Arrow() }
            GroupItem(Pos.Last, Icons.Rounded.NotificationsOff, tr("Biểu ngữ hệ thống", "System banners"),
                tr("Tắt pop-up trùng lặp để chỉ Viên thuốc hiển thị cuộc gọi, hẹn giờ, pin", "Turn off duplicate pop-ups so only the island shows calls, timers, battery"),
                color = Tile.Red, onClick = { open(Screen.BANNERS) }) { Arrow() }
        }

        SectionHeader(tr("Hệ thống", "System"))
        Group {
            
            GroupItem(Pos.First, Icons.Rounded.Language, tr("Ngôn ngữ · Language", "Language"), s.language.title,
                color = Tile.Blue, onClick = { languageDialog = true }) { Arrow() }
            if (SettingsBackup.supported) {
                GroupToggle(Pos.Middle, Icons.Rounded.CloudDone, tr("Tự động sao lưu", "Automatic backup"),
                    tr("Luôn lưu cấu hình mới nhất vào Download/CapsuleVN — gỡ app vẫn còn, cài lại chọn file để phục hồi", "Keeps your latest settings in Download/CapsuleVN — survives uninstall, pick it after reinstalling"),
                    s.autoBackup, color = Tile.Blue) { v -> SettingsRepo.update { copy(autoBackup = v) } }
            }
            GroupItem(Pos.Middle, Icons.Rounded.Backup, tr("Sao lưu cài đặt", "Back up settings"),
                tr("Lưu toàn bộ tuỳ chỉnh ra file — dùng khi gỡ / cài lại hoặc đổi máy", "Save all your settings to a file — for reinstalling or a new phone"),
                onClick = { backupLauncher.launch("CapsuleVN-caidat.json") }, color = Tile.Green) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.SettingsBackupRestore, tr("Khôi phục cài đặt", "Restore settings"),
                tr("Chọn file sao lưu để áp dụng lại toàn bộ tuỳ chỉnh", "Pick a backup file to bring all settings back"),
                onClick = { restoreLauncher.launch(BACKUP_TYPES) }, color = Tile.Teal) { Arrow() }
            GroupItem(Pos.Last, Icons.Rounded.Restore, tr("Đặt lại toàn bộ cài đặt", "Reset all settings"),
                tr("Khôi phục về trạng thái như khi mới cài đặt", "Back to how it was after installing"),
                onClick = { confirmReset = true }, color = Tile.Gray)
        }
    }

    if (languageDialog) {
        ChoiceDialog(tr("Ngôn ngữ · Language", "Language"), AppLanguage.entries, s.language, { it.title },
            { v -> SettingsRepo.update { copy(language = v) } }) { languageDialog = false }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Ui.Card,
            title = { Text(tr("Đặt lại cài đặt?", "Reset settings?")) },
            text = {
                Text(tr("Kích thước, màu sắc, hiệu ứng, thẻ và sự kiện sẽ trở về mặc định ban đầu.", "Size, colors, animations, cards and events go back to their defaults."))
            },
            confirmButton = {
                TextButton(onClick = {
                    SettingsRepo.reset()
                    confirmReset = false
                }) { Text(tr("Đặt lại", "Reset"), color = Ui.Primary) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(tr("Hủy", "Cancel"), color = Ui.Sub) } },
        )
    }
}

@Composable
fun BehaviorScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    var collapseDialog by remember { mutableStateOf(false) }

    Page(tr("Hành vi hiển thị", "Display behavior"), onBack) {
        SectionHeader(tr("Khi nào hiển thị", "When to show"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.VisibilityOff, tr("Ẩn viên thuốc khi trống", "Hide the empty capsule"),
                tr("Chỉ hiển thị viên thuốc khi có nội dung hoạt động", "Show the capsule only when it has something in it"),
                s.hideIdle, color = Tile.Gray) { v -> SettingsRepo.update { copy(hideIdle = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.ScreenRotation, tr("Ẩn khi xoay ngang", "Hide in landscape"),
                tr("Tự ẩn khi chơi game hoặc xem video", "Stays out of games and videos"),
                s.hideInLandscape, color = Tile.Gray) { v -> SettingsRepo.update { copy(hideInLandscape = v) } }
            GroupToggle(Pos.Last, Icons.Rounded.Apps, tr("Ẩn trình phát khi đang mở chính ứng dụng phát nhạc đó", "Hide the player in its own app"),
                tr("Thẻ phát nhạc tự ẩn khi ứng dụng nghe nhạc đang mở", "The music card hides while the player itself is open"),
                s.hideWhenPlayerOpen, color = Tile.Blue) { v -> SettingsRepo.update { copy(hideWhenPlayerOpen = v) } }
        }

        SectionHeader(tr("Lịch trình", "Schedule"))
        Group {
            GroupToggle(if (s.scheduleOn) Pos.First else Pos.Single, Icons.Rounded.Schedule, tr("Theo lịch trình", "On a schedule"),
                tr("Viên thuốc chỉ hiển thị từ ${hourLabel(s.scheduleFrom)} đến ${hourLabel(s.scheduleTo)}", "The island shows only from ${hourLabel(s.scheduleFrom)} to ${hourLabel(s.scheduleTo)}"),
                s.scheduleOn, color = Tile.Indigo) { v -> SettingsRepo.update { copy(scheduleOn = v) } }
            if (s.scheduleOn) {
                GroupSlider(Pos.Middle, Icons.Rounded.WbSunny, tr("Từ", "From"), s.scheduleFrom.toFloat(), 0f..23f,
                    hourLabel(s.scheduleFrom), steps = 22, color = Tile.Orange) { v -> SettingsRepo.update { copy(scheduleFrom = v.toInt()) } }
                GroupSlider(Pos.Last, Icons.Rounded.Bedtime, tr("Đến", "To"), s.scheduleTo.toFloat(), 0f..23f,
                    hourLabel(s.scheduleTo), steps = 22, color = Tile.Purple) { v -> SettingsRepo.update { copy(scheduleTo = v.toInt()) } }
            }
        }

        SectionHeader(tr("Khi mở rộng", "When expanded"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.UnfoldLess, tr("Tự thu nhỏ bảng mở rộng", "Auto-collapse the expanded island"), collapseLabel(s.autoCollapseSec),
                color = Tile.Indigo, onClick = { collapseDialog = true })
            GroupToggle(Pos.Last, Icons.Rounded.Circle, tr("Hoạt động thứ hai bên cạnh", "Second activity alongside"),
                tr("Khi có nhiều hoạt động, hoạt động thứ 2 là hình tròn nhỏ cạnh Viên thuốc. Khi mở rộng, vuốt trái / phải để chuyển giữa các hoạt động", "With several activities, the second one is a small circle next to the island. When expanded, swipe left / right to switch between them"),
                s.bubble, color = Tile.Purple) { v ->
                SettingsRepo.update { copy(bubble = v) }
            }
        }

        SectionHeader(tr("Khi chạm vào Viên thuốc trống", "Tapping the empty island"))
        Group {
            GroupToggle(if (s.idleExpand) Pos.First else Pos.Single, Icons.Rounded.UnfoldMore, tr("Mở rộng", "Expand"),
                tr("Đồng hồ, pin và các nút bên dưới. Tắt — chạm vào sẽ không có phản hồi", "Clock, battery and the buttons below. Off — tapping does nothing"),
                s.idleExpand, color = Tile.Blue) { v -> SettingsRepo.update { copy(idleExpand = v) } }
            if (s.idleExpand) {
                QuickAction.entries.forEachIndexed { i, a ->
                    GroupToggle(if (i == QuickAction.entries.lastIndex) Pos.Last else Pos.Middle, a.settingsIcon, a.title,
                        a.hint, a in s.quickActions, color = Tile.Orange) { v ->
                        
                        SettingsRepo.update { copy(quickActions = QuickAction.entries.filter { if (it == a) v else it in quickActions }) }
                    }
                }
            }
        }

        SectionHeader(tr("Rung & màn hình", "Haptics & screen"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.Vibration, tr("Rung phản hồi", "Haptics"),
                tr("Khi mở, đóng và chạm vào viên thuốc", "When opening, closing and tapping the capsule"),
                s.haptics, color = Tile.Pink) { v -> SettingsRepo.update { copy(haptics = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.ChatBubble, tr("Sáng màn hình khi có thông báo", "Wake the screen for alerts"),
                tr("Mặc định tắt: màn hình tắt thì thông báo chỉ hiện trên Màn hình chờ (AOD) của máy, không làm sáng màn hình. Bật nếu muốn màn hình sáng lên hiện trên Viên thuốc rồi tự tắt lại", "Screen off: messages, charging, headphones, payments… light up on the lock screen, then it turns off again. Not while in a pocket / face down"),
                s.wakeOnMessage, color = Tile.Blue) { v -> SettingsRepo.update { copy(wakeOnMessage = v) } }
            if (s.wakeOnMessage) {
                GroupToggle(Pos.Middle, Icons.Rounded.Bedtime, tr("Không sáng trong giờ ngủ", "Not during sleep hours"),
                    tr("Từ ${hourLabel(s.wakeQuietFrom)} đến ${hourLabel(s.wakeQuietTo)} thông báo vẫn hiện nhưng không làm sáng màn hình", "From ${hourLabel(s.wakeQuietFrom)} to ${hourLabel(s.wakeQuietTo)} alerts still arrive but don't wake the screen"),
                    s.wakeQuietOn, color = Tile.Indigo) { v -> SettingsRepo.update { copy(wakeQuietOn = v) } }
                if (s.wakeQuietOn) {
                    GroupSlider(Pos.Middle, Icons.Rounded.Bedtime, tr("Bắt đầu giờ ngủ", "Sleep starts"), s.wakeQuietFrom.toFloat(), 0f..23f,
                        hourLabel(s.wakeQuietFrom), steps = 22, color = Tile.Purple) { v -> SettingsRepo.update { copy(wakeQuietFrom = v.toInt()) } }
                    GroupSlider(Pos.Middle, Icons.Rounded.WbSunny, tr("Kết thúc giờ ngủ", "Sleep ends"), s.wakeQuietTo.toFloat(), 0f..23f,
                        hourLabel(s.wakeQuietTo), steps = 22, color = Tile.Orange) { v -> SettingsRepo.update { copy(wakeQuietTo = v.toInt()) } }
                }
            }
            GroupToggle(Pos.Last, Icons.Rounded.LightMode, tr("Sáng màn hình khi chuông reo", "Wake the screen while ringing"),
                tr("Khi có hẹn giờ, báo thức hoặc cuộc gọi đến, màn hình sẽ bật sáng trong lúc chuông kêu", "When a timer, alarm or incoming call rings, the screen turns on and stays on while it rings"),
                s.wakeForAlerts, color = Tile.Yellow) { v -> SettingsRepo.update { copy(wakeForAlerts = v) } }
        }
    }

    if (collapseDialog) {
        ChoiceDialog(tr("Tự thu nhỏ bảng mở rộng", "Auto-collapse the expanded island"), listOf(0, 5, 10, 20, 30), s.autoCollapseSec, ::collapseLabel,
            { v -> SettingsRepo.update { copy(autoCollapseSec = v) } }) { collapseDialog = false }
    }
}

private fun hourLabel(h: Int) = "%d:00".format(h)

private fun collapseLabel(sec: Int) = if (sec <= 0) tr("Chỉ khi chạm ra ngoài hoặc vuốt lên", "Only by tapping outside or swiping up")
    else tr("Sau $sec giây không chạm", "After $sec seconds without a touch")

private val accentColors = listOf(
    0xFF0A84FF, 0xFFC6C3F7, 0xFF64D2FF, 0xFF34C759, 0xFFFFD60A, 0xFFFF9F0A,
    0xFFFF453A, 0xFFFF2D92, 0xFFBF5AF2, 0xFFFFFFFF,
)

@Composable
fun NotificationsScreen(onBack: () -> Unit, open: (Screen) -> Unit = {}) {
    val s by SettingsRepo.state.collectAsState()
    var accentDialog by remember { mutableStateOf(false) }

    Page(tr("Tin nhắn & thông báo", "Messages & notifications"), onBack) {
        Group {
            GroupToggle(Pos.First, Icons.Rounded.ChatBubble, tr("Thông báo ứng dụng", "App notifications"),
                tr("Tin nhắn từ Telegram, Zalo, Messenger, WhatsApp, email... dưới dạng thẻ trên Viên thuốc, như trên iPhone", "Messages from Telegram, WhatsApp, email and other apps as a card in the island, like on iPhone"),
                s.cardMessages, color = Tile.Blue) { v -> SettingsRepo.update { copy(cardMessages = v) } }
            GroupToggle(Pos.Middle, Icons.AutoMirrored.Rounded.Reply, tr("Các nút trên thông báo", "Notification buttons"),
                tr("Gõ phím trả lời ngay trên Viên thuốc, và Đánh dấu đã đọc", "Reply with typing right in the island, and Mark as read"),
                s.msgActions, color = Tile.Indigo) { v -> SettingsRepo.update { copy(msgActions = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.UnfoldLess, tr("Thu gọn khi app tự hiện pop-up", "Compact when the app pops up"),
                tr("Tin nhắn bị hiện 2 lần mà không tắt được pop-up của app (vd. Samsung): Viên thuốc chỉ báo dạng viên nhỏ, nội dung xem ở pop-up của app", "Messages show twice and the app's pop-up can't be turned off (e.g. Samsung): the island shows a small pill, read it in the app's pop-up"),
                s.msgCompactWhenPopup, color = Tile.Orange) { v -> SettingsRepo.update { copy(msgCompactWhenPopup = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.Smartphone, tr("Từ ứng dụng đang mở", "From the open app"),
                tr("Hiển thị cả thông báo từ ứng dụng đang mở trên màn hình", "Also show notifications from the app that's on screen"),
                s.msgInApp, color = Tile.Teal) { v ->
                SettingsRepo.update { copy(msgInApp = v) }
            }
            GroupToggle(Pos.Middle, Icons.Rounded.MarkChatUnread, tr("Giữ tin chưa đọc trên Viên thuốc", "Keep unread messages"),
                tr("Hết thời gian hiển thị, tin vẫn được giữ lại: chấm nhỏ báo hiệu, chạm Viên thuốc để xem lại từng cuộc trò chuyện", "After the card hides, a small dot stays; tap the island to read each conversation again"),
                s.unreadKeep, color = Tile.Indigo) { v -> SettingsRepo.update { copy(unreadKeep = v) } }
            GroupToggle(Pos.Middle, Icons.Rounded.PhoneInTalk, tr("Chỉ một thông báo cuộc gọi", "One call alert only"),
                tr("App gọi điện (Điện thoại, Zalo…) còn tự hiện pop-up thì Viên thuốc không hiện thêm thẻ. Muốn chỉ dùng Viên thuốc: tắt pop-up cuộc gọi của app đó ở Biểu ngữ hệ thống", "If the calling app still shows its own pop-up, the island stays out of the way. To use only the island, turn that app's pop-up off in System banners"),
                s.callDedupe, color = Tile.Teal) { v -> SettingsRepo.update { copy(callDedupe = v) } }
            GroupToggle(Pos.Last, Icons.Rounded.Call, tr("Ẩn thẻ cuộc gọi khi đang ở màn hình gọi", "Hide call card on the call screen"),
                tr("Đang mở sẵn màn hình cuộc gọi của máy / Zalo / Messenger… thì không hiện thêm thẻ cuộc gọi đến cho khỏi trùng", "When the phone / app call screen is already open, don't show a second incoming-call card"),
                s.hideCallInCallApp, color = Tile.Green) { v -> SettingsRepo.update { copy(hideCallInCallApp = v) } }
        }

        SectionHeader(tr("Màu nút bấm", "Button color"))
        AccentPreview(s)
        GroupItem(Pos.Single, Icons.Rounded.Palette, tr("Màu nút bấm", "Button color"), "${s.accentMode.title} — ${s.accentMode.hint}",
            onClick = { accentDialog = true }, color = Tile.Orange)
        if (s.accentMode == AccentMode.CUSTOM) {
            Swatches(accentColors, s.accentColor) { c -> SettingsRepo.update { copy(accentColor = c) } }
        }
    }

    if (accentDialog) {
        ChoiceDialog(tr("Màu nút bấm", "Button color"), AccentMode.entries, s.accentMode, { "${it.title} — ${it.hint}" },
            { v -> SettingsRepo.update { copy(accentMode = v) } }) { accentDialog = false }
    }
}

@Composable
private fun AccentPreview(s: IslandSettings) {
    val context = LocalContext.current
    
    val accent = resolveAccent(context, s, Tints.Orange)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.Black).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PreviewTile(tr("Trả lời", "Reply"), accent, Modifier.weight(1f))
        PreviewTile(tr("Đánh dấu đã đọc", "Mark as read"), accent, Modifier.weight(2f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.ArrowUpward, null, tint = onAccent(accent), modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun PreviewTile(title: String, accent: Color, modifier: Modifier) {
    Box(
        modifier.height(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        FitText(title, accent, 14.sp, 10.sp, FontWeight.SemiBold)
    }
}

private val QuickAction.settingsIcon: ImageVector
    get() = when (this) {
        QuickAction.TIMER -> Icons.Rounded.HourglassBottom
        QuickAction.STOPWATCH -> Icons.Rounded.Timer
        QuickAction.FLASHLIGHT -> Icons.Rounded.FlashlightOn
        QuickAction.SCREENSHOT -> Icons.Rounded.Screenshot
        QuickAction.CAMERA -> Icons.Rounded.PhotoCamera
        QuickAction.LOCK -> Icons.Rounded.Lock
    }

private val QuickAction.hint: String?
    get() = when (this) {
        QuickAction.SCREENSHOT -> tr("Viên thuốc tự ẩn tạm thời để không bị dính vào ảnh chụp màn hình (Android 9+)", "The island hides for a moment, so it's not in the shot (Android 9+)")
        QuickAction.LOCK -> tr("Tắt màn hình tương tự phím nguồn (Android 9+)", "Turns the screen off like the power button (Android 9+)")
        else -> null
    }

@Composable
fun GamingScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current
    val gaming by IslandBus.gamingActive.collectAsState()
    val bat by IslandBus.battery.collectAsState()
    val ramPct by IslandBus.ramPercent.collectAsState()

    Page(tr("Chế độ Gaming & HUD", "Gaming Mode & HUD"), onBack) {
        SectionHeader(tr("Tương thích mọi dòng máy Android", "Universal Android Gaming"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.SportsEsports, tr("Chế độ Gaming", "Gaming Mode"),
                tr("Tự động chống che màn hình và bật HUD khi mở bất kỳ tựa game nào (Samsung, Xiaomi, OPPO, Vivo, Pixel, ROG…)", "Automatically activates anti-block popups and HUD when launching any game on any Android device"),
                s.gameModeEnabled, color = Tile.Green,
            ) { v -> SettingsRepo.update { copy(gameModeEnabled = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.NotificationsOff, tr("Thu gọn thông báo khi đang chơi game", "Compact notifications in games"),
                tr("Chuyển thẻ tin nhắn lớn thành viên thuốc mini gọn gàng để không bao giờ che bản đồ nhỏ (minimap) hay nút bấm", "Converts large message banners into a compact non-intrusive pill so game controls & minimap are never blocked"),
                s.gameSuppressPopups, color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(gameSuppressPopups = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.Memory, tr("Bảng theo dõi Gaming HUD (°C & RAM)", "Gaming HUD (°C & RAM)"),
                tr("Hiển thị nhiệt độ máy/pin (°C) và mức RAM đang dùng theo thời gian thực khi mở rộng Viên thuốc", "Shows real-time battery temperature (°C) and RAM usage inside the expanded capsule"),
                s.gameShowHud, color = Tile.Orange,
            ) { v -> SettingsRepo.update { copy(gameShowHud = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.ScreenRotation, tr("Giữ hiển thị trong game xoay ngang", "Keep visible in landscape games"),
                tr("Giữ viên thuốc gọn & Gaming HUD trong game màn hình ngang ngay cả khi bật Ẩn khi xoay ngang", "Keep the compact capsule & gaming HUD active in landscape games even when Hide in landscape is on"),
                s.gameBypassLandscapeHide, color = Tile.Indigo,
            ) { v -> SettingsRepo.update { copy(gameBypassLandscapeHide = v) } }
            GroupToggle(
                Pos.Last, Icons.Rounded.PlayArrow, tr("Bật cưỡng bức Chế độ Gaming ngay", "Force Gaming Mode On"),
                if (gaming) tr("Đang kích hoạt · ${if (bat.tempTenths > 0) "%.1f°C · ".format(bat.tempC) else ""}RAM ${ramPct.coerceAtLeast(42)}%",
                    "Currently active · ${if (bat.tempTenths > 0) "%.1f°C · ".format(bat.tempC) else ""}RAM ${ramPct.coerceAtLeast(42)}%")
                else tr("Bật thủ công Chế độ Gaming mọi lúc cho bất kỳ ứng dụng nào", "Manually enable Gaming Mode for any app"),
                s.gameForceOn, color = Tile.Purple,
            ) { v -> SettingsRepo.update { copy(gameForceOn = v) } }
        }

        SectionHeader(tr("Kiểm tra thử", "Test"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.SportsEsports, tr("Thử thông báo thu gọn trong Game", "Test compact gaming notification"),
                tr("Xem thử tin nhắn Zalo/Messenger hiển thị gọn như thế nào khi đang chiến game", "Preview how a message looks while gaming"),
                color = Tile.Green,
                onClick = {
                    IslandBus.post(
                        Notice(
                            title = "Zalo · Nguyễn Lê Lý Huỳnh",
                            state = tr("Vào trận thôi! Đang đợi ở sảnh…", "Vào trận thôi! Đang đợi ở sảnh…"),
                            icon = Ios.load(context, R.drawable.ios_chatbubble_ellipses),
                            tint = Tints.Blue,
                            style = NoticeStyle.PILL,
                            kindHint = com.akira.island.data.NoticeKind.COMPACT,
                        ),
                    )
                },
            )
            GroupItem(
                Pos.Last, Icons.Rounded.Memory, tr("Mở bảng Gaming HUD trên Viên thuốc", "Open Gaming HUD on capsule"),
                tr("Mở rộng Viên thuốc ngay để xem nhiệt độ °C và bộ nhớ RAM thực tế", "Expand capsule to view live °C temperature and RAM"),
                color = Tile.Orange,
                onClick = {
                    SettingsRepo.update { copy(gameShowHud = true, idleExpand = true) }
                    Controls.expand("idle")
                },
            )
        }
    }
}

@Composable
fun EcosystemScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current
    var protectDialog by remember { mutableStateOf(false) }

    Page(tr("Hệ sinh thái Samsung & Android", "Samsung & Android Ecosystem"), onBack) {
        SectionHeader(tr("Tích hợp thiết bị & phần cứng", "Device Integration"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.Edit, tr("Bút cảm ứng Samsung S Pen", "Samsung S Pen Stylus"),
                tr("Hiệu ứng Viên thuốc khi rút bút hoặc cất bút S Pen (dòng Galaxy S Ultra, Note, Z Fold, Tab)", "Capsule animation when removing or inserting the S Pen stylus (Galaxy S Ultra, Note, Fold, Tab)"),
                s.ecoSpPen, color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(ecoSpPen = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.Headphones, tr("Galaxy Buds / Watch & Thiết bị đeo Bluetooth", "Galaxy Buds / Watch & Bluetooth Wearables"),
                tr("Hiển thị tên thiết bị và vòng % pin cho tai nghe TWS & đồng hồ thông minh trên mọi máy Android", "Shows model name and battery ring for TWS earbuds and smartwatches across all Android brands"),
                s.ecoBudsWatch, color = Tile.Blue,
            ) { v -> SettingsRepo.update { copy(ecoBudsWatch = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.Bolt, tr("Sạc siêu nhanh & Nhiệt độ Pin (°C)", "Super Fast Charging & Battery Temp °C"),
                tr("Phân biệt Sạc nhanh / Sạc siêu nhanh 2.0 và hiển thị nhiệt độ pin (°C) trực tiếp", "Detects Fast / Super Fast Charging 2.0 and displays live battery temperature in °C"),
                s.ecoFastCharge, color = Tile.Green,
            ) { v -> SettingsRepo.update { copy(ecoFastCharge = v) } }
            GroupItem(
                Pos.Middle, Icons.Rounded.Shield, tr("Cảnh báo Bảo vệ Pin chống chai", "Battery Protection Alert"),
                if (s.ecoBatteryProtectLevel <= 0) tr("Tắt", "Off")
                else tr("Báo nhắc khi sạc đạt mức ${s.ecoBatteryProtectLevel}%", "Alert when charging reaches ${s.ecoBatteryProtectLevel}%"),
                color = Tile.Orange, onClick = { protectDialog = true },
            )
            GroupToggle(
                Pos.Last, Icons.Rounded.Cast, tr("Samsung DeX / Smart View / Cast", "Samsung DeX / Smart View / Cast"),
                tr("Báo trên Viên thuốc khi kết nối hoặc ngắt màn hình rời HDMI, TV không dây hoặc Samsung DeX", "Capsule alert when connecting or disconnecting an external display, TV Cast, or Samsung DeX"),
                s.ecoDexCast, color = Tile.Purple,
            ) { v -> SettingsRepo.update { copy(ecoDexCast = v) } }
        }

        SectionHeader(tr("Mô phỏng kiểm tra trực tiếp", "Live Feature Simulation"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.Edit, tr("Thử rút bút S Pen", "Test S Pen (Removed)"),
                tr("Mô phỏng hiệu ứng khi rút bút S Pen khỏi máy", "Simulate pulling out the S Pen stylus"),
                color = Tile.Teal,
                onClick = {
                    IslandBus.post(
                        Notice(
                            title = tr("Bút S Pen", "S Pen Stylus"),
                            state = tr("Đã rút bút", "Removed"),
                            icon = Ios.load(context, R.drawable.ios_hardware_chip),
                            tint = Tints.Teal,
                        ),
                    )
                },
            )
            GroupItem(
                Pos.Middle, Icons.Rounded.Bolt, tr("Thử Sạc siêu nhanh (Super Fast Charging)", "Test Super Fast Charging"),
                tr("Mô phỏng thông báo Sạc siêu nhanh 2.0 trên Viên thuốc", "Simulate Super Fast Charging 2.0 alert"),
                color = Tile.Green,
                onClick = {
                    val lv = IslandBus.battery.value.level.takeIf { it >= 0 } ?: 78
                    IslandBus.post(
                        Notice(
                            title = tr("Sạc siêu nhanh", "Super Fast Charging"),
                            state = "$lv%",
                            icon = Ios.load(context, R.drawable.ios_flash),
                            tint = Tints.Teal,
                            battery = lv,
                        ),
                    )
                },
            )
            GroupItem(
                Pos.Middle, Icons.Rounded.Headphones, tr("Thử kết nối Galaxy Buds3 Pro / Watch", "Test Galaxy Buds / Watch"),
                tr("Mô phỏng kết nối tai nghe Galaxy Buds3 Pro kèm vòng pin 92%", "Simulate connecting Galaxy Buds3 Pro with 92% battery ring"),
                color = Tile.Blue,
                onClick = {
                    IslandBus.post(
                        Notice(
                            title = "Galaxy Buds3 Pro",
                            caption = tr("Đã kết nối", "Connected"),
                            icon = Ios.load(context, R.drawable.ios_headset),
                            tint = Tints.Green,
                            ring = 0.92f,
                        ),
                    )
                },
            )
            GroupItem(
                Pos.Last, Icons.Rounded.Cast, tr("Thử kết nối Samsung DeX / Màn hình rời", "Test Samsung DeX / Smart View"),
                tr("Mô phỏng kết nối không dây Samsung DeX / Smart View", "Simulate connecting Samsung DeX / external monitor"),
                color = Tile.Purple,
                onClick = {
                    IslandBus.post(
                        Notice(
                            title = "Samsung DeX",
                            state = tr("Đã kết nối", "Connected"),
                            icon = Ios.load(context, R.drawable.ios_hardware_chip),
                            tint = Tints.Blue,
                        ),
                    )
                },
            )
        }
    }

    if (protectDialog) {
        val options = listOf(0, 80, 85, 90, 95, 100)
        ChoiceDialog(
            tr("Cảnh báo Bảo vệ Pin chống chai", "Battery Protection Alert"),
            options,
            s.ecoBatteryProtectLevel,
            { lv -> if (lv <= 0) tr("Tắt", "Off") else "$lv%" },
            { v -> SettingsRepo.update { copy(ecoBatteryProtectLevel = v) } },
        ) { protectDialog = false }
    }
}

@Composable
fun OtpSettingsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current

    Page(tr("Mã OTP 1 chạm", "1-tap OTP"), onBack) {
        SectionHeader(tr("Tùy chỉnh nhận diện & điền mã OTP", "OTP Settings"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.Key, tr("Nhận diện mã xác thực OTP thông minh", "Detect OTP verification codes"),
                tr("Tự động phát hiện mã xác thực 4–8 chữ số trong SMS, ngân hàng, Zalo, Telegram, Gmail và hiện nút 1 chạm trên Viên thuốc", "Detect 4–8 digit verification codes in SMS, banking apps, Zalo, Telegram & email and show a 1-tap button on the capsule"),
                s.otpEnabled, color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(otpEnabled = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.ContentCopy, tr("Tự động chép OTP ngay khi tin nhắn tới", "Auto-copy OTP on arrival"),
                tr("Tự động sao chép mã OTP vào bộ nhớ tạm (Clipboard) ngay khi nhận thông báo mà không cần chạm", "Automatically copy the OTP code to clipboard as soon as the message arrives without tapping"),
                s.otpAutoCopy, color = Tile.Blue,
            ) { v -> SettingsRepo.update { copy(otpAutoCopy = v) } }
            GroupToggle(
                Pos.Last, Icons.Rounded.Password, tr("Tự động điền OTP vào ô nhập liệu (1 chạm)", "1-Tap Auto-fill into input box"),
                tr("Khi chạm nút OTP trên Viên thuốc, tự động điền thẳng mã OTP vào ô nhập văn bản đang mở trên màn hình", "When tapping the OTP button on the capsule, automatically fill the code directly into the active input field on screen"),
                s.otpAutoFill, color = Tile.Green,
            ) { v -> SettingsRepo.update { copy(otpAutoFill = v) } }
        }

        SectionHeader(tr("Kiểm tra thử", "Test"))
        Group {
            GroupItem(
                Pos.Single, Icons.Rounded.Key, tr("Thử nhận tin nhắn chứa mã OTP (482910)", "Test OTP message card (482910)"),
                tr("Hiển thị thẻ tin nhắn mô phỏng kèm nút Chép & Điền OTP: 482910", "Show a simulated message with the 1-tap Copy & Fill OTP: 482910 button"),
                color = Tile.Teal,
                onClick = {
                    SettingsRepo.update { copy(otpEnabled = true) }
                    if (s.otpAutoCopy) OtpHelper.copyToClipboard(context, "482910")
                    val msg = AppMessage(
                        key = "test-otp",
                        pkg = context.packageName,
                        appName = "Vietcombank · OTP",
                        text = tr("Mã xác thực OTP giao dịch của Quý khách là 482910. Tuyệt đối không chia sẻ mã này.", "Mã xác thực OTP giao dịch của Quý khách là 482910. Tuyệt đối không chia sẻ mã này."),
                        avatar = null,
                        appIcon = NotificationParser.icon(context, context.packageName),
                        intent = null,
                        otpCode = "482910",
                    )
                    IslandBus.post(
                        Notice(
                            title = "Vietcombank",
                            icon = Ios.load(context, R.drawable.ios_chatbubble_ellipses),
                            tint = Tints.Green,
                            style = NoticeStyle.MESSAGE,
                            message = msg,
                            extraMs = 4_000,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
fun TtsSettingsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current

    Page(tr("Đọc thông báo qua tai nghe", "Read notifications aloud"), onBack) {
        SectionHeader(tr("Tùy chỉnh đọc thông báo bằng giọng nói", "Voice Announcer Settings"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.RecordVoiceOver, tr("Bật tự động đọc to thông báo", "Enable Voice Announcer"),
                tr("Tự động đọc to thông báo tin nhắn và cuộc gọi đến bằng giọng đọc Tiếng Việt của hệ thống Android", "Automatically read incoming messages and calls aloud using Android Text-To-Speech (Vietnamese supported)"),
                s.ttsEnabled, color = Tile.Orange,
            ) { v ->
                SettingsRepo.update { copy(ttsEnabled = v) }
                if (!v) VoiceAnnouncer.stop()
            }
            GroupToggle(
                Pos.Middle, Icons.Rounded.Headphones, tr("Chỉ đọc khi đang đeo tai nghe", "Only when wearing headphones"),
                tr("Chỉ đọc to khi đang kết nối tai nghe Bluetooth (Galaxy Buds, AirPods…) hoặc tai nghe dây — rất tiện và riêng tư khi đang lái xe", "Only read aloud when Bluetooth earbuds (Galaxy Buds, AirPods…) or wired headphones are connected — ideal while driving"),
                s.ttsOnlyHeadset, color = Tile.Blue,
            ) { v -> SettingsRepo.update { copy(ttsOnlyHeadset = v) } }
            GroupToggle(
                Pos.Middle, Icons.Rounded.ChatBubble, tr("Đọc cả nội dung chi tiết tin nhắn", "Read full message content"),
                tr("Bật — đọc tên người gửi kèm nội dung tin nhắn. Tắt — chỉ đọc tên ứng dụng và tên người gửi để bảo mật", "On — reads sender name and message body. Off — announces only the app and sender name for privacy"),
                s.ttsReadContent, color = Tile.Teal,
            ) { v -> SettingsRepo.update { copy(ttsReadContent = v) } }
            GroupToggle(
                Pos.Last, Icons.Rounded.PhoneInTalk, tr("Đọc tên người gọi khi có cuộc gọi đến", "Announce incoming calls"),
                tr("Đọc to tên người gọi (trong danh bạ) hoặc số điện thoại khi có cuộc gọi đến", "Speak the caller's name or number aloud on incoming calls"),
                s.ttsReadCalls, color = Tile.Green,
            ) { v -> SettingsRepo.update { copy(ttsReadCalls = v) } }
        }

        SectionHeader(tr("Kiểm tra thử", "Test"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.VolumeUp, tr("Nghe thử giọng đọc thông báo ngay", "Test Voice Announcer now"),
                tr("Phát thử ngay một câu đọc thông báo Tiếng Việt (kể cả khi chưa đeo tai nghe)", "Play a sample Vietnamese voice announcement right now (works even without headphones)"),
                color = Tile.Orange,
                onClick = { VoiceAnnouncer.testSpeak(context) },
            )
            GroupItem(
                Pos.Last, Icons.Rounded.NotificationsOff, tr("Dừng đọc ngay", "Stop speaking"),
                tr("Ngắt giọng đọc thông báo đang phát", "Stop current voice announcement"),
                color = Tile.Gray,
                onClick = { VoiceAnnouncer.stop() },
            )
        }
    }
}

/** Chọn file sao lưu, mở sẵn thư mục Download/CapsuleVN. */
internal object OpenBackup : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: android.content.Context, input: Array<String>): android.content.Intent =
        super.createIntent(context, input).apply {
            SettingsBackup.initialFolder?.let { putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, it) }
        }
}

internal val BACKUP_TYPES = arrayOf("application/json", "text/plain", "application/octet-stream")
