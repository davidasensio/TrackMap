package com.handysparksoft.trackmap.features.trackmap

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Rational
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.handysparksoft.domain.model.ParticipantLocation
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserMarkerData
import com.handysparksoft.trackmap.BuildConfig
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.data.server.FirebaseHandler
import com.handysparksoft.trackmap.core.extension.*
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.databinding.ActivityTrackmapBinding
import com.handysparksoft.trackmap.databinding.DialogMapTypeBinding
import com.handysparksoft.trackmap.databinding.MarkerSelectedBottomSheetBinding
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity.MyPositionState.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap


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

    @Inject
    lateinit var locationForegroundServiceHandler: LocationForegroundServiceHandler

    private lateinit var googleMap: GoogleMap

    private lateinit var mapActionHelper: MapActionHelper
    private var myPositionState: MyPositionState = Unallocated
    private val viewModel: TrackMapViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.trackMapViewModelFactory
        ).get(TrackMapViewModel::class.java)
    }

    lateinit var trackMapId: String

    private lateinit var participantDataChildEventListener: ChildEventListener
    private lateinit var participantsLocationChildEventListener: ChildEventListener

    private val userMarkerMap = hashMapOf<String, UserMarkerData>()

    private var frameOrFollowParticipantsInMap = true
    private var showRouteInMap = true

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
    private val participants = mutableSetOf<ParticipantLocation>()
    private var participantToFollow: ParticipantLocation? = null

    lateinit var countDownTimer: CountDownTimer

    private val startCap by lazy {
        CustomCap(
            BitmapDescriptorFactory.fromBitmap(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_flag_green
                )?.toBitmap()
            )
        )
    }
    private val endCap by lazy {
        CustomCap(
            BitmapDescriptorFactory.fromBitmap(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_flag_red
                )?.toBitmap()
            )
        )
    }
    private val polylineColors = listOf(
        Color.parseColor("#CFB6E5"),
        Color.parseColor("#C1E3FE"),
        Color.parseColor("#C1E3FE"),
        Color.parseColor("#FFD9E0"),
        Color.parseColor("#F1EECD"),
        Color.parseColor("#C9DECE"),
        Color.parseColor("#C2B1A8"),
        Color.parseColor("#D0C7B5"),
        Color.parseColor("#B3C28A"),
        Color.parseColor("#8CB78D"),
        Color.parseColor("#7D96B0"),
        Color.parseColor("#94C0C0"),
        Color.parseColor("#E7CB71"),
        Color.parseColor("#A9BD95"),
        Color.parseColor("#FDB692"),
        Color.parseColor("#C0A4A5")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        TrackEvent.EnterTrackMapActivity.track()

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
        unsubscribeForParticipantDataUpdates()
        dismissAll(true)
        googleMap.clear()
        participantMarkers.clear()
        customMarker = null
        participants.clear()
        cancelCountDownTimer()
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
            binding.frameOrFollowParticipantsInMapButton.gone()
            binding.showRouteButton.gone()
            binding.trackMapBottomCardView.visible()
            googleMap.isMyLocationEnabled = false
            googleMapFramePadding = GOOGLE_MAP_FRAME_MIN_PADDING_DP
        } else {
            binding.switchMapStyleButton.visible()
            binding.frameOrFollowParticipantsInMapButton.visible()
            binding.showRouteButton.visible()
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

        binding.frameOrFollowParticipantsInMapButton.setOnClickListener {
            val frameOnResource = R.drawable.ic_frame_on
            val frameOffResource = R.drawable.ic_frame_off_anim

            if (frameOrFollowParticipantsInMap) {
                binding.frameOrFollowParticipantsInMapButton.setImageResource(frameOffResource)
                try {
                    (binding.frameOrFollowParticipantsInMapButton.drawable as AnimatedVectorDrawable)
                        .start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.frameOrFollowParticipantsInMapButton.setImageResource(frameOnResource)
            }

            frameOrFollowParticipantsInMap = !frameOrFollowParticipantsInMap
        }

        binding.showRouteButton.setOnClickListener {
            val mapPathOnResource = R.drawable.ic_map_path_on
            val mapPathOffResource = R.drawable.ic_map_path_off_anim

            if (showRouteInMap) {
                binding.showRouteButton.setImageResource(mapPathOffResource)
                try {
                    (binding.showRouteButton.drawable as AnimatedVectorDrawable).start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                binding.showRouteButton.setImageResource(mapPathOnResource)
            }
            showRouteInMap = !showRouteInMap
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.frameOrFollowParticipantsInMapButton) { _, insets ->
            statusBarInsetHeight = insets.systemWindowInsetTop
            navigationBarInsetHeight = insets.systemWindowInsetBottom
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.showRouteButton) { _, insets ->
            statusBarInsetHeight = insets.systemWindowInsetTop
            navigationBarInsetHeight = insets.systemWindowInsetBottom
            insets
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

        binding.markerMapSelectedBottomSheet.userShowInfoToggle.setOnCheckedChangeListener { _, isChecked ->
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

        binding.markerMapSelectedBottomSheet.userFollowToggle.setOnCheckedChangeListener { _, isChecked ->
            selectedMarkerTag?.let { markerTag ->
                if (isChecked) {
                    participantToFollow = participants.firstOrNull { it.userId == markerTag }
                    activateFraming()
                } else {
                    if (markerTag == participantToFollow?.userId) {
                        participantToFollow = null
                    }
                }
            }
        }

        binding.markerMapSelectedBottomSheet.userPhone.apply {
            setOnClickListener {
                val phone = this.text.toString()
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                if (dialIntent.resolveActivity(packageManager) != null) {
                    startActivity(dialIntent)
                }
            }
        }
    }

    /**
     * Start Live Tracking animated alert functions
     */
    private fun setupTrackingAlert() {
        binding.liveTrackingAlertDialog.scaleX = 0f
        binding.liveTrackingAlertDialog.scaleY = 0f
        binding.liveTrackingAlertDialog.visibility = View.VISIBLE
        binding.liveTrackingAlertDialog.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(2000)
            .setDuration(750)
            .withStartAction {
                countDownTimer.start()
            }
            .start()

        countDownTimer = object : CountDownTimer(3500, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000)
                binding.liveTrackingInfoNumber.scaleX = 2.5f
                binding.liveTrackingInfoNumber.scaleY = 2.5f
                binding.liveTrackingInfoNumber.visibility = View.VISIBLE
                binding.liveTrackingInfoNumber.text = secondsRemaining.toString()
                binding.liveTrackingInfoNumber.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(900)
                    .start()
            }

            override fun onFinish() {
                binding.liveTrackingAlertDialog.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(250)
                    .withEndAction {
                        binding.liveTrackingAlertDialog.visibility = View.GONE
                        startUserTrackLocationService(trackMapId = trackMapId, startTracking = true)
                        TrackEvent.LiveTrackingAutoActionClick.track()
                        setResult(RESULT_OK)
                    }
                    .start()
            }
        }

        binding.liveTrackingAlertCancelButton.setOnClickListener {
            cancelCountDownTimer()
        }
    }

    private fun cancelCountDownTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
            binding.liveTrackingAlertDialog.visibility = View.GONE
        }
    }

    private fun startUserTrackLocationService(trackMapId: String, startTracking: Boolean) {
        locationForegroundServiceHandler.startUserLocationService(
            this,
            trackMapId,
            startTracking
        )
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

    private fun setMapPadding(extra: Int = 0) {
        val topPadding = statusBarInsetHeight
        val bottomPadding = navigationBarInsetHeight + extra
        val minBottomFABSpace = this.dip(GOOGLE_MAP_BOTTOM_FAB_SPACE_DP)

        googleMap.setPadding(0, topPadding, 0, bottomPadding)
        val layoutParamsFrame = binding.frameOrFollowParticipantsInMapButton.layoutParams
        (layoutParamsFrame as? ConstraintLayout.LayoutParams)?.apply {
            setMargins(marginStart, topMargin, marginEnd, minBottomFABSpace + bottomPadding)
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

                deactivateFraming()
            }
        }

        googleMap.setOnMyLocationButtonClickListener {
            deactivateFraming()
            false
        }

        googleMap.setOnMarkerClickListener {
            val markerTag = it.tag.toString()
            if (markerTag != customMarker?.tag) {
                val sameMarker = markerTag == selectedMarkerTag
                selectedMarkerTag = markerTag
                showInfoBottomSheet(markerTag, sameMarker)
                customMarker?.remove()
            } else {
                hideMarkerBottomSheet()
            }
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
            hideMarkerBottomSheet()
            waitForAnyMarkerIntentAction()
        }

        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this, ::onRenderMarkerWindowInfo))
    }

    private fun deactivateFraming() {
        if (frameOrFollowParticipantsInMap) {
            binding.frameOrFollowParticipantsInMapButton.performClick()
        }
    }

    private fun activateFraming() {
        if (!frameOrFollowParticipantsInMap) {
            binding.frameOrFollowParticipantsInMapButton.performClick()
        }
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
        binding.frameOrFollowParticipantsInMapButton.animate().translationY(value).start()
        binding.showRouteButton.animate().translationY(value).start()
    }

    private fun resetFrameButtonExtraSpace() {
        binding.frameOrFollowParticipantsInMapButton.animate().translationY(0f).start()
        binding.showRouteButton.animate().translationY(0f).start()
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
            trackMapId = it.trackMapId

            setupTrackMapForParticipantUpdates(it)

            if (!locationForegroundServiceHandler.hasLiveTrackingAlreadyStarted(trackMapId)) {
                setupTrackingAlert()
            }
        }
    }

    private fun tiltView() {
        prefs.lastLocation.let {
            val tilt = if (myPositionState == LocatedAndTilted) 30f else 0f
            mapActionHelper.moveToPosition(latLng = it, tilt = tilt)
        }
    }

    private fun loadParticipantsData(participantIds: List<String>?) {
        participants.clear()

        participantIds?.forEach { id ->
            // Load participant data (Once)
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
                            participantLocation?.let {
                                val batteryLevel = it.batteryLevel ?: 100
                                participants.add(
                                    it.copy(
                                        userId = id,
                                        batteryLevel = batteryLevel
                                    )
                                )
                                logDebug("Loaded participant data of: $participantIds")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }

            // Subscribe for participant location updates
            subscribeForParticipantLocationUpdates(id)
        }
    }

    private fun setupTrackMapForParticipantUpdates(trackMap: TrackMap) {
        subscribeForParticipantUpdates(trackMap.trackMapId)
    }

    private fun subscribeForParticipantUpdates(trackMapId: String) {
        unsubscribeForParticipantDataUpdates()

        participantDataChildEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                reloadParticipantData()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {
                reloadParticipantData()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        firebaseHandler.getChildTrackMapId(trackMapId)
            .addChildEventListener(participantDataChildEventListener)
    }

    private fun reloadParticipantData() {
        firebaseHandler.getChildTrackMapId(trackMapId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    (snapshot.value as? List<String>)?.let { liveParticipants ->
                        loadParticipantsData(liveParticipants)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun subscribeForParticipantLocationUpdates(userId: String) {
        participantsLocationChildEventListener = object : ChildEventListener {
            var atomicLatitude = 0.0
            var atomicLongitude = 0.0

            private fun onParticipantDataUpdate(snapshot: DataSnapshot) {
                participants.firstOrNull { it.userId == userId }?.let {
                    when (snapshot.key) {
                        "latitude" -> atomicLatitude = snapshot.value as Double
                        "longitude" -> atomicLongitude = snapshot.value as Double
                        "altitudeAMSL" -> it.altitudeAMSL = snapshot.value as Long
                        "altitudeGeoid" -> it.altitudeGeoid = snapshot.value as Long
                        "speed" -> it.speed = snapshot.value as Long
                        "batteryLevel" -> it.batteryLevel = snapshot.value as Long
                        "lastAccess" -> it.lastAccess = snapshot.value as Long
                    }

                    // Atomic LatLng update
                    if (atomicLatitude != 0.0 && atomicLongitude != 0.0) {
                        it.latitude = atomicLatitude
                        it.longitude = atomicLongitude
                        atomicLatitude = 0.0
                        atomicLongitude = 0.0

                        refreshTrackMap()
                    }
                }
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

    private fun unsubscribeForParticipantDataUpdates() {
        if (::participantDataChildEventListener.isInitialized) {
            firebaseHandler.rootRef.removeEventListener(participantDataChildEventListener)
        }
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

                if (showRouteInMap) {
                    printParticipantPolylines(userId, latLng)
                }
            }

            // Refresh selected marker bottom sheet info
            selectedMarkerTag?.let {
                refreshBottomSheetMarkerData(it)
            }

            if (frameOrFollowParticipantsInMap) {
                frameOrFollowParticipants()
            }
        }
    }


    private fun printParticipantPolylines(userId: String, latLng: LatLng) {
        if (participantRoutes[trackMapId] == null) {
            participantRoutes[trackMapId] = HashMap()
        }

        val currentLatLngs = participantRoutes[trackMapId]?.get(userId)
        if (currentLatLngs == null) {
            participantRoutes[trackMapId]?.put(userId, mutableListOf(latLng))
        } else {
            val lastLatLng = currentLatLngs.last()
            val results = FloatArray(1)
            Location.distanceBetween(
                lastLatLng.latitude,
                lastLatLng.longitude,
                latLng.latitude,
                latLng.longitude,
                results
            )
            val distanceInMeters = results[0]
            if (distanceInMeters > ROUTE_POINT_THRESHOLD_METERS) {
                currentLatLngs.add(latLng)
                participantRoutes[trackMapId]?.put(userId, currentLatLngs)
            }
        }

        val userLocations = participantRoutes[trackMapId]?.get(userId)
        val options = PolylineOptions()
            .addAll(userLocations)
            .width(5f)
            .color(getPolylineColorByUser(userId))
            .jointType(JointType.ROUND)
            .startCap(startCap)
            .geodesic(true)
            .endCap(RoundCap())

        googleMap.addPolyline(options)
    }

    private fun getPolylineColorByUser(userId: String): Int {
        val index = participants.map { it.userId }.indexOf(userId)
        return polylineColors[polylineColors.size % (1 + index)]
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

    /**
     * Move the camera to participant to follow
     * or to unique participant location
     * or to all participants  with a zoom of 17.
     */
    private fun frameOrFollowParticipants() {
        val followedParticipant = participantToFollow

        if (followedParticipant != null) {
            moveCameraToParticipant(followedParticipant)
        } else if (participantMarkers.size == 1) {
            participants.firstOrNull(::withActivityAndAvailableLatLng)
                ?.let { moveCameraToParticipant(it) }
        } else {
            val boundsBuilder = LatLngBounds.Builder()

            participants.filter(::withActivityAndAvailableLatLng).forEach { participant ->
                boundsBuilder.include(LatLng(participant.latitude, participant.longitude))
            }

            try {
                val framePadding = dip(googleMapFramePadding)
                val build = boundsBuilder.build()
                val cameraUpdateAction = CameraUpdateFactory.newLatLngBounds(build, framePadding)
                this.googleMap.animateCamera(cameraUpdateAction)
            } catch (e: IllegalStateException) {
                // No included points
            }
        }
    }

    private fun moveCameraToParticipant(participant: ParticipantLocation) {
        val latLng = LatLng(participant.latitude, participant.longitude)
        val zoomLevel = getZoomLevelAccordingSpeed(participant.speed)
        this.googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoomLevel
            )
        )
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
        val activityTimeFactor = if (BuildConfig.DEBUG) 4 else 1
        val activityTimeLimit = LAST_ACTIVITY_IN_MINUTES_LIMIT * activityTimeFactor
        return participantLocation.getLastActivityInMinutes() < activityTimeLimit
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
                setMapPadding(0)
                BottomSheetBehavior.STATE_HIDDEN
            } else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                setMapPadding(this.dip(GOOGLE_MAP_BOTTOM_SHEET_SPACE_DP))
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
        setMapPadding(0)
    }

    private fun refreshBottomSheetMarkerData(markerTag: String) {
        userMarkerMap[markerTag]?.let { userMarkerData ->
            userMarkerData.participanLocation?.let { participantLocation ->
                val userId = participantLocation.userId
                val userNickname = participantLocation.nickname

                val distance =
                    getUserSessionLocation()?.distanceTo(participantLocation.toLocation()) ?: 0f
                with(markerMapSelectedBottomSheetBinding) {
                    markerMapSelectedBottomSheetBinding.userNickname.text = userNickname ?: userId

                    val (time, timeUnit) = participantLocation.getLastActivity()
                    val timeUnitPlural = getTimeUnitPlural(time, timeUnit)
                    userLastActivity.text =
                        getString(R.string.user_info_last_activity, time.toString(), timeUnitPlural)

                    userSpeed.value = participantLocation.speed.toString()
                    userSpeed.maxValue = locationHandler.getMaxSpeed(trackMapId, userId).toString()

                    userAltitude.value = participantLocation.altitudeAMSL.toString()
                    userAltitude.maxValue =
                        locationHandler.getMaxAltitudeAMSL(trackMapId, userId).toString()

                    val (distanceFormatted, unit) = getDistanceFormatted(distance)
                    userDistanceFromYou.value = distanceFormatted
                    userDistanceFromYou.unit = unit

                    userFullName.text = participantLocation.fullName ?: userNickname
                    userPhone.text = participantLocation.phone
                    userPhone.visibility =
                        if (userPhone.text.isNotEmpty()) View.VISIBLE else View.INVISIBLE
                    participantLocation.image?.let { userImage ->
                        userProfileImage.setImageBitmap(Base64Utils.getBase64Bitmap(userImage))
                    }

                    userBatteryLevel.level = participantLocation.batteryLevel?.toInt() ?: 100

                    userShowInfoToggle.isChecked = userMarkerData.isShowingInfoWindow
                    userFollowToggle.isChecked = markerTag == participantToFollow?.userId
                }
            }
        }
    }

    private val cacheTimeUnitPlurals = hashMapOf<String, String>()
    private fun getTimeUnitPlural(time: Long, timeUnit: TimeUnit): String {
        val isAUnit = time == 1L
        val key = "$timeUnit$isAUnit"
        return cacheTimeUnitPlurals[key] ?: when (timeUnit) {
            TimeUnit.DAYS -> resources.getQuantityString(R.plurals.days, time.toInt())
            TimeUnit.HOURS -> resources.getQuantityString(R.plurals.hours, time.toInt())
            TimeUnit.MINUTES -> resources.getQuantityString(R.plurals.minutes, time.toInt())
            else -> resources.getQuantityString(R.plurals.seconds, time.toInt())
        }.apply {
            cacheTimeUnitPlurals[key] = this
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

            R.id.markerTitle.findTextView().text =
                it.userAlias(isUserSession, getString(R.string.you))
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
        const val TRACKMAP_PARAM = "trackMapId"
        private const val GOOGLE_MAP_FRAME_MIN_PADDING_DP = 16
        private const val GOOGLE_MAP_FRAME_MAX_PADDING_DP = 64
        private const val GOOGLE_MAP_MARKER_INTENT_SPACE = 56
        private const val GOOGLE_MAP_BOTTOM_FAB_SPACE_DP = 24
        private const val GOOGLE_MAP_BOTTOM_SHEET_SPACE_DP = 220
        private const val ANY_MARKER_INTENT_ACTION_DELAY_SECS = 5L
        private const val CUSTOM_LOCATED_MARKER_TAG = "CustomMarker"

        private const val HALF_EXPANDED_RATIO = 0.3f
        private const val UNIT_KM = "Km"
        private const val UNIT_KMH = "Km/h"
        private const val UNIT_METERS = "m"

        private const val LAST_ACTIVITY_IN_MINUTES_LIMIT = 480 // 8 hours


        // TrackMapId -> UserId, LatLng
        val participantRoutes = HashMap<String, HashMap<String, MutableList<LatLng>>>()
        private const val ROUTE_POINT_THRESHOLD_METERS = 4f

        fun start(context: Context, trackMap: TrackMap) {
            context.startActivity<TrackMapActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(TRACKMAP_PARAM, trackMap)
            }
        }
    }
}
