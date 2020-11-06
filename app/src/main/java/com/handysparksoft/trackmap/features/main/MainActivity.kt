package com.handysparksoft.trackmap.features.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var prefs: Prefs

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app.component.inject(this)
        prefs.splashScreenAfterDestroy = false

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    override fun onDestroy() {
        prefs.splashScreenAfterDestroy = true
        super.onDestroy()
    }

    private fun setupUI() {
        val navController = findNavController(R.id.fragment)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity<MainActivity>() {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}
