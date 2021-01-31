package com.handysparksoft.trackmap.features.notification

import android.content.Context
import com.handysparksoft.data.Result
import com.handysparksoft.domain.model.NotificationData
import com.handysparksoft.domain.model.PushNotification
import com.handysparksoft.domain.model.TrackMap
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.platform.Scope
import com.handysparksoft.trackmap.core.platform.UserHandler
import com.handysparksoft.usecases.GetUserAccessDataUseCase
import com.handysparksoft.usecases.SendPushNotificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class PushNotificationHandler @Inject constructor(
    private val sendPushNotificationUseCase: SendPushNotificationUseCase,
    private val getUserAccessDataUseCase: GetUserAccessDataUseCase,
    private val userHandler: UserHandler
) : Scope by Scope.Impl() {

    init {
        initScope()
    }

    fun destroy() {
        destroyScope()
    }

    fun notifyNewParticipantHasJoin(
        context: Context,
        userId: String,
        trackMap: TrackMap
    ) {
        launch(Dispatchers.IO) {
            val usersIds = trackMap.participantIds.filter { it != userId }
            val tokens = getUserTokens(usersIds)

            if (tokens.isNotEmpty()) {
                val participant = userHandler.getUserNickname()
                    ?: context.getString(R.string.push_notification_generic_user_joined_message)

                val notificationData = NotificationData(
                    title = context.getString(R.string.push_notification_user_joined_title),
                    body = context.getString(
                        R.string.push_notification_user_joined_message,
                        participant,
                        trackMap.name
                    )
                )
                val pushNotification = PushNotification(
                    to = null,
                    registration_ids = tokens,
                    notification = notificationData
                )
                val authorizationToken = context.getString(R.string.push_notification_server_key)

                sendPushNotificationUseCase.execute(
                    authorizationToken,
                    pushNotification
                )
            }
        }
    }

    fun pingParticipants(
        context: Context,
        userId: String,
        trackMap: TrackMap
    ) {
        launch(Dispatchers.Main) {
            val usersIds = trackMap.participantIds.filter { it != userId }
            val tokens = getUserTokens(usersIds)

            if (tokens.isNotEmpty()) {
                val participant = userHandler.getUserNickname() ?: userHandler.getUserFullName()

                val notificationData = NotificationData(
                    title = context.getString(
                        R.string.push_notification_ping_user_being_tracked_title,
                        trackMap.name
                    ),
                    body = context.getString(
                        R.string.push_notification_ping_user_being_tracked_participant,
                        participant
                    )
                )
                val pushNotification = PushNotification(
                    to = null,
                    registration_ids = tokens,
                    notification = notificationData
                )
                val authorizationToken = context.getString(R.string.push_notification_server_key)

                sendPushNotificationUseCase.execute(
                    authorizationToken,
                    pushNotification
                )
            }
        }
    }

    private suspend fun getUserTokens(usersIds: List<String>): List<String> {
        val tokens = mutableListOf<String>()
        val awaitAll = usersIds.map {
            async { getUserAccessDataUseCase.execute(it) }
        }.awaitAll()

        awaitAll.forEach { result ->
            if (result is Result.Success) {
                result.data.userToken?.let { tokens.add(it) }
            }
        }
        return tokens
    }
}
