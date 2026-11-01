package com.akira.island.ui.screens

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.IslandBus
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.island.rememberHaptic
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupStepSlider
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui

@Composable
fun SizeScreen(onBack: () -> Unit, open: (Screen) -> Unit = {}) {
    val s by SettingsRepo.state.collectAsState()
    val original = remember { SettingsRepo.current }
    val haptic = rememberHaptic()
    var selectedMode by remember { mutableIntStateOf(0) } // 0 = Viên thuốc thu gọn, 1 = Bảng mở rộng trạng thái

    DisposableEffect(selectedMode) {
        if (selectedMode == 0) {
            IslandBus.calibrating.value = true
            IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        } else {
            IslandBus.calibrating.value = false
            IslandBus.cardPreviewMode.value = CardPreviewMode.EXPANDED
        }
        onDispose {
            IslandBus.calibrating.value = false
            IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        }
    }

    val cancel = {
        SettingsRepo.update {
            withPlacement(original)
        }
        IslandBus.calibrating.value = false
        IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        onBack()
    }
    BackHandler(onBack = cancel)

    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    Box(Modifier.fillMaxSize()) {
        Page(
            tr("Vị trí & kích thước", "Position and size"),
            onBack = cancel,
            action = {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Ui.Card).clickable {
                        haptic()
                        if (selectedMode == 0) {
                            set { withPlacement(IslandSettings()) }
                        } else {
                            val d = IslandSettings()
                            set {
                                copy(
                                    expandedOffsetY = d.expandedOffsetY,
                                    expandedWidth = d.expandedWidth,
                                    expandedHeightExtra = d.expandedHeightExtra,
                                    expandedCornerRadius = d.expandedCornerRadius,
                                )
                            }
                        }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Restore, tr("Đặt lại", "Reset"), tint = Ui.Text)
                }
            },
        ) {
            
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Ui.Card)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val modes = listOf(
                    Icons.Rounded.AspectRatio to tr("Viên thuốc", "Capsule"),
                    Icons.Rounded.DashboardCustomize to tr("Bảng mở rộng", "Expanded board"),
                )
                modes.forEachIndexed { idx, (icon, label) ->
                    val active = selectedMode == idx
                    Row(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) Ui.Primary else Color.Transparent)
                            .clickable {
                                haptic()
                                selectedMode = idx
                            }
                            .padding(vertical = 11.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, null, tint = if (active) Ui.OnPrimary else Ui.Sub, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            color = if (active) Ui.OnPrimary else Ui.Text,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (selectedMode == 0) {
                LivePreviewHint(tr("Viên thuốc trên đỉnh màn hình đang chuyển màu đỏ để căn chỉnh trực tiếp quanh camera", "The capsule at the top turns red so you can align it around the camera live"))
                SectionHeader(tr("Vị trí quanh camera", "Position around camera"))
                Group {
                    GroupStepSlider(
                        Pos.First, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), s.offsetX, -200..200,
                        action = tr("Chính giữa", "Center") to { set { copy(offsetX = 0) } }, color = Tile.Blue,
                    ) { v -> set { copy(offsetX = v) } }
                    if (!s.notch) {
                        GroupStepSlider(Pos.Middle, Icons.Rounded.SwapVert, tr("Vị trí dọc (cách mép trên)", "Vertical position (from top)"), s.offsetY, -10..120, color = Tile.Indigo) { v ->
                            set { copy(offsetY = v) }
                        }
                    }
                    GroupToggle(
                        Pos.Last, Icons.Rounded.WaterDrop, tr("Kiểu tai thỏ (khuyết đỉnh)", "Like a notch"),
                        tr("Đối với camera giọt nước: Viên thuốc áp sát đỉnh màn hình và che phần khuyết", "For a waterdrop camera: the island sits against the top of the screen and covers the notch"),
                        s.notch, color = Tile.Teal,
                    ) { v -> set { copy(notch = v) } }
                }

                Spacer(Modifier.height(14.dp))
                SectionHeader(tr("Kích thước & bo góc", "Size & corners"))
                Group {
                    GroupStepSlider(Pos.First, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.width, 15..170, color = Tile.Purple) { v ->
                        set { copy(width = v) }
                    }
                    GroupStepSlider(Pos.Middle, Icons.Rounded.Height, tr("Chiều cao", "Height"), s.height, 15..50, color = Tile.Pink) { v ->
                        set { copy(height = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.BorderStyle,
                        tr("Bo góc", "Corner radius"),
                        if (s.capsuleCornerRadius < 0) s.height / 2 else s.capsuleCornerRadius,
                        0..25,
                        action = tr("Tự động (tròn đều)", "Auto (fully round)") to { set { copy(capsuleCornerRadius = -1) } },
                        valueText = if (s.capsuleCornerRadius < 0) tr("Tự động", "Auto") else null,
                        color = Tile.Yellow,
                    ) { v -> set { copy(capsuleCornerRadius = v) } }
                }

                Spacer(Modifier.height(14.dp))
                Group {
                    GroupItem(
                        Pos.Single,
                        Icons.Rounded.Tune,
                        tr("Thẻ tin nhắn, cuộc gọi & cập nhật", "Message, call & update cards"),
                        tr("Tùy chỉnh vị trí, kích thước & độ bo góc của các khung thẻ", "Position, size & corner radius of banner cards"),
                        color = Tile.Green,
                        onClick = { open(Screen.MSG_CALL_SIZE) },
                    ) { Arrow() }
                }
            } else {
                val conf = LocalConfiguration.current
                val autoWidth = (conf.screenWidthDp - 22).coerceAtMost(460)
                LivePreviewHint(tr("Bảng mở rộng (Giờ, Pin, Âm lịch, RAM/ROM, Phím tắt) đang hiển thị trực tiếp trên đỉnh màn hình — kéo thanh trượt để xem thay đổi ngay", "The expanded board (clock, battery, lunar date, RAM/ROM, shortcuts) is shown live at the top — drag a slider to see changes instantly"))

                SectionHeader(tr("Vị trí", "Position"))
                Group {
                    GroupStepSlider(
                        Pos.Single,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"),
                        s.expandedOffsetY,
                        -30..120,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(expandedOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v -> set { copy(expandedOffsetY = v) } }
                }

                SectionHeader(tr("Kích thước & bo góc", "Size & corners"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.Straighten,
                        tr("Chiều rộng", "Width"),
                        if (s.expandedWidth == 0) autoWidth.coerceIn(260, 460) else s.expandedWidth,
                        260..460,
                        action = tr("Tự động theo màn hình", "Auto (screen width)") to { set { copy(expandedWidth = 0) } },
                        color = Tile.Blue,
                        valueText = if (s.expandedWidth == 0) tr("Tự động", "Auto") else null,
                    ) { v -> set { copy(expandedWidth = v) } }

                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Height,
                        tr("Chiều cao thêm", "Extra height"),
                        s.expandedHeightExtra,
                        -20..60,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(expandedHeightExtra = 0) } },
                        color = Tile.Purple,
                    ) { v -> set { copy(expandedHeightExtra = v) } }

                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.Layers,
                        tr("Bo góc", "Corner radius"),
                        s.expandedCornerRadius,
                        12..50,
                        action = tr("Mặc định 44 dp", "Default 44 dp") to { set { copy(expandedCornerRadius = 44) } },
                        color = Tile.Orange,
                    ) { v -> set { copy(expandedCornerRadius = v) } }
                }
            }

            Spacer(Modifier.height(90.dp))
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(Ui.Primary)
                .clickable {
                    haptic()
                    IslandBus.calibrating.value = false
                    IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
                    onBack()
                }
                .padding(horizontal = 30.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Check, null, tint = Ui.OnPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(tr("Lưu", "Save"), color = Ui.OnPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MsgCallSizeScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val original = remember { SettingsRepo.current }
    val haptic = rememberHaptic()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Tin nhắn, 1 = Cuộc gọi, 2 = Cập nhật
    var previewOn by remember { mutableStateOf(true) }

    DisposableEffect(selectedTab, previewOn) {
        if (previewOn) {
            IslandBus.cardPreviewMode.value = when (selectedTab) {
                0 -> CardPreviewMode.MESSAGE
                1 -> CardPreviewMode.CALL
                else -> CardPreviewMode.UPDATE
            }
        } else {
            IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        }
        onDispose { }
    }
    DisposableEffect(Unit) {
        onDispose { IslandBus.cardPreviewMode.value = CardPreviewMode.NONE }
    }

    val cancel = {
        SettingsRepo.update { withMsgCallPlacement(original) }
        IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        onBack()
    }
    BackHandler(onBack = cancel)

    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    Box(Modifier.fillMaxSize()) {
        Page(
            tr("Kích thước & bo góc khung thẻ", "Cards size & corner radius"),
            onBack = cancel,
            action = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(if (previewOn) Ui.Primary.copy(alpha = 0.22f) else Ui.Card).clickable {
                            haptic()
                            previewOn = !previewOn
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (previewOn) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            tr("Xem trước trên đỉnh", "Preview on top"),
                            tint = if (previewOn) Ui.Primary else Ui.Sub,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(Ui.Card).clickable {
                            haptic()
                            val d = IslandSettings()
                            when (selectedTab) {
                                0 -> set { copy(msgOffsetY = d.msgOffsetY, msgWidth = d.msgWidth, msgHeightExtra = d.msgHeightExtra, msgLines = d.msgLines, cardCornerRadius = d.cardCornerRadius) }
                                1 -> set { copy(callOffsetY = d.callOffsetY, callWidth = d.callWidth, callHeight = d.callHeight, callCornerRadius = d.callCornerRadius) }
                                else -> set { copy(updateOffsetY = d.updateOffsetY, updateWidth = d.updateWidth, updateHeightExtra = d.updateHeightExtra, updateCornerRadius = d.updateCornerRadius) }
                            }
                        },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Restore, tr("Đặt lại", "Reset"), tint = Ui.Text, modifier = Modifier.size(20.dp))
                    }
                }
            },
        ) {
            
            Spacer(Modifier.height(130.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Ui.Card)
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val tabs = listOf(
                    Icons.Rounded.ChatBubble to tr("Thẻ tin nhắn", "Message"),
                    Icons.Rounded.Call to tr("Cuộc gọi đến", "Call"),
                    Icons.Rounded.SystemUpdate to tr("Cập nhật", "Update"),
                )
                tabs.forEachIndexed { idx, (icon, label) ->
                    val active = selectedTab == idx
                    Row(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) Ui.Primary else Color.Transparent)
                            .clickable {
                                haptic()
                                selectedTab = idx
                                if (previewOn) {
                                    IslandBus.cardPreviewMode.value = when (idx) {
                                        0 -> CardPreviewMode.MESSAGE
                                        1 -> CardPreviewMode.CALL
                                        else -> CardPreviewMode.UPDATE
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, null, tint = if (active) Ui.OnPrimary else Ui.Sub, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            label,
                            color = if (active) Ui.OnPrimary else Ui.Text,
                            fontSize = 13.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (selectedTab == 0) {
                SectionHeader(tr("Tùy chỉnh Thẻ tin nhắn", "Message card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"),
                        s.msgOffsetY,
                        -30..120,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(msgOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Chiều rộng", "Width"),
                        s.msgWidth,
                        220..440,
                        action = tr("Mặc định 380 dp", "Default 380 dp") to { set { copy(msgWidth = 380) } },
                        color = Tile.Blue,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Height,
                        tr("Chiều cao thêm", "Extra height"),
                        s.msgHeightExtra,
                        -15..60,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(msgHeightExtra = 0) } },
                        color = Tile.Purple,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgHeightExtra = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.FormatLineSpacing,
                        tr("Số dòng nội dung", "Text lines"),
                        s.msgLines,
                        1..6,
                        unit = tr("dòng", "lines"),
                        action = tr("Mặc định 3 dòng", "Default 3 lines") to { set { copy(msgLines = 3) } },
                        color = Tile.Teal,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgLines = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.BorderStyle,
                        tr("Bo góc", "Corner radius"),
                        s.cardCornerRadius,
                        8..44,
                        action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(cardCornerRadius = 32) } },
                        color = Tile.Yellow,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(cardCornerRadius = v) }
                    }
                }
            } else if (selectedTab == 1) {
                SectionHeader(tr("Tùy chỉnh Thẻ cuộc gọi đến", "Incoming call card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"),
                        s.callOffsetY,
                        -30..120,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(callOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Chiều rộng", "Width"),
                        s.callWidth,
                        220..440,
                        action = tr("Mặc định 370 dp", "Default 370 dp") to { set { copy(callWidth = 370) } },
                        color = Tile.Green,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Height,
                        tr("Chiều cao", "Height"),
                        s.callHeight,
                        52..130,
                        action = tr("Mặc định 82 dp", "Default 82 dp") to { set { copy(callHeight = 82) } },
                        color = Tile.Pink,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callHeight = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.BorderStyle,
                        tr("Bo góc", "Corner radius"),
                        s.callCornerRadius,
                        8..50,
                        action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(callCornerRadius = 32) } },
                        color = Tile.Yellow,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callCornerRadius = v) }
                    }
                }
            } else {
                SectionHeader(tr("Tùy chỉnh Thẻ cập nhật", "Update card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"),
                        s.updateOffsetY,
                        -30..120,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(updateOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Chiều rộng", "Width"),
                        s.updateWidth,
                        220..440,
                        action = tr("Mặc định 380 dp", "Default 380 dp") to { set { copy(updateWidth = 380) } },
                        color = Tile.Blue,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Height,
                        tr("Chiều cao thêm", "Extra height"),
                        s.updateHeightExtra,
                        -20..60,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(updateHeightExtra = 0) } },
                        color = Tile.Purple,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateHeightExtra = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.BorderStyle,
                        tr("Bo góc", "Corner radius"),
                        s.updateCornerRadius,
                        8..44,
                        action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(updateCornerRadius = 32) } },
                        color = Tile.Yellow,
                    ) { v ->
                        if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateCornerRadius = v) }
                    }
                }
            }

            Spacer(Modifier.height(90.dp))
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(Ui.Primary)
                .clickable {
                    haptic()
                    IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
                    onBack()
                }
                .padding(horizontal = 30.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Check, null, tint = Ui.OnPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(tr("Lưu", "Save"), color = Ui.OnPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun IslandSettings.withPlacement(from: IslandSettings) =
    copy(
        offsetX = from.offsetX,
        offsetY = from.offsetY,
        width = from.width,
        height = from.height,
        notch = from.notch,
        capsuleCornerRadius = from.capsuleCornerRadius,
        expandedOffsetY = from.expandedOffsetY,
        expandedWidth = from.expandedWidth,
        expandedHeightExtra = from.expandedHeightExtra,
        expandedCornerRadius = from.expandedCornerRadius,
    )

private fun IslandSettings.withMsgCallPlacement(from: IslandSettings) = copy(
    msgOffsetY = from.msgOffsetY,
    msgWidth = from.msgWidth,
    msgHeightExtra = from.msgHeightExtra,
    msgLines = from.msgLines,
    cardCornerRadius = from.cardCornerRadius,
    callOffsetY = from.callOffsetY,
    callWidth = from.callWidth,
    callHeight = from.callHeight,
    callCornerRadius = from.callCornerRadius,
    updateOffsetY = from.updateOffsetY,
    updateWidth = from.updateWidth,
    updateHeightExtra = from.updateHeightExtra,
    updateCornerRadius = from.updateCornerRadius,
)

@Composable
private fun LivePreviewHint(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E2032))
            .border(1.dp, Color(0xFF2E324A), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(Tints.Green))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color(0xFFB0B6CE), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}
