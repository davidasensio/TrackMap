package com.handysparksoft.trackmap.ui.common

import android.content.Context
import android.content.Intent
import android.location.Location
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.handysparksoft.trackmap.App

/**
 * Extension functions of:
 * Context / Activity / Fragment
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

/**
 * Extension functions of:
 * View
 */
fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

/**
 * Extension functions of:
 * Others
 */
fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}
