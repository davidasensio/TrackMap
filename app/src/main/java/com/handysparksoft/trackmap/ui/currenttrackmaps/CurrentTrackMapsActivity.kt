package com.handysparksoft.trackmap.ui.currenttrackmaps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.data.server.TrackMapRepository
import com.handysparksoft.trackmap.domain.TrackMap
import com.handysparksoft.trackmap.ui.common.gone
import com.handysparksoft.trackmap.ui.common.toast
import com.handysparksoft.trackmap.ui.common.visible
import kotlinx.android.synthetic.main.activity_current_track_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CurrentTrackMapsActivity : AppCompatActivity(), CurrentTrackMapsPresenter.View {

    private val presenter : CurrentTrackMapsPresenter by lazy { CurrentTrackMapsPresenter(TrackMapRepository()) }

    private val adapter: CurrentTrackMapsAdapter = CurrentTrackMapsAdapter {
        presenter.onCurrentTrackMapClicked(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_track_maps)
        presenter.onCreate(this)

        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun showProgress() {
        progress?.visible()
    }

    override fun hideProgress() {
        progress?.gone()
    }

    override fun updateData(currentTrackMaps: List<TrackMap>) {
        adapter.items = currentTrackMaps
        adapter.notifyDataSetChanged()
    }

    override fun onTrackMapClicked(trackMap: TrackMap) {
        toast("TrackMap ${trackMap.code} clicked")
    }
}
