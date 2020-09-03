package com.handysparksoft.trackmap.di

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.trackmap.data.server.ServerDataSource
import com.handysparksoft.trackmap.data.server.TrackMapDb
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun remoteDataSourceProvider(): RemoteDataSource = ServerDataSource(TrackMapDb.service)
}
