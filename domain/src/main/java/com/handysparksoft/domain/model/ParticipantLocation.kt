package com.handysparksoft.domain.model

data class ParticipantLocation(
    val userId: String,
    var latitude: Double,
    var longitude: Double,
    var altitudeAMSL: Long,
    var altitudeGeoid: Long
) {
    fun isSessionUser(userSessionId: String) = userId == userSessionId

    fun userAlias(sessionUser: Boolean): String {
        return if (sessionUser) {
            "$userId (you)"
        } else {
            userId
        }
    }
}
