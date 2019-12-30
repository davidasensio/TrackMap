package com.handysparksoft.trackmap.ui.currenttrackmaps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.data.server.TrackMapRepository
import com.handysparksoft.trackmap.ui.common.toast
import kotlinx.android.synthetic.main.activity_current_track_maps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CurrentTrackMapsActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var job: Job
    lateinit var adapter: CurrentTrackMapsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_track_maps)

        job = Job()

        setupUI()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun setupUI() {
        adapter = CurrentTrackMapsAdapter { item ->
            toast("Clicked ${item.code}")
        }
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter

        launch(Dispatchers.Main) {
            val list = TrackMapRepository().getTrackMapList()
            adapter.items = list
            adapter.notifyDataSetChanged()
        }
    }
}
