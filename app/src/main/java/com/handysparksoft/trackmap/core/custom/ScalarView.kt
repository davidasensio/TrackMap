package com.handysparksoft.trackmap.core.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.databinding.CustomScalarViewBinding

/**
 * Custom view to show scalar values with an icon and an unit: Altitude in m or Speed in Km/h
 */
class ScalarView : ConstraintLayout {
    private val binding = CustomScalarViewBinding.inflate(LayoutInflater.from(context), this)

    var name: String? = null
        set(value) {
            field = value
            binding.customScalarViewName.text = value
        }

    var value: String? = null
        set(value) {
            field = value
            binding.customScalarViewValue.text = value
        }

    var maxValue: String? = null
        set(value) {
            field = value
            if (field != null && unit != null) {
                val maxValue = "(Max)\n$value $unit"
                binding.customScalarViewMaxValue.text = maxValue
            }
        }

    var maxValueHidden: Boolean = false
        set(value) {
            field = value
            val visibility = if (value) View.GONE else View.VISIBLE
            binding.customScalarViewMaxValue.visibility = visibility
        }

    var unit: String? = null
        set(value) {
            field = value
            binding.customScalarViewUnit.text = value
        }

    var drawable: Drawable? = null
        set(value) {
            field = value
            binding.customScalarViewValue.setCompoundDrawablesWithIntrinsicBounds(value, null, null, null)
            binding.customScalarViewValue.compoundDrawablePadding = 14
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.ScalarView, defStyle, 0).apply {
            name = this.getString(R.styleable.ScalarView_scalarName)
            unit = this.getString(R.styleable.ScalarView_scalarUnit)
            value = this.getString(R.styleable.ScalarView_scalarValue)
            maxValue = this.getString(R.styleable.ScalarView_scalarMaxValue)
            maxValueHidden = this.getBoolean(R.styleable.ScalarView_scalarMaxValueHidden, false)
            drawable = this.getDrawable(R.styleable.ScalarView_scalarDrawable)

            binding.customScalarViewValue.compoundDrawablePadding = this.getDimensionPixelSize(R.styleable.ScalarView_android_drawablePadding, 16)
        }.recycle()
    }
}
