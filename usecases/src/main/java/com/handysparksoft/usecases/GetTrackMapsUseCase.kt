package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class GetTrackMapsUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String): Map<String, TrackMap> {
        val userTrackMaps = trackMapRepository.getUserTrackMaps(userId)
        val syncedUserTrackMaps = HashMap<String, TrackMap>()
        userTrackMaps.forEach { (key, userTrackMap) ->
            val trackMap = trackMapRepository.getTrackMapById(userTrackMap.trackMapId)
            trackMap?.let {
                syncedUserTrackMaps[key] = it
            }
        }
        return syncedUserTrackMaps
    }
}
