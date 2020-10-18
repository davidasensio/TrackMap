package com.handysparksoft.trackmap.features.join

import android.app.Activity
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
import com.google.android.material.snackbar.Snackbar
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.*
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.databinding.FragmentJoinBinding
import com.handysparksoft.trackmap.features.main.MainActivity


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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.joinFeedbackEvent.observe(viewLifecycleOwner, Observer(::onJoinFeedbackEvent))

        setupUI()
    }

    private fun setupUI() {
        addAutoCompletionTrackMapCode()
        binding.joinTrackMapButton.setOnClickListener {
            validateForm { success ->
                if (success) {
                    val trackMapCode = binding.joinCodeEditText.text.toString()
                    viewModel.joinTrackMap(trackMapCode, true)
                } else {
                    binding.root.snackbar(
                        message = getString(R.string.join_trackmap_validation),
                        type = SnackbarType.ERROR
                    )
                }
            }
            activity?.hideKeyBoard()
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
            requireActivity().onBackPressed()
        }
    }

    companion object {
        const val REQUEST_CODE = 111
        private val TRACKMAP_CODE_REGEX = """\d{3}-\d{3}""".toRegex()

        fun newInstance() = JoinFragment()

        fun start(context: Context) {
            context.startActivity<JoinFragment>()
        }

        fun startForResult(activity: Activity, requestCode: Int) {
            activity.startActivityForResult<JoinFragment>(requestCode)
        }
    }
}
