package com.dcac.realestatemanager.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import com.dcac.realestatemanager.R
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
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

    fun LocalDate.formatToDisplay(pattern: String = "dd/MM/yyyy"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return this.format(formatter)
    }

    fun calculatePricePerSquareMeter(price: Int, surface: Int): Int {
        return if (surface > 0) (price / surface) else 0
    }

    fun getIconForPropertyType(type: String): Int {
        return when (type) {
            "House" -> R.drawable.house_24px
            "Apartment" -> R.drawable.apartment_24px
            "Studio" -> R.drawable.studio_24px
            "Boat" -> R.drawable.houseboat_24px
            "Cabin" -> R.drawable.cabin_24px
            "Castle" -> R.drawable.castle_24px
            "Motor home" -> R.drawable.motor_home_24px
            else -> R.drawable.close_24px
        }
    }

    @DrawableRes
    fun getIconForPoiType(type: String): Int {
        return when (type) {
            "School" -> R.drawable.school_24px
            "Grocery" -> R.drawable.grocery_24px
            "Bakery" -> R.drawable.bakery_24px
            "Butcher" -> R.drawable.butcherl_24px
            "Restaurant" -> R.drawable.restaurant_24px
            else -> R.drawable.close_24px
        }
    }

    fun getColorForPoiType(type: String): Int = when (type) {
        "School" -> Color.CYAN
        "Grocery" -> Color.GREEN
        "Bakery" -> Color.YELLOW
        "Butcher" -> Color.RED
        "Restaurant" -> Color.MAGENTA
        else -> Color.GRAY
    }

}