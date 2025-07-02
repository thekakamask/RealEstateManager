package com.openclassrooms.realestatemanager.data.poi

import kotlinx.coroutines.flow.Flow

class OfflinePoiRepository(
    private val poiDao: PoiDao
): PoiRepository {
    override fun getPoiForProperty(propertyId: Long): Flow<List<PoiEntity>>
    = poiDao.getPoiForProperty(propertyId)

    override suspend fun insertAll(poi: List<PoiEntity>)
    = poiDao.insertAll(poi)

    override suspend fun deletePoiForProperty(propertyId: Long)
    = poiDao.deletePoiForProperty(propertyId)

    override suspend fun deletePoi(poi: PoiEntity)
    = poiDao.deletePoi(poi)
}