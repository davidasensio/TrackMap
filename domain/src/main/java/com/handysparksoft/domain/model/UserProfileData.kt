package com.handysparksoft.domain.model

data class UserProfileData(
    val userId: String,
    val nickname: String,
    val fullName: String,
    val phone: String,
    val image: String?
)
