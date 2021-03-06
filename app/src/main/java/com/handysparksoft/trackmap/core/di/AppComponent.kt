package com.handysparksoft.trackmap.core.di

import android.app.Application
import com.handysparksoft.trackmap.App
import com.handysparksoft.trackmap.core.platform.FirebaseTracking
import com.handysparksoft.trackmap.core.platform.LocationForegroundService
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.EntriesFragment
import com.handysparksoft.trackmap.features.entries.EntriesViewModelFactory
import com.handysparksoft.trackmap.features.join.JoinViewModelFactory
import com.handysparksoft.trackmap.features.main.MainActivity
import com.handysparksoft.trackmap.features.participants.ParticipantsFragment
import com.handysparksoft.trackmap.features.participants.ParticipantsViewModelFactory
import com.handysparksoft.trackmap.features.profile.ProfileViewModelFactory
import com.handysparksoft.trackmap.features.splash.SplashActivity
import com.handysparksoft.trackmap.features.task.UpdateBatteryLevelWorker
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import com.handysparksoft.trackmap.features.trackmap.TrackMapViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, UseCaseModule::class, ViewModelModule::class])
interface AppComponent {

    // Exposed Graph components
    val entriesViewModelFactory: EntriesViewModelFactory
    val createViewModelFactory: CreateViewModelFactory
    val trackMapViewModelFactory: TrackMapViewModelFactory
    val joinViewModelFactory: JoinViewModelFactory
    val profileViewModelFactory: ProfileViewModelFactory
    val participantsViewModelFactory: ParticipantsViewModelFactory

    // Field injections
    fun inject(mainActivity: MainActivity)
    fun inject(entriesFragment: EntriesFragment)
    fun inject(trackMapActivity: TrackMapActivity)
    fun inject(locationForegroundService: LocationForegroundService)
    fun inject(splashActivity: SplashActivity)
    fun inject(firebaseTracking: FirebaseTracking)
    fun inject(updateBatteryLevelWorker: UpdateBatteryLevelWorker)
    fun inject(participantsFragment: ParticipantsFragment)
    fun inject(app: App)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }
}
