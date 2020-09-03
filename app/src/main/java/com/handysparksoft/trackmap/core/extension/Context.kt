package com.handysparksoft.trackmap.core.extension

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.handysparksoft.trackmap.App

/**
 * Extension functions of: Context / Activity / Fragment
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
