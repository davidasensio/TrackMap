package com.handysparksoft.trackmap.features.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.logError
import com.handysparksoft.trackmap.core.extension.showTransitionTo
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.FragmentProfileBinding
import com.handysparksoft.trackmap.features.profile.ProfileViewModel.UiModel.*
import com.takusemba.cropme.OnCropListener

class ProfileFragment : Fragment() {

    private val bindingHolder = FragmentViewBindingHolder<FragmentProfileBinding>()
    private val binding get() = bindingHolder.binding
    private var profileImageSelected: Bitmap? = null
    private val viewModel: ProfileViewModel by lazy {
        ViewModelProvider(
            this,
            (activity as Context).app.component.profileViewModelFactory
        ).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHolder.createBinding(this) {
            FragmentProfileBinding.inflate(layoutInflater, container, false)
        }
        return bindingHolder.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.model.observe(viewLifecycleOwner, Observer(::updateUi))
        viewModel.saveProfileDataEvent.observe(
            viewLifecycleOwner,
            Observer(::onSaveProfileDataEvent)
        )

        setupToolbar()
        setupUI()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_proifleFragment_to_entriesFragment)
            TrackEvent.ProfileBackActionClick.track()
        }
    }

    private fun setupUI() {
        viewModel.refresh()

        binding.profilePickImageView.setOnClickListener {
            // val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            // startActivityForResult(pickPhoto, PICK_INTERNAL_IMAGE_REQUEST_CODE)
            val launcher = requireActivity().registerForActivityResult(GetContent()) { uri: Uri? ->
                if (uri == null) return@registerForActivityResult
                binding.cropView.setUri(uri)
                showCropView()
                profileImageSelected = BitmapFactory.decodeStream(
                    requireActivity().contentResolver.openInputStream(uri)
                )
            }
            launcher.launch("image/*")
            TrackEvent.ProfilePickImageActionClick.track()
        }

        binding.profileImageCancelCropButton.setOnClickListener {
            closeCropView()
        }

        binding.profileImageCropButton.setOnClickListener {
            binding.cropView.crop()
        }

        binding.cropView.addOnCropListener(object : OnCropListener {
            override fun onFailure(e: Exception) {
                requireContext().logError(e.message ?: "Error cropping image")
            }

            override fun onSuccess(bitmap: Bitmap) {
                Glide.with(requireContext()).load(bitmap).into(binding.profileImageView)
                profileImageSelected = bitmap
                closeCropView()
            }
        })

        binding.profileImageView.setOnClickListener {
            profileImageSelected?.let {
                Glide.with(requireContext()).load(it).into(binding.profileZoomImageView)
                showZoomView()
            }
        }

        binding.profileImageZoomCloseButton.setOnClickListener {
            closeZoomView()
        }

        binding.saveButton.setOnClickListener {
            viewModel.saveUserProfile(
                binding.nicknameEditText.text.toString(),
                binding.fullNameEditText.text.toString(),
                binding.phoneEditText.text.toString(),
                profileImageSelected
            )
        }
    }

    private fun updateUi(model: ProfileViewModel.UiModel) {
        binding.progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                binding.nicknameEditText.setText(model.data.nickname)
                binding.fullNameEditText.setText(model.data.fullName)
                binding.phoneEditText.setText(model.data.phone)
                model.data.image?.let {
                    profileImageSelected = Base64Utils.getBase64Bitmap(it)
                    binding.profileImageView.setImageBitmap(profileImageSelected)
                }
            }
            is Error -> {
                val message = if (model.isNetworkError) {
                    getString(R.string.no_connection_error)
                } else {
                    getString(R.string.unknown_error, model.message)
                }
                binding.saveButton.snackbar(message)
            }
        }
    }

    private fun onSaveProfileDataEvent(event: Event<Boolean>) {
        event.getContentIfNotHandled().let { result ->
            if (result == true) {
                findNavController().navigate(R.id.action_proifleFragment_to_entriesFragment)
                TrackEvent.ProfileSaveActionClick.track()
            }
        }
    }

    private fun showCropView() {
        binding.profileImageView.showTransitionTo(
            binding.cropViewCardView,
            Easing.Enter
        )
    }

    private fun closeCropView() {
        binding.cropViewCardView.showTransitionTo(
            binding.profileImageView,
            Easing.Leave
        )
    }

    private fun showZoomView() {
        binding.profileImageView.showTransitionTo(
            binding.imageZoomCardView,
            Easing.Enter
        )
    }

    private fun closeZoomView() {
        binding.imageZoomCardView.showTransitionTo(
            binding.profileImageView,
            Easing.Leave
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_INTERNAL_IMAGE_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        /*val bitmap = BitmapFactory.decodeStream(
                            requireActivity().contentResolver.openInputStream(it)
                        )*/
                        binding.cropView.setUri(it)
                        showCropView()
                    }
                }
            }
        }
    }

    companion object {
        private const val PICK_INTERNAL_IMAGE_REQUEST_CODE = 100
        fun newInstance() = ProfileFragment()
    }
}
