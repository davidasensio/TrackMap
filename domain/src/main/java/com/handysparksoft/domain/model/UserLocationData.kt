package com.handysparksoft.domain.model

data class UserLocationData(
    val latitude: Double,
    val longitude: Double
)

data class UserAltitudeData(
    val altitudeAMSL: Long,
    val altitudeGeoid: Long
)
