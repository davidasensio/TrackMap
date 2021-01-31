package com.handysparksoft.trackmap.core.di

import com.handysparksoft.trackmap.core.platform.LocationForegroundServiceHandler
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateViewModelFactory
import com.handysparksoft.trackmap.features.entries.EntriesViewModelFactory
import com.handysparksoft.trackmap.features.join.JoinViewModelFactory
import com.handysparksoft.trackmap.features.notification.PushNotificationHandler
import com.handysparksoft.trackmap.features.participants.ParticipantsViewModelFactory
import com.handysparksoft.trackmap.features.profile.ProfileViewModelFactory
import com.handysparksoft.trackmap.features.trackmap.TrackMapViewModelFactory
import com.handysparksoft.usecases.*
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    fun entriesViewModelFactoryProvider(
        getTrackMapsUseCase: GetTrackMapsUseCase,
        saveUserUseCase: SaveUserUseCase,
        leaveTrackMapUseCase: LeaveTrackMapUseCase,
        stopLiveTrackingUseCase: StopLiveTrackingUseCase,
        favoriteTrackMapUseCase: FavoriteTrackMapUseCase,
        updateUserBatteryLevelUseCase: UpdateUserBatteryLevelUseCase,
        userHandler: UserHandler,
        prefs: Prefs,
        getTrackMapByIdUseCase: GetTrackMapByIdUseCase,
        pushNotificationHandler: PushNotificationHandler,
        locationForegroundServiceHandler: LocationForegroundServiceHandler
    ) =
        EntriesViewModelFactory(
            getTrackMapsUseCase,
            saveUserUseCase,
            leaveTrackMapUseCase,
            stopLiveTrackingUseCase,
            favoriteTrackMapUseCase,
            updateUserBatteryLevelUseCase,
            userHandler,
            prefs,
            getTrackMapByIdUseCase,
            pushNotificationHandler,
            locationForegroundServiceHandler
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
        userHandler: UserHandler,
        pushNotificationHandler: PushNotificationHandler
    ) =
        JoinViewModelFactory(
            joinTrackMapUseCase,
            saveUserTrackMapUseCase,
            userHandler,
            pushNotificationHandler
        )

    @Provides
    fun profileViewModelFactoryProvider(
        getUuserProfileDataUseCase: GetUserProfileDataUseCase,
        updateUserProfileUseCase: UpdateUserProfileUseCase,
        userHandler: UserHandler,
        prefs: Prefs,
    ) =
        ProfileViewModelFactory(
            getUuserProfileDataUseCase,
            updateUserProfileUseCase,
            userHandler,
            prefs
        )

    @Provides
    fun participantsViewModelFactoryProvider(
        userProfileDataUseCase: GetUserProfileDataUseCase,
        userHandler: UserHandler,
        prefs: Prefs
    ) =
        ParticipantsViewModelFactory(
            userProfileDataUseCase,
            userHandler,
            prefs
        )
}
