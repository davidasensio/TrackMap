package com.handysparksoft.trackmap.core.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.databinding.CustomBatteyLevelViewBinding
import kotlin.math.abs

/**
 * Custom view to show battery level value in %
 */
class BatteryLevelView : FrameLayout {
    private val binding = CustomBatteyLevelViewBinding.inflate(LayoutInflater.from(context), this)

    var level: Int = 100
        set(value) {
            val aux = abs(value)
            field = if (aux > 100) 100 else aux
            updateBatteryLevel(field)
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
        level = 100
    }

    @SuppressLint("SetTextI18n")
    private fun updateBatteryLevel(level: Int) {
        val iconResource = when {
            level > 75 -> R.drawable.ic_battery_100
            level > 50 -> R.drawable.ic_battery_75
            level > 25 -> R.drawable.ic_battery_50
            level > 10 -> R.drawable.ic_battery_25
            level > 5 -> R.drawable.ic_battery_10
            else -> R.drawable.ic_battery_0
        }
        val iconTintResource = when {
            level <= 10 -> R.color.colorNotification
            else -> TypedValue().let { typedValue ->
                context.theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true)
                typedValue.resourceId
            }
        }
        binding.customBatteryLevel.setIconResource(iconResource)
        binding.customBatteryLevel.setIconTintResource(iconTintResource)
        binding.customBatteryLevel.text = "$level%"
    }
}
