package com.dcac.realestatemanager.data.googleMap

import android.location.Location
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiEntity
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyEntity
import kotlinx.coroutines.flow.Flow

interface GoogleMapRepository {
    suspend fun getUserLocation(): Location?
    fun getAllProperties(): Flow<List<PropertyEntity>>
    fun getAllPoi(): Flow<List<PoiEntity>>
}