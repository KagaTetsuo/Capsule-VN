package com.akira.island.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PersistableBundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.R
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.service.LiveDebug
import com.akira.island.service.PayDebug
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.NotesDialog
import com.akira.island.ui.Page
import com.akira.island.ui.Perms
import com.akira.island.ui.PillAction
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import com.akira.island.ui.UpdateSection
import com.akira.island.ui.updateStatusText
import com.akira.island.update.UpdateChecker
import com.akira.island.update.UpdateState
import kotlinx.coroutines.launch

@Composable
fun InstructionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Page(tr("Hướng dẫn", "Instructions"), onBack) {
        Step(1, tr("Hỗ trợ tiếp cận", "Accessibility"),
            tr("Mở Cài đặt → Hỗ trợ tiếp cận → Ứng dụng đã cài đặt → Capsule VN và gạt bật dịch vụ. Chỉ có dịch vụ này mới có quyền vẽ lớp phủ lên thanh trạng thái, ngay cạnh camera.", "Open Settings → Accessibility → Installed apps → Dynamic Island and turn the service on. Only such a " +
                    "service can draw over the status bar, next to the camera."))
        Step(2, tr("Nếu công tắc bị mờ (Android 13+)", "If the switch is grayed out (Android 13+)"), restrictedHelp())
        Row { PillAction(tr("Mở Thông tin ứng dụng", "Open App info")) { Perms.openAppDetails(context) } }
        Step(3, tr("Quyền truy cập thông báo", "Notification access"),
            tr("Cần thiết cho thẻ âm nhạc, tiến trình tải xuống và đồng hồ hẹn giờ/bấm giờ. Viên thuốc hoàn toàn không lưu trữ và không gửi thông báo của bạn đi bất cứ đâu.", "Needed for the music, download progress and Google Clock timer and stopwatch cards. The island doesn't " +
                    "store or send notifications anywhere."))
        Row { PillAction(tr("Mở Quyền truy cập thông báo", "Open notification access")) { Perms.openNotifications(context) } }
        Step(4, tr("Cuộc gọi", "Calls"),
            tr("Tại tab Trang chủ, mở Quyền truy cập → Cuộc gọi và cấp quyền điện thoại, nhật ký cuộc gọi và danh bạ. Khi đó Viên thuốc sẽ hiển thị cuộc gọi đến từ bất kỳ trình gọi điện nào, hiện tên người gọi từ danh bạ, và các nút nhận/từ chối cuộc gọi sẽ hoạt động.", "On the Home tab open Permissions → Calls and allow access to the phone, call log and contacts. Then the " +
                    "island shows incoming calls with any dialer, shows the name from your contacts, and the answer and " +
                    "decline buttons work."))
        Step(5, tr("Đặt viên thuốc khớp vị trí camera", "Place the capsule over the camera"),
            tr("Tại tab Giao diện → Vị trí & kích thước. Trong khi màn hình này đang mở, viên thuốc sẽ có màu đỏ: hãy di chuyển thanh trượt sao cho vùng đỏ nằm vừa khít quanh camera, chọn chiều rộng/cao phù hợp rồi bấm “Lưu”.", "Appearance tab → Position and size. While the screen is open, the island is red: move it until the red sits " +
                    "exactly over the camera, pick the width and height and tap “Save”."))
        Step(6, tr("Pin (Samsung, Xiaomi, Huawei…)", "Battery (Xiaomi, Huawei, Samsung…)"),
            tr("Nếu Viên thuốc bị tự động biến mất, hãy cho phép ứng dụng tự khởi chạy và chuyển chế độ pin sang Không hạn chế — nếu không hệ điều hành Samsung One UI sẽ tắt dịch vụ chạy ngầm.", "If the island disappears, allow the app to autostart and remove battery restrictions — otherwise the " +
                    "firmware kills the service."))
        Row {
            PillAction(tr("Pin", "Battery")) { Perms.openBattery(context) }
            if (Perms.hasAutostart()) {
                Spacer(Modifier.width(8.dp))
                PillAction(tr("Tự khởi chạy", "Autostart")) { Perms.openAutostart(context) }
            }
        }
        Step(7, tr("Trả lời & thanh toán", "Replies and payments"),
            tr("Bên dưới thông báo tin nhắn sẽ có các nút tương tác nhanh. “Trả lời” sẽ mở ô nhập văn bản ngay trên Viên thuốc. Khi mở ứng dụng ví/ngân hàng, Viên thuốc sẽ hiện “Chạm vào đầu đọc”, và sau khi quẹt thẻ sẽ hiện dấu tích xanh kèm số tiền. Bạn có thể thử tất cả tại: Trang chủ → Thử nghiệm.", "Under a messenger notification you get the notification's own buttons. “Reply” opens an input field right" +
                    " in the island. When a payment app shows its phone payment screen, the island shows “Hold near reader”, " +
                    "and after paying — a check mark with the amount. You can try all of this: Home → Try it."))
        Step(8, tr("Điều hướng & Bản đồ", "Navigation"),
            tr("Khi ứng dụng điều hướng mở (Google Maps và các bản đồ khác), Viên thuốc sẽ nhận diện khoảng cách đến ngã rẽ, tên đường và thời gian đến nơi từ màn hình, đồng thời nhận diện mũi tên chỉ hướng. Viên thuốc không đọc màn hình của các ứng dụng khác. Nếu Viên thuốc biến mất sau khi cập nhật, hãy tắt và bật lại trong Hỗ trợ tiếp cận.", "While a navigation app is open (2GIS, Yandex Navigator, Maps and others), the island reads the distance " +
                    "to the next turn, the street and the arrival time from its screen, and recognizes the turn arrow from the" +
                    " picture. Other apps' screens aren't read. If the island disappears after an update, turn it off and on " +
                    "again in Accessibility."))
        Step(9, tr("Cử chỉ", "Gestures"),
            tr("Chạm — mở rộng, kéo xuống — mở rộng, vuốt lên hoặc chạm ra ngoài Viên thuốc — thu nhỏ, đẩy sang bên — bài hát kế tiếp/trước đó, chạm vào bong bóng nhỏ bên cạnh — mở rộng hoạt động thứ hai.", "Tap — expand, pull down — expand, pull up or touch outside the island — collapse, push sideways — next or" +
                    " previous track, tap the bubble — expand the second activity."))
    }
}

