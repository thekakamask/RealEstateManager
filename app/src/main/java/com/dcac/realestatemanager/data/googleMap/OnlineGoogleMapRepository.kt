package com.dcac.realestatemanager.data.googleMap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

class OnlineGoogleMapRepository(
    private val context: Context,
    private val propertyRepository: PropertyRepository,
    private val poiRepository: PoiRepository
) : GoogleMapRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun getUserLocation(): Location? {
        println("📍 getUserLocation() called")
        return try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(2000)
                .setMaxUpdates(1)
                .build()

            withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            println("✅ onLocationResult called")
                            fusedLocationClient.removeLocationUpdates(this)

                            val location = result.lastLocation
                            println("📍 Location obtained via updates: $location")

                            if (!continuation.isCompleted) {
                                continuation.resume(location, null)
                            }
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )

                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }


                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(3000)
                        fusedLocationClient.lastLocation.addOnSuccessListener { fallbackLocation ->
                            if (!continuation.isCompleted && fallbackLocation != null) {
                                println("📍 Fallback location after delay: $fallbackLocation")
                                continuation.resume(fallbackLocation, null)
                                fusedLocationClient.removeLocationUpdates(locationCallback)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getAllProperties(): Flow<List<Property>> =
        propertyRepository.getAllPropertiesByDate()

    override fun getAllPoiS(): Flow<List<Poi>> =
        poiRepository.getAllPoiS()


}