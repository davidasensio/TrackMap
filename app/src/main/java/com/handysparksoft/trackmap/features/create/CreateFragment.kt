package com.handysparksoft.trackmap.features.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.SnackbarType
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.viewbinding.FragmentViewBindingHolder
import com.handysparksoft.trackmap.databinding.FragmentCreateBinding

class CreateFragment : Fragment() {

    private val bindingHolder = FragmentViewBindingHolder<FragmentCreateBinding>()
    private val binding get() = bindingHolder.binding

    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(
            this,
            (activity as Context).app.component.createViewModelFactory
        ).get(CreateViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHolder.createBinding(this) {
            FragmentCreateBinding.inflate(layoutInflater, container, false)
        }
        return bindingHolder.binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.trackMapCreation.observe(this, Observer {
            it.getContentIfNotHandled()?.let { created ->
                if (created) {
                    requireActivity().onBackPressed()
                }
            }
        })

        setupUI()
    }

    private fun setupUI() {
        viewModel.getTrackMapCode().observe(this, Observer(::trackMapCodeObserver))

        binding.createTrackmapButton?.setOnClickListener {
            validateForm { success ->
                if (success) {
                    onCreateAction()
                } else {
                    binding.createTrackmapButton.snackbar(
                        message = getString(R.string.create_trackmap_validation),
                        type = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    private fun validateForm(callback: (success: Boolean) -> Unit) {
        val success = (binding.createNameEditText.text?.isNotEmpty() == true &&
                binding.createDescriptionEditText.text?.isNotEmpty() == true)
        callback(success)
    }

    private fun trackMapCodeObserver(generatedCode: String) {
        binding.createCodeEditText.setText(generatedCode)
    }

    private fun onCreateAction() {
        val code = binding.createCodeEditText.text.toString()
        val name = binding.createNameEditText.text.toString()
        val description = binding.createDescriptionEditText.text.toString()
        viewModel.createTrackMap(code, name, description)
    }

    companion object {
        fun newInstance() = CreateFragment()
    }
}
