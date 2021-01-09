package com.handysparksoft.trackmap.core.extension

import android.R
import android.graphics.Color
import android.os.Handler
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
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

fun View.visibleOrGone(condition: Boolean) {
    this.visibility = if (condition) View.VISIBLE else View.GONE
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

fun View.showTransitionTo(endView: View, easing: Easing, avoidHideViews: Boolean = false) {
    // Construct a container transform transition between two views
    val transition = com.google.android.material.transition.platform.MaterialContainerTransform()
    transition.scrimColor = Color.TRANSPARENT
    transition.interpolator = easing.interpolator

    // Define the start and the end view
    transition.startView = this
    transition.endView = endView
    transition.addTarget(endView)

    // Trigger the container transform transition
    TransitionManager.beginDelayedTransition(
        this.rootView as ViewGroup?,
        transition as android.transition.Transition
    )
    this.visibility = if (avoidHideViews) View.VISIBLE else View.INVISIBLE
    endView.visibility = View.VISIBLE
}

fun View.onPreDraw(callback: () -> Unit) {
    val treeObserver = this.viewTreeObserver
    treeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            Handler().postDelayed({
                callback.invoke()
            }, 250)
            treeObserver.removeOnPreDrawListener(this)
            return false
        }
    })
}
