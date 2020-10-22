package com.handysparksoft.trackmap.features.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.SnackbarType
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.snackbar
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.databinding.FragmentCreateBinding
import com.handysparksoft.trackmap.features.join.JoinFragment
import kotlinx.android.synthetic.main.fragment_create.*

class CreateFragment : Fragment() {

    private lateinit var binding: FragmentCreateBinding

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
        binding = FragmentCreateBinding.inflate(layoutInflater, container, false)
        return binding.root
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

        //initToolbar(toolbar)
        setupUI()
    }

    /*private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar) {
        activity?.apply {
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }*/

//    override fun onSupportNavigateUp(): Boolean {
////        onBackPressed()
//        return true
//    }

    private fun setupUI() {
        viewModel.getTrackMapCode().observe(this, Observer(::trackMapCodeObserver))

        createTrackmapButton?.setOnClickListener {
            validateForm { success ->
                if (success) {
                    onCreateAction()
                } else {
                    createTrackmapButton.snackbar(
                        message = getString(R.string.create_trackmap_validation),
                        type = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    private fun validateForm(callback: (success: Boolean) -> Unit) {
        val success = (createNameEditText?.text?.isNotEmpty() == true &&
                createDescriptionEditText?.text?.isNotEmpty() == true)
        callback(success)
    }

    private fun trackMapCodeObserver(generatedCode: String) {
        createCodeEditText?.setText(generatedCode)
    }

    private fun onCreateAction() {
        val code = createCodeEditText?.text.toString()
        val name = createNameEditText?.text.toString()
        val description = createDescriptionEditText?.text.toString()
        viewModel.createTrackMap(code, name, description)
    }

    companion object {
        fun newInstance() = CreateFragment()

        fun start(context: Context) {
            context.startActivity<CreateFragment>() {
                //putExtra("param1", 5)
            }
        }

        fun startActivityForResult(activity: Activity) {
            activity.startActivityForResult(Intent(activity, CreateFragment::class.java), 1)
        }
    }
}
