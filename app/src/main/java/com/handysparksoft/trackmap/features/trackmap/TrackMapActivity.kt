package com.handysparksoft.trackmap.features.trackmap

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.handysparksoft.domain.model.ParticipantLocation
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserMarkerData
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.data.server.FirebaseHandler
import com.handysparksoft.trackmap.core.extension.*
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.databinding.ActivityTrackmapBinding
import com.handysparksoft.trackmap.databinding.DialogMapTypeBinding
import com.handysparksoft.trackmap.databinding.MarkerSelectedBottomSheetBinding
import com.handysparksoft.trackmap.features.profile.ProfileViewModel
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity.MyPositionState.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TrackMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTrackmapBinding
    private lateinit var mapTypeBinging: DialogMapTypeBinding
    private lateinit var markerMapSelectedBottomSheetBinding: MarkerSelectedBottomSheetBinding

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

    private lateinit var participantsLocationChildEventListener: ChildEventListener
    private val activeParticipants = mutableSetOf<String>()

    private val userMarkerMap = hashMapOf<String, UserMarkerData>()

    private var frameAllParticipantsInMap = true

    private var pipModeEnabled = false
    private var googleMapFramePadding = GOOGLE_MAP_FRAME_MAX_PADDING_DP
    private val participantMarkers = mutableListOf<Marker>()
    private var customMarker: Marker? = null
    private var selectedMarkerTag: String? = null
    private var waitingForAnyMarkerIntentAction = false
    private var taskClearWaitingState: TimerTask? = null

    private var statusBarInsetHeight: Int = 0
    private var navigationBarInsetHeight: Int = 0

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackmapBinding.inflate(layoutInflater)
        mapTypeBinging = binding.dialogMapTypeLayout
        markerMapSelectedBottomSheetBinding = binding.markerMapSelectedBottomSheet
        setContentView(binding.root)

        injectComponents()

        supportActionBar?.hide()

        setupMapUI()
        setupUI()
        setupBottomSheet()
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

    override fun onBackPressed() {
        if (isMapStyleLayersVisible() || customMarker != null || userMarkerMap.values.any { it.isShowingInfoWindow }) {
            dismissAll(alsoInfoWindows = true)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setControlsVisibility(inPictureInPictureMode: Boolean) {
        if (inPictureInPictureMode) {
            dismissMapStyleLayers()
            clearInfoWindows()
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.frameAllParticipantsInMapButton) { _, insets ->
            statusBarInsetHeight = insets.systemWindowInsetTop
            navigationBarInsetHeight = insets.systemWindowInsetBottom
            insets
        }
        binding.markerMapSelectedBottomSheet.userShowInfoToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            selectedMarkerTag?.let { markerTag ->
                if (isChecked) {
                    clearInfoWindows()
                    setMarkerInfoWindowVisible(markerTag, visible = true)
                    participantMarkers.firstOrNull { it.tag == markerTag }?.showInfoWindow()
                } else {
                    setMarkerInfoWindowVisible(markerTag, visible = false)
                    participantMarkers.firstOrNull { it.tag == markerTag }?.hideInfoWindow()
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
        if (isMapStyleLayersVisible()) {
            mapTypeBinging.mapTypeCaradView.showTransitionTo(
                binding.switchMapStyleButton,
                Easing.Leave
            )
        }
    }

    private fun isMapStyleLayersVisible() =
        mapTypeBinging.mapTypeCaradView.visibility == View.VISIBLE

    private fun clearInfoWindows() {
        userMarkerMap.values.forEach { markerData ->
            markerData.isShowingInfoWindow = false
            participantMarkers.firstOrNull() { it.tag == markerData.tag }?.hideInfoWindow()
        }
    }

    private fun clearCustomMarker() {
        customMarker?.remove()
        customMarker = null
    }

    @SuppressLint("MissingPermission")
    private fun moveToLastLocation() {
        mapActionHelper.moveToPosition(prefs.lastLocation)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior =
            BottomSheetBehavior.from(markerMapSelectedBottomSheetBinding.markerMapSelectedContent)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.markerMapSelectedBottomSheet.userPhone.apply {
            setOnClickListener {
                val phone = this.text.toString()
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data= Uri.parse("tel:$phone")
                }
                if (dialIntent.resolveActivity(packageManager) != null) {
                    startActivity(dialIntent)
                }
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
    }

    private fun setMapPadding() {
        val topPadding = statusBarInsetHeight
        val bottomPadding = navigationBarInsetHeight

        googleMap.setPadding(0, topPadding, 0, bottomPadding)
        val layoutParams = binding.frameAllParticipantsInMapButton.layoutParams
        (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            setMargins(marginStart, topMargin, marginEnd, bottomMargin + navigationBarInsetHeight)
        }
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
            val markerTag = it.tag.toString()
            val sameMarker = markerTag == selectedMarkerTag
            selectedMarkerTag = markerTag
            showInfoBottomSheet(markerTag, sameMarker)
            animateFrameButtonToMakeSpace()
            keepShowingInfoWindowMarker()
            waitForAnyMarkerIntentAction()
            false
        }

        googleMap.setOnMapClickListener {
            dismissAll()

            //Except current selected marker
            keepShowingInfoWindowMarker()
        }

        googleMap.setOnMapLongClickListener { point ->
            customMarker?.remove()
            addCustomLocatedMarker(point)
            clearInfoWindows()
            waitForAnyMarkerIntentAction()
        }

        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this, ::onRenderMarkerWindowInfo))
    }

    private fun keepShowingInfoWindowMarker() {
        userMarkerMap.values.firstOrNull { it.isShowingInfoWindow }?.let { shownMarker ->
            participantMarkers.firstOrNull { it.tag == shownMarker.tag }?.showInfoWindow()
        }
    }

    private fun setMarkerInfoWindowVisible(markerTag: String, visible: Boolean) {
        val userMarkerData = getOrInitParticipantMarker(markerTag)
        userMarkerData.isShowingInfoWindow = visible
        userMarkerMap[markerTag] = userMarkerData
    }

    private fun dismissAll(alsoInfoWindows: Boolean = false) {
        if (alsoInfoWindows) {
            clearInfoWindows()
        }
        dismissMapStyleLayers()
        clearCustomMarker()
        hideMarkerBottomSheet()
        selectedMarkerTag = null
        taskClearWaitingState?.run()
    }

    private fun animateFrameButtonToMakeSpace() {
        val value = this.dip(GOOGLE_MAP_MARKER_INTENT_SPACE) * -1f
        binding.frameAllParticipantsInMapButton.animate().translationY(value).start()
    }

    private fun resetFrameButtonExtraSpace() {
        binding.frameAllParticipantsInMapButton.animate().translationY(0f).start()
    }

    private fun reAddCustomLocatedMarker() {
        customMarker?.let {
            addCustomLocatedMarker(it.position)
        }
    }

    private fun addCustomLocatedMarker(position: LatLng) {
        googleMapHandler.addMarker(position, "", null, null, null).also {
            it.tag = CUSTOM_LOCATED_MARKER_TAG
            customMarker = it
        }
    }

    private fun getDistanceFormatted(distance: Float): Pair<String, String> {
        return when {
            distance >= 1000 -> Pair(String.format("%.1f", distance / 1000), UNIT_KM)
            else -> Pair(String.format("%.0f", distance), UNIT_METERS)
        }
    }

    private fun waitForAnyMarkerIntentAction(seconds: Long = ANY_MARKER_INTENT_ACTION_DELAY_SECS) {
        waitingForAnyMarkerIntentAction = true

        taskClearWaitingState?.cancel()
        taskClearWaitingState = object : TimerTask() {
            override fun run() {
                waitingForAnyMarkerIntentAction = false
            }
        }
        Timer().schedule(taskClearWaitingState, TimeUnit.SECONDS.toMillis(seconds))
    }

    private fun setTrackMapData() {
        (intent.getSerializableExtra(TRACKMAP_PARAM) as? TrackMap)?.let {
            loadParticipantsData(it.participantIds)
            setupTrackMapForParticipantUpdates(it)
            setupTrackMapForParticipantLocations(it)


            // FIXME: remove after tests
            /*val taskLog = object : TimerTask() {
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
            }*/
            // END FIXME
        }
    }

    private fun tiltView() {
        prefs.lastLocation.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }

    private fun loadParticipantsData(participantIds: List<String>) {
        if (ProfileViewModel.profileDataUpdated) {
            participants.clear()
            ProfileViewModel.profileDataUpdated = false
        }
        participantIds.forEach { id ->
            val participantData = participants.firstOrNull { it.userId == id }
            if (participantData == null) {
                logDebug("Loading participant data of: $participantIds")
                firebaseHandler.getChildUserId(id)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val jsonValue = Gson().toJson(snapshot.value)
                            val participantLocation = Gson().fromJson(
                                jsonValue,
                                ParticipantLocation::class.java
                            )
                            participants.add(participantLocation.copy(userId = id))
                            logDebug("Loaded participant data of: $participantIds")
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
        }
    }

    private fun setupTrackMapForParticipantUpdates(trackMap: TrackMap) {
        subscribeForParticipantUpdates(trackMap.trackMapId)
    }

    private fun subscribeForParticipantUpdates(trackMapId: String) {
        participantsLocationChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                subscribeForParticipantLocationUpdates(snapshot.value as String)
                logDebug("Child added: ${snapshot.value}")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedParticipantId = snapshot.value as String
                participants.firstOrNull { it.userId == removedParticipantId }?.let {
                    participants.remove(it)
                }
                refreshTrackMap()
                logDebug("Child removed: ${snapshot.value}")
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
                    activeParticipants.add(it.userId)
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

        firebaseHandler.getChildUserId(userId)
            .addChildEventListener(participantsLocationChildEventListener)
    }

    private fun unsubscribeForParticipantLocationUpdates() {
        if (::participantsLocationChildEventListener.isInitialized) {
            firebaseHandler.rootRef.removeEventListener(participantsLocationChildEventListener)
        }
    }

    fun refreshTrackMap() {
        if (!waitingForAnyMarkerIntentAction) {
            googleMap.clear()
            participantMarkers.clear()
            resetFrameButtonExtraSpace()
            reAddCustomLocatedMarker()

            participants.filter(::withActivityAndAvailableLatLng).forEach { participantLocation ->
                val isUserSession = participantLocation.isSessionUser(userHandler.getUserId())
                val userMarkerData = getOrInitParticipantMarker(participantLocation.userId)
                val latLng = LatLng(participantLocation.latitude, participantLocation.longitude)

                val marker = googleMapHandler.addMarker(
                    latLng,
                    /*participantLocation.userAlias(isUserSession, true) +
                            " AMSL: " + participantLocation.altitudeAMSL + " m" +
                            " Geoid: " + participantLocation.altitudeGeoid + " m" +
                            " Speed: " + participantLocation.speed + " Km/h"*/
                    null,
                    null,
                    userMarkerData.icon,
                    participantLocation.image

                )
                val userId = participantLocation.userId
                marker.tag = userId
                addUserMarkerData(participantLocation)

                if (userMarkerData.isShowingInfoWindow) {
                    marker.showInfoWindow()
                }
                participantMarkers.add(marker)
            }

            // Refresh selected marker bottom sheet info
            selectedMarkerTag?.let {
                refreshBottomSheetMarkerData(it)
            }

            if (frameAllParticipantsInMap) {
                frameAllParticipants()
            }
        }
    }

    private fun addUserMarkerData(participantLocation: ParticipantLocation) {
        val userMarkerData = getOrInitParticipantMarker(participantLocation.userId)
        userMarkerData.participanLocation = participantLocation
        userMarkerMap[participantLocation.userId] = userMarkerData
    }

    private fun getOrInitParticipantMarker(userId: String): UserMarkerData {
        var userMakerData = userMarkerMap[userId]
        if (userMakerData == null) {
            val isUserSession = userId == userHandler.getUserId()
            val icon = if (isUserSession) {
                googleMapHandler.getDefaultUserMarker()
            } else {
                googleMapHandler.getRandomMarker()
            }
            userMakerData = UserMarkerData(userId, icon, false, null)
            userMarkerMap[userId] = userMakerData
        }
        return userMakerData
    }

    private fun frameAllParticipants() {
        if (participantMarkers.size == 1) {
            // Move the camera to unique participant location with a zoom of 17.
            participants.firstOrNull(::withActivityAndAvailableLatLng)?.let { participant ->
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

            participants.filter(::withActivityAndAvailableLatLng).forEach { participant ->
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

    private fun withActivityAndAvailableLatLng(participantLocation: ParticipantLocation): Boolean {
        return activeParticipants.contains(participantLocation.userId)
                && participantLocation.latitude != 0.0
                && participantLocation.longitude != 0.0
    }

    private fun getUserSessionLocation(): Location? {
        participants
            .filter(::withActivityAndAvailableLatLng)
            .firstOrNull { it.userId == userHandler.getUserId() }?.let {
                return Location("adhoc").apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
        return null
    }

    private fun getCurrentZoomLevel() = googleMap.cameraPosition.zoom

    /**
     * Bottom Sheet Selected Marker functions
     */
    private fun showInfoBottomSheet(markerTag: String, sameMarker: Boolean) {
        refreshBottomSheetMarkerData(markerTag)
        showOrHideMarkerBottomSheet(sameMarker)
    }

    private fun showOrHideMarkerBottomSheet(sameMarker: Boolean) {
        val state =
            if (sameMarker &&
                (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED ||
                        bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED)
            ) {
                BottomSheetBehavior.STATE_HIDDEN
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                BottomSheetBehavior.STATE_HALF_EXPANDED
            } else {
                bottomSheetBehavior.state
            }

        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.halfExpandedRatio = HALF_EXPANDED_RATIO
        bottomSheetBehavior.state = state
    }

    private fun hideMarkerBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun refreshBottomSheetMarkerData(markerTag: String) {
        userMarkerMap[markerTag]?.let { userMarkerData ->
            userMarkerData.participanLocation?.let { participantLocation ->

                val distance =
                    getUserSessionLocation()?.distanceTo(participantLocation.toLocation()) ?: 0f
                with(markerMapSelectedBottomSheetBinding) {
                    userNickname.text = participantLocation.nickname ?: participantLocation.userId

                    userSpeed.value = participantLocation.speed.toString()
                    userAltitude.value = participantLocation.altitudeAMSL.toString()

                    val (distanceFormatted, unit) = getDistanceFormatted(distance)
                    userDistanceFromYou.value = distanceFormatted
                    userDistanceFromYou.unit = unit

                    userFullName.text = participantLocation.fullName ?: participantLocation.nickname
                    userPhone.text = participantLocation.phone
                    userPhone.visibility = if (userPhone.text.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                    participantLocation.image.let { userImage ->
                        userProfileImage.setImageBitmap(Base64Utils.getBase64Bitmap(userImage))
                    }

                    userShowInfoToggle.isChecked = userMarkerData.isShowingInfoWindow
                }
            }
        }
    }

    /**
     * CustomInfoWindowAdapter functions
     */
    @SuppressLint("SetTextI18n")
    private fun onRenderMarkerWindowInfo(windowInfo: View, marker: Marker) {
        fun <T> Int.findView(): T {
            return windowInfo.findViewById(this) as T
        }

        fun Int.findTextView(): TextView {
            return windowInfo.findViewById(this) as TextView
        }

        val userMarkerData = userMarkerMap[marker.tag.toString()]
        userMarkerData?.participanLocation?.let {
            val isUserSession = userHandler.getUserId() == marker.tag.toString()
            val altitude = it.altitudeAMSL
            val speed = it.speed
            val distance = getUserSessionLocation()
                ?.distanceTo(it.toLocation()) ?: 0f
            val (distanceFinal, distanceUnit) = getDistanceFormatted(distance)

            R.id.markerTitle.findTextView().text = it.userAlias(isUserSession, getString(R.string.you))
            R.id.markerAltitudeValue.findTextView().text = "$altitude $UNIT_METERS"
            R.id.markerSpeedValue.findTextView().text = "$speed $UNIT_KMH"
            R.id.markerDistanceValue.findTextView().text = "$distanceFinal $distanceUnit"

            // Hide distance when it is oneself marker
            R.id.markerDistance.findTextView().visibleOrGone(!isUserSession)
            R.id.markerDistanceValue.findTextView().visibleOrGone(!isUserSession)
        }
    }

    inner class CustomInfoWindowAdapter(
        activity: Activity,
        private val onRenderMarkerWindowInfo: (windowInfo: View, marker: Marker) -> Unit
    ) : GoogleMap.InfoWindowAdapter {
        private val windowInfo: View = activity.layoutInflater.inflate(
            R.layout.custom_info_window, null
        )

        override fun getInfoWindow(marker: Marker): View? {
            val shouldShow = userMarkerMap[marker.tag.toString()]?.isShowingInfoWindow == true
            return if (marker.tag != CUSTOM_LOCATED_MARKER_TAG && shouldShow) {
                onRenderMarkerWindowInfo(windowInfo, marker)
                windowInfo
            } else null
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }
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
        private const val GOOGLE_MAP_MARKER_INTENT_SPACE = 56
        private const val ANY_MARKER_INTENT_ACTION_DELAY_SECS = 5L
        private const val CUSTOM_LOCATED_MARKER_TAG = "CustomMarker"

        private const val HALF_EXPANDED_RATIO = 0.3f
        private const val UNIT_KM = "Km"
        private const val UNIT_KMH = "Km/h"
        private const val UNIT_METERS = "m"

        private val participants = mutableSetOf<ParticipantLocation>()

        fun start(context: Context, trackMap: TrackMap) {
            context.startActivity<TrackMapActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(TRACKMAP_PARAM, trackMap)
            }
        }
    }
}
