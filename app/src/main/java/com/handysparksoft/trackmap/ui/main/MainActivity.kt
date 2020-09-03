package com.handysparksoft.trackmap.ui.main

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
import com.handysparksoft.trackmap.ui.common.MapActionHelper
import com.handysparksoft.trackmap.ui.common.PermissionChecker
import com.handysparksoft.trackmap.ui.common.app
import com.handysparksoft.trackmap.ui.common.toLatLng
import com.handysparksoft.trackmap.ui.creation.CreateActivity
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsActivity
import com.handysparksoft.trackmap.ui.main.MyPositionState.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
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

        supportActionBar?.hide()
        permissionChecker = PermissionChecker(this, container)
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        setupMapUI()
        setupUI()
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
