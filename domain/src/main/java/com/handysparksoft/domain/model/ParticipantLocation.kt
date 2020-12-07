package com.handysparksoft.domain.model

data class ParticipantLocation(
    val userId: String,
    val nickname: String?,
    val fullName: String?,
    val phone: String?,
    val image: String?,
    var latitude: Double,
    var longitude: Double,
    var altitudeAMSL: Long,
    var altitudeGeoid: Long,
    var speed: Long
) {
    fun isSessionUser(userSessionId: String) = userId == userSessionId

    fun userAlias(sessionUser: Boolean, shortIt: Boolean = false): String {
        val userAlias = if (shortIt) nickname?.take(10) +  "..." else nickname ?: userId
        return if (sessionUser) {
            "$userAlias (you)"
        } else {
            userAlias
        }
    }
}
