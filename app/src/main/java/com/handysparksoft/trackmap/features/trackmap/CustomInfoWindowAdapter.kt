package com.handysparksoft.trackmap.features.trackmap

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.handysparksoft.trackmap.R

internal class CustomInfoWindowAdapter(
    activity: Activity,
    private val onRenderMarkerWindowInfo: (windowInfo: View, marker: Marker) -> Unit
) : GoogleMap.InfoWindowAdapter {
    private val windowInfo: View = activity.layoutInflater.inflate(
        R.layout.custom_info_window, null
    )

    override fun getInfoWindow(marker: Marker): View? {
        return if (marker.tag != CUSTOM_LOCATED_MARKER_TAG) {
            onRenderMarkerWindowInfo(windowInfo, marker)
            windowInfo
        } else null
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    companion object {
        const val CUSTOM_LOCATED_MARKER_TAG = "CustomMarker"
    }
}