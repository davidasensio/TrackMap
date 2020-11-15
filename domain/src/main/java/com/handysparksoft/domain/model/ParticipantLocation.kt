package com.handysparksoft.domain.model

data class ParticipantLocation(
    val userId: String,
    val userNickname: String,
    var latitude: Double,
    var longitude: Double,
    var altitudeAMSL: Long,
    var altitudeGeoid: Long,
    var speed: Long
) {
    fun isSessionUser(userSessionId: String) = userId == userSessionId

    fun userAlias(sessionUser: Boolean, shortIt: Boolean = false): String {
        val userAlias = if (shortIt) userNickname.take(10) +  "..." else userNickname
        return if (sessionUser) {
            "$userAlias (you)"
        } else {
            userAlias
        }
    }
}
