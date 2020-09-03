package com.handysparksoft.trackmap.core.platform

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar
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
        val snackbar = Snackbar.make(
            snackView ?: activity.window.decorView,
            "Trackmap needs location permission to work correctly",
            Snackbar.LENGTH_SHORT
        )
        if (showSettingsButton) {
            snackbar.setAction("Settings") {
                requestLocationPermission(onGrantedPermission)
            }
        }
        snackbar.show()
    }
}
