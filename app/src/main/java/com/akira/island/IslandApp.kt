package com.akira.island

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import com.akira.island.core.IslandBus
import com.akira.island.data.SettingsRepo
import com.akira.island.ui.SetTimerActivity
import com.akira.island.data.SettingsBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class IslandApp : Application() {
    @OptIn(FlowPreview::class)
    override fun onCreate() {
        super.onCreate()
        SettingsRepo.init(this)
        
        IslandBus.scope.launch {
            SettingsRepo.state.map { it.timerFromAssistant }.distinctUntilChanged().collect { on ->
                runCatching {
                    packageManager.setComponentEnabledSetting(
                        ComponentName(this@IslandApp, SetTimerActivity::class.java),
                        if (on) PackageManager.COMPONENT_ENABLED_STATE_DEFAULT else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                }
            }
        }
        // Tự động sao lưu cấu hình (gộp các thay đổi liên tiếp, ghi sau 5 giây yên lặng)
        IslandBus.scope.launch {
            SettingsRepo.state.drop(1).debounce(5_000).collect { s ->
                if (s.autoBackup) withContext(Dispatchers.IO) { SettingsBackup.write(this@IslandApp, SettingsRepo.exportJson()) }
            }
        }
    }
}
