package com.akira.island.service

import android.app.KeyguardManager
import android.app.NotificationManager
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.hardware.usb.UsbDevice
import android.hardware.display.DisplayManager
import android.hardware.usb.UsbManager
import android.view.Display
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import com.akira.island.core.BatteryInfo
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.R
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Ios
import kotlin.math.abs

class SystemEvents(private val context: Context) {
    private fun ic(id: Int) = Ios.load(context, id)

    private val main = Handler(Looper.getMainLooper())
    private val startedAt = SystemClock.elapsedRealtime()
    private val audio = context.getSystemService(AudioManager::class.java)
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private val keyguard = context.getSystemService(KeyguardManager::class.java)
    private val s get() = SettingsRepo.current

    private var lowWarned = false
    private var protectWarned = false
    private val externalDisplays = HashSet<Int>()
    private var usbConnected: Boolean? = null
    private var brightnessMode = -1
    private var dnd: Boolean? = null
    private val headsetBattery = HashMap<String, Int>()
    private val wifiNetworks = HashSet<Network>()
    
    private var lastPowerChange = 0L

    private val warmingUp get() = SystemClock.elapsedRealtime() - startedAt < 3000

    private fun notice(enabled: Boolean, n: Notice) {
        if (enabled && !warmingUp) IslandBus.post(n)
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            if (displayId == Display.DEFAULT_DISPLAY) return
            externalDisplays.add(displayId)
            if (!s.ecoDexCast) return
            val dm = context.getSystemService(DisplayManager::class.java)
            val d = dm?.getDisplay(displayId)
            val rawName = d?.name?.takeIf { it.isNotBlank() } ?: tr("Màn hình rời", "External Display")
            val isDex = "dex" in rawName.lowercase()
            notice(
                true,
                Notice(
                    title = if (isDex) "Samsung DeX" else rawName,
                    state = tr("Đã kết nối", "Connected"),
                    icon = ic(R.drawable.ios_hardware_chip),
                    tint = Tints.Blue,
                ),
            )
        }

        override fun onDisplayRemoved(displayId: Int) {
            if (!externalDisplays.remove(displayId)) return
            if (!s.ecoDexCast) return
            notice(
                true,
                Notice(
                    title = tr("Màn hình / DeX", "Display / DeX"),
                    state = tr("Đã ngắt kết nối", "Disconnected"),
                    icon = ic(R.drawable.ios_hardware_chip),
                    tint = Tints.Gray,
                ),
            )
        }

