package com.akira.island.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Đọc nhanh cảm biến tiệm cận: máy đang úp / để trong túi thì không bật sáng màn hình. */
object Proximity {
    suspend fun near(context: Context): Boolean {
        val sm = context.getSystemService(SensorManager::class.java) ?: return false
        val sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: return false
        return withTimeoutOrNull(500) {
            suspendCancellableCoroutine { cont ->
                val done = AtomicBoolean(false)
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        if (!done.compareAndSet(false, true)) return
                        sm.unregisterListener(this)
                        cont.resume(e.values.firstOrNull()?.let { it < minOf(5f, sensor.maximumRange) } ?: false)
                    }

                    override fun onAccuracyChanged(s: Sensor?, a: Int) = Unit
                }
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
                cont.invokeOnCancellation { sm.unregisterListener(listener) }
            }
        } ?: false
    }
}
