package com.handysparksoft.trackmap.core.di

import android.app.Application
import com.handysparksoft.trackmap.App
import com.handysparksoft.trackmap.core.platform.LocationForegroundService
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.MainActivity
import com.handysparksoft.trackmap.features.entries.MainViewModelFactory
import com.handysparksoft.trackmap.features.join.JoinViewModelFactory
import com.handysparksoft.trackmap.features.splash.SplashActivity
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import com.handysparksoft.trackmap.features.trackmap.TrackMapViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, UseCaseModule::class, ViewModelModule::class])
interface AppComponent {

    // Exposed Graph components
    val mainViewModelFactory: MainViewModelFactory
    val createViewModelFactory: CreateViewModelFactory
    val trackMapViewModelFactory: TrackMapViewModelFactory
    val joinViewModelFactory: JoinViewModelFactory

    // Field injections
    fun inject(mainActivity: MainActivity)
    fun inject(trackMapActivity: TrackMapActivity)
    fun inject(locationForegroundService: LocationForegroundService)
    fun inject(splashActivity: SplashActivity)
    fun inject(app: App)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }
}
