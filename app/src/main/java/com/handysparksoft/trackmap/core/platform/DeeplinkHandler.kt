package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import androidx.core.app.ShareCompat

object DeeplinkHandler {

    const val PARAM_TRACKMAP_CODE = "code"

    /**
     * Generates a deeplink to access a specific Map by code
     */
    fun generateDeeplink(activity: Activity, code: String, name: String) {
        val joinTrackMapURL = "https://trackmap.firebaseapp.com?code=$code"
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
            .setType("text/plain")
            .setText("Join TrackMap \"$name\" here:\n$joinTrackMapURL")
            .intent
        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        }
    }
}
