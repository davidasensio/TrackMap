package com.handysparksoft.trackmap.core.extension

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Extension functions of: Location
 */
fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}
