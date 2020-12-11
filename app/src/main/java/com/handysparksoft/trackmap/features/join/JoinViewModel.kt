package com.handysparksoft.trackmap.features.join

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveUserTrackMapUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JoinViewModel(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler
) : ViewModel(), Scope by Scope.Impl() {

    private val _joinFeedbackEvent = MutableLiveData<Event<TrackMap>>()
    val joinFeedbackEvent: LiveData<Event<TrackMap>>
        get() = _joinFeedbackEvent

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

    fun joinTrackMap(trackMapCode: String, showFeedback: Boolean = false) {
        launch(Dispatchers.Main) {
            val userId = userHandler.getUserId()
            val joinedTrackMap = joinTrackMapUseCase.execute(userId, trackMapCode)
            joinedTrackMap?.let { trackMap ->
                val ownerId = trackMap.ownerId
                saveUserTrackMapUseCase.execute(userId, trackMap.trackMapId, trackMap)
                saveUserTrackMapUseCase.execute(ownerId, trackMap.trackMapId, trackMap)

                if (showFeedback) {
                    _joinFeedbackEvent.value = Event(trackMap)
                }
                TrackEvent.JoinedTrackMap.track()
            }
        }
    }
}

class JoinViewModelFactory(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            joinTrackMapUseCase::class.java,
            saveUserTrackMapUseCase::class.java,
            userHandler::class.java
        ).newInstance(
            joinTrackMapUseCase,
            saveUserTrackMapUseCase,
            userHandler
        )
    }
}
