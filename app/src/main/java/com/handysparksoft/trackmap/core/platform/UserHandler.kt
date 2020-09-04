package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.provider.Settings
import javax.inject.Inject

class UserHandler @Inject constructor(private val context: Context) {

    @SuppressLint("HardwareIds")
    fun getUserId() =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}
