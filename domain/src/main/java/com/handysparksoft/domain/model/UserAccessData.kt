package com.handysparksoft.domain.model

data class UserAccessData(
    val id: String,
    val userToken: String?,
    val lastAccess: Long
)
