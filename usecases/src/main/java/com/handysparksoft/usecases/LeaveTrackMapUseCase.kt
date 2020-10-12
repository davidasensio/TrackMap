package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.TrackMapParticipant

class LeaveTrackMapUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, trackMapId: String) {
        val targetTrackMap = trackMapRepository.getTrackMapById(trackMapId)
        if (targetTrackMap is Result.Success && targetTrackMap.data != null) {
            val updatedParticipantList = mutableSetOf<String>().also {
                it.addAll(targetTrackMap.data!!.participantIds)
                it.remove(userId)
            }
            val trackMapParticipant = TrackMapParticipant(trackMapId, updatedParticipantList.toList())
            trackMapRepository.leaveTrackMap(
                userId,
                trackMapId,
                trackMapParticipant
            )
        }
    }
}
