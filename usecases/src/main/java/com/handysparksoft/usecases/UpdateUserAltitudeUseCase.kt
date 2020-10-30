package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository

class UpdateUserAltitudeUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, altitudeAMSL: Long, altitudeGeoid: Long) =
        trackMapRepository.updateUserAltitude(userId, altitudeAMSL, altitudeGeoid)
}
