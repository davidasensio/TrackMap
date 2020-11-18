package com.handysparksoft.trackmap.core.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


@SuppressLint("AppCompatCustomView")
class CircleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    init {
        /*outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
        setBackgroundResource(R.drawable.circle_background)*/
        scaleType = ScaleType.CENTER_CROP
    }

    override fun draw(canvas: Canvas) {
        val radius = min(width, height) / 2f
        val circlePath = Path().apply {
            addCircle(width / 2f, height / 2f, radius, Path.Direction.CW)
        }
        canvas.clipPath(circlePath)
        super.draw(canvas)
    }

}
