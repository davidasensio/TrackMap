package com.handysparksoft.trackmap.di

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.data.source.RemoteDataSource
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun trackMapRepositoryProvider(remoteDataSource: RemoteDataSource) =
        TrackMapRepository(remoteDataSource)
}
