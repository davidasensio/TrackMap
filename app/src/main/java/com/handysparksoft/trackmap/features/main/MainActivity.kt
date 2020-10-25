package com.handysparksoft.trackmap.features.main

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.databinding.ActivityMainlBinding
import com.handysparksoft.trackmap.features.create.CreateFragment
import com.handysparksoft.trackmap.features.entries.EntriesFragment
import com.handysparksoft.trackmap.features.join.JoinFragment
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var prefs: Prefs

    lateinit var binding: ActivityMainlBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private val entriesFragment by lazy { EntriesFragment.newInstance() }
    private val joinFragment by lazy { JoinFragment.newInstance() }
    private val createFragment by lazy { CreateFragment.newInstance() }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(entriesFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_join_map -> {
                    loadFragment(joinFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_create_map -> {
                    loadFragment(createFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app.component.inject(this)
        prefs.splashScreenAfterDestroy = false

        binding = ActivityMainlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    override fun onDestroy() {
        prefs.splashScreenAfterDestroy = true
        super.onDestroy()
    }

    private fun setupUI() {
        loadFragment(entriesFragment, null)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(
            mOnNavigationItemSelectedListener
        )
    }

    private fun loadFragment(fragment: Fragment, backStack: String? = BACK_STACK_NAME) {
        supportFragmentManager.popBackStack(backStack, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .also {
                if (backStack != null) {
                    it.addToBackStack(backStack)
                }
            }
            .commit()
    }

    companion object {
        private const val BACK_STACK_NAME = "Main"

        fun start(context: Context) {
            context.startActivity<MainActivity>()
        }
    }
}
