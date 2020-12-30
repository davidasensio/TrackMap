package com.handysparksoft.trackmap

import android.app.Application
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.handysparksoft.trackmap.core.di.AppComponent
import com.handysparksoft.trackmap.core.di.DaggerAppComponent
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.logError
import com.handysparksoft.trackmap.core.platform.BatteryLevelHelper
import com.handysparksoft.trackmap.core.platform.FirebaseTracking
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.core.platform.network.ConnectionHandler
import com.handysparksoft.trackmap.features.task.UpdateBatteryLevelWorker
import javax.inject.Inject

class App : Application() {

    lateinit var component: AppComponent

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var prefs: Prefs

    override fun onCreate() {
        super.onCreate()

        // Initialize Dagger
        component = DaggerAppComponent.factory().create(this)

        // Initialize ConnectionHandler & UserHandler
        component.inject(this)
        connectionHandler.registerNetworkCallback()

        // Initialize Firebase Analytics and set UserId
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        FirebaseTracking(this)
        firebaseAnalytics.setUserId(userHandler.getUserId())

        // Firebase token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                logDebug("Fetching FCM token failed - ${task.exception}")
            }

            // Get new FCM registration token
            val token = task.result
            prefs.userToken = token
            logDebug("User token is $token")
        }

        // Periodic task for battery updates
        UpdateBatteryLevelWorker.initBatteryLevelPeriodicWork(this)
    }

    companion object {
        private lateinit var firebaseAnalytics: FirebaseAnalytics

        fun getFirebaseAnalyticsInstance() = firebaseAnalytics
    }
}

