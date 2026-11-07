package com.akira.island.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.IslandBus
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.data.CardBg
import com.akira.island.data.IslandSettings
import com.akira.island.data.Place
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.ChoiceDialog
import com.akira.island.ui.Group
import com.akira.island.ui.GroupItem
import com.akira.island.ui.GroupSlider
import com.akira.island.ui.Page
import com.akira.island.ui.Pos
import com.akira.island.ui.SectionHeader
import com.akira.island.ui.Tile
import com.akira.island.ui.Ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun LivePreviewHint(text: String) {
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

/**
 * Kiểu "Nền thẻ" (màu, ảnh, Liquid Glass, độ đậm). Mỗi loại thông báo tự chọn có dùng nền thẻ hay không
 * ngay trong trang riêng của loại đó — ở đây chỉ chỉnh bản thân kiểu nền.
 */
@Composable
fun CardBgScreen(onBack: () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bgDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        IslandBus.cardPreviewMode.value = CardPreviewMode.MESSAGE
        onDispose { IslandBus.cardPreviewMode.value = CardPreviewMode.NONE }
    }
    fun set(block: IslandSettings.() -> IslandSettings) = SettingsRepo.update(block)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val path = withContext(Dispatchers.IO) { saveCardImage(context, uri) }
            if (path != null) set { copy(cardBg = CardBg.IMAGE, cardBgImage = path) }
            else Toast.makeText(context, tr("Không đọc được ảnh", "Couldn't read the image"), Toast.LENGTH_SHORT).show()
        }
    }

    Page(
        tr("Nền thẻ thông báo", "Card background"), onBack,
        subtitle = tr("Kiểu nền dùng cho các loại đang bật \"Dùng nền thẻ\" (bật / tắt trong trang riêng của từng loại)", "The background used by every place that has \"Use card background\" on"),
    ) {
        Spacer(Modifier.size(120.dp))
        val using = Place.entries.filter { it !in s.plainBgPlaces }
        LivePreviewHint(
            if (using.isEmpty()) tr("Chưa có loại nào dùng nền thẻ", "No place uses the card background yet")
            else tr("Đang dùng ở: ", "Used by: ") + using.joinToString(", ") { it.title },
        )
        SectionHeader(tr("Kiểu nền", "Style"))
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
                Pos.Last, Icons.Rounded.Opacity,
                if (s.cardBg == CardBg.GLASS) tr("Độ đậm kính", "Glass density") else tr("Độ đậm nền", "Background opacity"),
                value = s.cardBgOpacity.toFloat(), range = 20f..100f, label = "${s.cardBgOpacity}%", color = Tile.Teal,
            ) { v -> set { copy(cardBgOpacity = v.toInt()) } }
        }
        if (s.cardBg == CardBg.COLOR || s.cardBg == CardBg.GLASS) {
            SectionHeader(tr("Màu nền", "Color"))
            Swatches(cardColors, s.cardBgColor) { c -> set { copy(cardBgColor = c) } }
        }
        Spacer(Modifier.size(24.dp))
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
