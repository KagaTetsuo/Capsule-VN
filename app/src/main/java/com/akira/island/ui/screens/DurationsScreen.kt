package com.akira.island.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.akira.island.R
import com.akira.island.core.IslandBus
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.durationKey
import com.akira.island.core.tr
import com.akira.island.data.NoticeKind
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Ios
import com.akira.island.ui.Group
import com.akira.island.ui.GroupStepSlider
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.posOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Thời gian hiển thị riêng cho từng loại thông báo trên Viên thuốc (nhanh / chậm tuỳ ý). */
@Composable
fun DurationsScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var lastDemo by remember { mutableStateOf(-1L) }

    // Kéo thanh trượt -> sau 0,4 giây hiện thông báo mẫu đúng loại, kéo dài đúng thời gian vừa chọn
    fun demo(key: String, title: String, ms: Int) {
        job?.cancel()
        job = scope.launch {
            delay(400)
            val icon = when (key) {
                "MESSAGE" -> R.drawable.ios_chatbubble_ellipses
                "UPDATE" -> R.drawable.ios_download
                NoticeKind.CHARGING.name -> R.drawable.ios_flash
                NoticeKind.AUDIO.name -> R.drawable.ios_headset
                NoticeKind.PAYMENT.name -> R.drawable.ios_checkmark_circle
                NoticeKind.NAV.name -> R.drawable.ios_navigate
                NoticeKind.ALARM.name -> R.drawable.ios_alarm
                NoticeKind.MODE.name -> R.drawable.ios_notifications_off
                else -> R.drawable.ios_wifi
            }
            val style = if (key == NoticeKind.MODE.name) NoticeStyle.PILL else NoticeStyle.WIDE
            val base = Notice(title, "%.1f s".format(ms / 1000f), Ios.load(context, icon), Tints.Orange, style = style)
            // Thông báo mẫu kéo dài đúng thời gian vừa chọn của loại đó
            val n = base.copy(extraMs = (ms - SettingsRepo.current.durationFor(base.durationKey())).toLong())
            if (IslandBus.notice.value?.id == lastDemo) IslandBus.notice.value = null
            lastDemo = n.id
            IslandBus.post(n)
        }
    }

    fun row(pos: Pos, key: String, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color): @Composable () -> Unit = {
        val own = s.noticeDurations[key]
        val ms = own ?: s.noticeDurationMs
        GroupStepSlider(
            pos, icon, title, ms / 100, 10..120,
            action = if (own != null) tr("Dùng mặc định", "Use default") to {
                SettingsRepo.update { copy(noticeDurations = noticeDurations - key) }
            } else null,
            color = color,
            valueText = tr("%.1f giây", "%.1f s").format(ms / 1000f) + if (own == null) tr(" · mặc định", " · default") else "",
        ) { v ->
            SettingsRepo.update { copy(noticeDurations = noticeDurations + (key to v * 100)) }
            demo(key, title, v * 100)
        }
    }

    Page(tr("Thời gian hiển thị", "Display time"), onBack,
        subtitle = tr("Mỗi loại thông báo có thể hiện nhanh hoặc lâu tuỳ ý. Kéo thanh trượt để xem thử ngay trên Viên thuốc", "Each kind of alert can show briefly or longer. Drag a slider to try it on the island")) {
        SectionHeader(tr("Mặc định cho mọi loại", "Default for all"))
        Group {
            GroupStepSlider(
                Pos.Single, Icons.Rounded.Schedule, tr("Thời gian mặc định", "Default time"), s.noticeDurationMs / 100, 10..120,
                color = Tile.Orange, valueText = tr("%.1f giây", "%.1f s").format(s.noticeDurationMs / 1000f),
            ) { v ->
                SettingsRepo.update { copy(noticeDurationMs = v * 100) }
                demo(NoticeKind.OTHER.name, tr("Thông báo mẫu", "Sample"), v * 100)
            }
        }
        SectionHeader(tr("Riêng từng loại", "Per kind"))
        Group {
            val items = listOf(
                Triple("MESSAGE", tr("Tin nhắn", "Messages"), Icons.Rounded.ChatBubble),
                Triple("UPDATE", tr("Cập nhật ứng dụng", "App updates"), Icons.Rounded.SystemUpdate),
            ) + NoticeKind.entries.map { Triple(it.name, it.title, Icons.Rounded.Timer) }
            val colors = listOf(Tile.Blue, Tile.Pink, Tile.Green, Tile.Teal, Tile.Indigo, Tile.Purple, Tile.Orange, Tile.Yellow, Tile.Gray)
            items.forEachIndexed { i, (key, title, icon) ->
                row(posOf(i, items.size), key, title, icon, colors[i % colors.size])()
            }
        }
    }
}
