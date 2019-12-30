package com.handysparksoft.trackmap.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.handysparksoft.trackmap.data.server.TrackMapRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsActivity
import splitties.activities.start
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var job: Job

    private lateinit var mMap: GoogleMap

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_create_map -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_join_map -> {
                    start<CurrentTrackMapsActivity>()
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

        job = Job()

        supportActionBar?.hide()

        setupMapUI()

        setupUI()

        launch(Dispatchers.Main) {
            println(TrackMapRepository().getTrackMapList())
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun setupMapUI() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.trackMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupUI() {
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        title = getString(R.string.app_name)
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
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}
