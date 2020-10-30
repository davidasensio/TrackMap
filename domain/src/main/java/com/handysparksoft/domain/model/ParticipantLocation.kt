package com.handysparksoft.domain.model

data class ParticipantLocation(
    val userId: String,
    var latitude: Double,
    var longitude: Double,
    var altitudeAMSL: Long,
    var altitudeGeoid: Long,
    var speed: Long
) {
    fun isSessionUser(userSessionId: String) = userId == userSessionId

    fun userAlias(sessionUser: Boolean, shortIt: Boolean = false): String {
        val userIdText = if (shortIt) userId.take(10) +  "..." else userId
        return if (sessionUser) {
            "$userIdText (you)"
        } else {
            userIdText
        }
    }
}
