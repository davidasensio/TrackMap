package com.handysparksoft.trackmap.core.platform

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.whenAvailable
import com.handysparksoft.trackmap.features.entries.MainActivity
import com.handysparksoft.usecases.UpdateUserLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationForegroundService : Service(), Scope by Scope.Impl() {

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var locationHandler: LocationHandler

    @Inject
    lateinit var updateUserLocationUseCase: UpdateUserLocationUseCase

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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopRequestLocationUpdates()
        destroyScope()
        super.onDestroy()
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
            .setContentText("TrackMap is running")
            .setSmallIcon(R.drawable.ic_map_black_24dp)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun requestLocationUpdates() {
        locationHandler.subscribeLocationUpdates { location ->
            location.whenAvailable {
                launch(Dispatchers.Default) {
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
}
