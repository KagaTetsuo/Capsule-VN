package com.akira.island.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BorderStyle
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LineWeight
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import com.akira.island.data.IslandSettings
import com.akira.island.data.NoticeKind
import com.akira.island.data.NoticeTweak
import com.akira.island.data.Place
import com.akira.island.data.SettingsRepo
import com.akira.island.data.cardBgOn
import com.akira.island.data.placeBorder
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.GroupStepSlider
import com.akira.island.ui.GroupToggle
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.Screen
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui

/** Loại đang mở (màn hình không nhận đối số). */
object PlaceNav {
    var place: Place = Place.IDLE
}

fun Place.icon(): ImageVector = when (this) {
    Place.IDLE -> Icons.Rounded.AspectRatio
    Place.ACTIVE -> Icons.Rounded.GraphicEq
    Place.EXPANDED -> Icons.Rounded.DashboardCustomize
    Place.MESSAGE -> Icons.Rounded.ChatBubble
    Place.CALL -> Icons.Rounded.Call
    Place.UPDATE -> Icons.Rounded.SystemUpdate
    Place.CHARGING -> Icons.Rounded.BatteryChargingFull
    Place.AUDIO -> Icons.Rounded.Headphones
    Place.PAYMENT -> Icons.Rounded.Contactless
    Place.NAV -> Icons.Rounded.Navigation
    Place.ALARM -> Icons.Rounded.Alarm
    Place.MODE -> Icons.Rounded.NotificationsOff
    Place.SENT -> Icons.Rounded.CheckCircle
    Place.OTHER -> Icons.Rounded.Wifi
}

fun Place.tile(): Color = when (this) {
    Place.IDLE -> Tile.Indigo
    Place.ACTIVE -> Tile.Purple
    Place.EXPANDED -> Tile.Blue
    Place.MESSAGE -> Tile.Blue
    Place.CALL -> Tile.Green
    Place.UPDATE -> Tile.Pink
    Place.CHARGING -> Tile.Green
    Place.AUDIO -> Tile.Teal
    Place.PAYMENT -> Tile.Indigo
    Place.NAV -> Tile.Blue
    Place.ALARM -> Tile.Orange
    Place.MODE -> Tile.Red
    Place.SENT -> Tile.Green
    Place.OTHER -> Tile.Gray
}

/** Dòng tóm tắt dưới tên mỗi loại trong tab Giao diện. */
fun placeSummary(s: IslandSettings, p: Place): String {
    val b = s.placeBorder(p)
    val parts = listOfNotNull(
        p.hint,
        tr("nền thẻ", "card background").takeIf { s.cardBgOn(p) },
        (tr("viền ", "border ") + b.palette.title.lowercase()).takeIf { b.on },
    )
    return parts.joinToString(" · ")
}

/** Mở trang riêng của một loại. */
fun openPlace(open: (Screen) -> Unit, p: Place) {
    PlaceNav.place = p
    open(Screen.PLACE)
}

/**
 * Trang tuỳ chỉnh riêng của MỘT nơi / MỘT loại thông báo: kích thước & vị trí, nền, thời gian hiển thị,
 * đường viền — chỉ áp dụng cho đúng loại này, không dùng chung với loại nào khác.
 */
