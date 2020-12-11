package com.handysparksoft.trackmap.core.extension

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.domain.model.ParticipantLocation

/**
 * Extension functions of: Location
 */
fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun Location.whenAvailable(block: (LatLng) -> Unit) {
    this.toLatLng().whenAvailable(block)
}

fun LatLng.whenAvailable(block: (LatLng) -> Unit) {
    if (this.latitude != 0.0 && this.longitude != 0.0) {
        block.invoke(this)
    }
}

fun ParticipantLocation.toLocation(): Location {
    val location = Location("participant")
    location.latitude = this.latitude
    location.longitude = this.longitude
    return location
}
