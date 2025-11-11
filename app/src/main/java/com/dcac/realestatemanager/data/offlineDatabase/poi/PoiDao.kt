package com.dcac.realestatemanager.data.offlineDatabase.poi

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface PoiDao {

    //QUERIES (FILTERED ON IS_DELETE = 0)
    //FOR UI
    @Query("SELECT * FROM poi WHERE id = :id AND is_deleted = 0")
    fun getPoiById(id: String): Flow<PoiEntity?>
    @Query("SELECT * FROM poi WHERE is_deleted = 0")
    fun getAllPoiS(): Flow<List<PoiEntity>>

    //SYNC
    @Query("SELECT * FROM poi WHERE is_synced = 0")
    fun uploadUnSyncedPoiS(): Flow<List<PoiEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPoiInsertFromUi(poi: PoiEntity): String{
        firstPoiInsert(poi.copy(isSynced = false))
        return poi.id
    }
    suspend fun insertPoiSInsertFromUi(poiS: List<PoiEntity>){
        poiS.forEach { poi ->
            firstPoiInsert(poi.copy(isSynced = false))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstPoiInsert(poi: PoiEntity)

    //INSERTIONS FROM FIREBASE
    suspend fun insertPoiInsertFromFirebase(poi: PoiEntity) : String?{
        insertPoiIfNotExists(poi.copy(isSynced = true))
        return poi.firestoreDocumentId
    }
    suspend fun insertAllPoiSNotExistingFromFirebase(poiS: List<PoiEntity>){
        poiS.forEach { poi ->
            insertPoiIfNotExists(poi.copy(isSynced = true))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPoiIfNotExists(poi: PoiEntity)

    //UPDATE
    //WHEN POI IS UPDATE FROM A PHONE (UI → ROOM, will need to sync later)
    suspend fun updatePoiFromUIForceSyncFalse(poi: PoiEntity): String {
        updatePoi(poi.copy(isSynced = false))
        return poi.id
    }
    // WHEN FIREBASE SENDS AN UPDATED SINGLE OR MULTIPLE POIS TO ROOM
    suspend fun updatePoiFromFirebaseForceSyncTrue(poi: PoiEntity): String? {
        updatePoi(poi.copy(isSynced = true))
        return poi.firestoreDocumentId
    }
    suspend fun updateAllPoiFromFirebaseForceSyncTrue(poiS: List<PoiEntity>) {
        poiS.forEach { photo ->
            updatePoi(photo.copy(isSynced = true))
        }
    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePoi(photo: PoiEntity)

    //SOFT DELETE
    //MARK FROM UI POIS AS DELETED BEFORE REAL DELETE
    @Query("UPDATE poi SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPoiAsDeleted(id: String, updatedAt: Long)

    //HARD DELETE
    //AFTER DELETE POI FROM FIREBASE, DELETE POI FROM ROOM
    @Delete
    suspend fun deletePoi(poi: PoiEntity)
    @Query("DELETE FROM poi WHERE is_deleted = 1")
    suspend fun clearAllPoiSDeleted()

    //FOR TEST CHECK HARD DELETE
    @Query("SELECT * FROM poi WHERE id = :id")
    fun getPoiByIdIncludeDeleted(id: String): Flow<PoiEntity?>
    @Query("SELECT * FROM poi")
    fun getAllPoiSIncludeDeleted(): Flow<List<PoiEntity>>

    @Transaction
    @Query("SELECT * FROM poi WHERE id = :poiId AND is_deleted = 0")
    fun getPoiWithProperties(poiId: String): Flow<PoiWithPropertiesRelation>

    @RawQuery(observedEntities = [PoiEntity::class])
    fun getAllPoiSAsCursor(query: SupportSQLiteQuery): Cursor

}