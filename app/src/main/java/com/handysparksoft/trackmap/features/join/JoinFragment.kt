package com.handysparksoft.trackmap.features.join

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.SnackbarType
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.hideKeyBoard
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.TrackEvent
import com.handysparksoft.trackmap.core.platform.TrackEvent.JoinActionClick
import com.handysparksoft.trackmap.core.platform.track
import com.handysparksoft.trackmap.databinding.FragmentJoinBinding


class JoinFragment : Fragment() {
    private lateinit var binding: FragmentJoinBinding

    private val viewModel: JoinViewModel by lazy {
        ViewModelProvider(
            this,
            (activity as Context).app.component.joinViewModelFactory
        ).get(JoinViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.joinFeedbackEvent.observe(viewLifecycleOwner, Observer(::onJoinFeedbackEvent))

        setupUI()
    }

    private fun setupUI() {
        addAutoCompletionTrackMapCode()
        binding.joinTrackMapButton.setOnClickListener {
            validateForm { success ->
                if (success) {
                    val trackMapCode = binding.joinCodeEditText.text.toString()
                    viewModel.joinTrackMap(requireContext(), trackMapCode, true)
                } else {
                    binding.root.snackbar(
                        message = getString(R.string.join_trackmap_validation),
                        type = SnackbarType.ERROR
                    )
                }
            }
            activity?.hideKeyBoard()
            JoinActionClick.track()
        }
    }



    private fun validateForm(callback: (success: Boolean) -> Unit) {
        val success = (binding.joinCodeEditText.text?.matches(TRACKMAP_CODE_REGEX)) ?: false
        callback(success)
    }

    private fun addAutoCompletionTrackMapCode() {
        binding.joinCodeEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s.toString().let { text ->
                    if (text.length > 3 && text.indexOf("-") == -1) {
                        val formattedText = text.substring(0, 3) + "-" + text.substring(3)
                        binding.joinCodeEditText.setText(formattedText)
                        binding.joinCodeEditText.setSelection(formattedText.length)
                    }
                }
            }
        })
    }

    private fun onJoinFeedbackEvent(event: Event<TrackMap>) {
        event.getContentIfNotHandled()?.let {
            findNavController().navigate(R.id.action_joinFragment_to_entriesFragment)
        }
    }

    companion object {
        private val TRACKMAP_CODE_REGEX = """\d{3}-\d{3}""".toRegex()

        fun newInstance() = JoinFragment()
    }
}
