package com.handysparksoft.usecases

import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.domain.model.PushNotification

class SendPushNotificationUseCase(private val trackMapRepository: TrackMapRepository) {
    suspend fun execute(authorization: String, pushNotification: PushNotification) {
        trackMapRepository.sendPushNotification(authorization, pushNotification)
    }
}
