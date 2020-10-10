package com.handysparksoft.trackmap.core.di

import android.app.Application
import android.content.Context
import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.trackmap.core.data.server.ServerDataSource
import com.handysparksoft.trackmap.core.data.server.TrackMapDb
import com.handysparksoft.trackmap.core.platform.ConnectionHandler
import com.handysparksoft.trackmap.core.platform.GoogleMapHandler
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideApplicationContext(app: Application): Context = app

    @Provides
    fun remoteDataSourceProvider(): RemoteDataSource = ServerDataSource(TrackMapDb.service)

    @Provides
    fun googleMapHandlerProvider(app: Application): GoogleMapHandler = GoogleMapHandler(app)

    @Singleton
    @Provides
    fun connectionHandlerProvider(app: Application) = ConnectionHandler(app)
}
