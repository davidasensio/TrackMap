package com.handysparksoft.domain.model

data class UserLocationData(
    val latitude: Double,
    val longitude: Double,
    val lastAccess: Long? = null
)
