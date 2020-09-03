package com.handysparksoft.trackmap.di

import android.app.Application
import com.handysparksoft.trackmap.ui.creation.CreateViewModel
import com.handysparksoft.trackmap.ui.main.MainViewModel
import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class, DataModule::class, UseCaseModule::class, ViewModelModule::class])
interface AppComponent {

    val mainViewModel: MainViewModel
    val createViewModel: CreateViewModel

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: Application) : AppComponent
    }
}
