package com.dcac.realestatemanager.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

//Utility class to check if the device is currently connected to the Internet.
//Supports API 21+ (Lollipop and above).
class NetworkMonitor(private val context: Context) {

    //Returns true if the device has an active internet connection.
    fun isConnected(): Boolean {

        // Get system service responsible for managing network connections
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // ✅ For API 23 and above: use the modern NetworkCapabilities API
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            // Check if the network has the capability to access the Internet
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {

            // ✅ For API 21–22: use the deprecated but still functional API
            val networkInfo = connectivityManager.activeNetworkInfo

            // Check if there is an active network and it is connected
            networkInfo != null && networkInfo.isConnected
        }
    }
}
