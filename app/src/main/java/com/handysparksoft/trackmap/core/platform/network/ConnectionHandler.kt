package com.handysparksoft.trackmap.core.platform.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.handysparksoft.trackmap.core.extension.logDebug
import javax.inject.Inject

class ConnectionHandler @Inject constructor(private val context: Context) {
    companion object {
        var isNetworkAvailable = false
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isNetworkAvailable = true
        }

        override fun onLost(network: Network) {
            isNetworkAvailable = false
        }
    }

    fun registerNetworkCallback() {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            context.logDebug("No NetworkCallback registered yet!")
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun isNetworkAvailable() = isNetworkAvailable
}

