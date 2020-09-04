package com.handysparksoft.domain.model

data class User(
    val id: String,
    val lastAccess: Long
)

data class UserTrackingAction(
    val id: String,
    val isTracking: Boolean,
    val lat: Double,
    val long: Double
)
