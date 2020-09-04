package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.MainViewModelFactory
import com.handysparksoft.trackmap.features.trackmap.TrackMapViewModelFactory
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
        getTrackMapsUseCase: GetTrackMapsUseCase,
        saveUserUseCase: SaveUserUseCase,
        joinTrackMapUseCase: JoinTrackMapUseCase,
        userHandler: UserHandler
    ) =
        MainViewModelFactory(getTrackMapsUseCase, saveUserUseCase, joinTrackMapUseCase, userHandler)

    @Provides
    fun createViewModelFactoryProvider(
        saveTrackMapUseCase: SaveTrackMapUseCase,
        userHandler: UserHandler
    ) =
        CreateViewModelFactory(saveTrackMapUseCase, userHandler)

    @Provides
    fun trackMapViewModelFactoryProvider(userHandler: UserHandler) =
        TrackMapViewModelFactory(userHandler)
}
