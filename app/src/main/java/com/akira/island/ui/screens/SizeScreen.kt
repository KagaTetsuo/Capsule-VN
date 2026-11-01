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
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.R
import com.akira.island.core.AppMessage
import com.akira.island.core.CallInfo
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.IslandBus
import com.akira.island.core.MsgAction
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Act
import com.akira.island.island.CompactContent
import com.akira.island.island.Ios
import com.akira.island.island.callHeight
import com.akira.island.island.glassSurface
import com.akira.island.island.islandShape
import com.akira.island.island.rememberHaptic
import com.akira.island.service.NotificationParser
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

    DisposableEffect(Unit) {
        IslandBus.calibrating.value = true
        onDispose { IslandBus.calibrating.value = false }
    }

    val cancel = {
        SettingsRepo.update {
            withPlacement(original)
        }
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
                        set { withPlacement(IslandSettings()) }
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Restore, tr("Đặt lại", "Reset"), tint = Ui.Text)
                }
            },
        ) {
            Group {
                GroupStepSlider(
                    Pos.First, Icons.Rounded.SwapHoriz, tr("Ngang", "Horizontal"), s.offsetX, -200..200,
                    action = tr("Chính giữa", "Center") to { set { copy(offsetX = 0) } }, color = Tile.Blue,
                ) { v -> set { copy(offsetX = v) } }
                if (!s.notch) {
                    GroupStepSlider(Pos.Middle, Icons.Rounded.SwapVert, tr("Khoảng cách từ mép trên", "Top offset"), s.offsetY, -10..120, color = Tile.Indigo) { v ->
                        set { copy(offsetY = v) }
                    }
                }
                GroupToggle(
                    Pos.Last, Icons.Rounded.WaterDrop, tr("Kiểu tai thỏ (khuyết đỉnh)", "Like a notch"),
                    tr("Đối với camera giọt nước: Viên thuốc áp sát đỉnh màn hình và che phần khuyết", "For a waterdrop camera: the island sits against the top of the screen and covers the notch"),
                    s.notch, color = Tile.Teal,
                ) { v -> set { copy(notch = v) } }
            }
            Spacer(Modifier.height(16.dp))
            Group {
                GroupStepSlider(Pos.First, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.width, 15..170, color = Tile.Purple) { v ->
                    set { copy(width = v) }
                }
                GroupStepSlider(Pos.Last, Icons.Rounded.Height, tr("Chiều cao", "Height"), s.height, 15..50, color = Tile.Pink) { v ->
                    set { copy(height = v) }
                }
            }
            Spacer(Modifier.height(16.dp))
            Group {
                GroupItem(
                    Pos.Single,
                    Icons.Rounded.Tune,
                    tr("Thẻ tin nhắn, cuộc gọi & cập nhật", "Message, call & update cards"),
                    tr("Tùy chỉnh vị trí, kích thước & độ bo góc kèm mô phỏng trực tiếp", "Position, size & corner radius controls with live simulation"),
                    color = Tile.Green,
                    onClick = { open(Screen.MSG_CALL_SIZE) },
                ) { Arrow() }
            }
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(Ui.Primary)
                .clickable { haptic(); onBack() }
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

    DisposableEffect(selectedTab) {
        IslandBus.cardPreviewMode.value = when (selectedTab) {
            0 -> CardPreviewMode.MESSAGE
            1 -> CardPreviewMode.CALL
            else -> CardPreviewMode.UPDATE
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
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Ui.Card).clickable {
                        haptic()
                        val d = IslandSettings()
                        when (selectedTab) {
                            0 -> set { copy(msgOffsetY = d.msgOffsetY, msgWidth = d.msgWidth, msgHeightExtra = d.msgHeightExtra, msgLines = d.msgLines, cardCornerRadius = d.cardCornerRadius) }
                            1 -> set { copy(callOffsetY = d.callOffsetY, callWidth = d.callWidth, callHeight = d.callHeight, cardCornerRadius = d.cardCornerRadius) }
                            else -> set { copy(updateOffsetY = d.updateOffsetY, updateWidth = d.updateWidth, updateHeightExtra = d.updateHeightExtra, cardCornerRadius = d.cardCornerRadius) }
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
                val tabs = listOf(
                    Icons.Rounded.ChatBubble to tr("Tin nhắn", "Message"),
                    Icons.Rounded.Call to tr("Cuộc gọi", "Call"),
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
                                IslandBus.cardPreviewMode.value = when (idx) {
                                    0 -> CardPreviewMode.MESSAGE
                                    1 -> CardPreviewMode.CALL
                                    else -> CardPreviewMode.UPDATE
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(icon, null, tint = if (active) Ui.OnPrimary else Ui.Sub, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            label,
                            color = if (active) Ui.OnPrimary else Ui.Text,
                            fontSize = 13.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }

            CardLiveSimulationPreview(s, cardMode = selectedTab)

            SectionHeader(tr("Độ bo góc khung thông báo", "Notification cards corner radius"))
            Group {
                GroupStepSlider(
                    Pos.Single,
                    Icons.Rounded.BorderStyle,
                    tr("Độ bo tròn góc khung thẻ", "Card corner radius"),
                    s.cardCornerRadius,
                    8..44,
                    action = tr("Mặc định", "Default") to { set { copy(cardCornerRadius = 32) } },
                    color = Tile.Orange,
                ) { v ->
                    set { copy(cardCornerRadius = v) }
                }
            }

            if (selectedTab == 0) {
                SectionHeader(tr("Tùy chỉnh Thẻ tin nhắn", "Message card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí trên / dưới (Dọc)", "Top / bottom position"),
                        s.msgOffsetY,
                        -30..120,
                        action = tr("Mặc định", "Default") to { set { copy(msgOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Độ rộng bề ngang", "Horizontal width"),
                        s.msgWidth,
                        220..440,
                        action = tr("Mặc định", "Default") to { set { copy(msgWidth = 380) } },
                        color = Tile.Blue,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Height,
                        tr("Độ rộng bề dọc (Chiều cao)", "Vertical height offset"),
                        s.msgHeightExtra,
                        -15..60,
                        action = tr("Mặc định", "Default") to { set { copy(msgHeightExtra = 0) } },
                        color = Tile.Purple,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgHeightExtra = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.FormatLineSpacing,
                        tr("Dòng văn bản", "Text lines"),
                        s.msgLines,
                        1..6,
                        unit = tr("dòng", "lines"),
                        action = tr("Mặc định", "Default") to { set { copy(msgLines = 3) } },
                        color = Tile.Teal,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
                        set { copy(msgLines = v) }
                    }
                }
            } else if (selectedTab == 1) {
                SectionHeader(tr("Tùy chỉnh Thẻ cuộc gọi đến", "Incoming call card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí trên / dưới (Dọc)", "Top / bottom position"),
                        s.callOffsetY,
                        -30..120,
                        action = tr("Mặc định", "Default") to { set { copy(callOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Độ rộng bề ngang", "Horizontal width"),
                        s.callWidth,
                        220..440,
                        action = tr("Mặc định", "Default") to { set { copy(callWidth = 370) } },
                        color = Tile.Green,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.Height,
                        tr("Độ rộng bề dọc (Chiều cao)", "Vertical height"),
                        s.callHeight,
                        52..130,
                        action = tr("Mặc định", "Default") to { set { copy(callHeight = 82) } },
                        color = Tile.Pink,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.CALL
                        set { copy(callHeight = v) }
                    }
                }
            } else {
                SectionHeader(tr("Tùy chỉnh Thẻ cập nhật", "Update card adjustments"))
                Group {
                    GroupStepSlider(
                        Pos.First,
                        Icons.Rounded.SwapVert,
                        tr("Vị trí trên / dưới (Dọc)", "Top / bottom position"),
                        s.updateOffsetY,
                        -30..120,
                        action = tr("Mặc định", "Default") to { set { copy(updateOffsetY = 0) } },
                        color = Tile.Indigo,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateOffsetY = v) }
                    }
                    GroupStepSlider(
                        Pos.Middle,
                        Icons.Rounded.Straighten,
                        tr("Độ rộng bề ngang", "Horizontal width"),
                        s.updateWidth,
                        220..440,
                        action = tr("Mặc định", "Default") to { set { copy(updateWidth = 380) } },
                        color = Tile.Blue,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateWidth = v) }
                    }
                    GroupStepSlider(
                        Pos.Last,
                        Icons.Rounded.Height,
                        tr("Độ rộng bề dọc (Chiều cao)", "Vertical height offset"),
                        s.updateHeightExtra,
                        -20..60,
                        action = tr("Mặc định", "Default") to { set { copy(updateHeightExtra = 0) } },
                        color = Tile.Purple,
                    ) { v ->
                        IslandBus.cardPreviewMode.value = CardPreviewMode.UPDATE
                        set { copy(updateHeightExtra = v) }
                    }
                }
            }
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

@Composable
private fun CardLiveSimulationPreview(s: IslandSettings, cardMode: Int) {
    val context = LocalContext.current
    val tilt by IslandBus.glassTilt.collectAsState()
    val demoNotice = remember(context) {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        val dummy = PendingIntent.getBroadcast(
            context, 0, Intent("com.akira.island.TEST").setPackage(context.packageName), flags
        )
        Notice(
            title = "Nguyễn Lê Lý Huỳnh",
            icon = Ios.load(context, R.drawable.ios_chatbubble_ellipses),
            tint = Tints.Blue,
            style = NoticeStyle.MESSAGE,
            message = AppMessage(
                key = "sim-msg",
                pkg = context.packageName,
                appName = "Zalo · Capsule VN",
                text = tr("Mô phỏng thẻ tin nhắn trực tiếp: điều chỉnh vị trí trên/dưới, bề ngang và bề dọc", "Mô phỏng thẻ tin nhắn trực tiếp: điều chỉnh vị trí trên/dưới, bề ngang và bề dọc"),
                avatar = null,
                appIcon = NotificationParser.icon(context, context.packageName),
                intent = null,
                actions = listOf(
                    MsgAction(tr("Trả lời", "Reply"), dummy),
                    MsgAction(tr("Đánh dấu đã đọc", "Mark as read"), dummy),
                ),
            ),
        )
    }
    val demoCall = remember {
        CallInfo("sim-call", "", "Nguyễn Lê Lý Huỳnh", null, true, 0L, 0L, null, null, null, null)
    }
    val demoUpdate = remember(context) {
        Notice(
            title = tr("Đã có phiên bản 1.1.0", "Version 1.1.0 is available"),
            icon = Ios.load(context, R.drawable.ios_download),
            tint = Tints.Lavender,
            caption = tr("Tùy biến bo góc khung thông báo & kích thước thẻ cập nhật", "Customizable card corner radius & update size"),
            style = NoticeStyle.UPDATE,
            appIcon = NotificationParser.icon(context, context.packageName),
            update = com.akira.island.update.Release(
                version = "1.1.0",
                notes = tr("Tùy biến bo góc khung thông báo & kích thước thẻ cập nhật", "Customizable card corner radius & update size"),
                apkUrl = "https://github.com/KagaTetsuo/Capsule-VN/releases/download/v1.1.0/Capsule_VN.apk",
                pageUrl = "https://github.com/KagaTetsuo/Capsule-VN/releases",
                sizeBytes = 25_000_000L,
            ),
            openPkg = context.packageName,
            extraMs = 3_500,
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2E2A4F), Color(0xFF171620))))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
            .padding(top = 10.dp, bottom = 14.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .clip(CircleShape)
                .background(Tints.Green.copy(alpha = 0.16f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Tints.Green))
            Spacer(Modifier.width(6.dp))
            Text(
                tr("Mô phỏng trực tiếp: hiển thị trên Viên thuốc ở đỉnh màn hình & khung dưới", "Mô phỏng trực tiếp: hiển thị trên Viên thuốc ở đỉnh màn hình & khung dưới"),
                color = Tints.Green,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(if (cardMode == 1) 138.dp else 192.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0E0E14)),
            contentAlignment = Alignment.TopCenter,
        ) {
            val offsetDp = when (cardMode) {
                0 -> s.msgOffsetY
                1 -> s.callOffsetY
                else -> s.updateOffsetY
            }
            val visualShift = (14 + offsetDp * 0.45f).coerceIn(2f, 68f).dp
            val corner = when (cardMode) {
                1 -> s.cardCornerRadius.dp.coerceAtMost(callHeight(s) / 2)
                else -> s.cardCornerRadius.dp
            }
            val shape = islandShape(corner, s)

            Box(
                Modifier
                    .padding(top = visualShift)
                    .graphicsLayer {
                        scaleX = 0.84f
                        scaleY = 0.84f
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .clip(shape)
                    .then(
                        if (s.liquidGlass) {
                            Modifier.glassSurface(
                                shape,
                                Color(s.color),
                                s.glassOpacity / 100f,
                                s.glassRefraction / 100f,
                                s.glassSpecular / 100f,
                                tilt = { tilt },
                            )
                        } else {
                            Modifier.background(Color(s.color))
                        },
                    )
                    .then(if (s.outline) Modifier.border(s.outlineWidth.dp, Color(s.outlineColor), shape) else Modifier),
            ) {
                val act = when (cardMode) {
                    0 -> Act.NoticeA(demoNotice)
                    1 -> Act.CallA(demoCall)
                    else -> Act.NoticeA(demoUpdate)
                }
                val cardW = when (cardMode) {
                    0 -> s.msgWidth.dp
                    1 -> s.callWidth.dp
                    else -> s.updateWidth.dp
                }
                CompactContent(act, s, cardW)
            }

            Box(
                Modifier
                    .offset(y = 14.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F2937))
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
            )
        }

        Spacer(Modifier.height(6.dp))
        val summary = when (cardMode) {
            0 -> tr(
                "Vị trí dọc: ${if (s.msgOffsetY >= 0) "+${s.msgOffsetY}" else "${s.msgOffsetY}"} dp · Ngang: ${s.msgWidth} dp · Dọc: ${if (s.msgHeightExtra >= 0) "+${s.msgHeightExtra}" else "${s.msgHeightExtra}"} dp · Bo góc: ${s.cardCornerRadius} dp",
                "Offset: ${if (s.msgOffsetY >= 0) "+${s.msgOffsetY}" else "${s.msgOffsetY}"} dp · Width: ${s.msgWidth} dp · Height: ${if (s.msgHeightExtra >= 0) "+${s.msgHeightExtra}" else "${s.msgHeightExtra}"} dp · Corner: ${s.cardCornerRadius} dp",
            )
            1 -> tr(
                "Vị trí dọc: ${if (s.callOffsetY >= 0) "+${s.callOffsetY}" else "${s.callOffsetY}"} dp · Kích thước: ${s.callWidth}×${s.callHeight} dp · Bo góc: ${s.cardCornerRadius} dp",
                "Offset: ${if (s.callOffsetY >= 0) "+${s.callOffsetY}" else "${s.callOffsetY}"} dp · Size: ${s.callWidth}×${s.callHeight} dp · Corner: ${s.cardCornerRadius} dp",
            )
            else -> tr(
                "Vị trí dọc: ${if (s.updateOffsetY >= 0) "+${s.updateOffsetY}" else "${s.updateOffsetY}"} dp · Ngang: ${s.updateWidth} dp · Dọc: ${if (s.updateHeightExtra >= 0) "+${s.updateHeightExtra}" else "${s.updateHeightExtra}"} dp · Bo góc: ${s.cardCornerRadius} dp",
                "Offset: ${if (s.updateOffsetY >= 0) "+${s.updateOffsetY}" else "${s.updateOffsetY}"} dp · Width: ${s.updateWidth} dp · Height: ${if (s.updateHeightExtra >= 0) "+${s.updateHeightExtra}" else "${s.updateHeightExtra}"} dp · Corner: ${s.cardCornerRadius} dp",
            )
        }
        Text(summary, color = Ui.Sub, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
    }
}

private fun IslandSettings.withPlacement(from: IslandSettings) =
    copy(offsetX = from.offsetX, offsetY = from.offsetY, width = from.width, height = from.height, notch = from.notch)

private fun IslandSettings.withMsgCallPlacement(from: IslandSettings) = copy(
    msgOffsetY = from.msgOffsetY,
    msgWidth = from.msgWidth,
    msgHeightExtra = from.msgHeightExtra,
    msgLines = from.msgLines,
    callOffsetY = from.callOffsetY,
    callWidth = from.callWidth,
    callHeight = from.callHeight,
    updateOffsetY = from.updateOffsetY,
    updateWidth = from.updateWidth,
    updateHeightExtra = from.updateHeightExtra,
    cardCornerRadius = from.cardCornerRadius,
)
