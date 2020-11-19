package com.handysparksoft.trackmap.core.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.Constraints.LayoutParams
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.logError
import javax.inject.Inject


class GoogleMapHandler @Inject constructor(private val context: Context) {

    companion object {
        const val MARKER_ICON_DEFAULT_GREEN = R.drawable.ic_marker_green
        const val MARKER_ICON_DIAMOND_GREEN = R.drawable.ic_marker_diamond_green
        const val MARKER_ICON_DUCK_GREEN = R.drawable.ic_marker_duck_green
        const val MARKER_ICON_EYE_RED = R.drawable.ic_marker_eye_red
        const val MARKER_ICON_HEART_PINK = R.drawable.ic_marker_heart_pink
        const val MARKER_ICON_HEXAGON_RED = R.drawable.ic_marker_hexagon_red
        const val MARKER_ICON_INCLINED_BLACK = R.drawable.ic_marker_inclined_black
        const val MARKER_ICON_SQUARE_ORANGE = R.drawable.ic_marker_square_orange
        const val MARKER_ICON_STAR_YELLOW = R.drawable.ic_marker_star_yellow
        const val MARKER_ICON_TRIANGLE_BROWN = R.drawable.ic_marker_triangle_brown

        const val MARKER_ICON_DEFAULT_GREEN_BIG = R.drawable.ic_marker_green_person_42
        const val MARKER_ICON_DUCK_GREEN_BIG = R.drawable.ic_marker_duck_green_42
        const val MARKER_ICON_EYE_RED_BIG = R.drawable.ic_marker_eye_red_42
        const val MARKER_ICON_HEART_PINK_BIG = R.drawable.ic_marker_heart_pink_42
        const val MARKER_ICON_HEXAGON_RED_BIG = R.drawable.ic_marker_hexagon_red_42

        const val MARKER_ICON_FLAG_RED = R.drawable.ic_flag_red
        const val MARKER_ICON_FLAG_GREEN = R.drawable.ic_flag_green
        const val MARKER_ICON_FLAG_BLUE = R.drawable.ic_flag_blue
        const val MARKER_ICON_FLAG_YELLOW = R.drawable.ic_flag_yellow
        const val MARKER_ICON_FLAG_PINK = R.drawable.ic_flag_pink
    }

    private lateinit var googleMap: GoogleMap
    private val availableMarkers = mutableListOf(
        MARKER_ICON_DEFAULT_GREEN,
        MARKER_ICON_DIAMOND_GREEN,
        MARKER_ICON_DUCK_GREEN,
        MARKER_ICON_EYE_RED,
        MARKER_ICON_HEART_PINK,
        MARKER_ICON_HEXAGON_RED,
        MARKER_ICON_INCLINED_BLACK,
        MARKER_ICON_SQUARE_ORANGE,
        MARKER_ICON_STAR_YELLOW,
        MARKER_ICON_TRIANGLE_BROWN
    )
    private val availableBigMarkers = mutableListOf(
        MARKER_ICON_DEFAULT_GREEN_BIG,
        MARKER_ICON_DUCK_GREEN_BIG,
        MARKER_ICON_EYE_RED_BIG,
        MARKER_ICON_HEART_PINK_BIG,
        MARKER_ICON_HEXAGON_RED_BIG
    )
    private val markerWithImages = true

    fun initialize(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

    fun addMarker(
        latLng: LatLng,
        title: String,
        snippet: String?,
        icon: Int? = null,
        encodedImage: String?
    ): Marker {
        val markerOption = MarkerOptions()
            .position(latLng)
            .title(title)
            .snippet(snippet)

        if (icon != null) {
            if (markerWithImages && encodedImage != null) {
                markerOption.icon(getBitmapFromEncodedImage(context, encodedImage, icon))
            } else {
                markerOption.icon(getBitmapFromVector(context, icon))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            }
        }

        return this.googleMap.addMarker(markerOption)
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
            context.logError("Requested vector resource was not found")
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


    private fun getBitmapFromEncodedImage(
        context: Context,
        encodedImage: String,
        @DrawableRes vectorResourceId: Int,
    ): BitmapDescriptor {
        val bitmapImage = Base64Utils.getBase64Bitmap(encodedImage)

        val markerPersonLayout = LayoutInflater.from(context).inflate(
            R.layout.marker_person_layout,
            null
        )

        markerPersonLayout.findViewById<ImageView>(R.id.markerPersonPictureImageView).apply {
            setImageBitmap(bitmapImage)
        }
        markerPersonLayout.findViewById<ImageView>(R.id.markerPersonMarkerImageView).apply {
            setImageResource(vectorResourceId)
        }
        val loadBitmapFromView = loadBitmapFromView(markerPersonLayout)

        return BitmapDescriptorFactory.fromBitmap(loadBitmapFromView)
    }

    private fun loadBitmapFromView(view: View): Bitmap? {
        view.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        // return view.drawToBitmap( Bitmap.Config.ARGB_8888)
        return bitmap
    }

    fun getRandomMarker(
        ignoreList: List<Int> = listOf(
            MARKER_ICON_DEFAULT_GREEN,
            MARKER_ICON_DEFAULT_GREEN_BIG
        )
    ): Int {
        val markerList = if (markerWithImages) availableBigMarkers else availableMarkers
        val defaultMarker = if (markerWithImages) MARKER_ICON_EYE_RED_BIG else MARKER_ICON_EYE_RED
        return markerList.filter { !ignoreList.contains(it) }.shuffled().firstOrNull()
            ?: defaultMarker
    }

    fun getDefaultUserMarker(): Int {
        return if (markerWithImages) MARKER_ICON_DEFAULT_GREEN_BIG else MARKER_ICON_DEFAULT_GREEN
    }
}
