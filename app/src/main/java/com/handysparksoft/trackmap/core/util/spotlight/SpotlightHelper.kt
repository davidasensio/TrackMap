package com.handysparksoft.trackmap.core.util.spotlight

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.gone
import com.handysparksoft.trackmap.core.extension.visible
import com.handysparksoft.trackmap.databinding.LayoutTargetBinding
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.effet.RippleEffect
import com.takusemba.spotlight.shape.Circle

class SpotlightHelper {

    fun buildSpotlight(
        activity: Activity,
        targetViews: List<View?>,
        tooltips: List<Tooltip>,
        onStarted: (() -> Unit)? = null,
        onEnded: (() -> Unit)? = null,
        onCompleted: (() -> Unit)? = null,
    ): Spotlight {
        var targetIndex = 0
        val overlayLayoutBinding = LayoutTargetBinding.inflate(
            activity.layoutInflater,
            FrameLayout(activity),
            false
        )
        val targets = mutableListOf<Target>()

        targetViews.forEachIndexed { index, view ->
            if (view != null) {
                val tooltip = if (tooltips.size > index) tooltips[index] else null
                val target = buildTarget(overlayLayoutBinding, view, tooltip)
                targets.add(target)
            }
        }

        val spotlight = Spotlight.Builder(activity)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.spotlightBackground)
            .setDuration(SPOTLIGHT_ANIMATION_DURATION)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    onStarted?.invoke()
                }

                override fun onEnded() {
                    onEnded?.invoke()
                }
            }).build()

        // Navigation buttons
        overlayLayoutBinding.spotlightCloseTarget.setOnClickListener {
            spotlight.next()
            if (++targetIndex == targetViews.size) {
                onCompleted?.invoke()
            }
        }
        overlayLayoutBinding.spotlightClose.setOnClickListener { spotlight.finish() }

        return spotlight
    }

    private fun buildTarget(
        layoutBinding: LayoutTargetBinding,
        targetView: View,
        tooltip: Tooltip?
    ): Target {
        fun TextView.setTextAndShow(text: String) {
            this.text = text
            this.visible()
        }

        val topLeftTextView = layoutBinding.spotlightTopLeftTextView
        val topRightTextView = layoutBinding.spotlightTopRightTextView
        val bottomLeftTextView = layoutBinding.spotlightBottomLeftTextView
        val bottomRightTextView = layoutBinding.spotlightBottomRightTextView

        val target = Target.Builder()
            .setOverlay(layoutBinding.root.rootView)
            .setAnchor(targetView)
            .setShape(Circle(CIRCLE_TARGET_RADIUS))
            //.setShape(RoundedRectangle(targetView.height.toFloat(), targetView.width.toFloat(), 4f))
            .setEffect(RippleEffect(100f, 200f, Color.argb(30, 124, 255, 90)))
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    when (tooltip?.position) {
                        TooltipPosition.TopLeft -> topLeftTextView.setTextAndShow(tooltip.tooltip)
                        TooltipPosition.TopRight -> topRightTextView.setTextAndShow(tooltip.tooltip)
                        TooltipPosition.BottomLeft -> bottomLeftTextView.setTextAndShow(tooltip.tooltip)
                        TooltipPosition.BottomRight -> bottomRightTextView.setTextAndShow(tooltip.tooltip)
                    }
                }

                override fun onEnded() {
                    topLeftTextView.gone()
                    topRightTextView.gone()
                    bottomLeftTextView.gone()
                    bottomRightTextView.gone()
                }
            })
        return target.build()
    }

    sealed class TooltipPosition() {
        object TopLeft : TooltipPosition()
        object TopRight : TooltipPosition()
        object BottomLeft : TooltipPosition()
        object BottomRight : TooltipPosition()
    }

    data class Tooltip(val tooltip: String, val position: TooltipPosition)

    companion object {
        private const val SPOTLIGHT_ANIMATION_DURATION = 1000L
        private const val CIRCLE_TARGET_RADIUS = 100f
    }
}
