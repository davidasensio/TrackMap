package com.handysparksoft.trackmap.ui.common

import android.content.Context
import android.location.Location
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}
