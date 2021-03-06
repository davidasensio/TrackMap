package com.handysparksoft.trackmap.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.handysparksoft.trackmap.BuildConfig
import javax.inject.Inject

class UserHandler @Inject constructor(private val context: Context, private val prefs: Prefs) {

    fun getUserId(): String {
        return if (BuildConfig.DEBUG) {
            getDeviceData()
        } else {
            getAndroidSecureId()
        }
    }

    fun getUserNickname() = prefs.userProfileData?.nickname

    fun getUserNicknameOrFullName() = prefs.userProfileData?.nickname ?: getUserFullName()

    fun getUserFullName() = prefs.userProfileData?.fullName ?: getUserId()

    fun getUserBatteryLevel(): Int = BatteryLevelHelper.getBatteryLevel(context)

    @SuppressLint("HardwareIds")
    private fun getAndroidSecureId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun getDeviceData(): String {
        return "User - ${Build.MANUFACTURER} ${Build.MODEL} ${getSDKName()}"
    }

    private fun getSDKName(): String {
        return Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name
    }
}
