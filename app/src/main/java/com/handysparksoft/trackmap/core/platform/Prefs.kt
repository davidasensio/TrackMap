package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.handysparksoft.domain.model.UserProfileData
import java.util.concurrent.TimeUnit
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

    var userToken: String?
        get() = prefs.getString(KEY_USER_FIREBASE_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_USER_FIREBASE_TOKEN, value).apply()


    var splashScreenViewedForFirstTime: Boolean
        get() = prefs.getBoolean(KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME, value).apply()

    var splashScreenAfterDestroy: Boolean
        get() = prefs.getBoolean(KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY, false)
        set(value) = prefs.edit().putBoolean(KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY, value).apply()

    var onboardingSpotlightViewed: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SPOTLIGHT_VIEWED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_SPOTLIGHT_VIEWED, value).apply()

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


    // UserDataProfile Cache (Cache for list of participants)
    var userDataProfileMapCache: HashMap<String, UserProfileData>
        get() {
            return if (!isCacheExpired()) {
                val serializedValue = prefs.getString(KEY_USER_DATA_PROFILE_MAP_CACHE, null)
                serializedValue?.let {
                    try {
                        val type = object : TypeToken<HashMap<String, UserProfileData>>() {}.type
                        Gson().fromJson(it, type)
                    } catch (e: JsonSyntaxException) {
                        Log.e("***", "Error parsing DataProfileMapCache")
                        HashMap()
                    }
                } ?: HashMap()
            } else {
                Log.d("***", "Cache has expired!. Data will be renewed")
                HashMap()
            }
        }
        set(value) {
            val serializedValue = Gson().toJson(value)
            prefs.edit().putString(KEY_USER_DATA_PROFILE_MAP_CACHE, serializedValue).apply()
        }

    // Cache will expires after a period of time since the last update
    var userDataProfileMapCacheLastUpdate: Long
        get() = prefs.getLong(KEY_USER_DATA_PROFILE_MAP_CACHE_LAST_UPDATE, 0)
        set(value) = prefs.edit().putLong(KEY_USER_DATA_PROFILE_MAP_CACHE_LAST_UPDATE, value)
            .apply()

    private fun isCacheExpired(): Boolean =
        (System.currentTimeMillis() - userDataProfileMapCacheLastUpdate) > CACHE_EXPIRATION_TIME


    companion object {
        private const val KEY_USER_FIREBASE_TOKEN = "key_user_firebase_token"
        private const val KEY_USER_PROFILE_DATA = "key_user_profile_data"
        private const val KEY_SPLASH_SCREEN_VIEWED_FOR_FIRST_TIME = "key_splash_screen_viewed_first"
        private const val KEY_SPLASH_SCREEN_VIEWED_AFTER_DESTROY = "key_splash_screen_viewed_after"
        private const val KEY_ONBOARDING_SPOTLIGHT_VIEWED = "key_onboarding_spotlight_viewed"
        private const val KEY_LAST_LOCATION_LATITUDE = "key_last_location_latitude"
        private const val KEY_LAST_LOCATION_LONGITUDE = "key_last_location_longitude"
        private const val KEY_USER_DATA_PROFILE_MAP_CACHE = "key_user_data_profile_map_cache"
        private const val KEY_USER_DATA_PROFILE_MAP_CACHE_LAST_UPDATE =
            "key_user_data_profile_map_cache_last_update"

        private val CACHE_EXPIRATION_TIME = TimeUnit.HOURS.toMillis(8)
    }
}
