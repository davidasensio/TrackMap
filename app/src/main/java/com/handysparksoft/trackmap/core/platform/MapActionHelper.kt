package com.handysparksoft.trackmap.core.platform

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapActionHelper(private val googleMap: GoogleMap) {
    var mapType: Int = GoogleMap.MAP_TYPE_NORMAL
        set(value) {
            field = value
            googleMap.mapType = value
        }

    fun moveToPosition(
        latLng: LatLng,
        zoom: Float = DEFAULT_ZOOM_LEVEL,
        tilt: Float = 0f,
        bearing: Float = 0f
    ) {
        val cameraPosition = CameraPosition.Builder()
            .target(latLng) // Sets the center of the map to Mountain View
            .zoom(zoom) // Sets the zoom
            .bearing(bearing) // Sets the orientation of the camera to east
            .tilt(tilt) // Sets the tilt of the camera to 30 degrees
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun animateCamera(
        zoom: Float = DEFAULT_ZOOM_LEVEL,
        tilt: Float = 0f,
        bearing: Float = 0f
    ) {
        val cameraPosition = CameraPosition.Builder()
            .zoom(zoom) // Sets the zoom
            .bearing(bearing) // Sets the orientation of the camera to east
            .tilt(tilt) // Sets the tilt of the camera to 30 degrees
            .build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    companion object {
        const val DEFAULT_ZOOM_LEVEL = 17f
    }

}
