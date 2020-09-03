package com.handysparksoft.trackmap.core.di

import android.app.Application
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, UseCaseModule::class, ViewModelModule::class])
interface AppComponent {

    val createViewModelFactory: CreateViewModelFactory
    val currentViewModelFactory: CurrentTrackMapsViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application) : AppComponent
    }
}
