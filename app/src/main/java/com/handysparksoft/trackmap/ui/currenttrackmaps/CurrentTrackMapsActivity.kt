package com.handysparksoft.trackmap.ui.currenttrackmaps

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.handysparksoft.data.repository.TrackMapRepository
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.data.server.ServerDataSource
import com.handysparksoft.trackmap.data.server.TrackMapDb
import com.handysparksoft.trackmap.ui.common.app
import com.handysparksoft.trackmap.ui.common.startActivity
import com.handysparksoft.trackmap.ui.common.toast
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModel.UiModel.Content
import com.handysparksoft.trackmap.ui.currenttrackmaps.CurrentTrackMapsViewModel.UiModel.Loading
import kotlinx.android.synthetic.main.activity_current_track_maps.*

class CurrentTrackMapsActivity : AppCompatActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity<CurrentTrackMapsActivity>()
        }
    }

    private lateinit var adapter: CurrentTrackMapsAdapter
    private val viewModel: CurrentTrackMapsViewModel by lazy {
        ViewModelProvider(
            this,
            app.component.currentViewModelFactory
        ).get(CurrentTrackMapsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_track_maps)

        adapter = CurrentTrackMapsAdapter {
            viewModel.onCurrentTrackMapClicked(it)
        }

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        viewModel.model.observe(this, Observer(::updateUi))
        viewModel.navigation.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let {
                toast("TrackMap ${it.code} clicked")
            }
        })
    }

    private fun updateUi(model: CurrentTrackMapsViewModel.UiModel) {
        progress.visibility = if (model == Loading) View.VISIBLE else View.GONE
        when (model) {
            is Content -> {
                adapter.items = model.data
                adapter.notifyDataSetChanged()
            }
        }
    }
}
