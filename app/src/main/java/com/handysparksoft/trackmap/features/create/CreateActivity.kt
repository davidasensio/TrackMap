package com.handysparksoft.trackmap.features.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.trackmap.databinding.ActivityCreateBinding
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.extension.toast
import kotlinx.android.synthetic.main.activity_create.*

class CreateActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<CreateActivity>() {
                //putExtra("param1", 5)
            }
        }
        fun startActivityForResult(activity: Activity) {
            activity.startActivityForResult(Intent(activity, CreateActivity::class.java), 1)
        }
    }

    private lateinit var binding: ActivityCreateBinding
    private val viewModel: CreateViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.createViewModelFactory
        ).get(CreateViewModel::class.java)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.trackMapCreation.observe(this, Observer {
            it.getContentIfNotHandled()?.let { created ->
                if (created) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        })

        initToolbar(toolbar)
        setupUI()
    }

    private fun initToolbar(toolbar: androidx.appcompat.widget.Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupUI() {
        viewModel.getTrackMapCode().observe(this, Observer(::trackMapCodeObserver))

        createTrackmapButton?.setOnClickListener {
            onCreateAction()
        }
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
}
