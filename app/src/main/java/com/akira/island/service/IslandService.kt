package com.akira.island.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.KeyguardManager
import android.content.pm.ApplicationInfo
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.CardPreviewMode
import com.akira.island.core.ServiceCommand
import com.akira.island.core.Notice
import com.akira.island.core.NoticeStyle
import com.akira.island.core.Tints
import com.akira.island.core.tr
import com.akira.island.island.Ios
import com.akira.island.R
import com.akira.island.core.LangScope
import com.akira.island.data.IslandSettings
import com.akira.island.data.SettingsRepo
import com.akira.island.island.Egg
import com.akira.island.island.ISLAND_PAD_DP
import com.akira.island.island.IslandOverlay
import com.akira.island.ui.InterTypography
import com.akira.island.update.UpdateChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class IslandService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var display: FrameLayout? = null
    private var touch: TouchRoot? = null
    private val owner = OverlayLifecycleOwner()
    private var events: SystemEvents? = null
    private var torch: TorchController? = null
    private var alarm: TimerAlarm? = null
    private var calls: CallMonitor? = null
    private var navReader: NavScreenReader? = null
    private var clockReader: ClockScreenReader? = null
    private var payReader: PayScreenReader? = null
    private var alarmMonitor: AlarmMonitor? = null
    private var screenEvents = false
    private val keyguard by lazy { getSystemService(KeyguardManager::class.java) }
    
    private var keyguardShown = false

    private var keyboardSeen = false
    
    private var alerting = false
    private var wakeBlocked = false
    private var lastScreenOn = true
    private var replySince = 0L
    private var imePackages: Set<String> = emptySet()
    private var imeCheckedAt = 0L

    private var tiltOn = false
    private var tiltSmooth = 0f
    private val tiltListener = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            
            tiltSmooth += (e.values[0] / 9.81f).coerceIn(-1f, 1f).minus(tiltSmooth) * 0.12f
            if (abs(tiltSmooth - IslandBus.glassTilt.value) > 0.015f) IslandBus.glassTilt.value = tiltSmooth
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private var visible = true
    private var body = Rect.Zero
    private var bubble = Rect.Zero
    
    private var tall = true
    
    private var touchSince = 0L
    private val touchUpdate = Runnable {
        touchSince = 0L
        updateTouchWindow()
    }
    private val settle = Runnable {
        tall = false
        updateDisplayHeight()
    }
    
    private val shoot = Runnable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
    private val showAgain = Runnable { display?.alpha = 1f }
    
    private var fxTall = false
    private val fxSettle = Runnable {
        fxTall = false
        updateDisplayHeight()
    }

    private val displayParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        title = "DynamicIsland"
        cutoutAlways()
    }

    private val touchParams = WindowManager.LayoutParams(
        1,
        1,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        title = "DynamicIslandTouch"
        x = -10
        y = -10
        cutoutAlways()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        SettingsRepo.init(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        IslandBus.landscape.value = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        createWindows()
        KeepAliveService.sync(this)
        IslandTile.refresh(this)
        events = SystemEvents(this).also { it.start() }
        keyguardShown = keyguard?.isKeyguardLocked == true
        torch = TorchController(this).also { it.start() }
        alarm = TimerAlarm(this, scope).also { it.start() }
        calls = CallMonitor(this, scope).also { it.start() }
        navReader = NavScreenReader(this, scope)
        clockReader = ClockScreenReader(this, scope).also { ClockControl.reader = it }
        payReader = PayScreenReader(this, scope)
        alarmMonitor = AlarmMonitor(this).also { it.start() }
        IslandBus.serviceRunning.value = true

        
        scope.launch {
            IslandBus.calls.map { calls -> calls.values.any { it.incoming } }.distinctUntilChanged()
                .collect { ringing -> if (!ringing) IslandBus.callHidden.value = false }
        }

        scope.launch {
            IslandBus.commands.collect { cmd ->
                when (cmd) {
                    ServiceCommand.SCREENSHOT -> screenshotWithoutIsland()
                    ServiceCommand.LOCK -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                    ServiceCommand.TURN_OFF -> disableSelf()
                }
            }
        }

        scope.launch {
            IslandBus.otpFillRequests.collect { code ->
                fillOtpIntoFocusedField(code)
            }
        }

        scope.launch {
            IslandBus.reply.collect {
                keyboardSeen = false
                replySince = SystemClock.elapsedRealtime()
                setInputFocus(it != null)
            }
        }
        
        scope.launch {
            combine(IslandBus.timer, IslandBus.calls, IslandBus.ringingAlarms, IslandBus.screenOn) { t, calls, alarms, on ->
                (t?.ringing == true || calls.values.any { it.incoming } || alarms.isNotEmpty()) to on
            }.distinctUntilChanged().collect { (alert, on) -> onAlert(alert, on) }
        }
        
        scope.launch {
            IslandBus.screenOn.collect {
                if (!it) {
                    keyguardShown = true
                    PayMonitor.clearReady()
                }
            }
        }
        
        scope.launch { IslandBus.locked.collect { if (it) IslandBus.reply.value = null } }

        scope.launch {
            while (isActive) {
                if (IslandBus.screenOn.value) UpdateChecker.check(this@IslandService, force = false, announce = true)
                delay(60 * 60 * 1000L)
            }
        }

        scope.launch {
            combine(SettingsRepo.state.map { it.liquidGlass && it.enabled }, IslandBus.screenOn) { glass, on -> glass && on }
                .distinctUntilChanged()
                .collect { setTiltEnabled(it) }
        }

        scope.launch {
            combine(SettingsRepo.state, IslandBus.landscape, IslandBus.offSchedule, IslandBus.gamingActive) { s, land, _, _ -> s to land }
                .collect { (s, land) ->
                    updateRamAndGame(IslandBus.foregroundPackage.value)
                    applySettings(s, land)
                }
        }
        scope.launch {
            while (isActive) {
                if (IslandBus.screenOn.value && (IslandBus.gamingActive.value || IslandBus.expanded.value)) {
                    sampleRam()
                }
                delay(4_000L)
            }
        }

        scope.launch {
            combine(SettingsRepo.state.map { Triple(it.scheduleOn, it.scheduleFrom, it.scheduleTo) }.distinctUntilChanged(), IslandBus.screenOn) { sch, _ -> sch }
                .collectLatest { (on) ->
                    while (true) {
                        val now = Calendar.getInstance()
                        IslandBus.offSchedule.value = !SettingsRepo.current.inSchedule(now.get(Calendar.HOUR_OF_DAY))
                        if (!on) break
                        val next = (now.clone() as Calendar).apply {
                            add(Calendar.HOUR_OF_DAY, 1)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        delay(next.timeInMillis - now.timeInMillis + 500)
                    }
                }
        }

        val version = UpdateChecker.currentVersion(this)
        val seen = SettingsRepo.current.islandVersion
        if (seen != version) {
            if (seen.isNotEmpty() && SettingsRepo.current.autoUpdateCheck) {
                IslandBus.post(UpdateChecker.updatedNotice(this, version))
            }
            SettingsRepo.update { copy(islandVersion = version) }
        }

        Egg.watch(this, scope)
        
        scope.launch {
            val settings = combine(SettingsRepo.state, IslandBus.powerSave) { s, saver -> s.forPowerSave(saver) }
            combine(IslandBus.egg, IslandBus.media, IslandBus.notice, IslandBus.payReady, settings) { egg, m, n, pay, s ->
                (egg && m?.playing == true) ||
                    (!s.reduceEffects && s.fxPay && (pay != null || n?.style == NoticeStyle.PAY_DONE))
            }
                .distinctUntilChanged()
                .collect { on ->
                    handler.removeCallbacks(fxSettle)
                    if (on) {
                        fxTall = true
                        updateDisplayHeight()
                    } else {
                        handler.postDelayed(fxSettle, 1_500)
                    }
                }
        }
    }

    private fun createWindows() {
        val compose = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                MaterialTheme(colorScheme = darkColorScheme(), typography = InterTypography) {
                    LangScope {
                        IslandOverlay(
                            onTransition = ::onTransition,
                            
                            onBodyBounds = { r -> val q = r.snapped(); if (q != body) { body = q; onBoundsChanged() } },
                            onBubbleBounds = { r -> val q = r.snapped(); if (q != bubble) { bubble = q; onBoundsChanged() } },
                        )
                    }
                }
            }
        }
        val drawRoot = object : FrameLayout(this) {
            
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK && IslandBus.reply.value != null) {
                    if (event.action == KeyEvent.ACTION_UP) IslandBus.reply.value = null
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }
        drawRoot.addView(compose)
        owner.create()
        owner.attach(drawRoot)
        displayParams.height = tallHeight()
        runCatching { wm?.addView(drawRoot, displayParams) }.onSuccess { display = drawRoot }

        val touchRoot = TouchRoot(this, { display }, ::onOutsideTouch)
        runCatching { wm?.addView(touchRoot, touchParams) }.onSuccess { touch = touchRoot }
        scheduleSettle()
    }

    private fun onOutsideTouch() {
        if (IslandBus.reply.value != null) {
            if (!keyboardShown()) IslandBus.reply.value = null
            return
        }
        if (IslandBus.expanded.value) Controls.collapse()
    }

    private fun keyboardShown(): Boolean = runCatching {
        val list = windows
        list.isEmpty() || list.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
    }.getOrDefault(true)

    private fun applySettings(s: IslandSettings, landscape: Boolean) {
        val hideLand = s.hideInLandscape && landscape && !(s.gameModeEnabled && s.gameBypassLandscapeHide && IslandBus.gamingActive.value)
        visible = s.enabled && !hideLand && !IslandBus.offSchedule.value
        display?.visibility = if (visible) View.VISIBLE else View.GONE

        
        
        owner.setActive(visible)
        if (!visible) Controls.collapse()
        val y = dp(s.windowBaseY - ISLAND_PAD_DP)
        if (displayParams.y != y) {
            displayParams.y = y
            display?.let { v -> runCatching { wm?.updateViewLayout(v, displayParams) } }
        }
        updateTouchWindow()
    }

    private fun screenshotWithoutIsland() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        Controls.collapse()
        val view = display ?: return
        view.alpha = 0f
        handler.removeCallbacks(shoot)
        handler.removeCallbacks(showAgain)
        handler.postDelayed(shoot, 250)
        handler.postDelayed(showAgain, 1_600)
    }

    private fun onTransition() {
        if (!tall) {
            tall = true
            updateDisplayHeight()
        }
        scheduleSettle()
    }

    private fun onBoundsChanged() {

        
        val now = SystemClock.uptimeMillis()
        if (touchSince == 0L) touchSince = now
        handler.removeCallbacks(touchUpdate)
        handler.postAtTime(touchUpdate, minOf(now + TOUCH_QUIET_MS, touchSince + TOUCH_MAX_WAIT_MS))
        val bottom = islandBottom()
        if (!tall && bottom > displayParams.height) {
            tall = true
            updateDisplayHeight()
        }
        scheduleSettle()
    }

    private fun scheduleSettle() {
        handler.removeCallbacks(settle)
        handler.postDelayed(settle, SETTLE_MS)
    }

    private fun islandBottom(): Int =
        ceil(max(body.bottom, bubble.bottom)).toInt() + dp(ISLAND_PAD_DP)

    private fun updateDisplayHeight() {
        val view = display ?: return
        val h = if (tall || fxTall || IslandBus.expanded.value || IslandBus.cardPreviewMode.value != CardPreviewMode.NONE) tallHeight() else islandBottom().coerceAtLeast(dp(ISLAND_PAD_DP * 2))
        if (displayParams.height == h) return
        displayParams.height = h
        runCatching { wm?.updateViewLayout(view, displayParams) }
    }

    private fun updateTouchWindow() {
        val view = touch ?: return
        val area = when {
            body.isEmpty -> bubble
            bubble.isEmpty -> body
            else -> Rect(
                minOf(body.left, bubble.left), minOf(body.top, bubble.top),
                maxOf(body.right, bubble.right), maxOf(body.bottom, bubble.bottom),
            )
        }
        val usable = visible && !area.isEmpty && IslandBus.cardPreviewMode.value == CardPreviewMode.NONE
        val origin = IntArray(2)
        display?.getLocationOnScreen(origin)
        val x = if (usable) origin[0] + floor(area.left).toInt() else -10
        val y = if (usable) origin[1] + floor(area.top).toInt() else -10
        val w = if (usable) ceil(area.width).toInt().coerceAtLeast(1) else 1
        val h = if (usable) ceil(area.height).toInt().coerceAtLeast(1) else 1
        val flags = if (usable) touchParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        else touchParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        if (touchParams.x == x && touchParams.y == y && touchParams.width == w && touchParams.height == h &&
            touchParams.flags == flags
        ) return
        touchParams.x = x
        touchParams.y = y
        touchParams.width = w
        touchParams.height = h
        touchParams.flags = flags
        runCatching { wm?.updateViewLayout(view, touchParams) }
    }

    private fun setTiltEnabled(on: Boolean) {
        if (on == tiltOn) return
        tiltOn = on
        val sm = getSystemService(SensorManager::class.java) ?: return
        if (on) {
            val sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
            sm.registerListener(tiltListener, sensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sm.unregisterListener(tiltListener)
            tiltSmooth = 0f
            IslandBus.glassTilt.value = 0f
        }
    }

    private fun setInputFocus(on: Boolean) {
        val view = display ?: return
        val flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        val flags = if (on) displayParams.flags and flag.inv() else displayParams.flags or flag
        if (flags == displayParams.flags) return
        displayParams.flags = flags
        displayParams.softInputMode = if (on) WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        else WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        runCatching { wm?.updateViewLayout(view, displayParams) }
    }

    private fun onAlert(alert: Boolean, screenOn: Boolean) {
        if (!alert) {
            wakeBlocked = false
        } else if (alerting && lastScreenOn && !screenOn) {
            wakeBlocked = true
        }
        
        val wake = SettingsRepo.current.wakeForAlerts
        if (wake && alert && !alerting && !screenOn) ScreenWaker.wake(this, scope) { alerting && !wakeBlocked }
        alerting = alert
        lastScreenOn = screenOn
        setAlarmScreen(keepOn = wake && alert, turnOn = wake && alert && !wakeBlocked)
    }

    @Suppress("DEPRECATION")
    private fun setAlarmScreen(keepOn: Boolean, turnOn: Boolean) {
        val view = display ?: return
        val mask = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        var flags = displayParams.flags and mask.inv()
        if (keepOn) flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        if (turnOn) flags = flags or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        if (flags == displayParams.flags) return
        displayParams.flags = flags
        runCatching { wm?.updateViewLayout(view, displayParams) }
    }

    private fun tallHeight() = dp(580)

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> onWindowsChanged()
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> onWindowState(event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                clockReader?.onContentChanged(event)
                payReader?.onContentChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> clockReader?.onClicked(event)
        }
    }

    private fun setScreenEvents(on: Boolean) {
        if (on == screenEvents) return
        screenEvents = on
        val info = serviceInfo ?: return
        val extra = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
        info.eventTypes = if (on) info.eventTypes or extra else info.eventTypes and extra.inv()
        runCatching { serviceInfo = info }
    }

    private fun onWindowsChanged() {
        checkUnlock()
        if (IslandBus.reply.value == null) return
        if (keyboardShown()) {
            keyboardSeen = true
        } else if (keyboardSeen && SystemClock.elapsedRealtime() - replySince > 1000) {
            
            IslandBus.reply.value = null
        }
    }

    private fun checkUnlock() {
        if (!keyguardShown || !IslandBus.screenOn.value) return
        if (keyguard?.isKeyguardLocked == false) {
            keyguardShown = false
            events?.onUnlocked()
        }
    }

    private fun onWindowState(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return
        
        val cls = event.className?.toString().orEmpty()
        if (pkg == "com.android.systemui" || isKeyboard(pkg, cls)) return
        
        if (pkg == PayMonitor.GMS && "tapandpay" in cls.lowercase()) {
            PayMonitor.onTapAndPay(this)
            payReader?.onTapAndPay()
            setScreenEvents(true)
            return
        }
        if (pkg == packageName && !cls.endsWith("Activity")) return
        
        PayMonitor.onForeground(pkg)
        if (IslandBus.foregroundPackage.value != pkg && pkg != packageName) PayMonitor.onAppOpened(this, pkg)
        
        navReader?.onForeground(pkg)
        
        clockReader?.onForeground(pkg)
        
        payReader?.onForeground(pkg, cls)
        setScreenEvents(clockReader?.active == true || payReader?.active == true)
        IslandBus.foregroundPackage.value = pkg
        if (pkg == IslandBus.unreadLedPkg.value) IslandBus.clearUnread()
        updateRamAndGame(pkg)
    }

    private fun sampleRam() {
        runCatching {
            val am = getSystemService(ActivityManager::class.java) ?: return
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            if (info.totalMem > 0) {
                val used = (info.totalMem - info.availMem).coerceAtLeast(0L)
                val pct = ((used * 100L) / info.totalMem).toInt().coerceIn(1, 99)
                val usedGb = used.toDouble() / (1024.0 * 1024.0 * 1024.0)
                val totalGb = info.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
                IslandBus.ramPercent.value = pct
                IslandBus.ramGbText.value = "%.1f/%.0f GB".format(usedGb, totalGb)
            }
        }
    }

    private fun updateRamAndGame(pkg: String?) {
        val s = SettingsRepo.current
        val wasGaming = IslandBus.gamingActive.value
        val nowGaming = s.gameModeEnabled && (s.gameForceOn || (pkg != null && pkg != packageName && isGameApp(pkg)))
        IslandBus.gamingActive.value = nowGaming
        if (nowGaming) sampleRam()
        if (!wasGaming && nowGaming) {
            val bat = IslandBus.battery.value
            val tempStr = if (bat.tempTenths > 0) "%.1f°C · ".format(bat.tempC) else ""
            val ramStr = if (IslandBus.ramPercent.value > 0) "RAM ${IslandBus.ramPercent.value}%" else "ON"
            IslandBus.post(
                Notice(
                    title = "Gaming Mode",
                    state = "$tempStr$ramStr",
                    icon = Ios.load(this, R.drawable.ios_hardware_chip),
                    tint = Tints.Green,
                    style = NoticeStyle.PILL,
                ),
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun isGameApp(pkg: String): Boolean {
        val lower = pkg.lowercase()
        val knownPrefixes = listOf(
            "com.garena.", "com.tencent.ig", "com.pubg.", "com.dts.freefire",
            "com.mihoyo.", "com.hoyoverse.", "com.riotgames.", "com.supercell.",
            "com.roblox.", "com.mojang.", "com.ea.gp.", "com.activision.",
            "com.mobile.legends", "vn.vng.", "com.vng.", "com.zing.speed",
            "com.gameloft.", "com.netmarble.", "com.nexon.", "com.bandainamcoent."
        )
        if (knownPrefixes.any { lower.startsWith(it) || it in lower }) return true
        return runCatching {
            val info = packageManager.getApplicationInfo(pkg, 0)
            val isCatGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.category == ApplicationInfo.CATEGORY_GAME
            } else false
            val isFlagGame = (info.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            isCatGame || isFlagGame
        }.getOrDefault(false)
    }

    private fun isKeyboard(pkg: String, cls: String): Boolean {
        if (cls.contains("inputmethod", ignoreCase = true)) return true
        val now = SystemClock.elapsedRealtime()
        if (now - imeCheckedAt > 60_000) {
            imeCheckedAt = now
            imePackages = runCatching {
                getSystemService(InputMethodManager::class.java)?.enabledInputMethodList.orEmpty().map { it.packageName }.toSet()
            }.getOrDefault(emptySet())
        }
        return pkg in imePackages
    }

    override fun onInterrupt() = Unit

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        IslandBus.landscape.value = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    override fun onDestroy() {
        
        stopService(Intent(this, KeepAliveService::class.java))
        handler.removeCallbacks(shoot)
        handler.removeCallbacks(showAgain)
        handler.removeCallbacks(settle)
        handler.removeCallbacks(fxSettle)
        handler.removeCallbacks(touchUpdate)
        IslandBus.egg.value = false
        setTiltEnabled(false)
        IslandBus.serviceRunning.value = false
        IslandTile.refresh(this)
        events?.stop()
        torch?.stop()
        alarm?.stop()
        calls?.stop()
        navReader?.stop()
        clockReader?.stop()
        payReader?.stop()
        ClockControl.reader = null
        alarmMonitor?.stop()
        scope.cancel()
        touch?.let { runCatching { wm?.removeView(it) } }
        display?.let { runCatching { wm?.removeView(it) } }
        touch = null
        display = null
        owner.destroy()
        super.onDestroy()
    }

    private fun fillOtpIntoFocusedField(code: String) {
        runCatching {
            val root = rootInActiveWindow ?: return
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?.takeIf { it.isEditable }
                ?: findFirstEditable(root)
                ?: return
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, code)
            }
            if (!focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                focused.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            }
        }
    }

    private fun findFirstEditable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isEditable && node.isVisibleToUser) return node
        for (i in 0 until node.childCount) {
            val found = findFirstEditable(node.getChild(i))
            if (found != null) return found
        }
        return null
    }
}

private fun WindowManager.LayoutParams.cutoutAlways() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
}

@SuppressLint("ViewConstructor")
class TouchRoot(
    context: Context,
    private val target: () -> View?,
    private val onOutside: () -> Unit,
) : View(context) {
    private val origin = IntArray(2)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_OUTSIDE) {
            onOutside()
            return true
        }
        val view = target() ?: return false
        view.getLocationOnScreen(origin)
        val copy = MotionEvent.obtain(ev)
        
        copy.offsetLocation(ev.rawX - ev.x - origin[0], ev.rawY - ev.y - origin[1])
        view.dispatchTouchEvent(copy)
        copy.recycle()
        return true
    }
}

private const val SETTLE_MS = 700L
private const val TOUCH_QUIET_MS = 48L
private const val TOUCH_MAX_WAIT_MS = 250L

private fun Rect.snapped() = Rect(floor(left), floor(top), ceil(right), ceil(bottom))
