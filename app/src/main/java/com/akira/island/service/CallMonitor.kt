package com.akira.island.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.akira.island.core.CallInfo
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CallMonitor(private val context: Context, private val scope: CoroutineScope) {
    private var lastState: String? = null
    private var number: String? = null
    private var since = 0L
    private var lookupJob: Job? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) = handle(intent)
    }

    fun start() {
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED), ContextCompat.RECEIVER_EXPORTED,
        )
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
        remove()
    }

    private fun handle(intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        
        @Suppress("DEPRECATION")
        val num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (!num.isNullOrBlank()) number = num
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> publish(incoming = true)
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (lastState != TelephonyManager.EXTRA_STATE_OFFHOOK) since = System.currentTimeMillis()
                publish(incoming = false)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                number = null
                VoiceAnnouncer.stop()
                remove()
            }
        }
        lastState = state
    }

    private fun publish(incoming: Boolean) {
        val num = number
        val base = if (incoming) System.currentTimeMillis() else since
        put(CallInfo(KEY, dialer(), num?.let(::format) ?: tr("Cuộc gọi", "Call"), null, incoming, base, System.currentTimeMillis(),
            null, null, null, null))
        
        lookupJob?.cancel()
        if (num != null && granted(Manifest.permission.READ_CONTACTS)) {
            lookupJob = scope.launch {
                val contact = withContext(Dispatchers.IO) { lookup(num) }
                val cur = IslandBus.calls.value[KEY] ?: return@launch
                if (contact != null) {
                    put(cur.copy(name = contact.first, avatar = contact.second ?: cur.avatar))
                    if (incoming) VoiceAnnouncer.announceCall(context, contact.first)
                } else if (incoming) {
                    VoiceAnnouncer.announceCall(context, cur.name)
                }
            }
        } else if (incoming) {
            VoiceAnnouncer.announceCall(context, num?.let(::format) ?: tr("Cuộc gọi", "Call"))
        }
    }

    private fun put(c: CallInfo) {
        IslandBus.calls.value = IslandBus.calls.value + (KEY to c)
    }

    private fun remove() {
        lookupJob?.cancel()
        if (KEY in IslandBus.calls.value) IslandBus.calls.value = IslandBus.calls.value - KEY
    }

    private fun lookup(num: String): Pair<String, ImageBitmap?>? = runCatching {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num))
        val cols = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
        context.contentResolver.query(uri, cols, null, null, null)?.use { c ->
            if (!c.moveToFirst()) return@use null
            val name = c.getString(0) ?: return@use null
            val photo = c.getString(1)?.let { p ->
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(p))?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                }.getOrNull()
            }
            name to photo
        }
    }.getOrNull()

    private fun format(num: String): String =
        PhoneNumberUtils.formatNumber(num, Locale.getDefault().country) ?: num

    private fun dialer(): String = runCatching {
        context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage
    }.getOrNull() ?: ""

    private fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val KEY = "telephony"
    }
}
