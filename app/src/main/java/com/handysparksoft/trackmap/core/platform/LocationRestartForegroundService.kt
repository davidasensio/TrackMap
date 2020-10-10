package com.handysparksoft.trackmap.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.toast

class LocationRestartForegroundService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.logDebug("Broadcast listened")

        val locationForegroundService = LocationForegroundService()
        val serviceIntent = Intent(context, locationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        context.toast("Service initialized")
    }

    companion object {
        const val RESTART_SERVICE_ACTION = "restartservice"
    }
}
