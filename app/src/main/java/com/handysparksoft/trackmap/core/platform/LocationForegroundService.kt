package com.handysparksoft.trackmap.core.platform

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.handysparksoft.domain.model.UserGPSData
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.whenAvailable
import com.handysparksoft.trackmap.features.main.MainActivity
import com.handysparksoft.usecases.UpdateUserGPSDataUseCase
import com.handysparksoft.usecases.UpdateUserLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationForegroundService : Service(), Scope by Scope.Impl() {
    companion object {
        const val ACTION_STOP = "ACTION_STOP"
    }

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var locationHandler: LocationHandler

    @Inject
    lateinit var updateUserLocationUseCase: UpdateUserLocationUseCase

    @Inject
    lateinit var updateUserAltitudeUseCase: UpdateUserGPSDataUseCase

    @Inject
    lateinit var locationForegroundServiceHandler: LocationForegroundServiceHandler

    private var manuallyStopped: Boolean = false

    init {
        initScope()
    }

    // If the service is already running, this method is not called. One-time setup function
    override fun onCreate() {
        super.onCreate()
        app.component.inject(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelAndStartForeground()
        } else {
            startForeground(2, Notification())
        }

        requestLocationUpdates()
        requestGPSLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        checkIntent(intent)
        return START_STICKY
    }

    private fun checkIntent(intent: Intent?) {
        intent?.action?.let {
            when (it) {
                ACTION_STOP -> {
                    manuallyStopped = true
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopRequestLocationUpdates()
        stopRequestGPSLocationUpdates()
        locationForegroundServiceHandler.clearAllLiveTracking()
        destroyScope()

        super.onDestroy()

        if (!manuallyStopped) {
            forceRestartService()
        }
    }

    private fun forceRestartService() {
        val broadcastIntent = Intent()
        broadcastIntent.action = LocationRestartForegroundService.RESTART_SERVICE_ACTION
        broadcastIntent.setClass(this, LocationRestartForegroundService::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelAndStartForeground() {
        val notificationChannelId = "trackMapForegroundService"
        val channelName = "Foreground service TrackMap"
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.lightColor = Color.RED

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification = notificationBuilder
            .setOngoing(true)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tracking location is active")
            .setSmallIcon(R.drawable.ic_map_black_24dp)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("While tracking is active user location is being monitored. Stop tracking action is available below")
                    .setSummaryText("Tracking is active")
            )
            .addAction(getStopAction())
            .build()

        startForeground(1, notification)
    }

    private fun getStopAction(): NotificationCompat.Action? {
        val intent = Intent(this, LocationForegroundService::class.java)
        intent.action = ACTION_STOP
        val pendingIntent = PendingIntent.getService(this, 0, intent, 0)
        return NotificationCompat.Action(
            R.drawable.ic_stop,
            "Stop tracking",
            pendingIntent
        )
    }

    private fun requestLocationUpdates() {
        locationHandler.subscribeLocationUpdates { location ->
            location.whenAvailable {
                launch(Dispatchers.IO) {
                    updateUserLocationUseCase.execute(
                        userHandler.getUserId(),
                        it.latitude,
                        it.longitude
                    )
                }
            }
        }
    }

    private fun stopRequestLocationUpdates() {
        locationHandler.unsubscribeLocationUpdates()
    }

    private fun requestGPSLocationUpdates() {
        // Reset altitude and speed to 0 before start listening
        launch {
            updateUserAltitudeUseCase.execute(userHandler.getUserId(), UserGPSData(0, 0, 0))
        }

        // Start listening to NMEA messages
        locationHandler.subscribeToNMEAMessagesAndSpeed { nmeaMessage, speedInKmh ->
            nmeaMessage.altitudeAMSL?.let { altitudeAMSL ->
                logDebug("*** Altitude: $altitudeAMSL - (${nmeaMessage.type})")
                launch(Dispatchers.IO) {
                    val altitudeGeoid = nmeaMessage.altitudeGeoid ?: 0
                    updateUserAltitudeUseCase.execute(
                        userHandler.getUserId(),
                        UserGPSData(altitudeAMSL, altitudeGeoid, speedInKmh, System.currentTimeMillis())
                    )
                }
            }
        }
    }

    private fun stopRequestGPSLocationUpdates() {
        locationHandler.unsubscribeToNMEAMessages()
    }
}
