package com.handysparksoft.trackmap.core.di

import android.app.Application
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.MainViewModelFactory
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

    // Field injections
    fun inject(trackMapActivity: TrackMapActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application): AppComponent
    }
}
