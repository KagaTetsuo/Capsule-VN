package com.akira.island.ui.screens

import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt
import java.io.File
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.ChoiceDialog
import com.akira.island.data.CardBg
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import android.widget.Toast
import android.net.Uri
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.Context
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
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.Circle
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
            IslandBus.cardPreviewMode.value = tabPreview(selectedTab)
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
            tr("Khung thẻ thông báo", "Notification cards"),
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
                                2 -> set { copy(updateOffsetY = d.updateOffsetY, updateWidth = d.updateWidth, updateHeightExtra = d.updateHeightExtra, updateCornerRadius = d.updateCornerRadius) }
                                else -> set { copy(noticeOffsetX = d.noticeOffsetX, noticeOffsetY = d.noticeOffsetY, noticeWidth = d.noticeWidth, noticeHeightExtra = d.noticeHeightExtra, noticeCornerRadius = d.noticeCornerRadius) }
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
                    Icons.Rounded.ChatBubble to tr("Tin nhắn", "Message"),
                    Icons.Rounded.Call to tr("Cuộc gọi", "Call"),
                    Icons.Rounded.SystemUpdate to tr("Cập nhật", "Update"),
                    Icons.Rounded.NotificationsActive to tr("Khác", "Other"),
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
                                    IslandBus.cardPreviewMode.value = tabPreview(idx)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(icon, null, tint = if (active) Ui.OnPrimary else Ui.Sub, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                label,
                                color = if (active) Ui.OnPrimary else Ui.Text,
                                fontSize = 12.5.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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
            } else if (selectedTab == 2) {
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

            if (selectedTab == 3) {
                fun prev() { if (previewOn) IslandBus.cardPreviewMode.value = CardPreviewMode.NOTICE }
                val autoW = (LocalConfiguration.current.screenWidthDp - 22).coerceAtMost(460)
                SectionHeader(tr("Thông báo khác: sạc, tai nghe, thanh toán, sự kiện…", "Other alerts: charging, headphones, payments, events…"))
                Group {
                    GroupStepSlider(
                        Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position (up / down)"), s.noticeOffsetY, -30..120,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(noticeOffsetY = 0) } }, color = Tile.Indigo,
                    ) { v -> prev(); set { copy(noticeOffsetY = v) } }
                    GroupStepSlider(
                        Pos.Middle, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), s.noticeOffsetX, -120..120,
                        action = tr("Chính giữa", "Center") to { set { copy(noticeOffsetX = 0) } }, color = Tile.Teal,
                    ) { v -> prev(); set { copy(noticeOffsetX = v) } }
                    GroupStepSlider(
                        Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"),
                        if (s.noticeWidth == 0) autoW.coerceIn(200, 460) else s.noticeWidth, 200..460,
                        action = tr("Tự động theo màn hình", "Auto (screen width)") to { set { copy(noticeWidth = 0) } }, color = Tile.Blue,
                        valueText = if (s.noticeWidth == 0) tr("Tự động", "Auto") else null,
                    ) { v -> prev(); set { copy(noticeWidth = v) } }
                    GroupStepSlider(
                        Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), s.noticeHeightExtra, -16..40,
                        action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(noticeHeightExtra = 0) } }, color = Tile.Purple,
                    ) { v -> prev(); set { copy(noticeHeightExtra = v) } }
                    GroupStepSlider(
                        Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"),
                        if (s.noticeCornerRadius < 0) 28 else s.noticeCornerRadius, 0..44,
                        action = tr("Tự động (tròn đều)", "Auto (fully round)") to { set { copy(noticeCornerRadius = -1) } }, color = Tile.Yellow,
                        valueText = if (s.noticeCornerRadius < 0) tr("Tự động", "Auto") else null,
                    ) { v -> prev(); set { copy(noticeCornerRadius = v) } }
                }
            }

            CardStyleSection(s) {
                if (previewOn) IslandBus.cardPreviewMode.value = tabPreview(selectedTab)
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
    cardBg = from.cardBg,
    cardBgColor = from.cardBgColor,
    cardBgOpacity = from.cardBgOpacity,
    cardBgImage = from.cardBgImage,
    cardImageDim = from.cardImageDim,
    cardBorder = from.cardBorder,
    cardBorderColor = from.cardBorderColor,
    cardBorderWidth = from.cardBorderWidth,
    cardStyleExpanded = from.cardStyleExpanded,
    cardStylePills = from.cardStylePills,
    cardStyleIdle = from.cardStyleIdle,
    cardBorderLed = from.cardBorderLed,
    noticeOffsetX = from.noticeOffsetX,
    noticeOffsetY = from.noticeOffsetY,
    noticeWidth = from.noticeWidth,
    noticeHeightExtra = from.noticeHeightExtra,
    noticeCornerRadius = from.noticeCornerRadius,
)

