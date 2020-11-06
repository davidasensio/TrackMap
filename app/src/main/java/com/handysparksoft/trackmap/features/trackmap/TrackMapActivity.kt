package com.handysparksoft.trackmap.features.trackmap

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Rational
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.handysparksoft.domain.model.ParticipantLocation
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserMarkerData
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.data.server.FirebaseHandler
import com.handysparksoft.trackmap.core.extension.*
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.databinding.ActivityTrackmapBinding
import com.handysparksoft.trackmap.databinding.DialogMapTypeBinding
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity.MyPositionState.*
import java.util.*
import javax.inject.Inject

class TrackMapActivity : AppCompatActivity(), OnMapReadyCallback {

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

    private val userMarkerMap = hashMapOf<String, UserMarkerData>()

    private var frameAllParticipantsInMap = true

    private lateinit var binding: ActivityTrackmapBinding
    private lateinit var mapTypeBinging: DialogMapTypeBinding

    private var pipModeEnabled = true
    private var googleMapFramePadding = GOOGLE_MAP_FRAME_MAX_PADDING_DP
    private val participantMarkers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackmapBinding.inflate(layoutInflater)
        mapTypeBinging = binding.dialogMapTypeLayout
        setContentView(binding.root)

        injectComponents()

        supportActionBar?.hide()

