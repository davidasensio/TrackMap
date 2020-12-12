package com.handysparksoft.trackmap.features.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.onPreDraw
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.ShareIntentHandler
import com.handysparksoft.trackmap.core.platform.TrackEvent
import com.handysparksoft.trackmap.core.platform.track
import com.handysparksoft.trackmap.core.util.spotlight.SpotlightHelper
import com.handysparksoft.trackmap.core.util.spotlight.SpotlightHelper.Tooltip
import com.handysparksoft.trackmap.core.util.spotlight.SpotlightHelper.TooltipPosition.*
import com.handysparksoft.trackmap.databinding.ActivityMainBinding
import com.takusemba.spotlight.Spotlight
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var prefs: Prefs

    lateinit var binding: ActivityMainBinding

    private var spotlightOnboarding: Spotlight? = null
    private var onboardingStarted = false
    private var onboardingEnded = false


    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private var onSortMenuItemClickListener: (() -> Unit)? = null

    fun onSortMenuItemClick(listener: () -> Unit) {
        onSortMenuItemClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app.component.inject(this)
        prefs.splashScreenAfterDestroy = false

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupUI()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        binding.root.rootView.onPreDraw {
            if (!prefs.onboardingSpotlightViewed) {
                Handler().postDelayed({
                    spotlightOnboarding = getSpotlight().apply { start() }
                }, 250)
            }
        }
    }

    override fun onDestroy() {
        prefs.splashScreenAfterDestroy = true
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (onboardingStarted && !onboardingEnded) {
            spotlightOnboarding?.finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.fragment).navigate(R.id.action_entriesFragment_to_proifleFragment)
                TrackEvent.HomeActionClick.track()
            }
            R.id.menuShare -> {
                ShareIntentHandler.showShareAppIntentChooser(this)
                TrackEvent.MenuShareActionClick.track()
            }
            R.id.menuRate -> {
                ShareIntentHandler.rateAppInGooglePlayIntent(this)
                TrackEvent.MenuRateActionClick.track()
            }
            R.id.menuSort -> {
                onSortMenuItemClickListener?.invoke()
                TrackEvent.MenuSortActionClick.track()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_user_profile_white)
    }

    private fun setupUI() {
        val navController = findNavController(R.id.fragment)
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.label) {
                "Entries" -> supportActionBar?.show()
                else -> supportActionBar?.hide()
            }
            when (destination.label) {
                "Profile" -> binding.bottomNavigation.visibility = View.GONE
                else -> binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    private fun getSpotlight(): Spotlight {
        val toolbar = binding.toolbar
        val trackMapsTitle = binding.root.rootView.findViewById<View>(R.id.yourTrackMapsTitle)
        val bottomNavigationViewGroup = binding.bottomNavigation.getChildAt(0) as? ViewGroup

        val targets = listOf(
            toolbar.getChildAt(1), // Profile
            bottomNavigationViewGroup?.getChildAt(1), // Join
            bottomNavigationViewGroup?.getChildAt(2), // Create
            trackMapsTitle, // TrackMaps title
            toolbar.getChildAt(2), // Menu
            bottomNavigationViewGroup?.getChildAt(0), // Home
        )

        val tooltips = listOf(
            Tooltip(getString(R.string.onboarding_profile), TopLeft),
            Tooltip(getString(R.string.onboarding_bottom_nav_join), BottomRight),
            Tooltip(getString(R.string.onboarding_bottom_nav_create), BottomRight),
            Tooltip(getString(R.string.onboarding_trackmaps), TopRight),
            Tooltip(getString(R.string.onboarding_menu), TopRight),
            Tooltip(getString(R.string.onboarding_bottom_nav_home), BottomLeft),
        )

        return SpotlightHelper().buildSpotlight(
            activity = this,
            targetViews = targets,
            tooltips = tooltips,
            onStarted = { onboardingStarted = true },
            onEnded = { onboardingEnded = true },
            onCompleted = { prefs.onboardingSpotlightViewed = true }
        )
    }

    companion object {
        fun start(context: Context) {
            context.startActivity<MainActivity>() {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
