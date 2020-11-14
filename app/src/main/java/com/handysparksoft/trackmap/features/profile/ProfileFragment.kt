package com.handysparksoft.trackmap.features.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.FragmentProfileBinding
import com.handysparksoft.trackmap.features.profile.ProfileViewModel.UiModel.*

class ProfileFragment : Fragment() {

    private val bindingHolder = FragmentViewBindingHolder<FragmentProfileBinding>()
    private val binding get() = bindingHolder.binding

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.model.observe(this, Observer(::updateUi))
        viewModel.saveProfileDataEvent.observe(this, Observer(::onSaveProfileDataEvent))

        setupToolbar()
        setupUI()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_proifleFragment_to_entriesFragment)
        }
    }

    private fun setupUI() {
        viewModel.refresh()

        binding.saveButton.setOnClickListener {
            viewModel.saveUserProfile(
                binding.nicknameEditText.text.toString(),
                binding.fullNameEditText.text.toString(),
                binding.phoneEditText.text.toString()
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
            }
        }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}