@Composable
fun PlaceScreen(onBack: () -> Unit, open: (Screen) -> Unit) {
    val place = remember { PlaceNav.place }
    val s by SettingsRepo.state.collectAsState()
    var previewOn by remember { mutableStateOf(true) }
    var calibrate by remember { mutableStateOf(false) }

    // Xem trước đúng loại này trên Viên thuốc thật (tự ẩn khi rời trang / rời app)
    DisposableEffect(previewOn, calibrate) {
        if (previewOn) {
            IslandBus.calibrating.value = place == Place.IDLE && calibrate
            IslandBus.widthPreviewUntil.value = if (place == Place.ACTIVE) SystemClock.elapsedRealtime() + 10 * 60_000L else 0L
            IslandBus.previewNoticeKind.value = place.kind
            IslandBus.cardPreviewMode.value = when {
                place == Place.EXPANDED -> CardPreviewMode.EXPANDED
                place == Place.MESSAGE -> CardPreviewMode.MESSAGE
                place == Place.CALL -> CardPreviewMode.CALL
                place == Place.UPDATE -> CardPreviewMode.UPDATE
                place.kind != null -> CardPreviewMode.NOTICE
                else -> CardPreviewMode.NONE
            }
        }
        onDispose {
            IslandBus.calibrating.value = false
            IslandBus.widthPreviewUntil.value = 0L
            IslandBus.previewNoticeKind.value = null
            IslandBus.cardPreviewMode.value = CardPreviewMode.NONE
        }
    }

    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    Page(
        place.title, onBack, subtitle = place.hint,
        action = {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(if (previewOn) Ui.Primary.copy(alpha = 0.22f) else Ui.Card)
                    .clickable { previewOn = !previewOn },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (previewOn) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    tr("Xem trước trên đỉnh màn hình", "Preview at the top"),
                    tint = if (previewOn) Ui.Primary else Ui.Sub, modifier = Modifier.size(20.dp),
                )
            }
        },
    ) {
        // Chừa chỗ cho bản xem trước ở đỉnh màn hình để không che các thanh chỉnh (nút 👁 để ẩn)
        if (previewOn) {
            val space = previewSpace(s, place)
            if (space > 0) Spacer(Modifier.height(space.dp))
            LivePreviewHint(
                tr("Đang xem trước trên đỉnh màn hình — kéo thanh trượt thấy ngay. Nút 👁 ở góc trên để ẩn / hiện.",
                    "Live preview at the top — drag a slider to see it. Tap 👁 to hide / show."),
            )
        }

        SectionHeader(tr("Kích thước & vị trí", "Size & position"))
        when (place) {
            Place.IDLE -> IdleSize(s, calibrate) { calibrate = it }
            Place.ACTIVE -> ActiveSize(s)
            Place.EXPANDED -> ExpandedSize(s)
            Place.MESSAGE -> MessageSize(s)
            Place.CALL -> CallSize(s)
            Place.UPDATE -> UpdateSize(s)
            else -> KindSize(s, place.kind ?: NoticeKind.OTHER)
        }

        SectionHeader(tr("Nền", "Background"))
        Group {
            GroupToggle(
                Pos.First, Icons.Rounded.Wallpaper, tr("Dùng nền thẻ", "Use the card background"),
                tr("Bật: dùng nền màu / ảnh / Liquid Glass của \"Nền thẻ thông báo\". Tắt: nền đen thường", "On: the color / image / glass card background. Off: plain black"),
                s.cardBgOn(place), color = Tile.Orange,
            ) { v -> set { copy(plainBgPlaces = if (v) plainBgPlaces - place else plainBgPlaces + place) } }
            GroupItem(
                Pos.Last, Icons.Rounded.Palette, tr("Kiểu nền thẻ", "Card background style"), s.cardBg.title,
                color = Tile.Pink, onClick = { open(Screen.CARD_BG) },
            ) { Arrow() }
        }

        place.durationKey?.let { key ->
            SectionHeader(tr("Thời gian hiển thị", "Display time"))
            val own = s.noticeDurations[key]
            val ms = own ?: s.noticeDurationMs
            Group {
                GroupStepSlider(
                    Pos.Single, Icons.Rounded.Schedule, tr("Hiện trong", "Show for"), ms / 100, 10..120,
                    action = if (own != null) tr("Dùng mặc định", "Use default") to { set { copy(noticeDurations = noticeDurations - key) } } else null,
                    color = Tile.Orange,
                    valueText = tr("%.1f giây", "%.1f s").format(ms / 1000f) + if (own == null) tr(" · mặc định", " · default") else "",
                ) { v -> set { copy(noticeDurations = noticeDurations + (key to v * 100)) } }
            }
        }

        SectionHeader(tr("Đường viền", "Border"))
        val spec = s.placeBorder(place)
        BorderPreview(spec, null, place.title)
        BorderEditor(spec, null, tr("Đường viền riêng", "Own border")) { b -> set { copy(placeBorders = placeBorders + (place to b)) } }
        Spacer(Modifier.height(24.dp))
    }
}

