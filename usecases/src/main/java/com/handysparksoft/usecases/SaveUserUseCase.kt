package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository

class SaveUserUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, batteryLevel: Long, userToken: String?, lastAccess: Long) =
        trackMapRepository.saveUser(userId, batteryLevel, userToken, lastAccess)
}
