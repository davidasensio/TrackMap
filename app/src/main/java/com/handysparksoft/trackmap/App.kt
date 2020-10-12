package com.handysparksoft.trackmap

import android.app.Application
import com.handysparksoft.trackmap.core.di.AppComponent
import com.handysparksoft.trackmap.core.di.DaggerAppComponent
import com.handysparksoft.trackmap.core.platform.network.ConnectionHandler
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
    }
}

