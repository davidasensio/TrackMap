package com.handysparksoft.data.source

import com.handysparksoft.domain.model.TrackMap

interface RemoteDataSource {
    suspend fun listTrackMap(): Map<String, TrackMap>
    suspend fun putTrackMap(id: String, trackMap: TrackMap)
}
