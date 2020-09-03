package com.handysparksoft.trackmap.features.entries

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.extension.startActivity
import com.handysparksoft.trackmap.core.extension.toast
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModel.UiModel.Content
import com.handysparksoft.trackmap.features.entries.CurrentTrackMapsViewModel.UiModel.Loading
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
                toast("TrackMap ${it.trackMapId} clicked")
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
