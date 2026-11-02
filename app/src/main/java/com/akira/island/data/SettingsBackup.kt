package com.akira.island.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

/**
 * Tự động sao lưu cấu hình ra Download/CapsuleVN/CapsuleVN-caidat.json.
 * File nằm ở bộ nhớ chung nên vẫn còn sau khi gỡ ứng dụng — cài lại chỉ cần chọn file là phục hồi toàn bộ thiết lập.
 * (Android 10+; máy cũ hơn dùng Sao lưu cài đặt thủ công.)
 */
object SettingsBackup {
    const val FOLDER = "CapsuleVN"
    const val FILE = "CapsuleVN-caidat.json"
    private const val PREFS = "backup"
    private const val KEY_URI = "uri"

    val supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /** Ghi đè bản sao lưu tự động (tạo mới nếu chưa có hoặc file cũ đã bị xoá). */
    fun write(context: Context, json: String): Boolean {
        if (!supported) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val bytes = json.toByteArray()
        val saved = prefs.getString(KEY_URI, null)?.let(Uri::parse)
        if (saved != null && runCatching {
                context.contentResolver.openOutputStream(saved, "wt")?.use { it.write(bytes) } != null
            }.getOrDefault(false)
        ) return true
        return runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, FILE)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/" + FOLDER)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
            context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(bytes) } ?: return false
            prefs.edit().putString(KEY_URI, uri.toString()).apply()
            true
        }.getOrDefault(false)
    }

    /** Thư mục mở sẵn khi chọn file khôi phục. */
    val initialFolder: Uri?
        get() = runCatching {
            DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:" + Environment.DIRECTORY_DOWNLOADS + "/" + FOLDER,
            )
        }.getOrNull()
}
