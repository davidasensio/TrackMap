package com.handysparksoft.trackmap.core.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageView
import kotlin.math.min


@SuppressLint("AppCompatCustomView")
class CircleImageView : ImageView {
    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
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
