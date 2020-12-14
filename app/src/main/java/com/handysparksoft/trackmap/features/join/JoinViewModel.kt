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
import com.handysparksoft.usecases.GetUserAccessDataUseCase
import com.handysparksoft.usecases.JoinTrackMapUseCase
import com.handysparksoft.usecases.SaveUserTrackMapUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class JoinViewModel(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val getUserAccessDataUseCase: GetUserAccessDataUseCase,
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
                notifyNewParticipantHasJoin(context, userId, trackMap)
            }
        }
    }

    private fun notifyNewParticipantHasJoin(
        context: Context,
        userId: String,
        trackMap: TrackMap
    ) {
        launch(Dispatchers.IO) {
            val joinedUser = userHandler.getUserNickname()
            val tokens = mutableListOf<String>()
            val awaitAll = trackMap.participantIds.filter { it != userId }.map {
                async { getUserAccessDataUseCase.execute(it) }
            }.awaitAll()

            awaitAll.forEach { result ->
                if (result is Result.Success) {
                    result.data.userToken?.let { tokens.add(it) }
                }
            }

            if (tokens.isNotEmpty()) {
                val notificationData = NotificationData(
                    title = context.getString(R.string.push_notification_user_joined_title),
                    body = context.getString(
                        R.string.push_notification_user_joined_message,
                        joinedUser,
                        trackMap.name
                    )
                )
                val pushNotification = PushNotification(
                    to = null,
                    registration_ids = tokens,
                    notification = notificationData
                )
                val authorizationToken = context.getString(R.string.push_notification_server_key)
                joinTrackMapUseCase.executeSendPushNotification(
                    authorizationToken,
                    pushNotification
                )
            }
        }
    }
}

class JoinViewModelFactory(
    private val joinTrackMapUseCase: JoinTrackMapUseCase,
    private val saveUserTrackMapUseCase: SaveUserTrackMapUseCase,
    private val getUserAccessDataUseCase: GetUserAccessDataUseCase,
    private val userHandler: UserHandler
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            joinTrackMapUseCase::class.java,
            saveUserTrackMapUseCase::class.java,
            getUserAccessDataUseCase::class.java,
            userHandler::class.java
        ).newInstance(
            joinTrackMapUseCase,
            saveUserTrackMapUseCase,
            getUserAccessDataUseCase,
            userHandler
        )
    }
}
