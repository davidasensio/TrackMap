package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap

class GetTrackMapsUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(): Map<String, TrackMap> = trackMapRepository.getTrackMapList()
}
