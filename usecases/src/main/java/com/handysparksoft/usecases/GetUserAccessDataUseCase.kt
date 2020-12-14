package com.handysparksoft.usecases

import com.handysparksoft.data.Result
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.UserAccessData

class GetUserAccessDataUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(userId: String): Result<UserAccessData> {
        return trackMapRepository.getUserAccessData(userId)
    }
}
