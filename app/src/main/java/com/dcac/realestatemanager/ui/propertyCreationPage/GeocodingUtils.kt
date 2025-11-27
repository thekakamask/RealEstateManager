package com.dcac.realestatemanager.ui.propertyCreationPage

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LatLng(val latitude: Double, val longitude: Double)

suspend fun geocodeAddress(context: Context, address: String): LatLng? = withContext(Dispatchers.IO) {
    return@withContext try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(address, 1)
        if (!results.isNullOrEmpty()) {
            val location = results[0]
            LatLng(location.latitude, location.longitude)
        } else null
    } catch (e: Exception) {
        null
    }
}