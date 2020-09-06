package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository

class UpdateUserLocationUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, latitude: Double, longitude: Double) =
        trackMapRepository.updateUserLocation(userId, latitude, longitude)
}
