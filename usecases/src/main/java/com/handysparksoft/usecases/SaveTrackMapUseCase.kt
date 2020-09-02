package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class SaveTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(id: String, trackMap: TrackMap) =
        trackMapRepository.saveTrackMap(id, trackMap)
}
