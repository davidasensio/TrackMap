package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant

class JoinTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String) : TrackMap? {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (targetTrackMap != null) {
            if (targetTrackMap.ownerId != userId) {
                val updatedParticipantList = mutableListOf<String>().also {
                    it.addAll(targetTrackMap.participantIds)
                    it.add(userId)
                }
                val trackMapParticipant = TrackMapParticipant(trackMapId, updatedParticipantList)
                trackMapRepository.joinTrackMap(
                    userId,
                    trackMapId,
                    targetTrackMap,
                    trackMapParticipant
                )

                // Return targetTrackMap updated with participant list updated with the new participant (userId)
                val participantIdsUpdated = targetTrackMap.participantIds.toMutableList()
                participantIdsUpdated.add(userId)
                return targetTrackMap.copy(participantIds = participantIdsUpdated)
            } else {
                // Can not join in it's own TrackMap
            }
        } else {
            // Not found
        }
        return null
    }
}
