package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.trackmap.BuildConfig
import com.handysparksoft.trackmap.core.extension.toLatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHandler @Inject constructor(private val context: Context) {
    companion object {
        private val LOCATION_MIN_TIME_MILLIS = if (BuildConfig.DEBUG) 500L else 1000L
        private val LOCATION_PRIORITY = if (BuildConfig.DEBUG) {
            LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    // Define a listener that responds to location updates
    private lateinit var locationCallback: LocationCallback

    // FusedLocationClient For accessing last known location and requestLocation updates
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var lastLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation(callback: (lastLocationParam: LatLng) -> Unit) {
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                lastLocation = it.result?.toLatLng()
                lastLocation?.let { lastLatLng ->
                    callback(lastLatLng)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun subscribeLocationUpdates(listener: (Location) -> Unit) {
        val locationRequest = LocationRequest()
        locationRequest.interval = LOCATION_MIN_TIME_MILLIS
        locationRequest.fastestInterval = LOCATION_MIN_TIME_MILLIS
        locationRequest.priority = LOCATION_PRIORITY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    listener(it)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun unsubscribeLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
