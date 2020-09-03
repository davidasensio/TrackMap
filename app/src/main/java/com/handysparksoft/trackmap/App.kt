package com.handysparksoft.trackmap

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.handysparksoft.trackmap.di.AppComponent
import com.handysparksoft.trackmap.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric

class App : Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        // Initialize Dagger
        component = DaggerAppComponent.factory().create(this)

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}

