package com.handysparksoft.trackmap.di

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.usecases.GetTrackMapsUseCase
import com.handysparksoft.usecases.SaveTrackMapUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {

    @Provides
    fun getTrackMapsUseCaseProvider(trackMapRepository: TrackMapRepository) =
        GetTrackMapsUseCase(trackMapRepository)

    @Provides
    fun saveTrackMapUseCaseProvider(trackMapRepository: TrackMapRepository) =
        SaveTrackMapUseCase(trackMapRepository)
}
