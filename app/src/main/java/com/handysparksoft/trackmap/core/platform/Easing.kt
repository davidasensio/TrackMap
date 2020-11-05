package com.handysparksoft.trackmap.core.platform

import android.view.animation.Interpolator
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator

sealed class Easing(val interpolator: Interpolator) {
    object Standard : Easing(FastOutSlowInInterpolator())
    object Enter : Easing(LinearOutSlowInInterpolator())
    object Leave : Easing(FastOutLinearInInterpolator())
}
