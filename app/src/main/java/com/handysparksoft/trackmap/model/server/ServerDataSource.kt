package com.handysparksoft.trackmap.model.server

import com.handysparksoft.data.source.RemoteDataSource
import com.handysparksoft.domain.model.TrackMap

class ServerDataSource(private val service: TrackMapService) : RemoteDataSource {
    override suspend fun listTrackMap(): Map<String, TrackMap> {
        return service.listTrackMaps()
    }

    override suspend fun putTrackMap(id: String, trackMap: TrackMap) {
        service.saveTrackMap(id, trackMap)
    }
}
