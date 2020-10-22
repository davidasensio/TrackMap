package com.handysparksoft.trackmap.core.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.view.inputmethod.InputMethodManager
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

inline fun <reified T> Activity.startActivityForResult(requestCode: Int, configIntent: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    intent.apply(configIntent)
    this.startActivityForResult(intent, requestCode)
}

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.isDarkModeActive(): Boolean {
    val darkModeFlags = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlags == Configuration.UI_MODE_NIGHT_YES
}

fun Activity.hideKeyBoard() {
    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let {
        this.currentFocus?.let { view ->
            it.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
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
