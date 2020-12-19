package com.handysparksoft.trackmap.core.platform

import com.google.firebase.messaging.FirebaseMessagingService
import com.handysparksoft.trackmap.core.extension.logDebug
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var prefs: Prefs

    override fun onNewToken(newToken: String) {
        if (::prefs.isInitialized) {
            prefs.userToken = newToken
        }
        logDebug("onNewToken: $newToken")
        super.onNewToken(newToken)
    }
}
