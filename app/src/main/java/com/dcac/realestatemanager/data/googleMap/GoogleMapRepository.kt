package com.dcac.realestatemanager.data.googleMap

import android.location.Location
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.Flow

interface GoogleMapRepository {
    suspend fun getUserLocation(): Location?
    fun getAllProperties(): Flow<List<Property>>
    fun getAllPoiS(): Flow<List<Poi>>
}