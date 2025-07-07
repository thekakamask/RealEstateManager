package com.dcac.realestatemanager.data.offlinedatabase.poi

import com.dcac.realestatemanager.model.Poi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePoiRepository(
    private val poiDao: PoiDao
): PoiRepository {

    // Returns a flow of all POI entities stored in the database.
    override fun getAllPoiS(): Flow<List<Poi>> =
        poiDao.getAllPoiS().map { list -> list.map { it.toModel() } }

    // Returns a flow of business model POIs linked to the specified property ID.
    override fun getPoiSByPropertyId(propertyId: Long): Flow<List<Poi>> =
        poiDao.getPoiSForProperty(propertyId).map { list -> list.map { it.toModel() } }

    // Inserts single POI
    override suspend fun insertPoi(poi: PoiEntity) = poiDao.insertPoi(poi)

    // Inserts a list of POI entities into the database.
    override suspend fun insertAllPoiS(poiS: List<PoiEntity>)= poiDao.insertAllPoiS(poiS)

    // Deletes all POIs associated with the specified property ID.
    override suspend fun deletePoiSForProperty(propertyId: Long) = poiDao.deletePoiSForProperty(propertyId)

    // Deletes a specific POI entity from the database.
    override suspend fun deletePoi(poi: PoiEntity) = poiDao.deletePoi(poi)
}