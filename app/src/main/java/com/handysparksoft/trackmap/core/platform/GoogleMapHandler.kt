package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.logError
import javax.inject.Inject

class GoogleMapHandler @Inject constructor(private val context: Context) {

    /*const val MARKER_ICON_DEFAULT_GREEN = R.drawable.ic_marker_green
    const val MARKER_ICON_DIAMOND_GREEN = R.drawable.ic_marker_diamond_green
    const val MARKER_ICON_DUCK_GREEN = R.drawable.ic_marker_duck_green
    const val MARKER_ICON_EYE_RED = R.drawable.ic_marker_eye_red
    const val MARKER_ICON_HEART_PINK = R.drawable.ic_marker_heart_pink
    const val MARKER_ICON_HEXAGON_RED = R.drawable.ic_marker_hexagon_red
    const val MARKER_ICON_INCLINED_BLACK = R.drawable.ic_marker_inclined_black
    const val MARKER_ICON_SQUARE_ORANGE = R.drawable.ic_marker_square_orange
    const val MARKER_ICON_STAR_YELLOW = R.drawable.ic_marker_star_yellow
    const val MARKER_ICON_TRIANGLE_BROWN = R.drawable.ic_marker_triangle_brown

    const val MARKER_ICON_FLAG_RED = R.drawable.ic_flag_red
    const val MARKER_ICON_FLAG_GREEN = R.drawable.ic_flag_green
    const val MARKER_ICON_FLAG_BLUE = R.drawable.ic_flag_blue
    const val MARKER_ICON_FLAG_YELLOW = R.drawable.ic_flag_yellow
    const val MARKER_ICON_FLAG_PINK = R.drawable.ic_flag_pink*/

    private lateinit var googleMap: GoogleMap

    fun initialize(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    fun addMarker(latlng: LatLng, text: String, icon: Int = R.drawable.ic_marker_green) {
        this.googleMap.addMarker(
            MarkerOptions().position(latlng).title(
                text
            )
                .icon(
                    getBitmapFromVector(context, icon)
                )
            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    /**
     * Maps
     */
    private fun getBitmapFromVector(
        context: Context,
        @DrawableRes vectorResourceId: Int,
        @ColorInt tintColor: Int? = null
    ): BitmapDescriptor {

        val vectorDrawable = ResourcesCompat.getDrawable(
            context.resources, vectorResourceId, null
        )
        if (vectorDrawable == null) {
            logError("Requested vector resource was not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        tintColor?.let {
            DrawableCompat.setTint(vectorDrawable, tintColor)
        }
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
