package com.akira.island.core

import android.os.Environment
import android.os.StatFs

object StorageInfo {
    fun getRomUsage(): Pair<String, Int> {
        return runCatching {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val usedBytes = totalBytes - freeBytes
            val usedGb = usedBytes / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val pct = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0
            val roundTotal = when {
                totalGb > 600 -> 1024
                totalGb > 300 -> 512
                totalGb > 160 -> 256
                totalGb > 80 -> 128
                totalGb > 40 -> 64
                else -> totalGb.toInt()
            }
            "%.1f/%d GB".format(usedGb, roundTotal) to pct
        }.getOrDefault("0/0 GB" to 0)
    }
}
