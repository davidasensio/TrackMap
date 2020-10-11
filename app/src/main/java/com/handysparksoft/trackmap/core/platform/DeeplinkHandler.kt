package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.util.Base64
import androidx.core.app.ShareCompat
import java.nio.charset.Charset

object DeeplinkHandler {

    const val PARAM_TRACKMAP_CODE = "code"

    /**
     * Generates a deeplink to access a specific Map by code
     */
    fun generateDeeplink(activity: Activity, code: String, name: String) {
        val encodedCode = encodeBase64(code)
        val joinTrackMapURL = "https://trackmap.firebaseapp.com?code=$encodedCode"
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
            .setType("text/plain")
            .setText("Join TrackMap \"$name\" here:\n$joinTrackMapURL")
            .intent
        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        }
    }

    fun encodeBase64(message: String): String {
        val bytes = message.encodeToByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decodeBase64(message: String): String {
        val bytes = Base64.decode(message, Base64.DEFAULT)
        return String(bytes, Charset.defaultCharset())
    }
}
