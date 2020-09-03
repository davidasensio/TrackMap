package com.handysparksoft.trackmap.features.trackmap

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.toLatLng
import com.handysparksoft.trackmap.core.extension.toast
import com.handysparksoft.trackmap.core.platform.MapActionHelper
import com.handysparksoft.trackmap.core.platform.PermissionChecker
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.create.CreateActivity
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsActivity
import com.handysparksoft.trackmap.features.trackmap.MyPositionState.*
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    @Inject
    lateinit var userHandler: UserHandler

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, app.component.mainViewModelFactory).get(MainViewModel::class.java)
    }

    private lateinit var permissionChecker: PermissionChecker

    private lateinit var googleMap: GoogleMap
    private lateinit var mapActionHelper: MapActionHelper
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastLocation: LatLng? = null
    private var myPositionState: MyPositionState = Unlocated

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_create_map -> {
                    CreateActivity.start(this)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_join_map -> {
                    CurrentTrackMapsActivity.start(this)
                    return@OnNavigationItemSelectedListener true
                }
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

        injectComponents()

        supportActionBar?.hide()
        permissionChecker = PermissionChecker(this, container)
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        setupMapUI()
        setupUI()

        viewModel.saveUser()
    }

    private fun injectComponents() {
        app.component.inject(this) // Equals to DaggerAppComponent.factory().create(applicationContext as Application).inject(this)
    }

    private fun setupMapUI() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupUI() {
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        title = getString(R.string.app_name)

//        myPositionImageView?.setOnClickListener {
//            toggleView()
//        }
    }

    @SuppressLint("MissingPermission")
    private fun moveToLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                lastLocation = it.result?.toLatLng()
                mapActionHelper.moveToPosition(latLng = lastLocation!!)
            } else {

            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.mapActionHelper = MapActionHelper(googleMap)
        mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL

        permissionChecker.requestLocationPermission(onGrantedPermission = {
            startMap()
            moveToLastLocation()
        })
    }

    @SuppressLint("MissingPermission")
    private fun startMap() {
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener {
            myPositionState = if (myPositionState == Located) {
                LocatedAndTilted
            } else {
                Located
            }
            tiltView()
            true
        }

        googleMap.setOnCameraMoveStartedListener(object : GoogleMap.OnCameraMoveStartedListener {
            override fun onCameraMoveStarted(reason: Int) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    myPositionState = Unlocated
                }
            }
        })

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(39.46, -0.35)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun toggleView() {
        lastLocation?.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }

    private fun tiltView() {
        lastLocation?.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }
}

sealed class MyPositionState() {
    object Unlocated : MyPositionState()
    object Located : MyPositionState()
    object LocatedAndTilted : MyPositionState()
}
