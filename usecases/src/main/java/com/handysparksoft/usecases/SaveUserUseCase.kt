package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository

class SaveUserUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, userToken: String?, lastAccess: Long) =
        trackMapRepository.saveUser(userId, userToken, lastAccess)
}
