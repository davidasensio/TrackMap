package com.handysparksoft.trackmap.features.splash

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.features.main.MainActivity
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        app.component.inject(this)

        val splashDelay = if (!prefs.splashScreenViewed) {
            prefs.splashScreenViewed = true
            FIRST_DELAY_MS
        } else {
            REST_DELAY_MS
        }
        Handler().postDelayed({
            startMainActivity()
        }, splashDelay)
    }

    private fun startMainActivity() {
        MainActivity.start(this)
        finish()
    }

    companion object {
        private const val FIRST_DELAY_MS = 2000L
        private const val REST_DELAY_MS = 500L
    }
}
