package com.handysparksoft.trackmap.core.di

import android.app.Application
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModelFactory
import com.handysparksoft.trackmap.features.trackmap.MainActivity
import com.handysparksoft.trackmap.features.trackmap.MainViewModelFactory
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, UseCaseModule::class, ViewModelModule::class])
interface AppComponent {

    // Exposed Graph components
    val mainViewModelFactory: MainViewModelFactory
    val createViewModelFactory: CreateViewModelFactory
    val currentViewModelFactory: CurrentTrackMapsViewModelFactory

    // Field injections
    fun inject(mainActivity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application) : AppComponent
    }
}
