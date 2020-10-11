package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant

class JoinTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String) : TrackMap? {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (targetTrackMap != null) {
            //FIXME: targetTrackMap.participantIds could be null if the last partcipant leaves the TrackMap
            val currentParticipantIds = targetTrackMap.participantIds ?: listOf()
            //if (targetTrackMap.ownerId != userId) {
                val updatedParticipantList = mutableSetOf<String>().also {
                    it.addAll(currentParticipantIds)
                    it.add(userId)
                }
                val trackMapParticipant = TrackMapParticipant(trackMapId, updatedParticipantList.toList())
                trackMapRepository.joinTrackMap(
                    userId,
                    trackMapId,
                    trackMapParticipant
                )

                // Return targetTrackMap updated with participant list updated with the new participant (userId)
                val participantIdsUpdated = currentParticipantIds.toMutableSet()
                participantIdsUpdated.add(userId)
                return targetTrackMap.copy(participantIds = participantIdsUpdated.toList())
//            } else {
//                // Can not join in it's own TrackMap
//            }
        } else {
            // Not found
        }
        return null
    }
}
