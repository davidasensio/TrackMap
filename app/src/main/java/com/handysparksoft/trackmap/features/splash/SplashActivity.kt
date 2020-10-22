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

        val splashDelay = if (!prefs.splashScreenViewedForFirstTime) {
            prefs.splashScreenViewedForFirstTime = true
            FIRST_DELAY_MS
        } else {
            if (prefs.splashScreenAfterDestroy) {
                AFTER_DESTROY_DELAY_MS
            } else {
                REST_DELAY_MS
            }
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
        private const val FIRST_DELAY_MS = 2500L
        private const val AFTER_DESTROY_DELAY_MS = 1250L
        private const val REST_DELAY_MS = 750L
    }
}
