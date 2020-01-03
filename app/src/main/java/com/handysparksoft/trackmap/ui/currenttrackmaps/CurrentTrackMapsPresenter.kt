package com.handysparksoft.trackmap.ui.currenttrackmaps

import com.handysparksoft.trackmap.data.server.TrackMapRepository
import com.handysparksoft.trackmap.domain.TrackMap
import com.handysparksoft.trackmap.ui.common.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CurrentTrackMapsPresenter(private val trackMapRepository: TrackMapRepository) : Scope by Scope.Impl() {
    interface View {
        fun showProgress()
        fun hideProgress()
        fun updateData(currentTrackMaps: List<TrackMap>)
        fun onTrackMapClicked(trackMap: TrackMap)
    }

    private var view: View? = null

    fun onCreate(view: View) {
        initScope()

        this.view = view

        launch(Dispatchers.Main) {
            view.showProgress()
            view.updateData(trackMapRepository.getTrackMapList())
            view.hideProgress()
        }
    }

    fun onDestroy() {
        cancelScope()
        this.view = null
    }

    fun onCurrentTrackMapClicked(trackMap: TrackMap) {
        view?.onTrackMapClicked(trackMap)
    }
}
