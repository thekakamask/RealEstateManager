package com.dcac.realestatemanager.data.offlineDatabase.staticMap

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface StaticMapDao {

    // FOR UI
    @Query("SELECT * FROM static_map WHERE id = :id AND is_deleted = 0")
    fun getStaticMapById(id: String): Flow<StaticMapEntity?>

    @Query("SELECT * FROM static_map WHERE property_id = :propertyId AND is_deleted = 0")
    fun getStaticMapByPropertyId(propertyId: String): Flow<StaticMapEntity?>

    @Query("SELECT * FROM static_map WHERE is_deleted = 0")
    fun getAllStaticMap(): Flow<List<StaticMapEntity>>

    //SYNC
    @Query("SELECT * FROM static_map WHERE is_synced = 0")
    fun uploadUnSyncedStaticMap(): Flow<List<StaticMapEntity>>

    //INSERTIONS FROM UI
    suspend fun insertStaticMapInsertFromUI(staticMap: StaticMapEntity): String{
        firstStaticMapInsert(staticMap.copy(isSynced = false))
        return staticMap.id
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstStaticMapInsert(staticMap: StaticMapEntity)

    //INSERTIONS FROM FIREBASE
    suspend fun insertStaticMapInsertFromFirebase(staticMap: StaticMapEntity): String? {
        insertStaticMapIfNotExists(staticMap.copy(isSynced = true))
        return staticMap.firestoreDocumentId
    }
    /*suspend fun insertAllStaticMapsNotExistingFromFirebase(staticMaps: List<StaticMapEntity>){
        staticMaps.forEach { staticMap ->
            insertStaticMapIfNotExists(staticMap.copy(isSynced = true))
        }
    }*/
    @Insert(onConflict =OnConflictStrategy.IGNORE)
    suspend fun insertStaticMapIfNotExists(staticMap: StaticMapEntity)

    //UPDATE

    suspend fun updateStaticMapFromUIForceSyncFalse(staticMap: StaticMapEntity): String {
        updateStaticMap(staticMap.copy(isSynced = false))
        return staticMap.id
    }

    suspend fun updateStateMapFromFirebaseForceSyncTrue(staticMap: StaticMapEntity): String? {
        updateStaticMap(staticMap.copy(isSynced = true))
        return staticMap.firestoreDocumentId
    }

   /* suspend fun updateAllStaticMapsFromFirebaseForceSyncTrue(staticMaps: List<StaticMapEntity>){
        staticMaps.forEach { staticMap ->
            updateStaticMap(staticMap.copy(isSynced = true))
        }
    }*/
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStaticMap(staticMap: StaticMapEntity)

    //SOFT DELETE
    @Query("UPDATE static_map SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId")
    suspend fun markStaticMapsAsDeletedByProperty(propertyId: String, updatedAt: Long)

    @Query("UPDATE static_map SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markStaticMapAsDeleted(id: String, updatedAt: Long)

    //HARD DELETE
    @Delete
    suspend fun deleteStaticMap(staticMap: StaticMapEntity)
    @Query("DELETE FROM static_map WHERE property_id = :propertyId")
    suspend fun deleteStaticMapByPropertyId(propertyId: String)
    @Query("DELETE FROM static_map WHERE is_deleted = 1")
    suspend fun clearAllStaticMapsDeleted()

    // FOR SYNC AND TEST CHECK
    @Query("SELECT * FROM static_map WHERE id = :id")
    fun getStaticMapByIdIncludeDeleted(id: String): Flow<StaticMapEntity?>
    @Query("SELECT * FROM static_map WHERE property_id = :propertyId")
    fun getStaticMapByPropertyIdIncludeDeleted(propertyId: String): Flow<StaticMapEntity?>
    @Query("SELECT * FROM static_map")
    fun getAllStaticMapIncludeDeleted(): Flow<List<StaticMapEntity>>

    @RawQuery(observedEntities = [StaticMapEntity::class])
    fun getAllStaticMapAsCursor(query: SupportSQLiteQuery): Cursor
}