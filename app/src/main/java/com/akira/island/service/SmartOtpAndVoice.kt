package com.akira.island.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import java.util.Locale

object OtpHelper {
    private val keywordRegex = Regex(
        """(?i)(?:\botp\b|mã\s*xác\s*thực|ma\s*xac\s*thuc|mã\s*xác\s*nhận|ma\s*xac\s*nhan|mã\s*bảo\s*mật|mã\s*đăng\s*nhập|mã\s*kích\s*hoạt|xác\s*thực|xac\s*thuc|verification|verify\s*code|security\s*code|auth\s*code|login\s*code|passcode|one[\s-]*time|\bcode\b|\bg-\d{6}\b)""",
    )
    private val googleCodeRegex = Regex("""(?i)\bG-(\d{6})\b""")
    private val explicitAfterKeywordRegex = Regex(
        """(?i)(?:otp|mã|ma|code|passcode|pin)[^0-9]{0,24}?(\b\d{4,8}\b)""",
    )
    private val standaloneDigitsRegex = Regex("""\b(\d{4,8})\b""")
    private val moneyOrTimeAfterRegex = Regex("""^\s*(?:đ|vnd|vnđ|usd|k\b|tr\b|%|giờ|phút|giây|s\b|ms\b|mb\b|gb\b)""", RegexOption.IGNORE_CASE)

    fun extractOtp(title: String, text: String): String? {
        val combined = "$title\n$text"
        googleCodeRegex.find(combined)?.groupValues?.getOrNull(1)?.let { return it }
        if (!keywordRegex.containsMatchIn(combined)) return null

        explicitAfterKeywordRegex.findAll(text).forEach { m ->
            val candidate = m.groupValues[1]
            val after = text.substring(m.range.last + 1)
            if (!moneyOrTimeAfterRegex.containsMatchIn(after)) {
                return candidate
            }
        }

        standaloneDigitsRegex.findAll(text).forEach { m ->
            val candidate = m.groupValues[1]
            val before = if (m.range.first > 0) text[m.range.first - 1] else ' '
            val afterStr = text.substring(m.range.last + 1)
            if (before !in listOf(':', '/', '.', '-', '+') && !moneyOrTimeAfterRegex.containsMatchIn(afterStr)) {
                
                if (candidate.length == 4 && candidate.startsWith("202")) return@forEach
                return candidate
            }
        }
        return null
    }

    fun copyToClipboard(context: Context, code: String) {
        runCatching {
            val cm = context.getSystemService(ClipboardManager::class.java) ?: return
            cm.setPrimaryClip(ClipData.newPlainText("OTP Code", code))
        }
    }
}

object VoiceAnnouncer {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingText: String? = null
    private var focusRequest: AudioFocusRequest? = null

    private fun ensureInit(context: Context, onReady: () -> Unit) {
        if (tts != null && ready) {
            onReady()
            return
        }
        if (tts != null) return
        val appCtx = context.applicationContext
        tts = TextToSpeech(appCtx) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val viLocale = Locale("vi", "VN")
                val res = engine.isLanguageAvailable(viLocale)
                if (res >= TextToSpeech.LANG_AVAILABLE) {
                    engine.language = viLocale
                } else {
                    engine.language = Locale.getDefault()
                }
                engine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) = abandonFocus(appCtx)
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = abandonFocus(appCtx)
                })
                ready = true
                pendingText?.let { text ->
                    pendingText = null
                    speakInternal(appCtx, text)
                }
                onReady()
            } else {
                tts = null
                ready = false
            }
        }
    }

    fun isHeadsetConnected(context: Context): Boolean = runCatching {
        val am = context.getSystemService(AudioManager::class.java) ?: return false
        val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        outputs.any { d ->
            when (d.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_USB_HEADSET,
                -> true
                else -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                    d.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                        d.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && d.type == AudioDeviceInfo.TYPE_BLE_BROADCAST)
                    )
            }
        }
    }.getOrDefault(false)

    fun announceMessage(context: Context, appName: String, sender: String, text: String, sensitive: Boolean) {
        val s = SettingsRepo.current
        if (!s.ttsEnabled) return
        if (s.ttsOnlyHeadset && !isHeadsetConnected(context)) return
        if (IslandBus.calls.value.isNotEmpty()) return

        val cleanSender = sender.take(40)
        val cleanText = text.replace("\n", " ").trim().take(120)
        val speech = if (s.ttsReadContent && !sensitive && !IslandBus.locked.value && cleanText.isNotEmpty()) {
            tr(
                "Tin nhắn $appName từ $cleanSender: $cleanText",
                "Message on $appName from $cleanSender: $cleanText",
            )
        } else {
            tr(
                "Tin nhắn mới trên $appName từ $cleanSender",
                "New message on $appName from $cleanSender",
            )
        }
        speak(context, speech)
    }

    fun announceCall(context: Context, caller: String) {
        val s = SettingsRepo.current
        if (!s.ttsEnabled || !s.ttsReadCalls) return
        if (s.ttsOnlyHeadset && !isHeadsetConnected(context)) return
        val cleanCaller = caller.take(45)
        val speech = tr("Cuộc gọi đến từ $cleanCaller", "Incoming call from $cleanCaller")
        speak(context, speech)
    }

    fun testSpeak(context: Context) {
        val speech = tr(
            "Thử nghiệm đọc thông báo tự động trên Capsule VN. Tin nhắn Zalo từ Nguyễn Lê Lý Huỳnh: Chúc bạn một ngày tốt lành!",
            "Capsule VN voice announcer test. Message from Nguyen Le Ly Huynh: Have a great day!",
        )
        speak(context, speech)
    }

    fun stop() {
        runCatching { tts?.stop() }
    }

    private fun speak(context: Context, text: String) {
        val appCtx = context.applicationContext
        if (tts == null || !ready) {
            pendingText = text
            ensureInit(appCtx) {}
        } else {
            speakInternal(appCtx, text)
        }
    }

    private fun speakInternal(context: Context, text: String) {
        runCatching {
            val am = context.getSystemService(AudioManager::class.java)
            if (am != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    .build()
                focusRequest = req
                am.requestAudioFocus(req)
            }
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "capsule_tts_${System.currentTimeMillis()}")
        }
    }

    private fun abandonFocus(context: Context) {
        runCatching {
            val am = context.getSystemService(AudioManager::class.java) ?: return
            val req = focusRequest ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                am.abandonAudioFocusRequest(req)
            }
            focusRequest = null
        }
    }
}
