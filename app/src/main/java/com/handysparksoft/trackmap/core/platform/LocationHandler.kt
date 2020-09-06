package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.trackmap.core.extension.toLatLng
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHandler @Inject constructor(private val context: Context) {

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
}