@Composable
private fun LivePreviewHint(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF261A1C))
            .border(1.dp, Color(0xFF4A3528), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(Tints.Green))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Color(0xFFCDBFB4), fontSize = 12.5.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}

private val cardColors = listOf(
    0xFF1F1719, 0xFF000000, 0xFF2B0A10, 0xFF7A0F1E, 0xFFC8102E, 0xFF3A2A12,
    0xFF0F1E3A, 0xFF0E2A24, 0xFF2A1240, 0xFF3A3A40, 0xFFF5F0E6,
)

private val cardBorderColors = listOf(
    0xFFE6C068, 0xFFFFD700, 0xFFC8102E, 0xFFFF453A, 0xFFFFFFFF, 0xFF8E8E93,
    0xFF0A84FF, 0xFF34C759, 0xFFBF5AF2, 0xFF30B0C7,
)

/** Nền & viền dùng chung cho mọi thẻ thông báo: màu, ảnh nền, độ trong suốt, Liquid Glass, màu & độ dày viền. */
@Composable
private fun CardStyleSection(s: IslandSettings, preview: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bgDialog by remember { mutableStateOf(false) }
    fun set(block: IslandSettings.() -> IslandSettings) {
        preview()
        SettingsRepo.update(block)
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val path = withContext(Dispatchers.IO) { saveCardImage(context, uri) }
            if (path != null) set { copy(cardBg = CardBg.IMAGE, cardBgImage = path) }
            else Toast.makeText(context, tr("Không đọc được ảnh", "Couldn't read the image"), Toast.LENGTH_SHORT).show()
        }
    }

    SectionHeader(tr("Nền & viền bảng thông báo", "Banner background & border"))
    Group {
        GroupItem(
            Pos.First, Icons.Rounded.Wallpaper, tr("Kiểu nền", "Background"), s.cardBg.title,
            color = Tile.Orange, onClick = { bgDialog = true },
        ) { Arrow() }
        if (s.cardBg == CardBg.IMAGE) {
            GroupItem(
                Pos.Middle, Icons.Rounded.Image, tr("Chọn ảnh nền", "Pick an image"),
                if (s.cardBgImage.isEmpty()) tr("Chưa chọn ảnh", "No image yet") else tr("Đã chọn — chạm để đổi ảnh khác", "Selected — tap to change"),
                color = Tile.Pink,
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            ) { Arrow() }
            GroupSlider(
                Pos.Middle, Icons.Rounded.Contrast, tr("Làm tối ảnh để dễ đọc chữ", "Darken image for legibility"),
                value = s.cardImageDim.toFloat(), range = 0f..80f, label = "${s.cardImageDim}%", color = Tile.Gray,
            ) { v -> set { copy(cardImageDim = v.toInt()) } }
        }
        GroupSlider(
            Pos.Middle, Icons.Rounded.Opacity,
            if (s.cardBg == CardBg.GLASS) tr("Độ đậm kính", "Glass density") else tr("Độ đậm nền", "Background opacity"),
            value = s.cardBgOpacity.toFloat(), range = 20f..100f, label = "${s.cardBgOpacity}%", color = Tile.Teal,
        ) { v -> set { copy(cardBgOpacity = v.toInt()) } }
        GroupToggle(
            Pos.Middle, Icons.Rounded.Circle, tr("Áp dụng cho thông báo dạng viên nhỏ", "Also for small pill alerts"),
            tr("Sạc pin, tai nghe, chế độ chuông, thanh toán… Tắt: giữ nền đen như cũ", "Charging, headphones, ring mode, payments… Off: keep the classic black"),
            s.cardStylePills, color = Tile.Gray,
        ) { v -> set { copy(cardStylePills = v) } }
        GroupToggle(
            Pos.Middle, Icons.Rounded.AspectRatio, tr("Áp dụng cho Viên thuốc khi không hoạt động", "Also for the idle island"),
            tr("Viên thuốc trống quanh camera cũng dùng nền ảnh / màu / Liquid Glass & viền này", "The empty island around the camera uses this background & border too"),
            s.cardStyleIdle, color = Tile.Blue,
        ) { v -> set { copy(cardStyleIdle = v) } }
        GroupToggle(
            Pos.Last, Icons.Rounded.DashboardCustomize, tr("Áp dụng cho bảng mở rộng", "Also for the expanded island"),
            tr("Bảng Giờ – Pin, trình phát nhạc, hẹn giờ… dùng chung nền & viền này", "Clock, music player, timer… panels use the same background & border"),
            s.cardStyleExpanded, color = Tile.Indigo,
        ) { v -> set { copy(cardStyleExpanded = v) } }
    }
    if (s.cardBg == CardBg.COLOR || s.cardBg == CardBg.GLASS) {
        Swatches(cardColors, s.cardBgColor) { c -> set { copy(cardBgColor = c) } }
    }

    Group {
        GroupToggle(
            if (s.cardBorder) Pos.First else Pos.Single, Icons.Rounded.BorderStyle, tr("Viền thẻ", "Card border"),
            tr("Đường viền màu quanh thẻ thông báo", "A colored outline around notification cards"),
            s.cardBorder, color = Tile.Yellow,
        ) { v -> set { copy(cardBorder = v) } }
        if (s.cardBorder) {
            GroupToggle(
                Pos.Middle, Icons.Rounded.Animation, tr("Viền hiệu ứng (như Vòng sáng LED)", "Animated border (like the LED ring)"),
                tr("Cầu vồng xoay 360°, theo màu app, 1–3 màu tự chọn, sao chổi, nhịp thở… — chọn màu & chuyển động ở Giao diện → Vòng sáng LED", "Rainbow 360°, app color, 1–3 custom colors, comet, breathing… — pick colors & motion in Appearance → LED ring"),
                s.cardBorderLed, color = Tile.Purple,
            ) { v -> set { copy(cardBorderLed = v) } }
            GroupSlider(
                Pos.Last, Icons.Rounded.LineWeight, tr("Độ dày viền", "Border width"),
                value = s.cardBorderWidth, range = 0.5f..4f, label = "%.1f dp".format(s.cardBorderWidth), color = Tile.Yellow,
            ) { v -> set { copy(cardBorderWidth = (v * 2).roundToInt() / 2f) } }
        }
    }
    if (s.cardBorder && !s.cardBorderLed) {
        Swatches(cardBorderColors, s.cardBorderColor) { c -> set { copy(cardBorderColor = c) } }
    }

    if (bgDialog) {
        ChoiceDialog(tr("Kiểu nền thẻ", "Card background"), CardBg.entries, s.cardBg, { it.title }, { v ->
            if (v == CardBg.IMAGE && s.cardBgImage.isEmpty()) {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                set { copy(cardBg = v) }
            }
        }) { bgDialog = false }
    }
}

/** Chép ảnh người dùng chọn vào bộ nhớ riêng của app (thu nhỏ tối đa 1080 px) để overlay đọc được lâu dài. */
private fun saveCardImage(context: Context, uri: Uri): String? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1080) sample *= 2
    val bmp = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: return@runCatching null
    context.filesDir.listFiles { f -> f.name.startsWith("card_bg_") }?.forEach { it.delete() }
    val file = File(context.filesDir, "card_bg_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 88, it) }
    bmp.recycle()
    file.absolutePath
}.getOrNull()

private fun tabPreview(tab: Int): CardPreviewMode = when (tab) {
    0 -> CardPreviewMode.MESSAGE
    1 -> CardPreviewMode.CALL
    2 -> CardPreviewMode.UPDATE
    else -> CardPreviewMode.NOTICE
}