        setupMapUI()
        setupUI()
    }

    override fun onDestroy() {
        unsubscribeForParticipantLocationUpdates()
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        if (pipModeEnabled && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            when {
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O -> {
                    val params = PictureInPictureParams.Builder()
                    params.setAspectRatio(Rational(3, 4))
                    enterPictureInPictureMode(params.build())
                }
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N -> {
                    enterPictureInPictureMode()
                }
                else -> {
                    super.onUserLeaveHint()
                }
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        setControlsVisibility(isInPictureInPictureMode)
    }

    @SuppressLint("MissingPermission")
    private fun setControlsVisibility(inPictureInPictureMode: Boolean) {
        if (inPictureInPictureMode) {
            dismissMapStyleLayers()
            binding.switchMapStyleButton.gone()
            binding.frameAllParticipantsInMapButton.gone()
            binding.trackMapBottomCardView.visible()
            googleMap.isMyLocationEnabled = false
            googleMapFramePadding = GOOGLE_MAP_FRAME_MIN_PADDING_DP
        } else {
            binding.switchMapStyleButton.visible()
            binding.frameAllParticipantsInMapButton.visible()
            binding.trackMapBottomCardView.gone()
            googleMap.isMyLocationEnabled = true
            googleMapFramePadding = GOOGLE_MAP_FRAME_MAX_PADDING_DP

            // Allow  PIP mode only first time to avoid memory leaks
            pipModeEnabled = false
        }
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

        binding.frameAllParticipantsInMapButton.setOnClickListener {
            binding.frameAllParticipantsInMapButton.setImageResource(if (frameAllParticipantsInMap) R.drawable.ic_frame_off else R.drawable.ic_frame_on)
            frameAllParticipantsInMap = !frameAllParticipantsInMap
        }

        getNavigationBarHeight().also { height ->
            if (height > 0) {
                val layoutParams = binding.frameAllParticipantsInMapButton.layoutParams
                (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                    setMargins(marginStart, topMargin, marginEnd, bottomMargin + height)
                }
            }
        }

        setupMapTypeSwitcher()
    }

    private fun setupMapTypeSwitcher() {
        fun selectMapType(imageViewToSelect: ImageView, textViewToSelect: TextView) {
            mapTypeBinging.mapTypeDefaultImageView.isSelected = false
            mapTypeBinging.mapTypeDefaultTextView.isSelected = false

            mapTypeBinging.mapTypeSatelliteImageView.isSelected = false
            mapTypeBinging.mapTypeSatelliteTextView.isSelected = false

            mapTypeBinging.mapTypeTerrainImageView.isSelected = false
            mapTypeBinging.mapTypeTerrainTextView.isSelected = false

            imageViewToSelect.isSelected = true
            textViewToSelect.isSelected = true
        }


        binding.switchMapStyleButton.setOnClickListener {
            showMapStyleLayers()
        }

        mapTypeBinging.mapTypeDefaultImageView.isSelected = true
        mapTypeBinging.mapTypeDefaultTextView.isSelected = true

        mapTypeBinging.mapTypeDefaultImageView.setOnClickListener {
            mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL
            selectMapType(
                mapTypeBinging.mapTypeDefaultImageView,
                mapTypeBinging.mapTypeDefaultTextView
            )
        }

        mapTypeBinging.mapTypeSatelliteImageView.setOnClickListener {
            mapActionHelper.mapType = GoogleMap.MAP_TYPE_SATELLITE
            selectMapType(
                mapTypeBinging.mapTypeSatelliteImageView,
                mapTypeBinging.mapTypeSatelliteTextView
            )
        }

        mapTypeBinging.mapTypeTerrainImageView.setOnClickListener {
            mapActionHelper.mapType = GoogleMap.MAP_TYPE_TERRAIN
            selectMapType(
                mapTypeBinging.mapTypeTerrainImageView,
                mapTypeBinging.mapTypeTerrainTextView
            )
        }

        // Round shape
        mapTypeBinging.mapTypeDefaultImageView.clipToOutline = true
        mapTypeBinging.mapTypeSatelliteImageView.clipToOutline = true
        mapTypeBinging.mapTypeTerrainImageView.clipToOutline = true
    }

    private fun showMapStyleLayers() {
        binding.switchMapStyleButton.showTransitionTo(mapTypeBinging.mapTypeCaradView, Easing.Enter)
    }

    private fun dismissMapStyleLayers() {
        if (mapTypeBinging.mapTypeCaradView.visibility == View.VISIBLE) {
            mapTypeBinging.mapTypeCaradView.showTransitionTo(
                binding.switchMapStyleButton,
                Easing.Leave
            )
        }
    }

    private fun clearInfoWindows() {
        userMarkerMap.values.forEach {
            it.isShowingInfoWindow = false
        }
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
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
        bindMapListeners()
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
                logError("Can't find map style. Error: ${e.message}")
                mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        } else {
            mapActionHelper.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

        googleMap.setOnMapClickListener {
            dismissMapStyleLayers()
            clearInfoWindows()
        }
    }

    private fun setMapPadding() {
        val topPadding = dip(GOOGLE_MAP_TOP_PADDING_DP)
        googleMap.setPadding(0, topPadding, 0, 0)
    }

    private fun bindMapListeners() {
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
        } catch (e: SecurityException) {
            // Non granted permissions
            finish()
        }

        // Disable framing when gesture on Map detected
        googleMap.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                myPositionState = Unallocated

                if (frameAllParticipantsInMap) {
                    binding.frameAllParticipantsInMapButton.performClick()
                }
            }
        }

        googleMap.setOnMarkerClickListener {
            clearInfoWindows()
            it.showInfoWindow()
            val tagId = it.tag.toString()
            val userMarkerData = getParticipantMarker(tagId)
            userMarkerData.isShowingInfoWindow = true
            userMarkerMap[tagId] = userMarkerData
            true
        }
    }

    private fun setTrackMapData() {
        (intent.getSerializableExtra(TRACKMAP_PARAM) as? TrackMap)?.let {
            setupTrackMapForParticipantUpdates(it)
            setupTrackMapForParticipantLocations(it)

            // FIXME: remove after tests
            val taskLog = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        binding.tempLog.text = LocationHandler.nmeaLog.toString()
                    }
                }
            }
            Timer().scheduleAtFixedRate(taskLog, 0, 1500)
            binding.tempLog.movementMethod = ScrollingMovementMethod()
            binding.tempLog.setOnLongClickListener {
                binding.tempLogContainer.alpha =
                    if (binding.tempLogContainer.alpha == 1f) 0f else 1f
                true
            }
            binding.tempLogClearButton.setOnClickListener {
                LocationHandler.nmeaLog.clear()
                binding.tempLog.text = ""
            }
            // END FIXME
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
            private fun onParticipantDataUpdate(snapshot: DataSnapshot) {
                participants.firstOrNull { it.userId == userId }?.let {
                    when (snapshot.key) {
                        "latitude" -> it.latitude = snapshot.value as Double
                        "longitude" -> it.longitude = snapshot.value as Double
                        "altitudeAMSL" -> it.altitudeAMSL = snapshot.value as Long
                        "altitudeGeoid" -> it.altitudeGeoid = snapshot.value as Long
                        "speed" -> it.speed = snapshot.value as Long
                    }
                }
                refreshTrackMap()
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                onParticipantDataUpdate(snapshot)
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
        participants.add(ParticipantLocation(userId, defaultLatitude, defaultLongitude, 0, 0, 0))
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
        participantMarkers.clear()

        participants.filter(::withAvailableLatLng).forEach { participantLocation ->
            val isUserSession = participantLocation.isSessionUser(userHandler.getUserId())
            val userMarkerData = getParticipantMarker(participantLocation.userId)
            val latLng = LatLng(participantLocation.latitude, participantLocation.longitude)

            val marker = googleMapHandler.addMarker(
                latLng,
                participantLocation.userAlias(isUserSession, true) +
                        " AMSL: " + participantLocation.altitudeAMSL + " m" +
                        " Geoid: " + participantLocation.altitudeGeoid + " m" +
                        " Speed: " + participantLocation.speed + " Km/h",
                null,
                userMarkerData.icon
            )
            marker.tag = participantLocation.userId

            if (userMarkerData.isShowingInfoWindow) {
                marker.showInfoWindow()
            }
            participantMarkers.add(marker)
        }
        if (frameAllParticipantsInMap) {
            frameAllParticipants()
        }
    }


    private fun getParticipantMarker(userId: String): UserMarkerData {
        var userMakerData = userMarkerMap[userId]
        if (userMakerData == null) {
            val isUserSession = userId == userHandler.getUserId()
            val icon = if (isUserSession) {
                GoogleMapHandler.MARKER_ICON_DEFAULT_GREEN
            } else {
                googleMapHandler.getRandomMarker()
            }
            userMakerData = UserMarkerData(icon, false)
            userMarkerMap[userId] = userMakerData
        }
        return userMakerData
    }


    private fun frameAllParticipants() {
        if (participantMarkers.size == 1) {
            // Move the camera to unique participant location with a zoom of 17.
            participants.firstOrNull(::withAvailableLatLng)?.let { participant ->
                val latLng = LatLng(participant.latitude, participant.longitude)
                val zoomLevel = getZoomLevelAccordingSpeed(participant.speed)
                this.googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        zoomLevel
                    )
                )
            }
        } else {
            val boundsBuilder = LatLngBounds.Builder()

            participants.filter(::withAvailableLatLng).forEach { participant ->
                boundsBuilder.include(LatLng(participant.latitude, participant.longitude))
            }

            val framePadding = dip(googleMapFramePadding)
            val build = boundsBuilder.build()
            val cameraUpdateAction = CameraUpdateFactory.newLatLngBounds(build, framePadding)
            this.googleMap.animateCamera(cameraUpdateAction)
        }
    }

    private fun getZoomLevelAccordingSpeed(speed: Long): Float {
        return when {
            speed > 120 -> 12f
            speed > 100 -> 13f
            speed > 80 -> 14f
            speed > 50 -> 15f
            speed > 30 -> 16f
            else -> MapActionHelper.DEFAULT_ZOOM_LEVEL
        }
    }

    private fun withAvailableLatLng(participantLocation: ParticipantLocation): Boolean {
        return participantLocation.latitude != 0.0 && participantLocation.longitude != 0.0
    }

    sealed class MyPositionState {
        object Unallocated : MyPositionState()
        object Located : MyPositionState()
        object LocatedAndTilted : MyPositionState()
    }

    companion object {
        private const val TRACKMAP_PARAM = "trackMapId"
        private const val GOOGLE_MAP_FRAME_MIN_PADDING_DP = 16
        private const val GOOGLE_MAP_FRAME_MAX_PADDING_DP = 64
        private const val GOOGLE_MAP_TOP_PADDING_DP = 32

        fun start(context: Context, trackMap: TrackMap) {
            context.startActivity<TrackMapActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(TRACKMAP_PARAM, trackMap)
            }
        }
    }
}

