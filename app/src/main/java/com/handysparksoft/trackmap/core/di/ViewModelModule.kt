package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.MainViewModelFactory
import com.handysparksoft.trackmap.features.join.JoinViewModelFactory
import com.handysparksoft.trackmap.features.profile.ProfileViewModelFactory
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
        userHandler: UserHandler,
        prefs: Prefs
    ) =
        MainViewModelFactory(
            getTrackMapsUseCase,
            saveUserUseCase,
            joinTrackMapUseCase,
            leaveTrackMapUseCase,
            saveUserTrackMapUseCase,
            updateUserLocationUseCase,
            userHandler,
            prefs
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

    @Provides
    fun joinViewModelFactoryProvider(
        joinTrackMapUseCase: JoinTrackMapUseCase,
        saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
        getUserAccessDataUseCase: GetUserAccessDataUseCase,
        userHandler: UserHandler
    ) =
        JoinViewModelFactory(
            joinTrackMapUseCase,
            saveUserTrackMapUseCase,
            getUserAccessDataUseCase,
            userHandler
        )

    @Provides
    fun profileViewModelFactoryProvider(
        getUuserProfileDataUseCase: GetUserProfileDataUseCase,
        updateUserProfileUseCase: UpdateUserProfileUseCase,
        userHandler: UserHandler,
        prefs: Prefs,
    ) =
        ProfileViewModelFactory(getUuserProfileDataUseCase, updateUserProfileUseCase, userHandler, prefs)
}
