package com.handysparksoft.domain.model

data class PushNotification(
    val to: String?,
    val registration_ids: List<String>?,
    val notification: NotificationData
)

data class NotificationData(
    val sound: String = "default",
    val body: String = "",
    val title: String = "",
    val content_available: Boolean = true,
    val priority: String = "high"
)
