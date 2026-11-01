package com.akira.island.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import com.akira.island.core.tr
import com.akira.island.data.SettingsRepo

class IslandTile : TileService() {
    override fun onStartListening() {
        SettingsRepo.init(this)
        val tile = qsTile ?: return
        val on = IslandBus.serviceRunning.value
        tile.state = if (on) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) tile.subtitle = if (on) tr("Bật", "On") else tr("Tắt", "Off")
        tile.updateTile()
    }

    override fun onClick() {
        SettingsRepo.init(this)
        if (IslandBus.serviceRunning.value) {
            Controls.turnOffIsland()
            
            qsTile?.let {
                it.state = Tile.STATE_INACTIVE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.subtitle = tr("Tắt", "Off")
                it.updateTile()
            }
        } else {
            open(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun open(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE))
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    companion object {
        
        fun refresh(context: Context) {
            runCatching { requestListeningState(context, ComponentName(context, IslandTile::class.java)) }
        }
    }
}
