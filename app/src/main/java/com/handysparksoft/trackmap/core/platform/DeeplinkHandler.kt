package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.util.Base64
import com.handysparksoft.trackmap.R
import java.nio.charset.Charset

object DeeplinkHandler {

    private const val BASE_URL = "https://trackmap.page.link/map"
    const val PARAM_TRACKMAP_CODE = "code"

    /**
     * Generates a deeplink to access a specific Map by code
     */
    fun generateDeeplink(activity: Activity, code: String, name: String) {
        val encodedCode = encodeBase64(code)
        val joinTrackMapURL = "$BASE_URL?code=$encodedCode"
        val content =
            activity.getString(R.string.share_trackmap_link_content, name, joinTrackMapURL, code)

        ShareIntentHandler.showTrackMapShareIntent(activity, content)
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
