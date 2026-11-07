package com.akira.island.ui.screens

import rikka.shizuku.Shizuku
import kotlinx.coroutines.launch
import com.akira.island.ui.Ui
import com.akira.island.ui.GroupToggle
import com.akira.island.service.BankMode
import com.akira.island.data.SettingsRepo
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import android.net.Uri
import android.content.pm.PackageManager
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AddBox
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.akira.island.R
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import com.akira.island.service.IslandTile
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.Page
import com.akira.island.ui.Perms
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile

@Composable
fun BanksScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val s by SettingsRepo.state.collectAsState()
    val running by IslandBus.serviceRunning.collectAsState()
    val scope = rememberCoroutineScope()
    val (perm, refresh) = rememberOnResume {
        Triple(BankMode.canWriteSecure(context), BankMode.hasUsageAccess(context), BankMode.shizukuRunning())
    }
    val (canWrite, usage, shizuku) = perm
    val postLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        refresh()
        if (!ok) Perms.openAppNotifications(context)
    }
    var appsDialog by remember { mutableStateOf(false) }
    var shortcutDialog by remember { mutableStateOf(false) }
    var adbDialog by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    fun grant() {
        busy = true
        scope.launch {
            val ok = BankMode.grantWithShizuku(context)
            busy = false
            refresh()
            Toast.makeText(
                context,
                if (ok) tr("Đã cấp quyền — Chế độ ngân hàng sẵn sàng", "Granted — banking mode is ready")
                else tr("Shizuku chưa cấp được quyền, thử lại hoặc dùng máy tính (ADB)", "Shizuku couldn't grant it, try again or use ADB"),
                Toast.LENGTH_LONG,
            ).show()
        }
    }
    DisposableEffect(Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { code, result ->
            if (code == SHIZUKU_CODE && result == PackageManager.PERMISSION_GRANTED) grant()
        }
        runCatching { Shizuku.addRequestPermissionResultListener(listener) }
        onDispose { runCatching { Shizuku.removeRequestPermissionResultListener(listener) } }
    }

    Page(tr("Ngân hàng & VNeID", "Banks & ID apps"), onBack,
        subtitle = tr("Ngân hàng và VNeID chặn mọi app đang bật Hỗ trợ tiếp cận. Chế độ ngân hàng tự tắt Viên thuốc khi bạn mở các app này và tự bật lại khi bạn rời đi.", "Banks and ID apps block any app with an active accessibility service. Banking mode turns the island off when you open them and back on when you leave.")) {

        SectionHeader(tr("Chế độ ngân hàng tự động", "Automatic banking mode"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.AccountBalance, tr("Tự tắt khi mở ngân hàng / VNeID", "Pause for bank / ID apps"),
                when {
                    canWrite && usage -> tr("Hoàn toàn tự động: tắt khi mở, tự bật lại khi rời app", "Fully automatic: off when opened, back on when you leave")
                    usage -> tr("Tự tắt khi mở; rời app sẽ có nhắc chạm để bật lại", "Pauses when opened; a reminder to turn back on when you leave")
                    else -> tr("Tự tắt khi mở app — không cần cài đặt gì thêm. Dùng xong chạm thông báo để bật lại", "Pauses when opened — no setup needed. Tap the notification to turn back on")
                },
                s.bankMode, color = Tile.Indigo,
            ) { v ->
                SettingsRepo.update { copy(bankMode = v) }
                // Bật chế độ ngân hàng: xin luôn quyền hiện thông báo để nhắc bật lại hoạt động
                if (v && !Perms.postNotifications(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val list = remember(s.bankApps, s.bankAppsEdited) { BankMode.apps(context) }
            GroupItem(
                Pos.Middle, Icons.Rounded.Apps, tr("Ứng dụng áp dụng", "Apps"),
                if (list.isEmpty()) tr("Chưa có ứng dụng nào", "No apps yet")
                else tr("${list.size} ứng dụng", "${list.size} apps") + (if (s.bankAppsEdited) "" else tr(" · tự nhận diện", " · auto-detected")),
                color = Tile.Blue, onClick = { appsDialog = true },
            ) { Arrow() }
            GroupItem(
                Pos.Last, Icons.Rounded.AddBox, tr("Lối tắt mở an toàn (không bị tự thoát)", "Safe-open shortcut (never closes)"),
                tr("Tạo biểu tượng ở màn hình chính cho app hay tự thoát lần đầu (Xanh SM, Tammi…): chạm vào là tắt Viên thuốc hẳn rồi mới mở app", "Adds a home-screen icon for apps that close on first open: it turns the island off fully, then opens the app"),
                color = Tile.Green, onClick = { shortcutDialog = true },
            ) { Arrow() }
        }
        val lastClick by BankMode.lastClick.collectAsState()
        if (lastClick.isNotEmpty()) {
            InfoText(tr("Lần chạm gần nhất ở màn hình chính: ", "Last home-screen tap: ") + lastClick)
        }

        val post = Perms.postNotifications(context)
        val overlay = Perms.overlay(context)
        SectionHeader(tr("Bật lại tiện hơn (tuỳ chọn)", "Easier resume (optional)"))
        Group {
            GroupItem(
                Pos.First, Icons.Rounded.NotificationsActive, tr("Cho phép Capsule VN hiện thông báo", "Allow Capsule VN notifications"),
                if (post) tr("Đã bật — nhắc bật lại và nút “Bật lại ngay” sẽ hiện", "On — reminders and the “Turn on now” button will show")
                else tr("Bắt buộc để thấy nhắc bật lại Viên thuốc (Android 13+)", "Required to see the turn-back-on reminder (Android 13+)"),
                color = if (post) Tile.Green else Tile.Orange,
                onClick = {
                    if (!post && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) postLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    else Perms.openAppNotifications(context)
                },
            ) { Status(post) }
            GroupItem(
                Pos.Middle, Icons.Rounded.OpenInNew, tr("Tự mở lại app ngân hàng nếu bị đóng", "Reopen the bank app if it closes"),
                if (overlay) tr("Đã bật — app ngân hàng tự đóng lần đầu sẽ được mở lại ngay", "On — a bank app that closes itself is reopened right away")
                else tr("Cấp “Hiển thị trên ứng dụng khác” để không phải mở app ngân hàng 2 lần", "Grant “Display over other apps” so you don't have to open the bank app twice"),
                color = if (overlay) Tile.Green else Tile.Blue,
                onClick = { Perms.openOverlay(context) },
            ) { Status(overlay) }
            GroupItem(
                Pos.Middle, Icons.Rounded.QueryStats, tr("Nhắc bật lại khi rời app", "Remind when you leave"),
                if (usage) tr("Đã bật — rời app ngân hàng sẽ hiện nhắc 1 chạm", "On — a one-tap reminder when you leave the bank app")
                else tr("Cấp “Truy cập dữ liệu sử dụng” trong Cài đặt — không cần máy tính", "Grant Usage access in Settings — no computer needed"),
                color = if (usage) Tile.Green else Tile.Blue,
                onClick = { if (!usage) Perms.openUsageAccess(context) },
            ) { Status(usage) }
            GroupItem(
                Pos.Middle, Icons.Rounded.AdminPanelSettings, tr("Tự bật lại hoàn toàn (nâng cao)", "Fully automatic resume (advanced)"),
                if (canWrite) tr("Đã cấp — giữ vĩnh viễn, có thể gỡ Shizuku & tắt Tuỳ chọn nhà phát triển", "Granted — permanent; you can remove Shizuku & turn off Developer options")
                else if (shizuku) tr("Chạm để cấp qua Shizuku — không cần máy tính", "Tap to grant via Shizuku — no computer needed")
                else tr("Cài & khởi động Shizuku (Gỡ lỗi không dây), hoặc dùng máy tính (ADB)", "Install & start Shizuku (wireless debugging), or use a computer (ADB)"),
                color = if (canWrite) Tile.Green else Tile.Gray,
                onClick = {
                    when {
                        canWrite -> Unit
                        busy -> Unit
                        shizuku && BankMode.shizukuGranted() -> grant()
                        shizuku -> BankMode.requestShizuku(SHIZUKU_CODE)
                        else -> openShizuku(context)
                    }
                },
            ) { Status(canWrite) }
            GroupItem(
                Pos.Last, Icons.Rounded.Terminal, tr("Cấp bằng máy tính (ADB)", "Grant with a computer (ADB)"),
                tr("Xem & sao chép 2 lệnh cần chạy", "See & copy the two commands"),
                color = Tile.Gray, onClick = { adbDialog = true },
            ) { Arrow() }
        }

        SectionHeader(tr("Tắt / bật bằng tay", "Manual"))
        Group {
            val tileAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            if (tileAsk) {
                GroupItem(
                    Pos.First, Icons.Rounded.AddBox, tr("Thêm phím tắt Viên thuốc", "Add the Island tile"),
                    tr("Vào Cài đặt nhanh — một chạm để tắt / bật", "To quick settings — one tap to turn off / on"),
                    color = Tile.Blue, onClick = { addTile(context) },
                ) { Arrow() }
            }
            GroupItem(
                if (tileAsk) Pos.Middle else Pos.First, Icons.Rounded.PowerSettingsNew, tr("Tắt Viên thuốc ngay bây giờ", "Turn the island off now"),
                if (running) tr("Bật lại trong Hỗ trợ tiếp cận hoặc qua phím tắt", "Turn it back on in Accessibility or with the tile")
                else tr("Viên thuốc hiện đã tắt", "The island is already off"),
                color = Tile.Orange, onClick = { if (running) Controls.turnOffIsland() },
            )
            GroupItem(
                Pos.Last, Icons.Rounded.Lock, tr("Bật Viên thuốc", "Turn the island on"),
                tr("Mở Hỗ trợ tiếp cận — Capsule VN → Bật lên", "Opens Accessibility — Capsule VN → turn on"),
                color = Tile.Green, onClick = { Perms.openAccessibility(context) },
            ) { Arrow() }
        }
        InfoText(
            tr("Tự bật lại hoàn toàn (nâng cao, tuỳ chọn) — Android không cho file APK tự cấp quyền này, nên mỗi máy cần làm 1 lần:\n1. Cài Shizuku từ Google Play.\n2. Bật Tuỳ chọn nhà phát triển → Gỡ lỗi không dây.\n3. Mở Shizuku → Ghép nối bằng mã → Bắt đầu.\n4. Quay lại đây, chạm “Tự bật lại hoàn toàn” → Cho phép.\n5. Xong — có thể gỡ Shizuku và TẮT Tuỳ chọn nhà phát triển (ngân hàng sẽ dùng bình thường). Quyền vẫn giữ qua khởi động lại & cập nhật OTA, chỉ mất khi gỡ cài đặt Capsule VN.", "Grant with Shizuku (Android 11+, no computer):\n1. Install Shizuku from Google Play.\n2. Turn on Developer options → Wireless debugging.\n3. Open Shizuku → Pair → Start.\n4. Come back and tap “Fully automatic resume” → Allow.\n5. Done — you can remove Shizuku and turn OFF Developer options. The permission survives reboots & updates; it is only lost if Capsule VN is uninstalled."),
        )
    }

    if (appsDialog) BankAppsDialog { appsDialog = false }
    if (shortcutDialog) SafeShortcutDialog { shortcutDialog = false }
    if (adbDialog) {
        val cmds = remember { BankMode.adbCommands(context) }
        AlertDialog(
            onDismissRequest = { adbDialog = false },
            containerColor = Ui.Card,
            title = { Text(tr("Cấp quyền bằng máy tính", "Grant with a computer")) },
            text = {
                Column {
                    Text(tr("Bật Gỡ lỗi USB, cắm máy vào máy tính rồi chạy:", "Turn on USB debugging, connect to a computer and run:"), color = Ui.Sub, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(cmds, color = Ui.Text, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("adb", cmds))
                    Toast.makeText(context, tr("Đã sao chép lệnh", "Commands copied"), Toast.LENGTH_SHORT).show()
                    adbDialog = false
                }) { Text(tr("Sao chép", "Copy"), color = Ui.Primary) }
            },
            dismissButton = { TextButton(onClick = { adbDialog = false }) { Text(tr("Đóng", "Close"), color = Ui.Sub) } },
        )
    }
}

private const val SHIZUKU_CODE = 3105

private fun openShizuku(context: Context) {
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage("moe.shizuku.privileged.api")
        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

@Composable
private fun BankAppsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val all = remember { BankMode.launchable(context) }
    val detected = remember { BankMode.detected(context) }
    var picked by remember { mutableStateOf(BankMode.apps(context)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ui.Card,
        title = { Text(tr("Ứng dụng áp dụng", "Apps")) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                val sorted = all.sortedByDescending { it.first in detected }
                items(sorted, key = { it.first }) { (pkg, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { picked = if (pkg in picked) picked - pkg else picked + pkg }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = pkg in picked, onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = Ui.Primary, checkmarkColor = Ui.OnPrimary, uncheckedColor = Ui.Outline),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(label, color = Ui.Text, fontSize = 15.sp, maxLines = 1)
                            if (pkg in detected) Text(tr("Tự nhận diện", "Auto-detected"), color = Ui.Primary, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                SettingsRepo.update { copy(bankApps = picked.sorted(), bankAppsEdited = true) }
                BankMode.invalidate()
                onDismiss()
            }) { Text(tr("Lưu", "Save"), color = Ui.Primary) }
        },
        dismissButton = {
            TextButton(onClick = {
                SettingsRepo.update { copy(bankApps = emptyList(), bankAppsEdited = false) }
                BankMode.invalidate()
                onDismiss()
            }) { Text(tr("Tự nhận diện lại", "Auto-detect"), color = Ui.Sub) }
        },
    )
}

/** Chọn app để tạo "Lối tắt mở an toàn" ở màn hình chính. */
@Composable
private fun SafeShortcutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val apps = remember {
        val list = BankMode.apps(context)
        BankMode.launchable(context).filter { it.first in list }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ui.Card,
        title = { Text(tr("Tạo lối tắt mở an toàn", "Create a safe-open shortcut")) },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                item {
                    Text(
                        tr("Chọn app — một biểu tượng mới sẽ được thêm vào màn hình chính. Dùng biểu tượng đó thay cho biểu tượng gốc để không bao giờ bị tự thoát.",
                            "Pick an app — a new icon is added to your home screen. Use it instead of the original icon so the app never closes itself."),
                        color = Ui.Sub, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                if (apps.isEmpty()) {
                    item { Text(tr("Chưa có app nào trong danh sách", "No apps in the list yet"), color = Ui.Sub) }
                }
                items(apps, key = { it.first }) { (pkg, label) ->
                    Text(
                        label, color = Ui.Text, fontSize = 15.sp, maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val ok = pinSafeShortcut(context, pkg, label)
                                Toast.makeText(
                                    context,
                                    if (ok) tr("Đang thêm “$label” vào màn hình chính…", "Adding “$label” to the home screen…")
                                    else tr("Màn hình chính này không cho thêm lối tắt", "This launcher doesn't support shortcuts"),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                if (ok) onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Đóng", "Close"), color = Ui.Primary) } },
    )
}

/** Ghim lối tắt: chạm vào -> SafeLaunchActivity tắt Viên thuốc hẳn rồi mới mở app. */
private fun pinSafeShortcut(context: Context, pkg: String, label: String): Boolean = runCatching {
    val sm = context.getSystemService(android.content.pm.ShortcutManager::class.java) ?: return false
    if (!sm.isRequestPinShortcutSupported) return false
    val bmp = context.packageManager.getApplicationIcon(pkg).toBitmap(192, 192)
    val intent = Intent(context, com.akira.island.service.SafeLaunchActivity::class.java)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(com.akira.island.service.SafeLaunchActivity.EXTRA_PKG, pkg)
    val info = android.content.pm.ShortcutInfo.Builder(context, "safe_$pkg")
        .setShortLabel(label)
        .setLongLabel(tr("$label (mở an toàn)", "$label (safe open)"))
        .setIcon(Icon.createWithBitmap(bmp))
        .setIntent(intent)
        .build()
    sm.requestPinShortcut(info, null)
}.getOrDefault(false)

private fun addTile(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val bar = context.getSystemService(StatusBarManager::class.java) ?: return
    runCatching {
        bar.requestAddTileService(
            ComponentName(context, IslandTile::class.java), context.getString(R.string.tile_label),
            Icon.createWithResource(context, R.drawable.ic_stat_capsule), context.mainExecutor,
        ) { result ->
            if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                Toast.makeText(context, tr("Phím tắt đã có trong Cài đặt nhanh", "The tile is already in quick settings"), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
internal fun BanksItem(pos: Pos, open: (Screen) -> Unit) {
    GroupItem(pos, Icons.Rounded.AccountBalance, tr("Ngân hàng & VNeID", "Banks & ID apps"),
        if (SettingsRepo.current.bankMode) tr("Chế độ ngân hàng đang bật — tự tắt / bật Viên thuốc", "Banking mode on — pauses the island automatically")
        else tr("App ngân hàng không mở được? Bật chế độ ngân hàng tự động", "Bank app won't open? Turn on automatic banking mode"),
        onClick = { open(Screen.BANKS) }, color = Tile.Indigo) { Arrow() }
}
