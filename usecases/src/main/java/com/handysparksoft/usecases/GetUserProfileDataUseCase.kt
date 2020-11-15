package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.UserProfileData

class GetUserProfileDataUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String): Result<UserProfileData> {
        return trackMapRepository.getUserProfileData(userId)
    }
}
