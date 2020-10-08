package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.MainViewModelFactory
import com.handysparksoft.trackmap.features.trackmap.TrackMapViewModelFactory
import com.handysparksoft.usecases.*
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    fun mainViewModelFactoryProvider(
        getTrackMapsUseCase: GetTrackMapsUseCase,
        saveUserUseCase: SaveUserUseCase,
        joinTrackMapUseCase: JoinTrackMapUseCase,
        leaveTrackMapUseCase: LeaveTrackMapUseCase,
        saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
        updateUserLocationUseCase: UpdateUserLocationUseCase,
        userHandler: UserHandler
    ) =
        MainViewModelFactory(
            getTrackMapsUseCase,
            saveUserUseCase,
            joinTrackMapUseCase,
            leaveTrackMapUseCase,
            saveUserTrackMapUseCase,
            updateUserLocationUseCase,
            userHandler
        )

    @Provides
    fun createViewModelFactoryProvider(
        saveTrackMapUseCase: SaveTrackMapUseCase,
        saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
        userHandler: UserHandler
    ) =
        CreateViewModelFactory(saveTrackMapUseCase, saveUserTrackMapUseCase, userHandler)

    @Provides
    fun trackMapViewModelFactoryProvider(userHandler: UserHandler) =
        TrackMapViewModelFactory(userHandler)
}
