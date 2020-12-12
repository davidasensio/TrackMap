package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.os.Bundle
import com.handysparksoft.trackmap.App
import com.handysparksoft.trackmap.core.extension.app
import javax.inject.Inject

fun TrackEvent.track() {
    FirebaseTracking.track(this)
}

class FirebaseTracking(context: Context) {

    @Inject
    lateinit var userHandler: UserHandler

    init {
        context.app.component.inject(this)

        App.getFirebaseAnalyticsInstance().setDefaultEventParameters(Bundle().apply {
            putString("user", userHandler.getUserId())
            putString("user_nickname", userHandler.getUserNickname())
            putString("user_fullname", userHandler.getUserFullName())
        })
    }

    companion object {
        fun track(trackEvent: TrackEvent) {
            App.getFirebaseAnalyticsInstance()
                .logEvent(trackEvent::class.java.simpleName, Bundle().apply {
                })
        }
    }
}

sealed class TrackEvent {
    object OnboardingStarted : TrackEvent()
    object OnboardingEnded : TrackEvent()
    object OnboardingCompleted : TrackEvent()

    object HomeActionClick : TrackEvent()
    object JoinActionClick : TrackEvent()
    object CreateActionClick : TrackEvent()

    object MenuShareActionClick : TrackEvent()
    object MenuRateActionClick : TrackEvent()
    object MenuSortActionClick : TrackEvent()

    object ProfileBackActionClick : TrackEvent()
    object ProfilePickImageActionClick : TrackEvent()
    object ProfileSaveActionClick : TrackEvent()

    object GoActionClick : TrackEvent()
    object LeaveActionClick : TrackEvent()
    object ShareActionClick : TrackEvent()

    object CreatedTrackMap : TrackEvent()
    object JoinedTrackMap : TrackEvent()

    object EnterTrackMapActivity : TrackEvent()
}

sealed class TrackParam {
}
