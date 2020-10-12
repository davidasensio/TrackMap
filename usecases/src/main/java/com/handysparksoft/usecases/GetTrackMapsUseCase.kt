package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class GetTrackMapsUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String): Result<Map<String, TrackMap>> {
        val userTrackMaps = trackMapRepository.getUserTrackMaps(userId)
        if (userTrackMaps is Result.Success) {
            val syncedUserTrackMaps = Result.Success(HashMap<String, TrackMap>())
            userTrackMaps.data.forEach { (key, userTrackMap) ->
                val trackMap = trackMapRepository.getTrackMapById(userTrackMap.trackMapId)
                if (trackMap is Result.Success) {
                    trackMap.data?.let {
                       syncedUserTrackMaps.data[key] = it
                    }
                }
            }
            return syncedUserTrackMaps
        }
        return userTrackMaps
    }
}
