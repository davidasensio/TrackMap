package com.handysparksoft.trackmap.core.extension

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

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

fun View.snackbar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT,
    @StringRes actionResId: Int = android.R.string.ok,
    actionListener: View.OnClickListener? = null
) {
    Snackbar.make(this, message, length).apply {
        actionListener?.let {
            setAction(actionResId, actionListener)
        }
        show()
    }
}
