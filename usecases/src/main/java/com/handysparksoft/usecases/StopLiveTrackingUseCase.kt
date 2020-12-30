package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMapLiveTrackingParticipant

class StopLiveTrackingUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String) {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)

        if (targetTrackMap is Result.Success && targetTrackMap.data != null) {
            val currentLiveParticipantIds = targetTrackMap.data?.liveParticipantIds ?: listOf()
            val updatedLiveParticipantList = mutableSetOf<String>().also {
                it.addAll(currentLiveParticipantIds)
                it.remove(userId)
            }
            val trackMapLiveTrackingParticipant =
                TrackMapLiveTrackingParticipant(trackMapId, updatedLiveParticipantList.toList())
            trackMapRepository.stopLiveTracking(
                trackMapId,
                trackMapLiveTrackingParticipant
            )
        }
    }
}
