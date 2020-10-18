package com.handysparksoft.trackmap.core.extension

import android.R
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

enum class SnackbarType { DEFAULT, OK, WARNING, ERROR }

fun View.snackbar(
    message: String,
    length: Int = Snackbar.LENGTH_SHORT,
    type: SnackbarType = SnackbarType.DEFAULT,
    @StringRes actionResId: Int = R.string.ok,
    actionListener: ((View) -> Unit)? = null
) {
    val text = when (type) {
        SnackbarType.DEFAULT -> message
        SnackbarType.OK -> "✅    $message"
        SnackbarType.WARNING -> "⚠️    $message"
        SnackbarType.ERROR -> "❌    $message"
    }

    Snackbar.make(this, text, length).apply {
        actionListener?.let {
            setAction(actionResId, actionListener)
        }
        show()
    }
}

