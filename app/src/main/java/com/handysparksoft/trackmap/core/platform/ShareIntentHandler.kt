package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.handysparksoft.trackmap.R

object ShareIntentHandler {
    fun showShareAppIntentChooser(activity: Activity) {
        val title = activity.getString(R.string.share_app_title)
        val content = activity.getString(R.string.share_app_content)
        val googlePlayUrl = activity.getString(R.string.share_app_google_play_url)
        val shareText = "$content \uD83D\uDCCD\n\n$googlePlayUrl"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        startChooserIntent(sendIntent, activity)
    }

    fun rateAppInGooglePlayIntent(activity: Activity) {
        val url = "https://play.google.com/store/apps/details?id=com.handysparksoft.trackmap"
        val viewIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
            setPackage("com.android.vending")
        }

        startChooserIntent(viewIntent, activity)
    }

    fun showTrackMapShareIntent(activity: Activity, content: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }

        startChooserIntent(sendIntent, activity)
    }

    private fun startChooserIntent(sendIntent: Intent, activity: Activity) {
        val shareIntent = Intent.createChooser(sendIntent, null)
        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        }
    }
}
