package com.handysparksoft.trackmap.features.entries

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.crashlytics.android.Crashlytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.extension.toast
import com.handysparksoft.trackmap.core.platform.LocationHandler
import com.handysparksoft.trackmap.core.platform.LocationForegroundService
import com.handysparksoft.trackmap.core.platform.PermissionChecker
import com.handysparksoft.trackmap.core.platform.Prefs
import com.handysparksoft.trackmap.features.create.CreateActivity
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Content
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Loading
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<MainActivity>()
        }
    }

    private lateinit var adapter: TrackMapEntriesAdapter
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.mainViewModelFactory
        ).get(MainViewModel::class.java)
    }

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var locationHandler: LocationHandler

    // FIXME make injectable
    private lateinit var permissionChecker: PermissionChecker

    private lateinit var locationForegroundService: LocationForegroundService

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_create_map -> {
                    CreateActivity.startActivityForResult(this)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_join_map -> {
                    joinTrackMapTemporal() //FIXME: needs to be refactored to fragment or FragmentDialog
                }
                /*R.id.navigation_search_trackmap -> {
                    MainActivity.start(this)
                    return@OnNavigationItemSelectedListener true
                }*/
                R.id.navigation_force_crash -> {
                    Crashlytics.getInstance().crash()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app.component.inject(this)
        permissionChecker = PermissionChecker(this, mainContentLayout)

        setAdapter()

        viewModel.model.observe(this, Observer(::updateUi))
        viewModel.navigation.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                TrackMapActivity.start(this, it)
            }
        })
        viewModel.saveUser()

        setupUI()

        permissionChecker.requestLocationPermission(onGrantedPermission = {
            updateLastLocation()
//            startUserTrackLocation()
            startUserTrackLocationService()
        })
    }

    private fun updateLastLocation() {
        locationHandler.getLastLocation {
            prefs.lastLocation = it
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            viewModel.refresh()
        }
    }

    private fun setAdapter() {
        adapter = TrackMapEntriesAdapter {
            viewModel.onCurrentTrackMapClicked(it)
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter
    }

    private fun updateUi(model: MainViewModel.UiModel) {
        progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                adapter.items = model.data
                adapter.notifyDataSetChanged()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun setupUI() {
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun joinTrackMapTemporal() {
        val promptJoinDialog = AlertDialog.Builder(this)
        val promptDialogView = layoutInflater.inflate(R.layout.dialog_prompt_join, null)
        promptJoinDialog.setView(promptDialogView)

        promptJoinDialog
            .setCancelable(true)
            .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                val trackMapCodeEditText =
                    promptDialogView.findViewById<EditText>(R.id.trackMapCodeEditText)
                viewModel.joinTrackMap(trackMapCodeEditText.text.toString())
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }

    private fun startUserTrackLocation() {
        locationHandler.subscribeLocationUpdates {
            viewModel.updateUserLocation(it)
        }
    }

    private fun startUserTrackLocationService() {
        locationForegroundService = LocationForegroundService()
        val serviceIntent = Intent(this, locationForegroundService::class.java)
        if (!isMyServiceRunning(locationForegroundService::class.java, this)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            toast("Service initialized")
        } else {
            toast("Service already initialized!")
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
        val manager: ActivityManager =
            mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                logDebug("Service status - Running")
                return true
            }
        }
        logDebug("Service status - Not Running")
        return false
    }

    fun isLocationEnabledOrNot(context: Context): Boolean {
        var locationManager: LocationManager? = null
        locationManager =
            context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
}
