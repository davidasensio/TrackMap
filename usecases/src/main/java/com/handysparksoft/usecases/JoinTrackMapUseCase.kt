package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.TrackMapParticipant

class JoinTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String): TrackMap? {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (targetTrackMap is Result.Success && targetTrackMap.data != null) {
            //FIXME: targetTrackMap.participantIds could be null if the last partcipant leaves the TrackMap
            val currentParticipantIds = targetTrackMap.data!!.participantIds ?: listOf()
            val updatedParticipantList = mutableSetOf<String>().also {
                it.addAll(currentParticipantIds)
                it.add(userId)
            }
            val trackMapParticipant =
                TrackMapParticipant(trackMapId, updatedParticipantList.toList())
            trackMapRepository.joinTrackMap(
                userId,
                trackMapId,
                trackMapParticipant
            )

            // Return targetTrackMap updated with participant list updated with the new participant (userId)
            val participantIdsUpdated = currentParticipantIds.toMutableSet()
            participantIdsUpdated.add(userId)
            return targetTrackMap.data!!.copy(participantIds = participantIdsUpdated.toList())
        } else {
            // Not found
        }
        return null
    }
}
