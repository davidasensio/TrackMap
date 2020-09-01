package com.handysparksoft.trackmap.ui.creation

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.trackmap.databinding.ActivityCreateBinding
import com.handysparksoft.trackmap.ui.common.startActivity
import com.handysparksoft.trackmap.ui.common.toast
import kotlinx.android.synthetic.main.activity_create.*

class CreateActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<CreateActivity>() {
                putExtra("param1", 5)
            }
        }
    }

    private lateinit var viewModel: CreateActivityViewModel
    private lateinit var binding: ActivityCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CreateActivityViewModel::class.java)

        this.toast("Param1 is ${intent.getIntExtra("param1", 0)}")

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
        viewModel.getTrackmapCode().observe(this, Observer(::trackmapCodeObserver))

        createTrackmapButton?.setOnClickListener {
            onCreateAction()
        }
    }

    private fun trackmapCodeObserver(generatedCode: String) {
        createCodeEditText?.setText(generatedCode)
    }

    private fun onCreateAction() {
        val code = createCodeEditText?.text.toString()
        val name = createNameEditText?.text.toString()
        val description = createDescriptionEditText?.text.toString()
        viewModel.createTrackmap(code, name, description)
        toast("Creating map...")
    }
}