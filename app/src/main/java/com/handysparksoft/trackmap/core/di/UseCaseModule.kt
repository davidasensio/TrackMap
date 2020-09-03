package com.handysparksoft.trackmap.core.di

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveTrackMapUseCase
import com.handysparksoft.usecases.SaveUserUseCase
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
}