/** Bản xem trước nằm ở đỉnh màn hình cao bao nhiêu (dp, phần vượt quá tiêu đề trang). */
private fun previewSpace(s: IslandSettings, p: Place): Int = when (p) {
    Place.EXPANDED -> 150 + s.expandedHeightExtra + s.expandedOffsetY
    Place.MESSAGE -> 40 + s.msgLines * 20 + s.msgHeightExtra + s.msgOffsetY
    Place.CALL -> s.callHeight + s.callOffsetY - 40
    Place.UPDATE -> 90 + s.updateHeightExtra + s.updateOffsetY
    else -> 0
}.coerceIn(0, 360)

@Composable
private fun IdleSize(s: IslandSettings, calibrate: Boolean, onCalibrate: (Boolean) -> Unit) {
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)
    Group {
        GroupToggle(
            Pos.First, Icons.Rounded.AspectRatio, tr("Tô đỏ để căn quanh camera", "Red overlay to align"),
            tr("Viên thuốc chuyển màu đỏ viền trắng để căn đúng lỗ camera", "The island turns red with a white outline to align with the camera"),
            calibrate, color = Tile.Red,
        ) { onCalibrate(it) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), s.offsetX, -200..200,
            action = tr("Chính giữa", "Center") to { set { copy(offsetX = 0) } }, color = Tile.Blue,
        ) { v -> set { copy(offsetX = v) } }
        if (!s.notch) {
            GroupStepSlider(Pos.Middle, Icons.Rounded.SwapVert, tr("Vị trí dọc (cách mép trên)", "Vertical position (from top)"), s.offsetY, -10..120, color = Tile.Indigo) { v ->
                set { copy(offsetY = v) }
            }
        }
        GroupToggle(
            Pos.Middle, Icons.Rounded.WaterDrop, tr("Kiểu tai thỏ (khuyết đỉnh)", "Like a notch"),
            tr("Đối với camera giọt nước: Viên thuốc áp sát đỉnh màn hình và che phần khuyết", "For a waterdrop camera: the island sits against the top of the screen"),
            s.notch, color = Tile.Teal,
        ) { v -> set { copy(notch = v) } }
        GroupStepSlider(Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.width, 15..170, color = Tile.Purple) { v ->
            set { copy(width = v) }
        }
        GroupStepSlider(Pos.Middle, Icons.Rounded.Height, tr("Chiều cao", "Height"), s.height, 15..50, color = Tile.Pink) { v ->
            set { copy(height = v) }
        }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"),
            if (s.capsuleCornerRadius < 0) s.height / 2 else s.capsuleCornerRadius, 0..25,
            action = tr("Tự động (tròn đều)", "Auto (fully round)") to { set { copy(capsuleCornerRadius = -1) } },
            valueText = if (s.capsuleCornerRadius < 0) tr("Tự động", "Auto") else null, color = Tile.Yellow,
        ) { v -> set { copy(capsuleCornerRadius = v) } }
    }
}

@Composable
private fun ActiveSize(s: IslandSettings) {
    fun set(block: IslandSettings.() -> IslandSettings) {
        IslandBus.widthPreviewUntil.value = maxOf(IslandBus.widthPreviewUntil.value, SystemClock.elapsedRealtime() + 2_500)
        SettingsRepo.update(block)
    }
    Group {
        GroupSlider(
            Pos.First, Icons.Rounded.LineWeight, tr("Độ dày", "Thickness"),
            value = s.activeExtra.toFloat(), range = 0f..14f, label = "+${s.activeExtra} dp", steps = 13, color = Tile.Purple,
        ) { v -> set { copy(activeExtra = v.toInt()) } }
        GroupSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"),
            value = s.activeWidth.toFloat(), range = 0f..120f, steps = 23,
            label = if (s.activeWidth == 0) tr("Tự động theo nội dung", "Auto (fits content)") else "+${s.activeWidth} dp", color = Tile.Blue,
        ) { v -> set { copy(activeWidth = (v / 5).toInt() * 5) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), s.activeOffsetY, -30..60,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(activeOffsetY = 0) } }, color = Tile.Indigo,
        ) { v -> set { copy(activeOffsetY = v) } }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), s.activeOffsetX, -120..120,
            action = tr("Chính giữa", "Center") to { set { copy(activeOffsetX = 0) } }, color = Tile.Teal,
        ) { v -> set { copy(activeOffsetX = v) } }
    }
}

