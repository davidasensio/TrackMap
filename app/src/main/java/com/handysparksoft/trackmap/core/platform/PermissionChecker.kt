package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.handysparksoft.trackmap.R
import com.handysparksoft.trackmap.core.extension.SnackbarType
import com.handysparksoft.trackmap.core.extension.snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


class PermissionChecker(private val activity: Activity, private val snackView: View? = null) {
    private var showSettingsButton = true

    fun requestLocationPermission(onGrantedPermission: () -> Unit) {
        Dexter.withActivity(activity)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    onGrantedPermission()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response?.isPermanentlyDenied == true) {
                         showSettingsButton = false
                    }
                    showSnackbarMessage(onGrantedPermission)
                }
            })
            .check()
    }

    private fun showSnackbarMessage(onGrantedPermission: () -> Unit) {
        val actionListener = if (showSettingsButton) {
            { _: View -> requestLocationPermission(onGrantedPermission) }
        } else {
            null
        }
        (snackView ?: activity.window.decorView).snackbar(
            message = activity.getString(R.string.permission_need),
            length = BaseTransientBottomBar.LENGTH_INDEFINITE,
            type = SnackbarType.WARNING,
            actionResId = R.string.settings,
            actionListener = actionListener
        )
    }
}
