package com.akira.island.ui

import android.app.Activity
import android.os.Bundle
import android.provider.AlarmClock
import com.akira.island.core.Controls
import com.akira.island.data.SettingsRepo

class SetTimerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsRepo.init(this)
        
        val seconds = (intent?.getIntExtra(AlarmClock.EXTRA_LENGTH, 0) ?: 0).coerceAtMost(86_400)
        if (seconds > 0) Controls.startTimerSeconds(seconds) else Controls.startTimer(SettingsRepo.current.timerMinutes)
        Controls.expand("timer")
        finish()
    }
}
