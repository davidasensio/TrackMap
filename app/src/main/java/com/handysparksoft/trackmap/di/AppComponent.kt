package com.handysparksoft.trackmap.di

import android.app.Application
import com.handysparksoft.trackmap.ui.creation.CreateViewModel
import com.handysparksoft.trackmap.ui.creation.CreateViewModelFactory
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModel
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModelFactory
import com.handysparksoft.trackmap.ui.main.MainViewModel
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
