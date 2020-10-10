package com.handysparksoft.trackmap.core.extension

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import com.handysparksoft.trackmap.App
import com.handysparksoft.trackmap.BuildConfig

/**
 * Extension functions of: Context / Activity / Fragment / Intent
 */
val Context.app: App
    get() = applicationContext as App

inline fun <reified T> Context.startActivity(configIntent: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(configIntent)
    this.startActivity(intent)
}

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.isDarkModeActive(): Boolean {
    val darkModeFlags = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlags == Configuration.UI_MODE_NIGHT_YES
}

fun Intent.addClearAllFlags() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
}

fun Intent.addClearTopFlag() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

fun Context.logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d(this::class.java.simpleName, message)
    }
}

fun Context.logError(message: String) {
    if (BuildConfig.DEBUG) {
        Log.e(this::class.java.simpleName, message)
    }
}
