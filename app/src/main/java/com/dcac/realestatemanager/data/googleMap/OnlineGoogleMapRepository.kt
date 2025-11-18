package com.dcac.realestatemanager.data.googleMap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OnlineGoogleMapRepository(
    private val context: Context,
    private val propertyRepository: PropertyRepository,
    private val poiRepository: PoiRepository
) : GoogleMapRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // Assume permission checked outside this method
    override suspend fun getUserLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun geocodeAddress(address: String): Location? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    val geoResult = results.first()
                    Location("").apply {
                        latitude = geoResult.latitude
                        longitude = geoResult.longitude
                    }
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun getAllProperties(): Flow<List<Property>> =
        propertyRepository.getAllPropertiesByDate()

    override fun getAllPoiS(): Flow<List<Poi>> =
        poiRepository.getAllPoiS()


}