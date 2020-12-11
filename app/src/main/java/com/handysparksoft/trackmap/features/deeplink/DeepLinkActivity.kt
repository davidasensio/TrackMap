package com.handysparksoft.trackmap.features.deeplink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.handysparksoft.trackmap.core.platform.DeeplinkHandler
import com.handysparksoft.trackmap.features.entries.EntriesFragment
import com.handysparksoft.trackmap.features.main.MainActivity

class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {

        val action = intent.action
        val data: Uri? = intent.data

        if (Intent.ACTION_VIEW == action) {
            data?.getQueryParameter(DeeplinkHandler.PARAM_TRACKMAP_CODE)?.let { trackMapCode ->
                val intentTrackMap = Intent(this@DeepLinkActivity, MainActivity::class.java)
                intentTrackMap.putExtra(EntriesFragment.KEY_INTENT_TRACKMAP_CODE, trackMapCode)
                finish()
                startActivity(intentTrackMap)
            }
        }
    }
}

