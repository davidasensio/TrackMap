package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModelFactory
import com.handysparksoft.trackmap.features.trackmap.MainViewModelFactory
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveTrackMapUseCase
import com.handysparksoft.usecases.SaveUserUseCase
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    fun mainViewModelFactoryProvider(
        saveUserUseCase: SaveUserUseCase,
        joinTrackMapUseCase: JoinTrackMapUseCase,
        userHandler: UserHandler
    ) =
        MainViewModelFactory(saveUserUseCase, joinTrackMapUseCase, userHandler)

    @Provides
    fun createViewModelFactoryProvider(
        saveTrackMapUseCase: SaveTrackMapUseCase,
        userHandler: UserHandler
    ) =
        CreateViewModelFactory(saveTrackMapUseCase, userHandler)

    @Provides
    fun currentViewModelFactoryProvider(getTrackMapsUseCase: GetTrackMapsUseCase, userHandler: UserHandler) =
        CurrentTrackMapsViewModelFactory(getTrackMapsUseCase, userHandler)
}
