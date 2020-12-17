package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class BatteryLevelHelper {
    companion object {
        private const val BATTERY_LEVEL_FALLBACK = 100

        fun getBatteryLevel(context: Context): Int {
            val batteryStatus: Intent? =
                IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
                    context.registerReceiver(null, intentFilter)
                }

            return batteryStatus?.let { intent ->
                val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                level * 100 / scale
            } ?: BATTERY_LEVEL_FALLBACK
        }
    }
}