@Composable
private fun ExpandedSize(s: IslandSettings) {
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)
    val autoWidth = (LocalConfiguration.current.screenWidthDp - 22).coerceAtMost(460)
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), s.expandedOffsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(expandedOffsetY = 0) } }, color = Tile.Indigo,
        ) { v -> set { copy(expandedOffsetY = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"),
            if (s.expandedWidth == 0) autoWidth.coerceIn(260, 460) else s.expandedWidth, 260..460,
            action = tr("Tự động theo màn hình", "Auto (screen width)") to { set { copy(expandedWidth = 0) } }, color = Tile.Blue,
            valueText = if (s.expandedWidth == 0) tr("Tự động", "Auto") else null,
        ) { v -> set { copy(expandedWidth = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), s.expandedHeightExtra, -20..60,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(expandedHeightExtra = 0) } }, color = Tile.Purple,
        ) { v -> set { copy(expandedHeightExtra = v) } }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.Layers, tr("Bo góc", "Corner radius"), s.expandedCornerRadius, 12..50,
            action = tr("Mặc định 44 dp", "Default 44 dp") to { set { copy(expandedCornerRadius = 44) } }, color = Tile.Orange,
        ) { v -> set { copy(expandedCornerRadius = v) } }
    }
}

@Composable
private fun MessageSize(s: IslandSettings) {
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), s.msgOffsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(msgOffsetY = 0) } }, color = Tile.Indigo,
        ) { v -> set { copy(msgOffsetY = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.msgWidth, 220..440,
            action = tr("Mặc định 380 dp", "Default 380 dp") to { set { copy(msgWidth = 380) } }, color = Tile.Blue,
        ) { v -> set { copy(msgWidth = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), s.msgHeightExtra, -15..60,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(msgHeightExtra = 0) } }, color = Tile.Purple,
        ) { v -> set { copy(msgHeightExtra = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.FormatLineSpacing, tr("Số dòng nội dung", "Text lines"), s.msgLines, 1..6,
            unit = tr("dòng", "lines"), action = tr("Mặc định 3 dòng", "Default 3 lines") to { set { copy(msgLines = 3) } }, color = Tile.Teal,
        ) { v -> set { copy(msgLines = v) } }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"), s.cardCornerRadius, 8..44,
            action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(cardCornerRadius = 32) } }, color = Tile.Yellow,
        ) { v -> set { copy(cardCornerRadius = v) } }
    }
}

@Composable
private fun CallSize(s: IslandSettings) {
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), s.callOffsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(callOffsetY = 0) } }, color = Tile.Indigo,
        ) { v -> set { copy(callOffsetY = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.callWidth, 220..440,
            action = tr("Mặc định 370 dp", "Default 370 dp") to { set { copy(callWidth = 370) } }, color = Tile.Green,
        ) { v -> set { copy(callWidth = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao", "Height"), s.callHeight, 52..130,
            action = tr("Mặc định 82 dp", "Default 82 dp") to { set { copy(callHeight = 82) } }, color = Tile.Pink,
        ) { v -> set { copy(callHeight = v) } }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"), s.callCornerRadius, 8..50,
            action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(callCornerRadius = 32) } }, color = Tile.Yellow,
        ) { v -> set { copy(callCornerRadius = v) } }
    }
}

