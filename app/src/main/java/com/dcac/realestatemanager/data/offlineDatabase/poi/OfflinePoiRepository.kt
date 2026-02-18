package com.dcac.realestatemanager.data.offlineDatabase.poi

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.PoiWithProperties
import com.dcac.realestatemanager.utils.Utils.normalize
import com.dcac.realestatemanager.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePoiRepository(
    private val poiDao: PoiDao
): PoiRepository {

    // FOR UI
    override fun getPoiById(id: String): Flow<Poi?> =
        poiDao.getPoiById(id).map { it?.toModel() }
    override fun getAllPoiS(): Flow<List<Poi>> =
        poiDao.getAllPoiS().map { list -> list.map { it.toModel() } }

    //SYNC
    override fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>> =
        poiDao.uploadUnSyncedPoiS()

    // INSERTIONS
    override suspend fun insertPoiInsertFromUI(poi: Poi): Poi {

        val normalizedName = poi.name.normalize()
        val normalizedAddress = poi.address.normalize()

        val existing = poiDao.findExistingPoi(
            name = normalizedName,
            address = normalizedAddress
        )

        return if (existing != null) {
            existing.toModel()
        } else {
            val entity = poi.toEntity().copy(
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            poiDao.insertPoiInsertFromUi(entity)
            entity.toModel()
        }
    }
    override suspend fun insertPoiSInsertFromUi(poiS: List<Poi>): List<Poi> {
        return poiS.map { insertPoiInsertFromUI(it) }
    }
    //INSERTIONS FROM FIREBASE
    override suspend fun insertPoiInsertFromFirebase(poi: PoiOnlineEntity, firebaseDocumentId: String) {
        poiDao.insertPoiInsertFromFirebase(poi.toEntity(firestoreId = firebaseDocumentId))
    }
    override suspend fun insertPoiSInsertFromFirebase(poiS: List<Pair<PoiOnlineEntity, String>>) {
        val entities = poiS.map {(poi, firebaseDocumentId) ->
            poi.toEntity(firestoreId = firebaseDocumentId)
        }
        poiDao.insertAllPoiSNotExistingFromFirebase(entities)
    }

    //UPDATE
    override suspend fun updatePoiFromUI(poi: Poi) {
        poiDao.updatePoiFromUIForceSyncFalse(poi.toEntity())
    }
    override suspend fun updatePoiFromFirebase(poi: PoiOnlineEntity, firebaseDocumentId: String) {
        poiDao.updatePoiFromFirebaseForceSyncTrue(poi.toEntity(firestoreId = firebaseDocumentId))
    }

    override suspend fun updateAllPoiSFromFirebase(poiS: List<Pair<PoiOnlineEntity, String>>) {
        val entities = poiS.map { (poi, firebaseDocumentId) ->
            poi.toEntity(firestoreId = firebaseDocumentId)
        }
        poiDao.updateAllPoiFromFirebaseForceSyncTrue(entities)
    }

    //SOFT DELETE
    override suspend fun markPoiAsDeleted(poi: Poi) {
        poiDao.markPoiAsDeleted(poi.universalLocalId, System.currentTimeMillis())
    }

    //HARD DELETE
    override suspend fun deletePoi(poi: PoiEntity) {
        poiDao.deletePoi(poi)
    }
    override suspend fun clearAllPoiSDeleted() {
        poiDao.clearAllPoiSDeleted()
    }

    // FOR SYNC AND TEST CHECK
    override fun getPoiByIdIncludeDeleted(id: String): Flow<PoiEntity?> =
        poiDao.getPoiByIdIncludeDeleted(id)
    override fun getAllPoiIncludeDeleted(): Flow<List<PoiEntity>> =
        poiDao.getAllPoiSIncludeDeleted()

    override fun getPoiWithProperties(poiId: String): Flow<PoiWithProperties> {
        return poiDao.getPoiWithProperties(poiId).map { relation ->
            relation.toModel()
        }
    }

}