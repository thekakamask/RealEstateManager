package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import android.content.Context
import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.model.StaticMap
import com.dcac.realestatemanager.network.StaticMapApiService
import com.dcac.realestatemanager.utils.Utils
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class OfflineStaticMapRepository(
    private val staticMapApiService: StaticMapApiService,
    private val staticMapDao: StaticMapDao
): StaticMapRepository, StaticMapDataSource {

    // Call API retrofit to get map image
    override suspend fun getStaticMapImage(config: StaticMapConfig): ByteArray? {
        return try {
            val response = staticMapApiService.getStaticMapImage(
                center = config.center,
                zoom = config.zoom,
                size = config.size,
                mapType = config.mapType,
                markers = config.markers,
                style = config.styles
            )
            if (response.isSuccessful) {
                response.body()?.bytes()
            } else {
                Log.e(
                    "StaticMapRepository",
                    "Failed response from API: code=${response.code()}, message=${response.message()}"
                )
                null
            }
        } catch (e: IOException) {
            Log.e("StaticMapRepository", "Network error while calling Static Maps API", e)
            null
        }
    }

    // Save all bytes received into a PNG file in the internal storage
    override fun saveStaticMapToLocal(context: Context, fileName: String, bytes: ByteArray): String? {
        return try {
            // Save bytes to internal storage and return a file:// Uri string
            Utils.saveBytesToAppStorage(
                context = context,
                bytes = bytes,
                subDir = "maps",
                fileName = fileName
            )?.toString()

        } catch (e: IOException) {
            Log.e("StaticMapRepository", "Error saving static map locally", e)
            null
        } catch (e: Exception) {
            Log.e("StaticMapRepository", "Unexpected error saving static map locally", e)
            null
        }
    }

    override fun getStaticMapById(id: String): Flow<StaticMap?> =
        staticMapDao.getStaticMapById(id).map { it?.toModel() }


    override fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMap?> =
        staticMapDao.getStaticMapByPropertyId(propertyId).map { it?.toModel() }


    override fun getAllStaticMap(): Flow<List<StaticMap>> =
        staticMapDao.getAllStaticMap().map { list -> list.map { it.toModel() } }


    override fun uploadUnSyncedStaticMapToFirebase(): Flow<List<StaticMapEntity>> =
        staticMapDao.uploadUnSyncedStaticMap()

    //INSERTIONS
    //INSERTIONS FROM UI
    override suspend fun insertStaticMapInsertFromUI(staticMap: StaticMap): String =
        staticMapDao.insertStaticMapInsertFromUI(staticMap.toEntity())

    //INSERTIONS FROM FIREBASE
    override suspend fun insertStaticMapInsertFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String,
        localUri: String
    ) {
        val entity = staticMap.toEntity(firestoreId = firestoreId).copy(uri = localUri)
        staticMapDao.insertStaticMapInsertFromFirebase(entity)
    }

    //UPDATE
    override suspend fun updateStaticMapFromUI(staticMap: StaticMap) {
        staticMapDao.updateStaticMapFromUIForceSyncFalse(staticMap.toEntity())
    }

    override suspend fun updateStaticMapFromFirebase(
        staticMap: StaticMapOnlineEntity,
        firestoreId: String
    ) {
        val existing = staticMapDao
            .getStaticMapByIdIncludeDeleted(staticMap.universalLocalId)
            .first()

        val preservedUri = existing?.uri ?: ""

        staticMapDao.updateStateMapFromFirebaseForceSyncTrue(
            staticMap.toEntity(firestoreId = firestoreId)
                .copy(uri = preservedUri)
        )
    }

    //SOFT DELETE
    override suspend fun markStaticMapAsDeleted(staticMap: StaticMap) {
        staticMapDao.markStaticMapAsDeleted(staticMap.universalLocalId, System.currentTimeMillis())
    }

    override suspend fun markStaticMapAsDeletedByProperty(propertyId: String) {
        staticMapDao.markStaticMapsAsDeletedByProperty(propertyId, System.currentTimeMillis())
    }

    //HARD DELETE
    override suspend fun deleteStaticMapByPropertyId(propertyId: String) {
        staticMapDao.deleteStaticMapByPropertyId(propertyId)
    }

    override suspend fun deleteStaticMap(staticMap: StaticMapEntity) {
        staticMapDao.deleteStaticMap(staticMap)
    }

    override suspend fun clearAllStaticMapsDeleted() {
        staticMapDao.clearAllStaticMapsDeleted()
    }

    //FOR TEST HARD DELETE CHECK
    override fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?> =
        staticMapDao.getStaticMapByIdIncludeDeleted(id)

    override fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?> =
        staticMapDao.getStaticMapByPropertyIdIncludeDeleted(propertyId)

    override fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>> =
        staticMapDao.getAllStaticMapIncludeDeleted()

}