@Composable
private fun Step(n: Int, title: String, text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Ui.Card).padding(18.dp),
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Ui.Primary), contentAlignment = Alignment.Center) {
            Text("$n", color = Ui.OnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Ui.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(4.dp))
            Text(text, color = Ui.Sub, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

private val GithubPurple = Color(0xFF8250DF)

private fun openGithub(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${UpdateChecker.REPO}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

@Composable
fun AppInfoScreen(open: (Screen) -> Unit) {
    val context = LocalContext.current
    val s by SettingsRepo.state.collectAsState()
    val update by UpdateChecker.state.collectAsState()
    val scope = rememberCoroutineScope()
    val version = remember { UpdateChecker.currentVersion(context) }
    var notes by remember { mutableStateOf(false) }

    Page(tr("Giới thiệu", "About")) {
        AppHeader(version, update)
        UpdateSection()

        SectionHeader(tr("Bản cập nhật", "Updates"))
        Group {
            GroupToggle(Pos.First, Icons.Rounded.SystemUpdate, tr("Kiểm tra bản cập nhật", "Check for updates"),
                tr("Kiểm tra phiên bản mới trên GitHub mỗi 3 giờ", "Look on GitHub every 3 hours for a new version"),
                s.autoUpdateCheck, color = Tile.Green) { v ->
                SettingsRepo.update { copy(autoUpdateCheck = v) }
            }
            GroupItem(Pos.Middle, Icons.Rounded.Refresh, tr("Kiểm tra ngay", "Check now"), updateStatusText(update, version),
                onClick = { scope.launch { UpdateChecker.check(context.applicationContext, force = true) } }, color = Tile.Blue)
            GroupItem(Pos.Last, Icons.Rounded.NewReleases, tr("Có gì mới", "What's new"), tr("Các thay đổi trong phiên bản $version", "Changes in version $version"),
                onClick = { notes = true }, color = Tile.Purple) { Arrow() }
        }

        SectionHeader(tr("Trợ giúp", "Help"))
        Group {
            GroupItem(Pos.First, Icons.Rounded.Lightbulb, tr("Hướng dẫn", "Instructions"),
                tr("Cách bật Viên thuốc trên Android 13+", "How to turn the island on with Android 13+"),
                onClick = { open(Screen.INSTRUCTION) }, color = Tile.Yellow) { Arrow() }
            BanksItem(Pos.Middle, open)
            GroupItem(Pos.Middle, Icons.Rounded.Navigation, tr("Chẩn đoán điều hướng", "Navigation diagnostics"),
                tr("Thông tin ứng dụng bản đồ, gọi xe hoặc giao hàng gửi tới — nếu không thấy khoảng cách", "What a navigation, taxi or delivery app sends — if the distance doesn't show"),
                onClick = { open(Screen.DIAG) }, color = Tile.Teal) { Arrow() }
            GroupItem(Pos.Middle, Icons.Rounded.CreditCard, tr("Chẩn đoán thanh toán", "Payment diagnostics"),
                tr("Thông tin Viên thuốc nhận diện khi thanh toán — nếu không hiện “Đã thanh toán”", "What the island saw when you paid — if “Paid” didn't show"),
                onClick = { open(Screen.PAYDIAG) }, color = Tile.Green) { Arrow() }
            AppDetailsItem(Pos.Last)
        }

        SectionHeader(tr("Thông tin & Tác quyền", "Information & Credits"))
        Group {
            GroupItem(Pos.Single, ImageVector.vectorResource(R.drawable.ic_github), "Capsule VN",
                "© 2026 Capsule VN · Nguyễn Lê Lý Huỳnh (github.com/KagaTetsuo/Capsule-VN)",
                onClick = { openGithub(context) }, color = GithubPurple) { Arrow() }
        }
    }

    if (notes) NotesDialog { notes = false }
}

@Composable
private fun AppHeader(version: String, update: UpdateState) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Ui.Card).padding(vertical = 26.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        
        Image(
            painter = painterResource(R.drawable.ic_app_icon),
            contentDescription = "Capsule VN",
            modifier = Modifier.size(88.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text("Capsule VN", color = Ui.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("© 2026 Capsule VN · Nguyễn Lê Lý Huỳnh · Zalo: 0399991787", color = Ui.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
        Text(tr("Phiên bản $version", "Version $version"), color = Ui.Sub, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp))
        val (label, color) = when (update) {
            UpdateState.UpToDate -> tr("Đã là mới nhất", "Up to date") to Tints.Green
            is UpdateState.Available -> tr("Đã có phiên bản ${update.release.version}", "Version ${update.release.version} is available") to Ui.Primary
            UpdateState.Checking -> tr("Đang kiểm tra bản cập nhật…", "Checking for updates…") to Ui.Sub
            is UpdateState.Error -> tr("Không thể kiểm tra bản cập nhật", "Couldn't check for updates") to Tints.Orange
            UpdateState.Idle -> null to Ui.Sub
        }
        if (label != null) {
            Text(
                label, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp).clip(CircleShape).background(color.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
fun IdeaScreen(onBack: () -> Unit) {
    Page(tr("Giới thiệu về Viên thuốc", "About the island"), onBack) {
        IslandPreview()
        SectionHeader(tr("Tính năng", "What it does"))
        InfoText(
            tr("Viên thuốc kiểu iPhone bao quanh camera Android của bạn: âm nhạc với ảnh bìa, sóng nhạc động và lời đồng bộ; hẹn giờ với vòng đếm ngược; đồng hồ bấm giờ; tiến trình tải xuống; đèn pin tùy chỉnh độ sáng; thông báo kèm nút trả lời nhanh; thanh toán chạm; bản đồ điều hướng; trạng thái sạc và sự kiện hệ thống. Khi có hai hoạt động, hoạt động thứ hai sẽ thành bong bóng tròn cạnh Viên thuốc.", "An iPhone-style capsule over your Android camera: music with artwork, an equalizer and synced lyrics, a " +
                    "timer with a progress ring, a stopwatch, download progress, a flashlight with brightness control, " +
                    "notifications with replies, payments, navigation, charging and system events. A second activity goes into" +
                    " a bubble next to the island."),
        )
        SectionHeader(tr("Phát triển & Tác quyền", "Development & Rights"))
        InfoText(
            tr(
                "Capsule VN được nghiên cứu, phát triển và tối ưu hóa trải nghiệm chuyên sâu cho người dùng Android tại Việt Nam bởi Nguyễn Lê Lý Huỳnh.\n\n" +
                    "Liên hệ Zalo hỗ trợ: 0399991787\n" +
                    "Mã nguồn dự án: https://github.com/KagaTetsuo/Capsule-VN\n\n" +
                    "Lời bài hát đồng bộ thời gian thực được hỗ trợ bởi dịch vụ mở LRCLIB.",
                "Capsule VN is developed, customized and optimized for Android users in Vietnam by Nguyen Le Ly Huynh.\n\n" +
                    "Zalo support: 0399991787\n" +
                    "Source repository: https://github.com/KagaTetsuo/Capsule-VN\n\n" +
                    "Real-time synchronized lyrics are provided by LRCLIB.",
            ),
        )
        SectionHeader(tr("Cách thức hoạt động", "How it works"))
        InfoText(
            tr("Viên thuốc được hiển thị bởi dịch vụ hỗ trợ tiếp cận, thông tin bài hát lấy từ phiên phát nhạc hệ thống, tiến trình và hẹn giờ lấy từ thông báo. Hiệu ứng động chỉ hoạt động khi có nội dung; tùy chọn “Giảm hiệu ứng” sẽ tắt sóng nhạc động và hiệu ứng co giãn đàn hồi.", "The capsule is drawn by an accessibility service, music data comes from the system media sessions, " +
                    "progress and timers from notifications. Animations run only while the capsule shows something; Reduce " +
                    "effects turns off the equalizer and the jelly."),
        )
    }
}



@Composable
internal fun InfoText(text: String) {
    Text(
        text, color = Ui.Sub, fontSize = 14.sp, lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ui.Card).padding(20.dp),
    )
}

@Composable
fun DiagScreen(onBack: () -> Unit) {
    val dumps by LiveDebug.dumps.collectAsState()
    DumpScreen(
        tr("Chẩn đoán điều hướng", "Navigation diagnostics"),
        tr("Hãy bật ứng dụng bản đồ, gọi xe hoặc giao hàng rồi quay lại đây: bên dưới là những gì ứng dụng gửi trong thông báo. Nếu khoảng cách hoặc mũi tên rẽ không hiển thị trên Viên thuốc, hãy bấm “Sao chép tất cả” và gửi nội dung này để báo lỗi.", "Start a navigation, taxi or delivery app and come back here: below is what the app sends in its " +
                "notification. If the distance or the turn arrow doesn't show in the island, tap “Copy all” and send this " +
                "text."),
        tr("Chưa có gì: chưa nhận được thông báo điều hướng, gọi xe hoặc giao hàng.", "Nothing yet: no navigation, taxi or delivery notification has arrived."),
        dumps.values.toList(), onBack,
    )
}

@Composable
fun PayDiagScreen(onBack: () -> Unit) {
    val log by PayDebug.log.collectAsState()
    DumpScreen(
        tr("Chẩn đoán thanh toán", "Payment diagnostics"),
        tr("Hãy thực hiện một giao dịch thanh toán chạm rồi quay lại đây: bên dưới là những gì Viên thuốc nhận diện được (màn hình thanh toán, thông báo hoặc SMS ngân hàng). Nếu không hiện “Đã thanh toán”, bấm “Sao chép tất cả” để gửi báo lỗi. Dữ liệu chỉ lưu tạm trong bộ nhớ RAM và tự xóa khi khởi động lại.", "Pay with your phone and come back here: below is what the island saw (the payment screen, a bank " +
                "notification or SMS) and how it understood it. If “Paid” didn't show, tap “Copy all” and send the text. " +
                "Entries stay only in the phone's memory and are gone after a restart."),
        tr("Chưa có gì: Viên thuốc chưa nhận diện được màn hình thanh toán hoặc thông báo ngân hàng.", "Nothing yet: the island hasn't seen a payment screen or a bank notification."),
        log, onBack,
    )
}

@Composable
private fun DumpScreen(title: String, intro: String, empty: String, items: List<String>, onBack: () -> Unit) {
    val context = LocalContext.current
    Page(title, onBack) {
        InfoText(intro)
        if (items.isEmpty()) {
            Text(empty, color = Ui.Sub, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
        } else {
            Row {
                PillAction(tr("Sao chép tất cả", "Copy all")) {
                    
                    val clip = ClipData.newPlainText(title, items.joinToString("\n\n"))
                    clip.description.extras = PersistableBundle().apply { putBoolean("android.content.extra.IS_SENSITIVE", true) }
                    context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(clip)
                }
            }
            items.forEach { dump ->
                Text(
                    dump, color = Ui.Text, fontSize = 12.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Ui.Card).padding(16.dp),
                )
            }
        }
    }
}
