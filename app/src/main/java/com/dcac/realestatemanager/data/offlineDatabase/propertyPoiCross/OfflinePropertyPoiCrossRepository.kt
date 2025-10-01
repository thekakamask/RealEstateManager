package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.model.PropertyPoiCross
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflinePropertyPoiCrossRepository(
    private val dao: PropertyPoiCrossDao
) : PropertyPoiCrossRepository {

    //FOR UI

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

    override fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCross?> {
        return dao.getCrossByIds(propertyId, poiId).map { it?.toModel() }
    }

    override suspend fun insertCrossRef(crossRef: PropertyPoiCross) {
        dao.insertCrossRef(crossRef.toEntity())
    }

    override suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCross>) {
        dao.insertAllCrossRefs(crossRefs.map { it.toEntity() })
    }

    override suspend fun updateCrossRef(crossRef: PropertyPoiCross) {
        dao.updateCrossRef(crossRef.toEntity())
    }

    override suspend fun markCrossRefAsDeleted(propertyId: Long, poiId: Long) {
        dao.markCrossRefAsDeleted(propertyId, poiId, System.currentTimeMillis())
    }

    override suspend fun markCrossRefsAsDeletedForProperty(propertyId: Long) {
        dao.markCrossRefsAsDeletedForProperty(propertyId, System.currentTimeMillis())
    }

    override suspend fun markCrossRefsAsDeletedForPoi(poiId: Long) {
        dao.markCrossRefsAsDeletedForPoi(poiId, System.currentTimeMillis())
    }

    override suspend fun markAllCrossRefsAsDeleted() {
        dao.markAllCrossRefsAsDeleted(System.currentTimeMillis())
    }

    // FOR FIREBASE SYNC

    override fun getCrossEntityByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?> {
        return dao.getCrossByIds(propertyId, poiId)
    }

    override suspend fun deleteCrossRefsForProperty(propertyId: Long) {
        dao.deleteCrossRefsForProperty(propertyId)
    }

    override suspend fun deleteCrossRefsForPoi(poiId: Long) {
        dao.deleteCrossRefsForPoi(poiId)
    }

    override suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity) {
        dao.deleteCrossRef(crossRef)
    }

    override suspend fun clearAllDeleted() {
        dao.clearAllDeleted()
    }

    override fun uploadUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>> {
        return dao.uploadUnSyncedPropertiesPoiSCross()
    }

    override suspend fun downloadCrossRefFromFirebase(crossRef: PropertyPoiCrossOnlineEntity) {
        dao.saveCrossRefFromFirebase(crossRef.toEntity())
    }

    //FOR TEST HARD DELETE
    override fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PropertyPoiCrossEntity>> =
        dao.getCrossRefsByPropertyIdIncludeDeleted(propertyId)

    override fun getCrossRefsByPoiIdIncludeDeleted(poiId: Long): Flow<List<PropertyPoiCrossEntity>> =
        dao.getCrossRefsByPoiIdIncludeDeleted(poiId)

    override fun getCrossRefsByIdsIncludedDeleted(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?> =
        dao.getCrossRefsByIdsIncludedDeleted(propertyId, poiId)

    override fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>> =
        dao.getAllCrossRefsIncludeDeleted()

}
