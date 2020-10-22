package com.handysparksoft.trackmap.features.trackmap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.handysparksoft.domain.model.ParticipantLocation
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.data.server.FirebaseHandler
import com.handysparksoft.trackmap.core.extension.*
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.features.trackmap.MyPositionState.*
import kotlinx.android.synthetic.main.activity_trackmap.*
import javax.inject.Inject

class TrackMapActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val TRACKMAP_PARAM = "trackMapId"
        private const val GOOGLE_MAP_FRAME_PADDING_DP = 64
        private const val GOOGLE_MAP_TOP_PADDING_DP = 32

        fun start(context: Context, trackMap: TrackMap) {
            context.startActivity<TrackMapActivity> {
                putExtra(TRACKMAP_PARAM, trackMap)
            }
        }
    }

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var locationHandler: LocationHandler

    @Inject
    lateinit var firebaseHandler: FirebaseHandler

    @Inject
    lateinit var googleMapHandler: GoogleMapHandler

    private lateinit var googleMap: GoogleMap

    private lateinit var mapActionHelper: MapActionHelper
    private var myPositionState: MyPositionState = Unallocated
    private val viewModel: TrackMapViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.trackMapViewModelFactory
        ).get(TrackMapViewModel::class.java)
    }

    private val participants = mutableSetOf<ParticipantLocation>()

    private lateinit var participantsLocationChildEventListener: ChildEventListener

    private val userMarkerMap = hashMapOf<String, Int>()

    private var viewAllParticipantsInMap = true

    private val mapStyles = arrayOf(
        GoogleMap.MAP_TYPE_NORMAL,
        GoogleMap.MAP_TYPE_SATELLITE
    )
    private var mapStyleCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trackmap)

        injectComponents()

        supportActionBar?.hide()

        setupMapUI()
        setupUI()
    }

    override fun onDestroy() {
        unsubscribeForParticipantLocationUpdates()
        super.onDestroy()
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
        title = getString(R.string.app_name)

        viewAllMarkersInMapImageView?.setOnClickListener {
            viewAllMarkersInMapImageView?.setImageResource(if (viewAllParticipantsInMap) R.drawable.ic_frame_off else R.drawable.ic_frame_on)
            viewAllParticipantsInMap = !viewAllParticipantsInMap
        }

        setMapStyleImageView?.setOnClickListener {
            val nextTypeIndex = ++mapStyleCounter % mapStyles.size
            mapActionHelper.mapType = mapStyles[nextTypeIndex]
        }

//        myPositionImageView?.setOnClickListener {
//            toggleView()
//        }
    }

    @SuppressLint("MissingPermission")
    private fun moveToLastLocation() {
        mapActionHelper.moveToPosition(prefs.lastLocation)
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
        googleMapHandler.initialize(googleMap)
        mapActionHelper = MapActionHelper(googleMap)

        setMapStyle()
        startMap()
        moveToLastLocation()
        setTrackMapData()
    }

    private fun setMapStyle() {
        setMapPadding()
        if (isDarkModeActive()) {
            try {
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.map_night_style
                    )
                )
            } catch (e: Exception) {
                logError("Can't map find style. Error: ${e.message}")
                mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        } else {
            mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    private fun setMapPadding() {
        val topPadding = dip(GOOGLE_MAP_TOP_PADDING_DP)
        googleMap.setPadding(0, topPadding, 0, 0)
    }

    private fun startMap() {
        try {
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

            googleMap.setOnCameraMoveStartedListener(object :
                GoogleMap.OnCameraMoveStartedListener {
                override fun onCameraMoveStarted(reason: Int) {
                    if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                        myPositionState = Unallocated
                    }
                }
            })

            // Add a marker in Sydney and move the camera
//        val sydney = LatLng(39.46, -0.35)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        } catch (e: SecurityException) {
            // Non granted permissions
            finish()
        }
    }

    private fun setTrackMapData() {
        (intent.getSerializableExtra(TRACKMAP_PARAM) as? TrackMap)?.let {
            setupTrackMapForParticipantUpdates(it)
            setupTrackMapForParticipantLocations(it)
        }
    }

    private fun toggleView() {
        prefs.lastLocation?.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }

    private fun tiltView() {
        prefs.lastLocation.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }

    private fun setupTrackMapForParticipantUpdates(trackMap: TrackMap) {
        subscribeForParticipantUpdates(trackMap.trackMapId)
    }

    private fun subscribeForParticipantUpdates(trackMapId: String) {
        participantsLocationChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                subscribeForParticipantLocationUpdates(snapshot.value as String)
                logDebug("*** Child added: ${snapshot.value}")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedParticipantId = snapshot.value as String
                participants.firstOrNull { it.userId == removedParticipantId }?.let {
                    participants.remove(it)
                }
                refreshTrackMap()
                logDebug("*** Child removed: ${snapshot.value}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        firebaseHandler.getChildTrackMapId(trackMapId)
            .addChildEventListener(participantsLocationChildEventListener)
    }

    private fun setupTrackMapForParticipantLocations(trackMap: TrackMap) {
        trackMap.participantIds.forEach { userId ->
            subscribeForParticipantLocationUpdates(userId)
        }
    }

    private fun subscribeForParticipantLocationUpdates(userId: String) {
        participantsLocationChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                participants.firstOrNull { it.userId == userId }?.let {
                    when (snapshot.key) {
                        "latitude" -> it.latitude = snapshot.value as Double
                        "longitude" -> it.longitude = snapshot.value as Double
                    }
                }
                refreshTrackMap()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        val defaultLatitude =
            if (userId == userHandler.getUserId()) prefs.lastLocationLatitude.toDouble() else 0.0
        val defaultLongitude =
            if (userId == userHandler.getUserId()) prefs.lastLocationLongitude.toDouble() else 0.0
        participants.add(ParticipantLocation(userId, defaultLatitude, defaultLongitude))
        firebaseHandler.getChildUserId(userId)
            .addChildEventListener(participantsLocationChildEventListener)
    }

    private fun unsubscribeForParticipantLocationUpdates() {
        if (::participantsLocationChildEventListener.isInitialized) {
            firebaseHandler.rootRef.removeEventListener(participantsLocationChildEventListener)
        }
    }


    fun refreshTrackMap() {
        googleMap.clear()
        participants.filter(::withAvailableLatLng).forEach { participantLocation ->
            val isUserSession = participantLocation.isSessionUser(userHandler.getUserId())
            val participantIcon = getParticipantMarker(participantLocation.userId, isUserSession)
            val latLng = LatLng(participantLocation.latitude, participantLocation.longitude)

            googleMapHandler.addMarker(
                latLng,
                participantLocation.userAlias(isUserSession),
                participantIcon
            )
        }
        if (viewAllParticipantsInMap) {
            frameAllParticipants()
        }
    }


    private fun getParticipantMarker(userId: String, isUserSession: Boolean): Int {
        var participantIcon = userMarkerMap[userId]
        if (participantIcon == null) {
            participantIcon = if (isUserSession) {
                GoogleMapHandler.MARKER_ICON_DEFAULT_GREEN
            } else {
                googleMapHandler.getRandomMarker()
            }
            userMarkerMap[userId] = participantIcon
        }
        return participantIcon
    }


    private fun frameAllParticipants() {
        val boundsBuilder = LatLngBounds.Builder()

        participants.filter(::withAvailableLatLng).forEach { participant ->
            boundsBuilder.include(LatLng(participant.latitude, participant.longitude))
        }

        val framePadding = dip(GOOGLE_MAP_FRAME_PADDING_DP)
        val build = boundsBuilder.build()
        val cameraUpdateAction = CameraUpdateFactory.newLatLngBounds(build, framePadding)
        this.googleMap.animateCamera(cameraUpdateAction)
    }

    private fun withAvailableLatLng(participantLocation: ParticipantLocation): Boolean {
        return participantLocation.latitude != 0.0 && participantLocation.longitude != 0.0
    }
}

sealed class MyPositionState {
    object Unallocated : MyPositionState()
    object Located : MyPositionState()
    object LocatedAndTilted : MyPositionState()
}
