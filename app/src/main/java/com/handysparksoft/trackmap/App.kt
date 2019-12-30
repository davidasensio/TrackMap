package com.handysparksoft.trackmap

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}

