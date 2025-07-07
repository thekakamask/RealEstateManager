package com.dcac.realestatemanager.data.googleMap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyRepository
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

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

    override fun getAllProperties(): Flow<List<Property>> =
        propertyRepository.getAllPropertiesByDate()

    override fun getAllPoiS(): Flow<List<Poi>> =
        poiRepository.getAllPoiS()
}