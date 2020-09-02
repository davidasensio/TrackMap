package com.handysparksoft.data.repository

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap

class TrackMapRepository(private val remoteDataSource: RemoteDataSource) {

    suspend fun getTrackMapList(): Map<String, TrackMap> {
        return remoteDataSource.listTrackMap()
    }

    suspend fun saveTrackMap(id: String, trackMap: TrackMap) {
        remoteDataSource.putTrackMap(id, trackMap)
    }
}
