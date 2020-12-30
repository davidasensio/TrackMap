package com.handysparksoft.trackmap.features.entries

import android.app.Activity
import android.content.Intent
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
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.core.platform.network.ConnectionHandler
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.FragmentEntriesBinding
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.*
import com.handysparksoft.trackmap.features.entries.sort.SortEntriesBottomSheetDialogFragment
import com.handysparksoft.trackmap.features.join.JoinViewModel
import com.handysparksoft.trackmap.features.main.MainActivity
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import javax.inject.Inject

class EntriesFragment : Fragment() {
    private val bindingHolder = FragmentViewBindingHolder<FragmentEntriesBinding>()
    private val binding get() = bindingHolder.binding

    private lateinit var adapter: EntriesAdapter

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            requireActivity().app.component.mainViewModelFactory
        ).get(MainViewModel::class.java)
    }

    private val joinViewModel: JoinViewModel by lazy {
        ViewModelProvider(
            this,
            requireActivity().app.component.joinViewModelFactory
        ).get(JoinViewModel::class.java)
    }

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var locationHandler: LocationHandler

    @Inject
    lateinit var connectionHandler: ConnectionHandler

    @Inject
    lateinit var userHandler: UserHandler

    @Inject
    lateinit var locationForegroundServiceHandler: LocationForegroundServiceHandler

    // FIXME make injectable
    private lateinit var permissionChecker: PermissionChecker

    override fun onStart() {
        connectionHandler.registerNetworkCallback()
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHolder.createBinding(this) {
            FragmentEntriesBinding.inflate(layoutInflater, container, false)
        }
        return bindingHolder.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { fragmentActivity ->
            fragmentActivity.app.component.inject(this)
            permissionChecker = PermissionChecker(fragmentActivity, binding.mainContentLayout)
        }

        setAdapter()

        viewModel.model.observe(viewLifecycleOwner, Observer(::updateUi))
        viewModel.goEvent.observe(viewLifecycleOwner, Observer(::onGoEvent))
        viewModel.leaveEvent.observe(viewLifecycleOwner, Observer(::onLeaveEvent))
        viewModel.shareEvent.observe(viewLifecycleOwner, Observer(::onShareEvent))
        joinViewModel.joinFeedbackEvent.observe(viewLifecycleOwner, Observer(::onJoinFeedbackEvent))
        viewModel.saveUser()
        viewModel.updateUserBatteryLevel()

        setupUI()

        (requireActivity() as MainActivity).let { mainActivity ->
            mainActivity.onSortMenuItemClick {
                SortEntriesBottomSheetDialogFragment().apply {
                    this.onSortByDateClick { viewModel.sortByDate() }
                    this.onSortByNameClick { viewModel.sortByName() }
                    this.onSortByParticipantsClick { viewModel.sortByParticipants() }
                    this.onSortByOwnedClick { viewModel.sortByOwned() }
                }.show(mainActivity.supportFragmentManager, "SortEntries")
            }
        }

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
            userSession = userHandler.getUserId(),
            onGoListener = {
                permissionChecker.requestLocationPermission(onGrantedPermission = {
                    updateLastLocation()
                    viewModel.onGoTrackMapClicked(it)
                    TrackEvent.GoActionClick.track()
                })
            },
            onLeaveListener = {
                viewModel.onLeaveTrackMapClicked(it)
                TrackEvent.LeaveActionClick.track()
            },
            onShareListener = {
                viewModel.onShareTrackMapClicked(it)
                TrackEvent.ShareActionClick.track()
            },
            onFavoriteListener = { trackMap, favorite ->
                viewModel.onFavoriteTrackMapClicked(trackMap, favorite)
                TrackEvent.FavoriteActionClick.track()
            },
            onLiveTrackingListener = { trackMap, startTracking ->
                permissionChecker.requestLocationPermission(onGrantedPermission = {
                    updateLastLocation()
                    startUserTrackLocationService(trackMap, startTracking)
                })
                TrackEvent.LiveTrackingActionClick.track()
            }
        )

        binding.recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycler.adapter = adapter
    }

    private fun updateUi(model: MainViewModel.UiModel) {
        binding.progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                adapter.items = model.data
                adapter.notifyDataSetChanged()
                locationForegroundServiceHandler.setUserTrackMapIds(model.data.map { it.trackMapId })
                binding.swipeRefreshLayout.isRefreshing = false
                binding.recycler.scrollToPosition(0)
            }
            is Error -> {
                binding.swipeRefreshLayout.isRefreshing = false
                val message =
                    if (model.isNetworkError) {
                        getString(R.string.no_connection_error)
                    } else {
                        getString(R.string.unknown_error, model.message)
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
            viewModel.refresh()
        }
    }

    private fun setupUI() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            connectionHandler.registerNetworkCallback()
            viewModel.refresh()
        }
    }

    private fun checkDeepLink() {
        val trackMapCodeExtra =
            requireActivity().intent.getStringExtra(KEY_INTENT_TRACKMAP_CODE)
        if (trackMapCodeExtra != null) {
            val decodedCode = DeeplinkHandler.decodeBase64(trackMapCodeExtra)
//            viewModel.joinTrackMap(trackMapCode = decodedCode, showFeedback = true)
            joinViewModel.joinTrackMap(
                context = requireContext(),
                trackMapCode = decodedCode,
                showFeedback = true
            )
        }
    }

    private fun startUserTrackLocationService(trackMap: TrackMap, startTracking: Boolean) {
        requireActivity().apply {
            locationForegroundServiceHandler.startUserLocationService(
                this,
                trackMap.trackMapId,
                startTracking
            )
        }
    }

    companion object {
        const val KEY_INTENT_TRACKMAP_CODE = "KEY_INTENT_TRACKMAP_CODE"

        fun newInstance() = EntriesFragment()
    }
}
