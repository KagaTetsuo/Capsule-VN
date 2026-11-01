package com.akira.island.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import kotlinx.coroutines.flow.MutableStateFlow

object SystemBanners {
    
    data class Seen(val channel: String, val call: Boolean)

    val seen = MutableStateFlow<Map<String, Seen>>(emptyMap())

    val popUp = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    const val SYSTEM_UI = "com.android.systemui"
    const val ANDROID = "android"

    fun onPosted(sbn: StatusBarNotification, incomingCall: Boolean) {
        val n = sbn.notification ?: return
        if (!incomingCall && n.category != Notification.CATEGORY_ALARM) return
        val channel = n.channelId ?: return
        val item = Seen(channel, incomingCall)
        if (seen.value[sbn.packageName] != item) seen.value = seen.value + (sbn.packageName to item)
    }

    fun onMessage(pkg: String, channel: String?, popsUp: Boolean) {
        if (popUp.value[pkg] != popsUp) popUp.value = popUp.value + (pkg to popsUp)
        if (channel != null && SettingsRepo.current.msgChannels[pkg] != channel) {
            SettingsRepo.update { copy(msgChannels = msgChannels + (pkg to channel)) }
        }
    }

    fun callApps(context: Context, seen: Map<String, Seen>): List<String> {
        val dialer = runCatching { context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage }.getOrNull()
        return (listOfNotNull(dialer) + seen.filterValues { it.call }.keys).distinct()
    }

    fun clockApps(context: Context, seen: Map<String, Seen>): List<String> {
        val timers = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(Intent(AlarmClock.ACTION_SET_TIMER), 0).map { it.activityInfo.packageName }
        }.getOrDefault(emptyList())
        return (timers + seen.filterValues { !it.call }.keys).distinct().filter { it != SYSTEM_UI && it != ANDROID && it != context.packageName }
    }

    fun label(context: Context, pkg: String): String = when (pkg) {
        SYSTEM_UI -> tr("Giao diện hệ thống", "System UI")
        ANDROID -> tr("Hệ thống Android", "Android System")
        else -> NotificationParser.appLabel(context, pkg)
    }

    fun openSettings(context: Context, pkg: String) {
        val channel = seen.value[pkg]?.channel ?: SettingsRepo.current.msgChannels[pkg]
        val intent = if (channel != null) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_CHANNEL_ID, channel)
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        }
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(intent) }.isSuccess) return
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }
}
