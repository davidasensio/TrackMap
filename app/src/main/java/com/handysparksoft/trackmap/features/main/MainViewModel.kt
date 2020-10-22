package com.handysparksoft.trackmap.features.main

import android.content.Context
import android.location.Location
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.core.extension.app
import com.handysparksoft.trackmap.core.platform.Event
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.trackmap.features.join.JoinViewModel
import com.handysparksoft.usecases.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel() : ViewModel(), Scope by Scope.Impl() {

//    sealed class UiModel {
//        object Loading : UiModel()
//        class Content(val data: List<TrackMap>) : UiModel()
//        class Error(val isNetworkError: Boolean, val message: String) : UiModel()
//    }
//
//    private val _model = MutableLiveData<UiModel>()
//    val model: MutableLiveData<UiModel>
//        get() {
//            if (_model.value == null) {
////                refresh()
//            }
//            return _model
//        }
//
//    private val _goEvent = MutableLiveData<Event<TrackMap>>()
//    val goEvent: LiveData<Event<TrackMap>>
//        get() = _goEvent
//
//    private val _leaveEvent = MutableLiveData<Event<TrackMap>>()
//    val leaveEvent: LiveData<Event<TrackMap>>
//        get() = _leaveEvent
//
//    private val _shareEvent = MutableLiveData<Event<TrackMap>>()
//    val shareEvent: LiveData<Event<TrackMap>>
//        get() = _shareEvent
//
//    private val _joinFeedbackEvent = MutableLiveData<Event<TrackMap>>()
//    val joinFeedbackEvent: LiveData<Event<TrackMap>>
//        get() = _joinFeedbackEvent
//
//    init {
//        initScope()
//    }
//
    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

//    fun refresh() {
//        launch(Dispatchers.Main) {
//            val userId = userHandler.getUserId()
//            _model.value = UiModel.Loading
//
//            val userTrackMaps = getTrackMapsUseCase.execute(userId)
//            if (userTrackMaps is Result.Success) {
//                _model.value = UiModel.Content(ArrayList(userTrackMaps.data.values))
//            } else if (userTrackMaps is Result.Error) {
//                _model.value =
//                    UiModel.Error(userTrackMaps.isNetworkError, "Code: ${userTrackMaps.code}")
//            }
//        }
//    }

//    fun onGoTrackMapClicked(trackMap: TrackMap) {
//        _goEvent.value = Event(trackMap)
//    }
//
//    fun onLeaveTrackMapClicked(trackMap: TrackMap) {
//        _leaveEvent.value = Event(trackMap)
//    }
//
//    fun onShareTrackMapClicked(trackMap: TrackMap) {
//        _shareEvent.value = Event(trackMap)
//    }

//    fun joinTrackMap(trackMapCode: String, showFeedback: Boolean = false) {
//        launch(Dispatchers.Main) {
//            val userId = userHandler.getUserId()
//            val joinedTrackMap = joinTrackMapUseCase.execute(userId, trackMapCode)
//            joinedTrackMap?.let { trackMap ->
//                val ownerId = trackMap.ownerId
//                saveUserTrackMapUseCase.execute(userId, trackMap.trackMapId, trackMap)
//                saveUserTrackMapUseCase.execute(ownerId, trackMap.trackMapId, trackMap)
//
//                if (showFeedback) {
//                    _joinFeedbackEvent.value = Event(trackMap)
//                }
//            }
//            //refresh()
//        }
//    }
}
