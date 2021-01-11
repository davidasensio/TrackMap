package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.domain.model.NMEAMessage
import com.handysparksoft.domain.model.ParticipantLocationSnapshot
import com.handysparksoft.trackmap.BuildConfig
import com.handysparksoft.trackmap.core.extension.toLatLng
import java.lang.reflect.Method
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.fixedRateTimer

@Singleton
class LocationHandler @Inject constructor(private val context: Context) {

    // Define a listener that responds to location updates
    private lateinit var locationCallback: LocationCallback

    // FusedLocationClient For accessing last known location and requestLocation updates
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private lateinit var locationManagerGPS: LocationManager
    private lateinit var locationListenerGPS: LocationListener

    private var lastLocation: LatLng? = null

    // For max / min / avg purposes when tracking
    private val participantSnapshots = HashMap<String, MutableList<ParticipantLocationSnapshot>>()
    private var canUpdateSnapshots = false

    init {
        fixedRateTimer(
            name = "SnapshotUpdateRate",
            period = SNAPSHOT_UPDATE_RATE_MILLIS,
            action = {
                canUpdateSnapshots = true
            }
        )
    }

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

    /**
     * Subscribe and unsubscribe functions to Location updates by Google Play Services (Fused)
     */
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

    /**
     * Subscribe and unsubscribe functions to Location updates by GPS provider
     * NMEA message sample: $GNGGA,130657.00,3929.215732,N,00022.071493,W,1,05,1.8,21.3,M,51.3,M,,*54
     */
    @SuppressLint("MissingPermission")
    fun subscribeToNMEAMessagesAndSpeed(listener: (message: NMEAMessage, speedInKmh: Long) -> Unit) {
        var currentSpeed = 0L
        locationManagerGPS = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListenerGPS = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.hasSpeed()) {
                    currentSpeed = (location.speed * MS_TO_KMH_FACTOR).toLong()
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Register the listener with the Location Manager to receive GPS location updates
        locationManagerGPS.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_GPS_MIN_TIME_MILLIS,
            LOCATION_GPS_MIN_DISTANCE_METERS,
            locationListenerGPS
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            locationManagerGPS.addNmeaListener { message, timestamp ->
                onValidMessage(message) {
                    nmeaLog.append(message)
                    listener(it, currentSpeed)
                }
            }
        } else {
            val nmeaListenerDeprecated = GpsStatus.NmeaListener { timestamp, message ->
                onValidMessage(message) {
                    listener(it, currentSpeed)
                }
            }
            try {
                val addNmeaListener: Method =
                    LocationManager::class.java.getMethod(
                        "addNmeaListener",
                        GpsStatus.NmeaListener::class.java
                    )
                addNmeaListener.invoke(locationManagerGPS, nmeaListenerDeprecated)
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun unsubscribeToNMEAMessages() {
        if (::locationListenerGPS.isInitialized) {
            locationManagerGPS.removeUpdates(locationListenerGPS)
        }
    }

    private fun onValidMessage(message: String, callback: (nmeaMessage: NMEAMessage) -> Unit) {
        if (REGEX_GGA_MESSAGE.toRegex().containsMatchIn(message)) {
            val nmeaMessage = NMEAMessage.textToNMEAMessage(message)
            if (nmeaMessage.altitudeAMSL != null) {
                callback.invoke(nmeaMessage)
            }
        }
    }

    fun addSnapshots(
        liveTrackingMapIds: MutableSet<String>,
        snapshot: ParticipantLocationSnapshot
    ) {
        if (canUpdateSnapshots) {
            canUpdateSnapshots = false
            liveTrackingMapIds.forEach {
                addParticipantSnapshot(it, snapshot)
            }
        }
    }

    fun addParticipantSnapshot(trackMapId: String, snapshot: ParticipantLocationSnapshot) {
        val trackMapSnapshots = participantSnapshots[trackMapId] ?: mutableListOf()
        trackMapSnapshots.add(snapshot)
        participantSnapshots[trackMapId] = trackMapSnapshots
    }

    @Synchronized
    fun getMaxSpeed(trackMapId: String, userId: String): Long {
        return try {
            participantSnapshots[trackMapId]?.filter { it.userId == userId }?.maxOf { it.speed }
                ?: 0
        } catch (e: NoSuchElementException) {
            0
        }
    }

    @Synchronized
    fun getMaxAltitudeAMSL(trackMapId: String, userId: String): Long {
        return try {
            participantSnapshots[trackMapId]?.filter { it.userId == userId }
                ?.maxOf { it.altitudeAMSL } ?: 0
        } catch (e: NoSuchElementException) {
            0
        }
    }

    companion object {
        val nmeaLog = StringBuilder()

        // Google Play Services
        private val LOCATION_MIN_TIME_MILLIS = if (BuildConfig.DEBUG) 500L else 2000L
        private val LOCATION_PRIORITY = if (BuildConfig.DEBUG) {
            LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        // Location Manager
        private val LOCATION_GPS_MIN_TIME_MILLIS = if (BuildConfig.DEBUG) 0 else 2000L
        private val LOCATION_GPS_MIN_DISTANCE_METERS = if (BuildConfig.DEBUG) 0f else 2f
        private const val REGEX_GGA_MESSAGE = "\\\$G[PLN]GGA.*"
        private const val MS_TO_KMH_FACTOR = 3.6

        // Snapshots Update Rate
        private const val SNAPSHOT_UPDATE_RATE_MILLIS = 5000L
    }
}