@Composable
private fun UpdateSize(s: IslandSettings) {
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), s.updateOffsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(updateOffsetY = 0) } }, color = Tile.Indigo,
        ) { v -> set { copy(updateOffsetY = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"), s.updateWidth, 220..440,
            action = tr("Mặc định 380 dp", "Default 380 dp") to { set { copy(updateWidth = 380) } }, color = Tile.Blue,
        ) { v -> set { copy(updateWidth = v) } }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), s.updateHeightExtra, -20..60,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { set { copy(updateHeightExtra = 0) } }, color = Tile.Purple,
        ) { v -> set { copy(updateHeightExtra = v) } }
        GroupStepSlider(
            Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"), s.updateCornerRadius, 8..44,
            action = tr("Mặc định 32 dp", "Default 32 dp") to { set { copy(updateCornerRadius = 32) } }, color = Tile.Yellow,
        ) { v -> set { copy(updateCornerRadius = v) } }
    }
}

/** Vị trí / kích thước riêng của một loại thông báo sự kiện hoặc viên nhỏ. */
@Composable
private fun KindSize(s: IslandSettings, kind: NoticeKind) {
    val own = s.noticeTweaks[kind]
    val pill = kind == NoticeKind.MODE
    val cur = own ?: if (pill || kind == NoticeKind.SENT) s.noticeGeneral.copy(width = 0, heightExtra = 0, corner = -1) else s.noticeGeneral
    val autoW = (LocalConfiguration.current.screenWidthDp - 22).coerceAtMost(460)
    val widths = when (kind) {
        NoticeKind.MODE -> 60..360
        NoticeKind.SENT -> 120..460
        else -> 200..460
    }
    fun put(tw: NoticeTweak) = SettingsRepo.update { copy(noticeTweaks = noticeTweaks + (kind to tw)) }
    Group {
        GroupStepSlider(
            Pos.First, Icons.Rounded.SwapVert, tr("Vị trí dọc (lên / xuống)", "Vertical position"), cur.offsetY, -30..120,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { put(cur.copy(offsetY = 0)) }, color = Tile.Indigo,
        ) { v -> put(cur.copy(offsetY = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.SwapHoriz, tr("Vị trí ngang (trái / phải)", "Horizontal position"), cur.offsetX, -120..120,
            action = tr("Chính giữa", "Center") to { put(cur.copy(offsetX = 0)) }, color = Tile.Teal,
        ) { v -> put(cur.copy(offsetX = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Straighten, tr("Chiều rộng", "Width"),
            when {
                cur.width > 0 -> cur.width.coerceIn(widths.first, widths.last)
                pill -> 120
                else -> autoW.coerceIn(widths.first, widths.last)
            },
            widths,
            action = (if (pill) tr("Tự động theo nội dung", "Auto (fits content)") else tr("Tự động theo màn hình", "Auto (screen width)")) to { put(cur.copy(width = 0)) },
            color = Tile.Blue, valueText = if (cur.width == 0) tr("Tự động", "Auto") else null,
        ) { v -> put(cur.copy(width = v)) }
        GroupStepSlider(
            Pos.Middle, Icons.Rounded.Height, tr("Chiều cao thêm", "Extra height"), cur.heightExtra, if (pill) -12..30 else -16..40,
            action = tr("Mặc định 0 dp", "Default 0 dp") to { put(cur.copy(heightExtra = 0)) }, color = Tile.Purple,
        ) { v -> put(cur.copy(heightExtra = v)) }
        GroupStepSlider(
            if (own != null) Pos.Middle else Pos.Last, Icons.Rounded.BorderStyle, tr("Bo góc", "Corner radius"),
            if (cur.corner < 0) (if (pill) 20 else 28) else cur.corner, if (pill) 0..30 else 0..44,
            action = tr("Tự động (tròn đều)", "Auto (fully round)") to { put(cur.copy(corner = -1)) }, color = Tile.Yellow,
            valueText = if (cur.corner < 0) tr("Tự động", "Auto") else null,
        ) { v -> put(cur.copy(corner = v)) }
        if (own != null) {
            GroupItem(
                Pos.Last, Icons.Rounded.Restore, tr("Đặt lại kích thước & vị trí", "Reset size & position"),
                tr("Về kích thước mặc định ban đầu", "Back to the default size"),
                color = Tile.Gray, onClick = { SettingsRepo.update { copy(noticeTweaks = noticeTweaks - kind) } },
            )
        }
    }
}
