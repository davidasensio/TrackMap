package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.platform.LocationForegroundService.Companion.ACTION_STOP
import com.handysparksoft.usecases.StartLiveTrackingUseCase
import com.handysparksoft.usecases.StopLiveTrackingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationForegroundServiceHandler @Inject constructor(
    private val userHandler: UserHandler,
    private val startLiveTrackingUseCase: StartLiveTrackingUseCase,
    private val stopLiveTrackingUseCase: StopLiveTrackingUseCase
) : Scope by Scope.Impl() {

    private lateinit var locationForegroundService: LocationForegroundService

    private var userTrackMapIds = mutableSetOf<String>()

    private val liveTrackingMapIds = mutableSetOf<String>()

    init {
        initScope()
    }

    fun setUserTrackMapIds(trackMapIds: List<String>) {
        userTrackMapIds = trackMapIds.toMutableSet()
    }

    fun startUserLocationService(activity: Activity, trackMapId: String, startTracking: Boolean) {
        if (startTracking) {
            startService(activity)
            liveTrackingMapIds.add(trackMapId)

            launch(Dispatchers.IO) {
                startLiveTrackingUseCase.execute(userHandler.getUserId(), trackMapId)
            }
        } else {
            liveTrackingMapIds.remove(trackMapId)

            launch(Dispatchers.IO) {
                stopLiveTrackingUseCase.execute(userHandler.getUserId(), trackMapId)
            }
            if (liveTrackingMapIds.size == 0) {
                clearAllLiveTracking()
                stopService(activity)
            }
        }
    }

    fun hasLiveTrackingAlreadyStarted(trackMapId: String): Boolean =
        liveTrackingMapIds.contains(trackMapId)

    fun clearAllLiveTracking() {
        userTrackMapIds.forEach { trackMapId ->
            launch(Dispatchers.IO) {
                stopLiveTrackingUseCase.execute(userHandler.getUserId(), trackMapId)
            }
        }
        userTrackMapIds.clear()
        liveTrackingMapIds.clear()
    }

    private fun startService(activity: Activity) {
        locationForegroundService = LocationForegroundService()
        val serviceIntent = Intent(activity, locationForegroundService::class.java)
        if (!isMyServiceRunning(locationForegroundService::class.java, activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent)
            } else {
                activity.startService(serviceIntent)
            }
            activity.logDebug("Service initialized")
        } else {
            activity.logDebug("Service already initialized!")
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>, activity: Activity): Boolean {
        val manager: ActivityManager =
            activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                activity.logDebug("Service status - Running")
                return true
            }
        }
        activity.logDebug("Service status - Not Running")
        return false
    }

    private fun isLocationEnabledOrNot(context: Context): Boolean {
        var locationManager: LocationManager? = null
        locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun stopService(activity: Activity) {
        if (::locationForegroundService.isInitialized) {
            val serviceIntent = Intent(activity, locationForegroundService::class.java)

            // This action will stop the service manually (So no conflict with autorestart feature)
            serviceIntent.action = ACTION_STOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent)
            } else {
                activity.startService(serviceIntent)
            }
        }
    }
}
