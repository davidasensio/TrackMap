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
        val title = activity.getString(R.string.share_trackmap_link_title)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            type = "text/plain"
        }

        startChooserIntent(sendIntent, activity)
    }

    fun showContactShareIntent(activity: Activity) {
        val emailIntent =
            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(activity.getString(R.string.intent_contact_email)))
                putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.intent_contact_subject))
                putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.intent_contact_text))
            }

        startChooserIntent(emailIntent, activity)
    }

    private fun startChooserIntent(sendIntent: Intent, activity: Activity) {
        val shareIntent = Intent.createChooser(sendIntent, null)
        if (shareIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(shareIntent)
        }
    }
}
