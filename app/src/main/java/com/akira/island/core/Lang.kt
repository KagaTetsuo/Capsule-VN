package com.akira.island.core

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import com.akira.island.data.AppLanguage
import com.akira.island.data.SettingsRepo
import java.util.Locale

/**
 * Ngôn ngữ ứng dụng: Hỗ trợ Tiếng Việt và Tiếng Anh.
 * Mặc định hệ thống ưu tiên Tiếng Việt khi ngôn ngữ máy là tiếng Việt.
 */
object Lang {
    val current: AppLanguage
        get() = when (SettingsRepo.current.language) {
            AppLanguage.VI -> AppLanguage.VI
            AppLanguage.EN -> AppLanguage.EN
            AppLanguage.SYSTEM -> when (systemLanguage().lowercase()) {
                "en" -> AppLanguage.EN
                else -> AppLanguage.VI
            }
        }

    val en: Boolean get() = current == AppLanguage.EN
    val vi: Boolean get() = current == AppLanguage.VI

    val locale: Locale
        get() = when (current) {
            AppLanguage.EN -> Locale.ENGLISH
            else -> Locale("vi", "VN")
        }

    fun systemLanguage(): String =
        runCatching { Resources.getSystem().configuration.locales[0].language }.getOrNull() ?: Locale.getDefault().language
}

/** Chuỗi văn bản theo ngôn ngữ hiện tại. */
fun tr(vi: String, en: String): String = when {
    Lang.en -> en
    else -> ViStrings.translate(en, vi)
}

/**
 * Đổi ngôn ngữ (trong cài đặt hoặc hệ thống) — vẽ lại giao diện theo ngôn ngữ mới.
 */
@Composable
fun LangScope(content: @Composable () -> Unit) {
    val s by SettingsRepo.state.collectAsState()
    LocalConfiguration.current
    key(s.language, Lang.current) { content() }
}
