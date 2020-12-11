package com.handysparksoft.domain.model

import kotlin.math.roundToLong

/**
 * Class for mapping NMEA messages from GPS sensors to data class
 * Message types are:
 *  + $GP - GPS only
 *  + $GL - GLONASS only
 *  + $GN - Combined
 *
 *  altitudeAMSL -> Altitude Above Mean Sea Level
 *  altitudeGeoid -> Altitude Above WSG84 ellipsoid
 */
data class NMEAMessage(
    val type: String,
    val time: String,
    val latitude: String,
    val NS: String,
    val longitude: String,
    val EW: String,
    val fixQuality: Int,
    val numberOfSatellites: Int,
    val hdop: Double?,
    val altitudeAMSL: Long?,
    val altitudeAMSLUnit: String,
    val altitudeGeoid: Long?,
    val altitudeGeoidUnit: String,
    val checksum: String
) {
    companion object {
        fun textToNMEAMessage(message: String): NMEAMessage {
            val tokens = message.split(",")
            return NMEAMessage(
                tokens[0],
                tokens[1],
                tokens[2],
                tokens[3],
                tokens[4],
                tokens[5],
                tokens[6].toIntOrZero(),
                tokens[7].toIntOrZero(),
                tokens[8].toDoubleOrNull(),
                tokens[9].toDoubleOrNull()?.roundToLong(),
                tokens[10],
                tokens[11].toDoubleOrNull()?.roundToLong(),
                tokens[12],
                tokens[14],
            )
        }

        private fun String.toIntOrZero() = try {
            this.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
}




