package com.handysparksoft.trackmap.features.join

import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.NotificationData
import com.handysparksoft.domain.model.PushNotification
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.*
import com.handysparksoft.trackmap.features.notification.PushNotificationHandler
import com.handysparksoft.usecases.GetUserAccessDataUseCase
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveUserTrackMapUseCase
import com.handysparksoft.usecases.SendPushNotificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class JoinViewModel(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler,
    private val pushNotificationHandler: PushNotificationHandler

) : ViewModel(), Scope by Scope.Impl() {

    private val _joinFeedbackEvent = MutableLiveData<Event<TrackMap>>()
    val joinFeedbackEvent: LiveData<Event<TrackMap>>
        get() = _joinFeedbackEvent

    init {
        initScope()
    }

    @CallSuper
    override fun onCleared() {
        pushNotificationHandler.destroy()
        destroyScope()
        super.onCleared()
    }

    fun joinTrackMap(context: Context, trackMapCode: String, showFeedback: Boolean = false) {
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

                // Push notification that new participant joined trackmap
                pushNotificationHandler.notifyNewParticipantHasJoin(context, userId, trackMap)
            }
        }
    }
}

class JoinViewModelFactory(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val userHandler: UserHandler,
    private val pushNotificationHandler: PushNotificationHandler
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return JoinViewModel(
            joinTrackMapUseCase,
            saveUserTrackMapUseCase,
            userHandler,
            pushNotificationHandler
        ) as T
    }
}
