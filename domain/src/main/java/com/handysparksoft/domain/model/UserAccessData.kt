package com.handysparksoft.domain.model

data class UserAccessData(
    val id: String,
    val batteryLevel: Long,
    val userToken: String?,
    val lastAccess: Long
)
