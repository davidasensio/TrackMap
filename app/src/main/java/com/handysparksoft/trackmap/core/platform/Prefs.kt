package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.handysparksoft.domain.model.UserProfileData
import javax.inject.Inject

class Prefs @Inject constructor(context: Context) {
    private val prefsFilename = "com.handysparksoft.trackmap.platform.prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFilename, 0)

    var userProfileData: UserProfileData?
        get() {
            val serializedValue = prefs.getString(KEY_USER_PROFILE_DATA, "")
            return Gson().fromJson(serializedValue, UserProfileData::class.java)
        }
        set(value) {
            val serializedValue = Gson().toJson(value)
            prefs.edit().putString(KEY_USER_PROFILE_DATA, serializedValue).apply()
        }

    var splashScreenViewedForFirstTime: Boolean
        get() = prefs.getBoolean(KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME, value).apply()

    var splashScreenAfterDestroy: Boolean
        get() = prefs.getBoolean(KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY, value).apply()

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
        private const val KEY_USER_PROFILE_DATA = "key_user_profile_data"
        private const val KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME = "key_splash_screen_viewed_first"
        private const val KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY = "key_splash_screen_viewed_after"
        private const val KEY_LAST_LOCATION_LATITUDE = "key_last_location_latitude"
        private const val KEY_LAST_LOCATION_LONGITUDE = "key_last_location_longitude"
    }
}
