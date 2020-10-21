package com.handysparksoft.trackmap.features.entries

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.logDebug
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.core.platform.network.ConnectionHandler
import com.handysparksoft.trackmap.databinding.FragmentEntriesBinding
import com.handysparksoft.trackmap.features.create.CreateFragment
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.*
import com.handysparksoft.trackmap.features.join.JoinFragment
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import kotlinx.android.synthetic.main.fragment_entries.*
import javax.inject.Inject

class EntriesFragment : Fragment() {
    private lateinit var binding: FragmentEntriesBinding

    private lateinit var adapter: EntriesAdapter

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            requireActivity().app.component.mainViewModelFactory
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

    override fun onStart() {
        connectionHandler.registerNetworkCallback()
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEntriesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let { fragmentActivity ->
            fragmentActivity.app.component.inject(this)
            permissionChecker = PermissionChecker(fragmentActivity, mainContentLayout)
        }

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
            //startUserTrackLocation()
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
        if (resultCode == Activity.RESULT_OK) {
            viewModel.refresh()
        }
    }

    private fun setAdapter() {
        adapter = EntriesAdapter(
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

        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
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
            is Error -> {
                swipeRefreshLayout.isRefreshing = false
                val message =
                    if (model.isNetworkError) {
                        getString(R.string.no_connection_error)
                    } else {
                        "Unknown error occurred. ${model.message}"
                    }
                binding.recycler.snackbar(
                    message = message
                )
            }
        }
    }

    private fun onGoEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            TrackMapActivity.start(requireContext(), it)
        }
    }

    private fun onLeaveEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            val leaveDialog = AlertDialog.Builder(requireContext())
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
            DeeplinkHandler.generateDeeplink(requireActivity(), it.trackMapId, it.name)
        }
    }

    private fun onJoinFeedbackEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            binding.recycler.snackbar(
                message = "You just joined TrackMap \"${it.name}\"",
                length = Snackbar.LENGTH_INDEFINITE
            ) {
                // Nothing to do
            }
        }
    }

    private fun setupUI() {
//        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        swipeRefreshLayout.setOnRefreshListener {
            connectionHandler.registerNetworkCallback()
//            if (!connectionHandler.isNetworkAvailable()) {
//                Snackbar.make(
//                    swipeRefreshLayout,
//                    R.string.no_connection_error,
//                    Snackbar.LENGTH_SHORT
//                ).show()
//                swipeRefreshLayout.isRefreshing = false
//            } else {
            viewModel.refresh()
//            }
        }
    }

    private fun checkDeepLink() {
        val trackMapCodeExtra = requireActivity().intent.getStringExtra(KEY_INTENT_TRACKMAP_CODE)
        if (trackMapCodeExtra != null) {
            val decodedCode = DeeplinkHandler.decodeBase64(trackMapCodeExtra)
            viewModel.joinTrackMap(trackMapCode = decodedCode, showFeedback = true)
        }
    }

    private fun joinTrackMapTemporal() {
        JoinFragment.startForResult(requireActivity(), JoinFragment.REQUEST_CODE)

        /*val promptJoinDialog = AlertDialog.Builder(this)
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
            .show()*/
    }

    private fun startUserTrackLocation() {
        locationHandler.subscribeLocationUpdates {
            viewModel.updateUserLocation(it)
        }
    }

    private fun startUserTrackLocationService() {
        requireActivity().apply {
            locationForegroundService = LocationForegroundService()
            val serviceIntent = Intent(requireContext(), locationForegroundService::class.java)
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
    }

    private fun isMyServiceRunning(serviceClass: Class<*>, mActivity: Activity): Boolean {
        val manager: ActivityManager =
            mActivity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                requireContext().logDebug("Service status - Running")
                return true
            }
        }
        requireContext().logDebug("Service status - Not Running")
        return false
    }

    fun isLocationEnabledOrNot(context: Context): Boolean {
        var locationManager: LocationManager? = null
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    companion object {
        const val KEY_INTENT_TRACKMAP_CODE = "KEY_INTENT_TRACKMAP_CODE"

        fun newInstance() = EntriesFragment()

        fun start(context: Context) {
            context.startActivity<EntriesFragment>()
        }
    }
}