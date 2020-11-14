package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.UserProfileData

class UpdateUserProfileUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, userProfile: UserProfileData) =
        trackMapRepository.updateUserProfile(userId, userProfile)
}
