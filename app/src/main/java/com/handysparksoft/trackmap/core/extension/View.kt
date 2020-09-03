package com.handysparksoft.trackmap.core.extension

import android.view.View

/**
 * Extension functions of: View
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
