package com.handysparksoft.domain.model

import java.util.concurrent.TimeUnit

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
    var speed: Long,
    var lastAccess: Long
) {
    fun isSessionUser(userSessionId: String) = userId == userSessionId

    fun userAlias(sessionUser: Boolean, extra: String, shortIt: Boolean = false): String {
        val userAlias = if (shortIt) nickname?.take(10) + "..." else nickname ?: userId
        return if (sessionUser) {
            "$userAlias ($extra)"
        } else {
            userAlias
        }
    }

    fun getLastActivity(): Pair<Long, TimeUnit> {
        val diff = System.currentTimeMillis() - lastAccess
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days >= 1 -> Pair(days, TimeUnit.DAYS)
            hours >= 1 -> Pair(hours, TimeUnit.HOURS)
            minutes >= 1 -> Pair(minutes, TimeUnit.MINUTES)
            else -> Pair(seconds, TimeUnit.SECONDS)
        }
    }

    fun getLastActivityInMinutes() = (System.currentTimeMillis() - lastAccess) / (1000 * 60)
}
