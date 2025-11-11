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

    override fun getCrossRefsForProperty(propertyId: String): Flow<List<PropertyPoiCross>> {
        return dao.getCrossRefsForProperty(propertyId).map { list ->
            list.map { it.toModel() }
        }
    }
    override fun getPoiIdsForProperty(propertyId: String): Flow<List<String>> {
        return dao.getPoiIdsForProperty(propertyId)
    }
    override fun getPropertyIdsForPoi(poiId: String): Flow<List<String>> {
        return dao.getPropertyIdsForPoi(poiId)
    }
    override fun getAllCrossRefs(): Flow<List<PropertyPoiCross>> {
        return dao.getAllCrossRefs().map { list ->
            list.map { it.toModel() }
        }
    }
    override fun getCrossByIds(propertyId: String, poiId: String): Flow<PropertyPoiCross?> {
        return dao.getCrossByIds(propertyId, poiId).map { it?.toModel() }
    }
    override fun uploadUnSyncedCrossRefsToFirebase(): Flow<List<PropertyPoiCrossEntity>> =
        dao.uploadUnSyncedCrossRefs()

    //INSERTIONS
    //INSERTIONS FROM UI
    override suspend fun insertCrossRefInsertFromUI(crossRef: PropertyPoiCross) {
        dao.insertCrossRefInsertFromUI(crossRef.toEntity())
    }
    override suspend fun insertAllCrossRefsInsertFromUI(crossRefs: List<PropertyPoiCross>) {
        dao.insertAllCrossRefInsertFromUi(crossRefs.map { it.toEntity() })
    }
    override suspend fun insertCrossRefInsertFromFirebase(crossRef: PropertyPoiCrossOnlineEntity, firebaseDocumentId: String) {
        dao.insertCrossRefInsertFromFirebase(crossRef.toEntity(firestoreId = firebaseDocumentId))
    }
    override suspend fun insertAllCrossRefInsertFromFirebase(crossRefs: List<Pair<PropertyPoiCrossOnlineEntity,String>>) {
        val entities = crossRefs.map {(crossRef, firebaseDocumentId) ->
            crossRef.toEntity(firestoreId = firebaseDocumentId)
        }
        dao.insertAllCrossRefNotExistingFromFirebase(entities)
    }

    //UPDATES
    override suspend fun updateCrossRefFromUI(crossRef: PropertyPoiCross) {
        dao.updateCrossRefFromUIForceSyncFalse(crossRef.toEntity())
    }
    override suspend fun updateAllCrossRefsFromUI(crossRefs: List<PropertyPoiCross>) {
        dao.updateAllCrossRefsFromUIForceSyncFalse(crossRefs.map { it.toEntity() })
    }
    override suspend fun updateCrossRefFromFirebase(crossRef: PropertyPoiCrossOnlineEntity, firebaseDocumentId: String) {
       dao.updateCrossRefFromUIForceSyncFalse(crossRef.toEntity(firestoreId = firebaseDocumentId))
    }
    override suspend fun updateAllCrossRefFromFirebase(crossRefs: List<Pair<PropertyPoiCrossOnlineEntity, String>>) {
        val entities = crossRefs.map { (crossRef, firebaseDocumentId) ->
            crossRef.toEntity(firestoreId = firebaseDocumentId)
        }
        dao.updateAllCrossRefFromFirebaseForceSyncTrue(entities)
    }

    //SOFT DELETE
    override suspend fun markCrossRefAsDeleted(propertyId: String, poiId: String) {
        dao.markCrossRefAsDeleted(propertyId, poiId, System.currentTimeMillis())
    }
    override suspend fun markCrossRefsAsDeletedForProperty(propertyId: String) {
        dao.markCrossRefsAsDeletedForProperty(propertyId, System.currentTimeMillis())
    }
    override suspend fun markCrossRefsAsDeletedForPoi(poiId: String) {
        dao.markCrossRefsAsDeletedForPoi(poiId, System.currentTimeMillis())
    }
    override suspend fun markAllCrossRefsAsDeleted() {
        dao.markAllCrossRefsAsDeleted(System.currentTimeMillis())
    }

    //HARD DELETE
    override suspend fun deleteCrossRefsForProperty(propertyId: String) {
        dao.deleteCrossRefsForProperty(propertyId)
    }
    override suspend fun deleteCrossRefsForPoi(poiId: String) {
        dao.deleteCrossRefsForPoi(poiId)
    }
    override suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity) {
        dao.deleteCrossRef(crossRef)
    }
    override suspend fun clearAllDeleted() {
        dao.clearAllDeleted()
    }

    //FOR TEST HARD DELETE
    override fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PropertyPoiCrossEntity>> =
        dao.getCrossRefsByPropertyIdIncludeDeleted(propertyId)
    override fun getCrossRefsByPoiIdIncludeDeleted(poiId: String): Flow<List<PropertyPoiCrossEntity>> =
        dao.getCrossRefsByPoiIdIncludeDeleted(poiId)
    override fun getCrossRefsByIdsIncludedDeleted(propertyId: String, poiId: String): Flow<PropertyPoiCrossEntity?> =
        dao.getCrossRefsByIdsIncludedDeleted(propertyId, poiId)
    override fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>> =
        dao.getAllCrossRefsIncludeDeleted()

}
