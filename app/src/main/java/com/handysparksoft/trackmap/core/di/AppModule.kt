package com.handysparksoft.trackmap.core.di

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.trackmap.core.data.server.ServerDataSource
import com.handysparksoft.trackmap.core.data.server.TrackMapDb
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun remoteDataSourceProvider(): RemoteDataSource = ServerDataSource(TrackMapDb.service)
}
