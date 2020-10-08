package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class Prefs @Inject constructor(context: Context) {
    private val prefsFilename = "com.handysparksoft.trackmap.platform.prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, 0)

    var splashScreenViewed: Boolean
        get() = prefs.getBoolean(KEY_SPLASH_SCREEN_VIEWED, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLASH_SCREEN_VIEWED, value).apply()

    var lastLocationLatitude: Float
        get() = prefs.getFloat(KEY_LAST_LOCATION_LATITUDE, 0f)
        set(value) = prefs.edit().putFloat(KEY_LAST_LOCATION_LATITUDE, value).apply()

    var lastLocationLongitude: Float
        get() = prefs.getFloat(KEY_LAST_LOCATION_LONGITUDE, 0f)
        set(value) = prefs.edit().putFloat(KEY_LAST_LOCATION_LONGITUDE, value).apply()

    var lastLocation: LatLng
        get() = LatLng(lastLocationLatitude.toDouble(), lastLocationLongitude.toDouble())
        set(value) {
            lastLocationLatitude = value.latitude.toFloat()
            lastLocationLongitude = value.longitude.toFloat()
        }


    companion object {
        private const val KEY_SPLASH_SCREEN_VIEWED = "key_splash_screen_viewed"
        private const val KEY_LAST_LOCATION_LATITUDE = "key_last_location_latitude"
        private const val KEY_LAST_LOCATION_LONGITUDE = "key_last_location_longitude"
    }
}