        override fun onDisplayChanged(displayId: Int) = Unit
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) = handle(intent, isInitialStickyBroadcast)
    }

    private val wifiCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            main.post {
                val wasEmpty = wifiNetworks.isEmpty()
                wifiNetworks += network
                if (wasEmpty) notice(s.evWifiOn, Notice("Wi-Fi", tr("Đã kết nối", "Connected"), ic(R.drawable.ios_wifi), Tints.Blue))
            }
        }

        override fun onLost(network: Network) {
            main.post {
                if (wifiNetworks.remove(network) && wifiNetworks.isEmpty()) {
                    notice(s.evWifiOff, Notice("Wi-Fi", tr("Đã ngắt kết nối", "Disconnected"), ic(R.drawable.ios_wifi), Tints.Gray))
                }
            }
        }
    }

    private val brightnessObserver = object : ContentObserver(main) {
        override fun onChange(selfChange: Boolean) {
            val mode = Settings.System.getInt(
                context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0,
            )
            if (mode == brightnessMode) return
            brightnessMode = mode
            val auto = mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            notice(
                s.evBrightness,
                Notice(
                    tr("Độ sáng", "Brightness"),
                    if (auto) tr("Tự động", "Auto") else tr("Thủ công", "Manual"),
                    if (auto) ic(R.drawable.ios_sunny) else ic(R.drawable.ios_contrast),
                    Tints.Yellow,
                ),
            )
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BT_BATTERY_CHANGED)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(USB_STATE)
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            addAction(ACTION_SPEN_INSERT)
        }
        runCatching {
            context.getSystemService(DisplayManager::class.java)?.registerDisplayListener(displayListener, main)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)

        dnd = isDnd()
        val power = context.getSystemService(PowerManager::class.java)
        IslandBus.screenOn.value = power?.isInteractive ?: true
        IslandBus.powerSave.value = power?.isPowerSaveMode == true
        IslandBus.locked.value = isLocked()
        IslandBus.keyguard.value = isKeyguard()
        brightnessMode = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, 0)
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), false, brightnessObserver,
        )

        runCatching {
            connectivity?.registerNetworkCallback(
                NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
                wifiCallback,
            )
        }
    }

    fun stop() {
        runCatching { context.unregisterReceiver(receiver) }
        runCatching { context.getSystemService(DisplayManager::class.java)?.unregisterDisplayListener(displayListener) }
        runCatching { context.contentResolver.unregisterContentObserver(brightnessObserver) }
        runCatching { connectivity?.unregisterNetworkCallback(wifiCallback) }
    }

    private fun handle(intent: Intent, initialSticky: Boolean) {
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> onBattery(intent)

            Intent.ACTION_POWER_CONNECTED -> {
                lastPowerChange = SystemClock.elapsedRealtime()
                val b = IslandBus.battery.value
                val level = b.level
                val title = when {
                    s.ecoFastCharge && b.superFastCharging -> tr("Sạc siêu nhanh", "Super Fast Charging")
                    s.ecoFastCharge && b.fastCharging -> tr("Sạc nhanh", "Fast Charging")
                    else -> tr("Đang sạc", "Charging")
                }
                val tint = if (s.ecoFastCharge && b.superFastCharging) Tints.Teal else Tints.Green
                notice(
                    s.evChargeStart,
                    Notice(title, if (level >= 0) "$level%" else null, ic(R.drawable.ios_flash), tint,
                        battery = level.takeIf { it >= 0 }),
                )
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                lastPowerChange = SystemClock.elapsedRealtime()
                val level = IslandBus.battery.value.level
                notice(
                    s.evChargeStop,
                    Notice(tr("Đã rút sạc", "Unplugged"), if (level >= 0) "$level%" else null, ic(R.drawable.ios_battery_full), Tints.Orange,
                        battery = level.takeIf { it >= 0 }),
                )
            }

            Intent.ACTION_USER_PRESENT -> onUnlocked()

            Intent.ACTION_SCREEN_OFF -> {
                IslandBus.screenOn.value = false
                
                if (keyguard?.isDeviceSecure == true) IslandBus.locked.value = true
                IslandBus.reply.value = null
                
                if (IslandBus.timer.value?.ringing == true) Controls.cancelTimer()
                Controls.collapse()
            }

            Intent.ACTION_SCREEN_ON -> {
                IslandBus.screenOn.value = true
                IslandBus.locked.value = isLocked()
                IslandBus.keyguard.value = isKeyguard()
            }

            PowerManager.ACTION_POWER_SAVE_MODE_CHANGED ->
                IslandBus.powerSave.value = context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true

            NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED -> {
                val on = isDnd()
                if (on == dnd) return
                dnd = on
                
                notice(
                    s.evDnd,
                    Notice(tr("Không làm phiền", "Do Not Disturb"), if (on) tr("Bật", "On") else tr("Tắt", "Off"), ic(R.drawable.ios_moon),
                        if (on) Tints.Purple else Tints.Gray),
                )
            }

            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                val on = intent.getBooleanExtra("state", false)
                if (on) notice(s.evAirplaneOn, Notice(tr("Chế độ máy bay", "Airplane Mode"), tr("Bật", "On"), ic(R.drawable.ios_airplane), Tints.Orange))
                else notice(s.evAirplaneOff, Notice(tr("Chế độ máy bay", "Airplane Mode"), tr("Tắt", "Off"), ic(R.drawable.ios_airplane), Tints.Gray))
            }

            AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                val n = when (intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1)) {
                    
                    AudioManager.RINGER_MODE_NORMAL -> Notice("", tr("Chuông", "Ring"), ic(R.drawable.ios_notifications), Color.White, style = NoticeStyle.PILL)
                    AudioManager.RINGER_MODE_VIBRATE -> Notice("", tr("Rung", "Vibrate"), ic(R.drawable.ios_phone_portrait),
                        Tints.Purple, style = NoticeStyle.PILL)
                    AudioManager.RINGER_MODE_SILENT -> Notice("", tr("Im lặng", "Silent"), ic(R.drawable.ios_notifications_off),
                        Tints.Red, style = NoticeStyle.PILL)
                    else -> null
                }
                if (n != null) notice(s.evRinger, n)
            }

            BluetoothDevice.ACTION_ACL_CONNECTED, BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    ?: return
                val name = runCatching { device.name }.getOrNull()
                val major = runCatching { device.bluetoothClass?.majorDeviceClass }.getOrNull()
                val isAudio = major == BluetoothClass.Device.Major.AUDIO_VIDEO
                val isWearable = s.ecoBudsWatch && major == BluetoothClass.Device.Major.WEARABLE
                if (!isAudio && !isWearable) return
                val fallbackName = if (isWearable) tr("Đồng hồ thông minh", "Smartwatch") else tr("Tai nghe", "Headphones")
                if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                    val directBat = if (s.ecoBudsWatch) {
                        runCatching { device.javaClass.getMethod("getBatteryLevel").invoke(device) as? Int }
                            .getOrNull()?.takeIf { it in 0..100 }
                    } else null
                    if (directBat != null) headsetBattery[device.address] = directBat
                    val battery = headsetBattery[device.address]
                    notice(
                        s.evHeadsetOn,
                        Notice(name ?: fallbackName, null, ic(R.drawable.ios_headset), if (isWearable) Tints.Teal else Tints.Green,
                            ring = battery?.let { it / 100f }, caption = tr("Đã kết nối", "Connected")),
                    )
                } else {
                    headsetBattery.remove(device.address)
                    notice(s.evHeadsetOff, Notice(name ?: fallbackName, null, ic(R.drawable.ios_headset), Tints.Gray,
                        caption = tr("Đã ngắt kết nối", "Disconnected")))
                }
            }

            BT_BATTERY_CHANGED -> {
                val device = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    ?: return
                val level = intent.getIntExtra(EXTRA_BT_BATTERY, -1)
                if (level >= 0) headsetBattery[device.address] = level
            }

            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val dev = IntentCompat.getParcelableExtra(intent, UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                notice(s.evUsbOn, Notice(dev?.productName ?: tr("Thiết bị USB", "USB device"), tr("Đã kết nối", "Connected"),
                    ic(R.drawable.ios_hardware_chip), Tints.Teal))
            }

            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val dev = IntentCompat.getParcelableExtra(intent, UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                notice(s.evUsbOff, Notice(dev?.productName ?: tr("Thiết bị USB", "USB device"), tr("Đã ngắt kết nối", "Disconnected"),
                    ic(R.drawable.ios_hardware_chip), Tints.Gray))
            }

            USB_STATE -> {
                val connected = intent.getBooleanExtra("connected", false)
                val prev = usbConnected
                usbConnected = connected
                if (initialSticky || prev == null || prev == connected) return

                main.postDelayed({
                    if (abs(SystemClock.elapsedRealtime() - lastPowerChange) < 3000) return@postDelayed
                    if (connected) notice(s.evUsbOn, Notice("USB", tr("Đã kết nối", "Connected"), ic(R.drawable.ios_hardware_chip), Tints.Teal))
                    else notice(s.evUsbOff, Notice("USB", tr("Đã ngắt kết nối", "Disconnected"), ic(R.drawable.ios_hardware_chip), Tints.Gray))
                }, 1500)
            }

            ACTION_SPEN_INSERT -> {
                if (initialSticky || !s.ecoSpPen) return
                val inserted = intent.getBooleanExtra("penInsert", true)
                notice(
                    true,
                    Notice(
                        title = tr("Bút S Pen", "S Pen Stylus"),
                        state = if (inserted) tr("Đã cất bút", "Inserted") else tr("Đã rút bút", "Removed"),
                        icon = ic(R.drawable.ios_hardware_chip),
                        tint = if (inserted) Tints.Purple else Tints.Teal,
                    ),
                )
            }
        }
    }

    private var lastUnlock = 0L

    fun onUnlocked() {
        IslandBus.locked.value = false
        IslandBus.keyguard.value = false
        val now = SystemClock.elapsedRealtime()
        if (now - lastUnlock < 3_000) return
        lastUnlock = now
        notice(s.evUnlock, Notice("", null, ic(R.drawable.ios_lock_open), Color.White, style = NoticeStyle.PILL, kindHint = com.akira.island.data.NoticeKind.UNLOCK))
    }

    private fun isLocked(): Boolean = keyguard?.isDeviceLocked == true

    private fun isKeyguard(): Boolean = keyguard?.isKeyguardLocked == true

    private fun isDnd(): Boolean {
        val filter = notifications?.currentInterruptionFilter ?: return false
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
    }

    private fun onBattery(intent: Intent) {
        val raw = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val level = if (raw >= 0) raw * 100 / scale else -1
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val charging = plugged != 0 &&
            (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val fastType = intent.getIntExtra("fastcharger_type", 0)
        val fastFlag = intent.getBooleanExtra("fastcharger", false)
        val maxVolt = intent.getIntExtra("max_charging_voltage", 0)
        val maxCurr = intent.getIntExtra("max_charging_current", 0)
        val superFast = charging && (fastType >= 3 || voltageMv >= 8600 || maxVolt >= 9_000_000)
        val fast = charging && (superFast || fastFlag || fastType >= 1 || voltageMv >= 6200 || maxCurr >= 2_000_000)

        val prev = IslandBus.battery.value
        IslandBus.battery.value = BatteryInfo(level, charging, tempTenths, fast, superFast)

        if (!charging || (s.ecoBatteryProtectLevel > 0 && level < s.ecoBatteryProtectLevel - 2)) {
            protectWarned = false
        } else if (charging && s.ecoBatteryProtectLevel > 0 && level >= s.ecoBatteryProtectLevel && !protectWarned && prev.level in 1 until s.ecoBatteryProtectLevel) {
            protectWarned = true
            notice(
                true,
                Notice(
                    tr("Bảo vệ pin", "Battery Protection"),
                    "$level%",
                    ic(R.drawable.ios_flash),
                    Tints.Teal,
                    battery = level,
                ),
            )
        }

        if (charging || level > 5) {
            lowWarned = false
        } else if (level >= 0 && !lowWarned) {
            lowWarned = true
            notice(s.evLowBattery, Notice(tr("Pin yếu", "Low Battery"), "$level%", ic(R.drawable.ios_battery_dead), Tints.Red, battery = level))
        }
    }

    private companion object {
        
        const val BT_BATTERY_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        const val EXTRA_BT_BATTERY = "android.bluetooth.device.extra.BATTERY_LEVEL"
        const val USB_STATE = "android.hardware.usb.action.USB_STATE"
        const val ACTION_SPEN_INSERT = "com.samsung.pen.INSERT"
    }
}
