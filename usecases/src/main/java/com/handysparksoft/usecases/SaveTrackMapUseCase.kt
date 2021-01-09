package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class SaveTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(trackMapId: String, trackMap: TrackMap) =
        trackMapRepository.saveTrackMap(trackMapId, trackMap)

    suspend fun checkIfTrackMapCodeAlreadyExists(trackMapId: String): Boolean {
        val trackMap = trackMapRepository.getTrackMapById(trackMapId)
        return (trackMap is Result.Success)
    }
}
