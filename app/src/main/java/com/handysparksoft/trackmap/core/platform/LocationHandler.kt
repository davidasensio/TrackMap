package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.trackmap.BuildConfig
import com.handysparksoft.trackmap.core.extension.toLatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHandler @Inject constructor(private val context: Context) {
    companion object {
        private val LOCATION_MIN_TIME_MILLIS = if (BuildConfig.DEBUG) 10L else 1000L
        private val LOCATION_MIN_DISTANCE_METERS = if (BuildConfig.DEBUG) 0.25f else 5f
    }

    // Acquire a reference to the system Location Manager
    private lateinit var locationManager: LocationManager

    // Define a listener that responds to location updates
    private lateinit var locationListener: LocationListener

    // For accessing last known location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var lastLocation: LatLng? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation(callback: (lastLocationParam: LatLng) -> Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
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
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define a listener that responds to location updates
        locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                listener(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_MIN_TIME_MILLIS,
            LOCATION_MIN_DISTANCE_METERS,
            locationListener
        )

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            LOCATION_MIN_TIME_MILLIS,
            LOCATION_MIN_DISTANCE_METERS,
            locationListener
        )

    }

    fun unsubscribeLocationUpdates() {
        if (::locationListener.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }
}
