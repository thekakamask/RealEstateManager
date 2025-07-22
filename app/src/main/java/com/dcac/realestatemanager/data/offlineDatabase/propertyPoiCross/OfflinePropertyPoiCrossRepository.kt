package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflinePropertyPoiCrossRepository(
    private val dao: PropertyPoiCrossDao
) : PropertyPoiCrossRepository {

    override suspend fun insertCrossRef(crossRef: PropertyPoiCross) {
        dao.insertCrossRef(crossRef.toEntity())
    }

    override suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCross>) {
        dao.insertAllCrossRefs(crossRefs.map { it.toEntity() })
    }

    override suspend fun deleteCrossRefsForProperty(propertyId: Long) {
        dao.deleteCrossRefsForProperty(propertyId)
    }

    override suspend fun deleteCrossRefsForPoi(poiId: Long) {
        dao.deleteCrossRefsForPoi(poiId)
    }

    override suspend fun clearAllCrossRefs() {
        dao.clearAllCrossRefs()
    }

    override fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCross>> {
        return dao.getCrossRefsForProperty(propertyId).map { list ->
            list.map { it.toModel() }
        }
    }

    override fun getAllCrossRefs(): Flow<List<PropertyPoiCross>> {
        return dao.getAllCrossRefs().map { list ->
            list.map { it.toModel() }
        }
    }

    override fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>> {
        return dao.getPoiIdsForProperty(propertyId)
    }

    override fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>> {
        return dao.getPropertyIdsForPoi(poiId)
    }
}
