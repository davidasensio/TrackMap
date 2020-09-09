package com.handysparksoft.trackmap.core.extension

import android.location.Location
import com.google.android.gms.maps.model.LatLng

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
