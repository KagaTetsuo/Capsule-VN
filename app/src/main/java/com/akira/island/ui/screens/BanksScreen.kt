package com.akira.island.ui.screens

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.Icons
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
    val running by IslandBus.serviceRunning.collectAsState()

    Page(tr("Ứng dụng ngân hàng không mở được", "Bank app won't open"), onBack,
        subtitle = tr("Ngân hàng báo phát hiện ứng dụng bên thứ ba hoặc dịch vụ hỗ trợ tiếp cận?", "Your bank says it detected a third-party app or accessibility service?")) {
        InfoText(
            tr("Các ngân hàng làm điều này để bảo vệ người dùng chống lừa viên thuốc: khi có bất kỳ dịch vụ hỗ trợ tiếp cận nào bật ngoài Google Play, họ sẽ chặn truy cập. Viên thuốc hoàn toàn không đọc dữ liệu ngân hàng, nhưng không thể vượt qua bước kiểm tra bảo mật này — chỉ có thể tạm tắt Viên thuốc. Cách tiện nhất là dùng phím tắt trên Cài đặt nhanh: chạm một cái — Viên thuốc tắt và bạn vào app ngân hàng; giao dịch xong chạm lại để bật Viên thuốc.", "Banks do this to protect against fraud: while any accessibility service not from Google Play is on, " +
                    "they won't let you in. The island reads nothing in your bank, but there's no way around the check — " +
                    "only turning the island off for a while. The easiest way is a tile in quick settings: tap it — the " +
                    "island turns off and you can use the bank; tap it again to turn the island back on."),
        )
        Group {
            val tileAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            if (tileAsk) {
                GroupItem(
                    Pos.First, Icons.Rounded.AddBox, tr("Thêm phím tắt Viên thuốc", "Add the Island tile"),
                    tr("Đến Cài đặt nhanh — chỉ một chạm", "To quick settings — one tap"),
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
                tr("Mở Hỗ trợ tiếp cận — Capsule VN → Bật lên", "Opens Accessibility — Dynamic Island → turn on"),
                color = Tile.Green, onClick = { Perms.openAccessibility(context) },
            ) { Arrow() }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            SectionHeader(tr("Phím tắt trên Cài đặt nhanh", "The quick settings tile"))
            InfoText(
                tr("Mở hết thanh Cài đặt nhanh, bấm vào biểu tượng bút chì và kéo phím tắt Viên thuốc vào danh sách.", "Open quick settings fully, tap the pencil and drag the Island tile to the others."),
            )
        }
    }
}

private fun addTile(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val bar = context.getSystemService(StatusBarManager::class.java) ?: return
    runCatching {
        bar.requestAddTileService(
            ComponentName(context, IslandTile::class.java), context.getString(R.string.tile_label),
            Icon.createWithResource(context, R.drawable.ic_tab_island), context.mainExecutor,
        ) { result ->
            if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                Toast.makeText(context, tr("Phím tắt đã có trong Cài đặt nhanh", "The tile is already in quick settings"), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
internal fun BanksItem(pos: Pos, open: (Screen) -> Unit) {
    GroupItem(pos, Icons.Rounded.AccountBalance, tr("Ứng dụng ngân hàng không mở được", "Bank app won't open"),
        tr("Phím tắt để tạm thời tắt Viên thuốc trong giây lát", "A tile to turn the island off for a while"),
        onClick = { open(Screen.BANKS) }, color = Tile.Indigo) { Arrow() }
}
