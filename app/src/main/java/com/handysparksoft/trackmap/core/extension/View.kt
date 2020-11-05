package com.handysparksoft.trackmap.core.extension

import android.R
import android.graphics.Color
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.handysparksoft.trackmap.core.platform.Easing

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

fun View.showTransitionTo(endView: View, easing: Easing) {
    // Construct a container transform transition between two views
    val transition = com.google.android.material.transition.platform.MaterialContainerTransform()
    transition.scrimColor = Color.TRANSPARENT
    transition.interpolator = easing.interpolator

    // Define the start and the end view
    transition.startView = this
    transition.endView = endView
    transition.addTarget(endView)

    // Trigger the container transform transition
    TransitionManager.beginDelayedTransition(this.rootView as ViewGroup?, transition as android.transition.Transition)
    this.visibility = View.INVISIBLE
    endView.visibility = View.VISIBLE
}
