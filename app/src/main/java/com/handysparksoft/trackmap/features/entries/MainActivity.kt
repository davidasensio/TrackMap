package com.handysparksoft.trackmap.features.entries

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.crashlytics.android.Crashlytics
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.features.create.CreateActivity
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Content
import com.handysparksoft.trackmap.features.entries.MainViewModel.UiModel.Loading
import com.handysparksoft.trackmap.features.trackmap.TrackMapActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<MainActivity>()
        }
    }

    private lateinit var adapter: TrackMapEntriesAdapter
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.mainViewModelFactory
        ).get(MainViewModel::class.java)
    }
    private val mOnNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.navigation_create_map -> {
                            CreateActivity.start(this)
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_dashboard -> {
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_join_map -> {
                            joinTrackMapTemporal() //FIXME: needs to be refactored to fragment or FragmentDialog
                        }
                        R.id.navigation_search_trackmap -> {
                            MainActivity.start(this)
                            return@OnNavigationItemSelectedListener true
                        }
                        R.id.navigation_force_crash -> {
                            Crashlytics.getInstance().crash()
                            return@OnNavigationItemSelectedListener true
                        }
                    }
                    false
                }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setAdapter()

        viewModel.model.observe(this, Observer(::updateUi))
        viewModel.navigation.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                TrackMapActivity.start(this)
            }
        })
        viewModel.saveUser()

        setupUI()
    }

    private fun setAdapter() {
        adapter = TrackMapEntriesAdapter {
            viewModel.onCurrentTrackMapClicked(it)
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter
    }

    private fun updateUi(model: MainViewModel.UiModel) {
        progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                adapter.items = model.data
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupUI() {
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun joinTrackMapTemporal() {
        val promptJoinDialog = AlertDialog.Builder(this)
        val promptDialogView = layoutInflater.inflate(R.layout.dialog_prompt_join, null)
        promptJoinDialog.setView(promptDialogView)

        promptJoinDialog
            .setCancelable(true)
            .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                val trackMapCodeEditText =
                    promptDialogView.findViewById<EditText>(R.id.trackMapCodeEditText)
                viewModel.joinTrackMap(trackMapCodeEditText.text.toString())
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })
            .create()
            .show()
    }
}
