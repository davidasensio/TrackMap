package com.handysparksoft.trackmap.core.di

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.usecases.*
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {

    @Provides
    fun saveUserUseCaseProvider(trackMapRepository: TrackMapRepository) =
        SaveUserUseCase(trackMapRepository)

    @Provides
    fun joinTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        JoinTrackMapUseCase(trackMapRepository)

    @Provides
    fun getTrackMapsUseCaseProvider(trackMapRepository: TrackMapRepository) =
        GetTrackMapsUseCase(trackMapRepository)

    @Provides
    fun saveTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        SaveTrackMapUseCase(trackMapRepository)

    @Provides
    fun saveUserTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        SaveUserTrackMapUseCase(trackMapRepository)

    @Provides
    fun updateUserLocationUseCaseProvider(trackMapRepository: TrackMapRepository) =
        UpdateUserLocationUseCase(trackMapRepository)

    @Provides
    fun updateUserGPSDataUseCaseProvider(trackMapRepository: TrackMapRepository) =
        UpdateUserGPSDataUseCase(trackMapRepository)

    @Provides
    fun updateUserBatteryLevelUseCaseProvider(trackMapRepository: TrackMapRepository) =
        UpdateUserBatteryLevelUseCase(trackMapRepository)

    @Provides
    fun leaveTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        LeaveTrackMapUseCase(trackMapRepository)

    @Provides
    fun getUserProfileUseCaseProvider(trackMapRepository: TrackMapRepository) =
        GetUserProfileDataUseCase(trackMapRepository)

    @Provides
    fun getUserAccessDataUseCaseProvider(trackMapRepository: TrackMapRepository) =
        GetUserAccessDataUseCase(trackMapRepository)

    @Provides
    fun updateUserProfileUseCaseProvider(trackMapRepository: TrackMapRepository) =
        UpdateUserProfileUseCase(trackMapRepository)

    @Provides
    fun favoriteTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        FavoriteTrackMapUseCase(trackMapRepository)

    @Provides
    fun startLiveTrackingUseCaseProvider(trackMapRepository: TrackMapRepository) =
        StartLiveTrackingUseCase(trackMapRepository)

    @Provides
    fun stopLiveTrackingUseCaseProvider(trackMapRepository: TrackMapRepository) =
        StopLiveTrackingUseCase(trackMapRepository)

    @Provides
    fun sendPushNotificationUseCaseProvider(trackMapRepository: TrackMapRepository) =
        SendPushNotificationUseCase(trackMapRepository)

    @Provides
    fun getTrackMapByIdUseCaseProvider(trackMapRepository: TrackMapRepository) =
        GetTrackMapByIdUseCase(trackMapRepository)
}
