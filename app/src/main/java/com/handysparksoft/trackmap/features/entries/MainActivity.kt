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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.features.create.CreateActivity
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Content
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Loading
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

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

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    // FIXME make injectable
    private lateinit var permissionChecker: PermissionChecker

    private lateinit var locationForegroundService: LocationForegroundService

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                /*R.id.navigation_create_map -> {
                    CreateActivity.startActivityForResult(this)
                    return@OnNavigationItemSelectedListener true
                }*/
                R.id.navigation_home -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_join_map -> {
                    joinTrackMapTemporal() //FIXME: needs to be refactored to fragment or FragmentDialog
                }
                /*R.id.navigation_search_trackmap -> {
                    MainActivity.start(this)
                    return@OnNavigationItemSelectedListener true
                }*/
                /*R.id.navigation_force_crash -> {
                    Crashlytics.getInstance().crash()
                    return@OnNavigationItemSelectedListener true
                }*/
            }
            true
        }

    override fun onStart() {
        connectionHandler.registerNetworkCallback()
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app.component.inject(this)
        permissionChecker = PermissionChecker(this, mainContentLayout)

        setAdapter()

        viewModel.model.observe(this, Observer(::updateUi))
        viewModel.goEvent.observe(this, Observer(::onGoEvent))
        viewModel.leaveEvent.observe(this, Observer(::onLeaveEvent))
        viewModel.shareEvent.observe(this, Observer(::onShareEvent))
        viewModel.joinFeedbackEvent.observe(this, Observer(::onJoinFeedbackEvent))
        viewModel.saveUser()

        setupUI()

        permissionChecker.requestLocationPermission(onGrantedPermission = {
            updateLastLocation()
//            startUserTrackLocation()
            startUserTrackLocationService()
        })

        checkDeepLink()
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
        adapter = TrackMapEntriesAdapter(
            onGoListener = {
                viewModel.onGoTrackMapClicked(it)
            },
            onLeaveListener = {
                viewModel.onLeaveTrackMapClicked(it)
            },
            onShareListener = {
                viewModel.onShareTrackMapClicked(it)
            }
        )

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

    private fun onGoEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            TrackMapActivity.start(this, it)
        }
    }

    private fun onLeaveEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            val leaveDialog = AlertDialog.Builder(this)
            leaveDialog.setMessage(getString(R.string.leave_trackmap_question, it.name))
            leaveDialog.setPositiveButton(R.string.leave) { dialog, _ ->
                viewModel.leave(it)
                dialog.dismiss()
            }
            leaveDialog.setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            leaveDialog.show()
        }
    }

    private fun onShareEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            DeeplinkHandler.generateDeeplink(this, it.trackMapId, it.name)
        }
    }

    private fun onJoinFeedbackEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            bottomNavigation.snackbar(
                message = "You just joined TrackMap \"${it.name}\"",
                length = Snackbar.LENGTH_INDEFINITE,
                actionListener = {
                    // Nothing to do
                }
            )
        }
    }

    private fun setupUI() {
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        swipeRefreshLayout.setOnRefreshListener {
            connectionHandler.registerNetworkCallback()
            if (!connectionHandler.isNetworkConnected()) {
                Snackbar.make(
                    swipeRefreshLayout,
                    R.string.no_connection_error,
                    Snackbar.LENGTH_SHORT
                ).show()
                swipeRefreshLayout.isRefreshing = false
            } else {
                viewModel.refresh()
            }
        }
        createTrackMapFAB.setOnClickListener {
            CreateActivity.startActivityForResult(this)
        }
    }

    private fun checkDeepLink() {
        val trackMapCodeExtra = intent.getStringExtra(KEY_INTENT_TRACKMAP_CODE)
        if (trackMapCodeExtra != null) {
            val decodedCode = DeeplinkHandler.decodeBase64(trackMapCodeExtra)
            viewModel.joinTrackMap(trackMapCode = decodedCode, showFeedback = true)
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
            logDebug("Service initialized")
        } else {
            logDebug("Service already initialized!")
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


    companion object {
        const val KEY_INTENT_TRACKMAP_CODE = "KEY_INTENT_TRACKMAP_CODE"

        fun start(context: Context) {
            context.startActivity<MainActivity>()
        }
    }
}
