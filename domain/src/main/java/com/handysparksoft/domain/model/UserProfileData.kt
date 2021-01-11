package com.handysparksoft.domain.model

import java.io.Serializable

data class UserProfileData(
    val userId: String,
    val nickname: String?,
    val fullName: String?,
    val phone: String?,
    val image: String? // Base64 format
) : Serializable
