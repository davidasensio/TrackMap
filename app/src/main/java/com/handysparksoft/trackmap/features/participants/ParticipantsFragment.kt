package com.handysparksoft.trackmap.features.participants

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.domain.model.UserProfileData
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.showTransitionTo
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.FragmentParticipantsBinding
import com.handysparksoft.trackmap.features.participants.ParticipantsViewModel.UiModel.*
import javax.inject.Inject

class ParticipantsFragment : Fragment() {
    private val bindingHolder = FragmentViewBindingHolder<FragmentParticipantsBinding>()
    private val binding get() = bindingHolder.binding

    private lateinit var adapter: ParticipantsAdapter
    private lateinit var trackMap: TrackMap

    private val viewModel: ParticipantsViewModel by lazy {
        ViewModelProvider(
            this,
            requireActivity().app.component.participantsViewModelFactory
        ).get(ParticipantsViewModel::class.java)
    }

    @Inject
    lateinit var userHandler: UserHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHolder.createBinding(this) {
            FragmentParticipantsBinding.inflate(layoutInflater, container, false)
        }
        return bindingHolder.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { fragmentActivity ->
            fragmentActivity.app.component.inject(this)
        }

        (arguments?.get(TRACKMAP_ARGUMENT) as? TrackMap)?.let {
            trackMap = it
            viewModel.refresh(trackMap)
            setupUI()
        }

        setAdapter()

        viewModel.model.observe(viewLifecycleOwner, Observer(::updateUi))

        setupToolbar()
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        findNavController().popBackStack(R.id.participantsFragment, true)
    }

    private fun setupToolbar() {
        val trackMapName = trackMap.name
        val trackMapOwner = trackMap.ownerName.let {
            if (it.length > USER_NAME_LIMIT) it.take(USER_NAME_LIMIT) + "..." else it
        }
        val trackMapDate =
            DateUtils.getRelativeDateFromTime(requireContext(), trackMap.creationDate)
        val trackMapCreation =
            getString(R.string.participants_subtitle, trackMapOwner, trackMapDate)

        binding.toolbar.title = trackMapName
        binding.toolbar.subtitle = trackMapCreation

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_participantsFragment_to_entriesFragment)
            TrackEvent.ParticipantsBackActionClick.track()
        }
    }

    private fun setupUI() {
        binding.participantsContentLayout.setOnClickListener {
            closeZoomView()
        }
    }


    private fun setAdapter() {
        adapter = ParticipantsAdapter(
            userSessionId = userHandler.getUserId(),
            trackMapOwnerId = trackMap.ownerId,
            onProfileImageClickListener = { fromProfileImageView, userProfileData ->
                onProfileImageClicked(fromProfileImageView, userProfileData)
            },
            onClickListener = {
                closeZoomView()
            }
        )

        binding.recycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycler.adapter = adapter
    }

    private fun updateUi(model: ParticipantsViewModel.UiModel) {
        binding.progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                adapter.items = model.data
                adapter.notifyDataSetChanged()
                binding.trackMapParticipantsTitle.text = getString(R.string.participants_count_title, model.data.size)
            }
            is Error -> {
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

    private fun onProfileImageClicked(
        fromProfileImageView: View,
        userProfileData: UserProfileData
    ) {
        if (binding.participantZoomCardView.visibility == View.VISIBLE) {
            closeZoomView()
        } else {
            userProfileData.image?.let {
                val participantImage = Base64Utils.getBase64Bitmap(it)
                Glide.with(requireContext()).load(participantImage)
                    .into(binding.participantZoomImageView)
                binding.participantZoomTextView.text = userProfileData.nickname
                binding.participantZoomPhoneImageButton.setOnClickListener {
                    val phone = userProfileData.phone
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    if (dialIntent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(dialIntent)
                    }
                }

                showZoomView(fromProfileImageView)
            }
        }
    }

    private fun showZoomView(fromProfileImageView: View) {
        fromProfileImageView.showTransitionTo(
            endView = binding.participantZoomCardView,
            easing = Easing.Enter,
            avoidHideViews = true
        )
        binding.participantZoomCardView.tag = fromProfileImageView
    }

    private fun closeZoomView() {
        (binding.participantZoomCardView.tag as? View)?.let {
            binding.participantZoomCardView.showTransitionTo(
                it,
                Easing.Leave
            )
        }
        binding.participantZoomCardView.tag = null
    }

    companion object {
        const val TRACKMAP_ARGUMENT = "trackMapArg"
        const val USER_NAME_LIMIT = 16

        fun newInstance() = ParticipantsFragment()
    }
}
