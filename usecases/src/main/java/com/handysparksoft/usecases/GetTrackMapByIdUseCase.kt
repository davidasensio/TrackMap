package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class GetTrackMapByIdUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(trackMapId: String): TrackMap? {
        val trackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (trackMap is Result.Success && trackMap.data != null) {
            return trackMap.data
        }
        return null
    }
}
