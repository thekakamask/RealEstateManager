package com.dcac.realestatemanager.utils

import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Utility class for currency conversion, date formatting and network checks.

object Utils {

    /**
     * 💱 Converts property price from USD to EUR.
     * NOTE: Must be shown during the exam.
     * @param dollars Amount in USD.
     * @return Converted amount in EUR.
     */

    fun convertDollarToEuro(dollars: Int): Int {
        return (dollars * 0.812).roundToInt()
    }

    fun convertEuroToDollar(euros: Int): Int {
        return (euros / 0.812).roundToInt()
    }

    /**
     * 📅 Returns today's date in "yyyy/MM/dd" format.
     * NOTE: Must be shown during the exam.
     * @return Formatted date string.
     */
    fun getTodayDate(): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(Date())
    }

    fun getTodayDateLegacy(): String = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())


    /**
     * 🌐 Checks if device has an active internet connection (Wi-Fi or cellular).
     * Compatible with API 21+.
     */
    @Deprecated("Use NetworkMonitor.isConnected() instead")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }


    fun saveUriToAppStorage(context: Context, uri: Uri): File? {
        return try {
            val resolver: ContentResolver = context.contentResolver
            val inputStream: InputStream = resolver.openInputStream(uri) ?: return null

            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}