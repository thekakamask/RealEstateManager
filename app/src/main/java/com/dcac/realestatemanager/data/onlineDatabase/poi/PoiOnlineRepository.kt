package com.dcac.realestatemanager.data.onlineDatabase.poi

import com.dcac.realestatemanager.model.Poi

interface PoiOnlineRepository {


    suspend fun uploadPoi(poi: Poi, poiId: String): Poi

    suspend fun getPoi(poiId: String): Poi?

    suspend fun getAllPoiS(): List<Poi>

    suspend fun deletePoi(poiId: String)
}