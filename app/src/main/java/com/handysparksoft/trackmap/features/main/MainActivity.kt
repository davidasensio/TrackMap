package com.handysparksoft.trackmap.features.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.extension.toast
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.core.platform.ShareIntentHandler
import com.handysparksoft.trackmap.databinding.ActivityMainBinding
import com.handysparksoft.trackmap.features.entries.sort.SortEntriesBottomSheetDialogFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var prefs: Prefs

    lateinit var binding: ActivityMainBinding


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

    override fun onDestroy() {
        prefs.splashScreenAfterDestroy = true
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                toast("Profile icon clicked!")
            }
            R.id.menuShare -> {
                ShareIntentHandler.showShareAppIntentChooser(this)
            }
            R.id.menuRate -> {
                ShareIntentHandler.rateAppInGooglePlayIntent(this)
            }
            R.id.menuSort -> {
                onSortMenuItemClickListener?.invoke()
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
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity<MainActivity>() {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
