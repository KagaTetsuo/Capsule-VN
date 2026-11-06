package com.akira.island.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.app.Person
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette
import androidx.core.graphics.drawable.toBitmap
import com.akira.island.core.AppMessage
import com.akira.island.core.IslandBus
import com.akira.island.core.MsgAction
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.R
import com.akira.island.data.AccentMode
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Ios

object Messages {
    
    private val shown = object : LinkedHashMap<String, String>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?) = size > 100
    }

    private val repliedAt = HashMap<String, Long>()

    private val skipCategories = setOf(
        Notification.CATEGORY_TRANSPORT,
        Notification.CATEGORY_CALL,
        Notification.CATEGORY_PROGRESS,
        Notification.CATEGORY_SERVICE,
        Notification.CATEGORY_SYSTEM,
        Notification.CATEGORY_STATUS,
        Notification.CATEGORY_NAVIGATION,
        Notification.CATEGORY_STOPWATCH,
        Notification.CATEGORY_LOCATION_SHARING,
    )

    fun maybeShow(service: NotificationListenerService, sbn: StatusBarNotification, appName: String) {
        val s = SettingsRepo.current
        if (!s.cardMessages || !s.enabled) return
        // Thông báo của hệ thống (pin yếu, USB…) không phải tin nhắn — đã có thẻ sự kiện riêng
        if (sbn.packageName == SystemBanners.SYSTEM_UI || sbn.packageName == SystemBanners.ANDROID) return
        val n = sbn.notification ?: return
        if (sbn.isOngoing) return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
        if (n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return
        if (n.category in skipCategories) return
        
        if (!s.msgInApp && IslandBus.foregroundPackage.value == sbn.packageName) return

        val ranking = NotificationListenerService.Ranking()
        val ranked = runCatching { service.currentRanking?.getRanking(sbn.key, ranking) == true }.getOrDefault(false)
        if (ranked) {
            if (ranking.importance < NotificationManager.IMPORTANCE_DEFAULT) return
            if (!ranking.matchesInterruptionFilter()) return
        }

        
        val locked = IslandBus.locked.value
        if (locked && !Lockscreen.showsNotifications(service)) return
        val visibility = if (ranked && ranking.lockscreenVisibilityOverride != NotificationListenerService.Ranking.VISIBILITY_NO_OVERRIDE) {
            ranking.lockscreenVisibilityOverride
        } else {
            n.visibility
        }
        val secret = visibility == Notification.VISIBILITY_SECRET
        if (locked && secret) return
        val sensitive = Lockscreen.isSecure(service) &&
            (secret || visibility == Notification.VISIBILITY_PRIVATE && !Lockscreen.showsPrivate(service))

        val ex = n.extras ?: return
        
        if (ownLastMessage(ex)) return
        val replied = repliedAt[sbn.key]
        if (replied != null && SystemClock.elapsedRealtime() - replied < 5_000) return
        val title = (ex.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            ?: ex.getCharSequence(Notification.EXTRA_TITLE))?.toString()?.trim().orEmpty()
        val text = lastMessage(ex)
            ?: (ex.getCharSequence(Notification.EXTRA_TEXT) ?: ex.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString()?.trim()
            ?: ""
        if (title.isEmpty() && text.isEmpty()) return

        val signature = "$title\n$text"
        val previous = shown[sbn.key]
        if (previous == signature) return
        
        if (previous != null && n.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0) return
        shown[sbn.key] = signature

        val avatar = runCatching {
            n.getLargeIcon()?.loadDrawable(service)?.toBitmap(128, 128)?.asImageBitmap()
        }.getOrNull()
        val actions = if (s.msgActions) buttons(n) else emptyList()
        val otpCode = if (s.otpEnabled && !sensitive) OtpHelper.extractOtp(title, text) else null
        if (otpCode != null && s.otpAutoCopy && !IslandBus.locked.value) {
            OtpHelper.copyToClipboard(service, otpCode)
        }
        VoiceAnnouncer.announceMessage(service, appName, title.ifEmpty { appName }, text, sensitive)

        val message = AppMessage(
            key = sbn.key,
            pkg = sbn.packageName,
            appName = appName,
            text = text,
            avatar = avatar,
            appIcon = NotificationParser.icon(service, sbn.packageName),
            intent = n.contentIntent,
            actions = actions,
            
            accent = if (s.accentMode == AccentMode.ADAPTIVE) accentOf(avatar ?: NotificationParser.icon(service, sbn.packageName)) else null,
            history = history(ex),
            sensitive = sensitive,
            otpCode = otpCode,
        )
        val brandColor = appBrandColor(sbn.packageName, message.accent)
        val card = Notice(
            title = title.ifEmpty { appName },
            icon = Ios.load(service, R.drawable.ios_chatbubble_ellipses),
            tint = brandColor,
            style = NoticeStyle.MESSAGE,
            message = message,
            extraMs = if (actions.isEmpty()) 2_000 else 4_500,
        )
        IslandBus.setUnread(card)

        val appPopsUp = !ranked || ranking.importance >= NotificationManager.IMPORTANCE_HIGH
        val compact = (IslandBus.gamingActive.value && s.gameModeEnabled && s.gameSuppressPopups) ||
            // Ứng dụng tự hiện pop-up (không tắt được trên một số máy): Viên thuốc chỉ báo gọn để không bị 2 lần
            (s.msgCompactWhenPopup && appPopsUp && ranked && sbn.packageName !in s.popupOffApps)
        if (compact) {
            val shortBody = if (text.length > 22) text.take(22) + "…" else text
            IslandBus.post(
                Notice(
                    title = title.ifEmpty { appName },
                    state = shortBody.ifBlank { appName },
                    icon = Ios.load(service, R.drawable.ios_chatbubble_ellipses),
                    tint = brandColor,
                    style = NoticeStyle.PILL,
                    openPkg = sbn.packageName,
                ),
            )
            SystemBanners.onMessage(sbn.packageName, n.channelId, popsUp = !ranked || ranking.importance >= NotificationManager.IMPORTANCE_HIGH)
            return
        }

        IslandBus.post(card)
        
        SystemBanners.onMessage(sbn.packageName, n.channelId, popsUp = !ranked || ranking.importance >= NotificationManager.IMPORTANCE_HIGH)
    }

    private fun buttons(n: Notification): List<MsgAction> {
        val all = n.actions?.mapNotNull { a ->
            val intent = a.actionIntent ?: return@mapNotNull null
            val title = a.title?.toString()?.trim().orEmpty()
            if (title.isEmpty()) return@mapNotNull null
            val inputs = a.remoteInputs?.filter { it.allowFreeFormInput }.orEmpty()
            MsgAction(title, intent, inputs)
        }.orEmpty()
        val reply = all.firstOrNull { it.isReply }
        val read = all.firstOrNull { a ->
            !a.isReply && listOf("đã đọc", "read").any { it in a.title.lowercase() }
        }
        val other = all.firstOrNull { !it.isReply }
        return listOfNotNull(reply, read ?: other)
    }

    private fun history(ex: Bundle): List<String> {
        @Suppress("DEPRECATION")
        val arr: Array<Parcelable> = ex.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return emptyList()
        val group = ex.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        return arr.mapNotNull { p ->
            val b = p as? Bundle ?: return@mapNotNull null
            val text = b.getCharSequence("text")?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return@mapNotNull null
            val sender = b.getCharSequence("sender")?.toString()
            if (group && !sender.isNullOrBlank()) "$sender: $text" else text
        }.takeLast(6)
    }

    private fun accentOf(image: ImageBitmap?): Color? {
        image ?: return null
        return runCatching {
            val palette = Palette.from(image.asAndroidBitmap()).maximumColorCount(12).generate()
            val rgb = palette.getVibrantColor(palette.getLightVibrantColor(palette.getDominantColor(0)))
            if (rgb == 0) return null
            val c = Color(rgb)
            
            if (c.luminance() < 0.4f) lerp(c, Color.White, 0.45f) else c
        }.getOrNull()
    }

    private fun lastMessage(ex: Bundle): String? {
        @Suppress("DEPRECATION")
        val arr: Array<Parcelable>? = ex.getParcelableArray(Notification.EXTRA_MESSAGES)
        val last = arr?.lastOrNull() as? Bundle ?: return null
        val text = last.getCharSequence("text")?.toString()?.trim() ?: return null
        val group = ex.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false)
        val sender = last.getCharSequence("sender")?.toString()
        return if (group && !sender.isNullOrBlank()) "$sender: $text" else text
    }

    private fun ownLastMessage(ex: Bundle): Boolean {
        @Suppress("DEPRECATION")
        val arr: Array<Parcelable> = ex.getParcelableArray(Notification.EXTRA_MESSAGES) ?: return false
        val last = arr.lastOrNull() as? Bundle ?: return false
        if (!last.containsKey("sender") && !last.containsKey("sender_person") && !last.containsKey("person")) return true
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && sentByMe(last, ex)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun sentByMe(message: Bundle, ex: Bundle): Boolean {
        @Suppress("DEPRECATION")
        val sender = runCatching { message.getParcelable<Person>("sender_person") }.getOrNull()
        @Suppress("DEPRECATION")
        val me = runCatching { ex.getParcelable<Person>(Notification.EXTRA_MESSAGING_PERSON) }.getOrNull() ?: return false
        if (me.key != null && sender?.key == me.key) return true
        val name = (sender?.name ?: message.getCharSequence("sender"))?.toString()
        return !name.isNullOrBlank() && name == me.name?.toString()
    }

    fun onReplied(key: String) {
        repliedAt[key] = SystemClock.elapsedRealtime()
        IslandBus.removeUnread(key)
    }

    fun forget(key: String) {
        shown.remove(key)
        repliedAt.remove(key)
        IslandBus.removeUnread(key)
        if (shown.isEmpty()) {
            IslandBus.clearUnread()
        }
    }

    private fun appBrandColor(pkg: String, fallback: Color?): Color {
        val p = pkg.lowercase()
        return when {
            "zalo" in p -> Color(0xFF0068FF)
            "orca" in p || "messenger" in p -> Color(0xFF00B2FF)
            "facebook" in p || "katana" in p -> Color(0xFF1877F2)
            "telegram" in p -> Color(0xFF2AABEE)
            "whatsapp" in p -> Color(0xFF25D366)
            "instagram" in p -> Color(0xFFE1306C)
            "musically" in p || "trill" in p || "tiktok" in p -> Color(0xFF00F2FE)
            "gm" in p || "mail" in p -> Color(0xFFEA4335)
            "shopee" in p -> Color(0xFFEE4D2D)
            "youtube" in p -> Color(0xFFFF0033)
            else -> fallback ?: Tints.Blue
        }
    }
}

private object Lockscreen {
    
    fun isSecure(context: Context): Boolean =
        context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    fun showsNotifications(context: Context): Boolean = secureInt(context, "lock_screen_show_notifications", 1) != 0

    fun showsPrivate(context: Context): Boolean = secureInt(context, "lock_screen_allow_private_notifications", 0) != 0

    private fun secureInt(context: Context, name: String, unreadable: Int): Int = try {
        Settings.Secure.getInt(context.contentResolver, name)
    } catch (_: Settings.SettingNotFoundException) {
        1
    } catch (_: Exception) {
        unreadable
    }
}
