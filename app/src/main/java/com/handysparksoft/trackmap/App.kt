package com.handysparksoft.trackmap

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.handysparksoft.trackmap.core.di.AppComponent
import com.handysparksoft.trackmap.core.di.DaggerAppComponent
import com.handysparksoft.trackmap.core.platform.ConnectionHandler
import com.handysparksoft.trackmap.core.platform.UserHandler
import io.fabric.sdk.android.Fabric
import javax.inject.Inject

class App : Application() {

    lateinit var component: AppComponent

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    override fun onCreate() {
        super.onCreate()

        // Initialize Dagger
        component = DaggerAppComponent.factory().create(this)

        // Initialize ConnectionHandler
        component.inject(this)
        connectionHandler.registerNetworkCallback()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}

