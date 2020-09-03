package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class JoinTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String) {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (targetTrackMap != null) {
            if (targetTrackMap.ownerId != userId) {
                trackMapRepository.joinTrackMap(trackMapId)
            } else {
                // Can not join in it's own TrackMap
            }
        } else {
            // Not found
        }
    }
}
