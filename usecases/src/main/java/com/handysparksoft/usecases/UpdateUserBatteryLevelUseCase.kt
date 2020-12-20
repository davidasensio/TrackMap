package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.UserBatteryLevel

class UpdateUserBatteryLevelUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String, userBatteryLevel: UserBatteryLevel) =
        trackMapRepository.updateUserBatteryLevel(userId, userBatteryLevel)
}
