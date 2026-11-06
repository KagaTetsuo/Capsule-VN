package com.akira.island.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.tr
import com.akira.island.data.Surface
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.Screen
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import com.akira.island.ui.posOf
import java.text.Normalizer

/** Một mục có thể tìm thấy: tên, mô tả, từ khoá và màn hình sẽ mở. */
private class Entry(val title: String, val hint: String, val keys: String, val screen: Screen, val surface: Surface? = null)

/** Bỏ dấu tiếng Việt để gõ "vien" vẫn ra "viền". */
private fun fold(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").replace('đ', 'd')

private fun entries(): List<Entry> = listOf(
    Entry(tr("Tùy chỉnh theo từng nơi", "Customize each place"), tr("Kích thước, vị trí, nền, đường viền từng nơi", "Size, position, background, border per place"),
        "vien duong vien border hieu ung xoay 360 nhap nhay loang mau led rgb don sac da sac glow quang sang", Screen.SURFACES),
    Entry(Surface.IDLE.title, tr("Viên thuốc khi không có hoạt động", "When nothing is happening"), "binh thuong idle vien nen", Screen.SURFACES, Surface.IDLE),
    Entry(Surface.ACTIVE.title, Surface.ACTIVE.hint, "hoat dong nhac hen gio dieu huong vien chieu rong", Screen.SURFACES, Surface.ACTIVE),
    Entry(Surface.PILL.title, tr("Kích thước, vị trí, viền thông báo viên nhỏ", "Size, position, border of small pills"),
        "vien nho pill che do chuong im lang rung da gui kich thuoc vi tri", Screen.SURFACES, Surface.PILL),
    Entry(Surface.CARD.title, tr("Viền & nền khung thẻ thông báo", "Card border & background"), "khung the tin nhan cuoc goi vien the", Screen.SURFACES, Surface.CARD),
    Entry(Surface.EXPANDED.title, tr("Viền, nền, kích thước bảng mở rộng", "Expanded board border, background, size"), "bang mo rong gio pin am lich expanded", Screen.SURFACES, Surface.EXPANDED),
    Entry(tr("Vị trí & kích thước", "Position & size"), tr("Căn Viên thuốc quanh camera", "Align the island around the camera"),
        "vi tri kich thuoc camera can chinh lech rong cao bo goc tai tho", Screen.SIZE),
    Entry(tr("Khi có hoạt động", "With activity"), tr("Độ dày, chiều rộng, vị trí khi đang có nhạc, hẹn giờ…", "Thickness, width, position while active"),
        "chieu rong hoat dong do day nhac", Screen.LOOK),
    Entry(tr("Khung thẻ thông báo", "Notification cards"), tr("Tin nhắn, cuộc gọi, cập nhật: kích thước, số dòng, nền ảnh / kính", "Messages, calls, updates: size, lines, image / glass"),
        "khung the tin nhan cuoc goi cap nhat so dong nen anh kinh liquid glass bo goc", Screen.MSG_CALL_SIZE),
    Entry(tr("Thời gian hiển thị", "Display time"), tr("Mỗi loại thông báo hiện nhanh / chậm riêng", "Fast / slow per kind of alert"),
        "thoi gian hien thi nhanh cham giay duration", Screen.DURATIONS),
    Entry(tr("Màu sắc & viền", "Color & border"), tr("Màu nền, bóng đổ", "Color, shadow"), "mau sac mau nen bong do", Screen.COLORS),
    Entry(tr("Vòng sáng LED", "LED ring"), tr("Đèn báo sự kiện, tin chưa đọc, đang sạc", "Light for events, unread, charging"),
        "led vong sang den bao chua doc sac rgb", Screen.LED_RING),
    Entry(tr("Mở rộng & độ đàn hồi", "Expansion & elasticity"), tr("Kiểu chuyển động, tốc độ", "Motion style, speed"),
        "chuyen dong animation dan hoi toc do chat long", Screen.ANIM),
    Entry(tr("Hiệu ứng đặc biệt", "Special effects"), tr("Sóng nhạc, phát sáng, rung, khung hình", "Equalizer, glow, shake, frame rate"),
        "hieu ung song nhac phat sang rung fps khung hinh giam hieu ung tiet kiem pin", Screen.EFFECTS),
    Entry(tr("Thẻ hoạt động", "Live activity cards"), tr("Nhạc, hẹn giờ, đèn pin, điều hướng, tải xuống", "Music, timers, flashlight, navigation, downloads"),
        "the hoat dong nhac hen gio den pin dieu huong tai xuong ghi am", Screen.CARDS),
    Entry(tr("Tin nhắn & thông báo", "Messages & notifications"), tr("Chọn app, trả lời nhanh, ẩn nội dung", "Apps, quick reply, hide content"),
        "tin nhan thong bao zalo messenger telegram tra loi nhanh an noi dung trung lap", Screen.NOTIFICATIONS),
    Entry(tr("Biểu ngữ hệ thống (chống hiện 2 lần)", "System banners (no duplicates)"), tr("Tắt pop-up của app để không trùng", "Turn off app pop-ups to avoid duplicates"),
        "bieu ngu pop up trung lap hien 2 lan heads up samsung status bar", Screen.BANNERS),
    Entry(tr("Sự kiện hệ thống", "System events"), tr("Sạc, Wi-Fi, tai nghe, thanh toán, báo thức", "Charging, Wi-Fi, headphones, payments, alarm"),
        "su kien sac pin wifi tai nghe bluetooth thanh toan nfc bao thuc che do chuong", Screen.EVENTS),
    Entry(tr("Lời bài hát (Karaoke)", "Synced lyrics"), "", "loi bai hat karaoke lyrics", Screen.LYRICS),
    Entry(tr("Mã OTP 1 chạm", "1-tap OTP"), "", "otp ma xac thuc sms ngan hang", Screen.OTP_SETTINGS),
    Entry(tr("Đọc thông báo qua tai nghe", "Read notifications aloud"), "", "doc thong bao giong noi tai nghe tts", Screen.TTS_SETTINGS),
    Entry(tr("Chế độ Gaming & HUD", "Gaming Mode & HUD"), "", "game gaming hud nhiet do ram", Screen.GAMING),
    Entry(tr("Hệ sinh thái Samsung & Android", "Samsung & Android ecosystem"), "", "s pen buds watch dex sac sieu nhanh bao ve pin", Screen.ECOSYSTEM),
    Entry(tr("Cử chỉ chạm & vuốt", "Touch & swipe gestures"), tr("Chạm, chạm đúp, nhấn giữ, vuốt", "Tap, double tap, hold, swipe"),
        "cu chi cham cham dup nhan giu vuot mo app", Screen.GESTURES),
    Entry(tr("Hành vi hiển thị", "Display behavior"), tr("Ẩn khi toàn màn hình, màn hình tắt / AOD, giờ im lặng", "Fullscreen, screen off / AOD, quiet hours"),
        "hanh vi an toan man hinh xoay ngang man hinh khoa aod bat sang man hinh gio im lang", Screen.BEHAVIOR),
    Entry(tr("Ngân hàng & VNeID", "Banks & VNeID"), tr("Tự tạm tắt khi mở app ngân hàng", "Pause automatically in banking apps"),
        "ngan hang bank vneid shizuku tu tat tro nang", Screen.BANKS),
    Entry(tr("Giữ Viên thuốc hoạt động ổn định", "Keep the island running"), tr("Chống bị hệ thống tắt", "Stop the system from killing it"),
        "bi tat chay nen toi uu pin on dinh keep alive", Screen.KEEPALIVE),
    Entry(tr("Quyền truy cập", "Permissions"), "", "quyen truy cap permission thong bao tro nang", Screen.PERMISSIONS),
    Entry(tr("Thử nghiệm tính năng", "Try features"), "", "thu nghiem test demo xem thu", Screen.TESTS),
    Entry(tr("Hướng dẫn", "Guide"), "", "huong dan cach dung", Screen.INSTRUCTION),
)

/**
 * Ô tìm kiếm tính năng: gõ không dấu cũng được ("vien", "otp", "trung lap"…).
 * Trả về true khi đang có kết quả (để màn hình ẩn bớt nội dung khác nếu muốn).
 */
@Composable
fun FeatureSearch(open: (Screen) -> Unit) {
    var q by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val all = remember(com.akira.island.core.Lang.locale) { entries() }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Ui.Card)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = Ui.Sub, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (q.isEmpty()) Text(tr("Tìm tính năng: viền, OTP, trùng lặp…", "Search: border, OTP, duplicates…"), color = Ui.Sub, fontSize = 15.sp)
            BasicTextField(
                q, { q = it }, singleLine = true,
                textStyle = TextStyle(color = Ui.Text, fontSize = 15.sp),
                cursorBrush = SolidColor(Ui.Primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (q.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close, tr("Xoá", "Clear"), tint = Ui.Sub,
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).clickable { q = "" },
            )
        }
    }
    if (q.isBlank()) return
    val words = fold(q).split(' ').filter { it.isNotBlank() }
    val hits = all.filter { e ->
        val hay = fold(e.title + " " + e.hint) + " " + e.keys
        words.all { hay.contains(it) }
    }
    if (hits.isEmpty()) {
        Text(tr("Không tìm thấy tính năng phù hợp", "Nothing found"), color = Ui.Sub, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
        return
    }
    Group {
        hits.forEachIndexed { i, e ->
            GroupItem(
                posOf(i, hits.size), Icons.Rounded.Search, e.title, e.hint.ifBlank { null }, color = Tile.Indigo,
                onClick = {
                    focus.clearFocus()
                    e.surface?.let { SurfaceNav.initial = it }
                    if (e.screen == Screen.SURFACES && e.surface == null) SurfaceNav.initial = Surface.IDLE
                    open(e.screen)
                },
            ) { Arrow() }
        }
    }
}
