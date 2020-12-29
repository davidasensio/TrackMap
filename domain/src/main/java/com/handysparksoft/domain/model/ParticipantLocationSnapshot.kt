package com.handysparksoft.domain.model

data class ParticipantLocationSnapshot(
    val userId: String,
    var latitude: Double,
    var longitude: Double,
    var altitudeAMSL: Long,
    var altitudeGeoid: Long,
    var speed: Long,
    var lastAccess: Long
) {
    companion object {
        fun fromParticipantLocation(participantLocation: ParticipantLocation): ParticipantLocationSnapshot {
            return ParticipantLocationSnapshot(
                userId = participantLocation.userId,
                latitude = participantLocation.latitude,
                longitude = participantLocation.longitude,
                altitudeAMSL = participantLocation.altitudeAMSL,
                altitudeGeoid = participantLocation.altitudeGeoid,
                speed = participantLocation.speed,
                lastAccess = System.currentTimeMillis()
            )
        }
    }
}
