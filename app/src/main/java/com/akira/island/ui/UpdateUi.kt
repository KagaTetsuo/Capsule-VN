package com.akira.island.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akira.island.core.IslandBus
import com.akira.island.core.Lang
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.update.DownloadState
import com.akira.island.update.UpdateChecker
import com.akira.island.update.UpdateState
import com.akira.island.update.localNotes
import kotlinx.coroutines.launch

@Composable
fun UpdateSection() {
    val context = LocalContext.current
    val state by UpdateChecker.state.collectAsState()
    val download by UpdateChecker.download.collectAsState()
    val release = (state as? UpdateState.Available)?.release ?: return
    var notes by remember(release.version) { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Ui.Card).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(18.dp)).background(Ui.Primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SystemUpdate, null, tint = Ui.OnPrimary, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(tr("Đã có phiên bản ${release.version}", "Version ${release.version} is available"),
                    color = Ui.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                val size = if (release.sizeBytes > 0) tr(" · %.1f MB", " · %.1f MB").format(release.sizeBytes / 1_048_576.0) else ""
                Text(tr("Đang cài đặt: ${UpdateChecker.currentVersion(context)}$size", "Installed: ${UpdateChecker.currentVersion(context)}$size"),
                    color = Ui.Sub, fontSize = 14.sp)
            }
        }
        if (release.notes.isNotBlank()) {
            Row(
                Modifier.padding(top = 10.dp).clip(RoundedCornerShape(12.dp)).clickable { notes = !notes }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(tr("Có gì mới", "What's new"), color = Ui.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.Rounded.KeyboardArrowDown, null, tint = Ui.Primary,
                    modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = if (notes) 180f else 0f },
                )
            }
            AnimatedVisibility(notes) {
                ReleaseNotes(release.notes, Modifier.padding(top = 6.dp).heightIn(max = 360.dp).verticalScroll(rememberScrollState()))
            }
        }
        Spacer(Modifier.height(14.dp))
        val d = download
        val ready = UpdateChecker.readyFile(release, d)
        when {
            d is DownloadState.Running -> {
                LinearProgressIndicator(
                    progress = { d.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Ui.Primary,
                    trackColor = Ui.IconBg,
                )
                Text(tr("Đang tải xuống… ${(d.progress * 100).toInt()}%", "Downloading… ${(d.progress * 100).toInt()}%"),
                    color = Ui.Sub, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            ready != null -> {
                Text(
                    tr("Nếu trình cài đặt không mở, hãy cấp quyền cài đặt ứng dụng cho Viên thuốc rồi bấm Cài đặt lại.", "If the installer didn't open, allow the island to install apps and tap Install again."),
                    color = Ui.Sub, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 10.dp),
                )
                FilledPill(tr("Cài đặt", "Install")) { UpdateChecker.install(context, ready) }
            }
            else -> {
                if (d is DownloadState.Failed) {
                    Text(d.message, color = Color(0xFFFF8A80), fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    
                    FilledPill(tr("Cập nhật", "Update")) {
                        IslandBus.scope.launch { UpdateChecker.downloadAndInstall(context.applicationContext, release) }
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier.clip(CircleShape).clickable { UpdateChecker.skip(release.version) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    ) { Text(tr("Để sau", "Later"), color = Ui.Sub, fontSize = 15.sp, fontWeight = FontWeight.Medium) }
                }
            }
        }
    }
}

@Composable
private fun FilledPill(text: String, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape).background(Ui.Primary).clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = Ui.OnPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
}

fun updateStatusText(state: UpdateState, current: String): String = when (state) {
    UpdateState.Checking -> tr("Đang kiểm tra…", "Checking…")
    UpdateState.UpToDate -> tr("Bạn đang dùng phiên bản mới nhất $current", "You have the latest version $current")
    is UpdateState.Available -> tr("Đã có phiên bản ${state.release.version}", "Version ${state.release.version} is available")
    is UpdateState.Error -> state.message
    UpdateState.Idle -> tr("Phiên bản đang cài: $current", "Installed version: $current")
}

@Composable
fun WhatsNewDialog() {
    val context = LocalContext.current
    val current = remember { UpdateChecker.currentVersion(context) }
    val notes = remember {
        runCatching { context.assets.open(whatsNewFile()).bufferedReader().use { it.readText() } }.getOrNull()
    }
    val seen = SettingsRepo.current.lastSeenVersion
    var show by remember {
        mutableStateOf(notes != null && seen != current && (seen.isNotEmpty() || SettingsRepo.hadSavedSettings))
    }
    
    LaunchedEffect(Unit) {
        if (!show && seen != current) SettingsRepo.update { copy(lastSeenVersion = current) }
    }
    if (show && notes != null) {
        NotesDialog {
            SettingsRepo.update { copy(lastSeenVersion = current) }
            show = false
        }
    }
}

@Composable
fun NotesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val current = remember { UpdateChecker.currentVersion(context) }
    val notes = remember {
        runCatching { context.assets.open(whatsNewFile()).bufferedReader().use { it.readText() } }.getOrNull()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Ui.Card,
        titleContentColor = Ui.Text,
        title = { Text(tr("Có gì mới trong $current", "What's new in $current")) },
        text = {
            if (notes != null) {
                ReleaseNotes(notes, Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()))
            } else {
                Text(tr("Không tìm thấy danh sách thay đổi", "No list of changes found"), color = Ui.Sub, fontSize = 14.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(tr("Đã hiểu", "Got it"), color = Ui.Primary) } },
    )
}

@Composable
fun ReleaseNotes(markdown: String, modifier: Modifier = Modifier) {
    
    val lines = localNotes(markdown)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.isBlank() || line.startsWith("## ") || line.startsWith("# ") || line == "---" ||
                    line.startsWith("**Cài đặt") || line.startsWith("**Install") -> Unit
                line.startsWith("### ") -> Text(
                    line.removePrefix("### "), color = Ui.Primary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp),
                )
                line.startsWith("- ") || line.startsWith("* ") -> Row {
                    Text("•", color = Ui.Sub, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(inline(line.drop(2)), color = Ui.Sub, fontSize = 14.sp, lineHeight = 20.sp)
                }
                else -> Text(inline(line), color = Ui.Sub, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

private val link = Regex("""\[([^\]]+)]\([^)]+\)""")

private fun inline(text: String): AnnotatedString = buildAnnotatedString {
    val clean = link.replace(text.replace("`", ""), "$1")
    clean.split("**").forEachIndexed { i, part ->
        if (i % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Ui.Text)) { append(part) } else append(part)
    }
}

private fun whatsNewFile() = if (Lang.en) "whatsnew.en.md" else "whatsnew.md"
