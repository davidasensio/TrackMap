package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.UserGPSData

class UpdateUserGPSDataUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, userGPSData: UserGPSData) =
        trackMapRepository.updateUserGPSData(userId,  userGPSData)
}
