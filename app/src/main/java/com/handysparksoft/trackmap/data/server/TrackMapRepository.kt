package com.handysparksoft.trackmap.data.server

import com.handysparksoft.trackmap.domain.TrackMap

class TrackMapRepository {
    private val service = TrackMapDb.service

    suspend fun getTrackMapList(): Map<String, TrackMap> {
        return service.listTrackMapAsync()
    }

    suspend fun putTrackMap(id: String, trackMap: TrackMap) {
        service.putTrackMap(id, trackMap)
    }
}
