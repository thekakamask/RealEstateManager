package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

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
interface PropertyPoiCrossDao {

    //QUERIES (FILTERED ON IS_DELETE = 0)
    //FOR UI
    @Query("SELECT * FROM property_poi_cross_ref WHERE property_id = :propertyId AND is_deleted = 0")
    fun getCrossRefsForProperty(propertyId: String): Flow<List<PropertyPoiCrossEntity>>
    @Query("SELECT poi_id FROM property_poi_cross_ref WHERE property_id = :propertyId AND is_deleted = 0")
    fun getPoiIdsForProperty(propertyId: String): Flow<List<String>>
    @Query("SELECT property_id FROM property_poi_cross_ref WHERE poi_id = :poiId AND is_deleted = 0")
    fun getPropertyIdsForPoi(poiId: String): Flow<List<String>>
    @Query("SELECT * FROM property_poi_cross_ref WHERE is_deleted = 0")
    fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>>
    @Query("SELECT * FROM property_poi_cross_ref WHERE property_id = :propertyId AND poi_id = :poiId AND is_deleted = 0")
    fun getCrossByIds(propertyId: String, poiId: String): Flow<PropertyPoiCrossEntity?>

    //SYNC
    @Query("SELECT * FROM property_poi_cross_ref WHERE is_synced = 0")
    fun uploadUnSyncedCrossRefs(): Flow<List<PropertyPoiCrossEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertCrossRefInsertFromUI(crossRef: PropertyPoiCrossEntity): String {
        firstCrossRefInsert(crossRef.copy(isSynced = false))
        return "${crossRef.universalLocalPropertyId}_${crossRef.universalLocalPoiId}"
    }
    suspend fun insertAllCrossRefInsertFromUi(crossRefs: List<PropertyPoiCrossEntity>){
        crossRefs.forEach { crossRef ->
            firstCrossRefInsert(crossRef.copy(isSynced = false))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstCrossRefInsert(crossRef: PropertyPoiCrossEntity)

    //INSERTIONS FROM FIREBASE
    suspend fun insertCrossRefInsertFromFirebase(crossRef: PropertyPoiCrossEntity): String? {
        insertCrossRefIfNotExists(crossRef.copy(isSynced = true))
        return crossRef.firestoreDocumentId
    }
    suspend fun insertAllCrossRefNotExistingFromFirebase(crossRefs: List<PropertyPoiCrossEntity>){
        crossRefs.forEach { crossRef ->
            insertCrossRefIfNotExists(crossRef.copy(isSynced = true))
        }
    }
    @Insert(onConflict= OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefIfNotExists(propertyPoiCrossEntity: PropertyPoiCrossEntity)

    //UPDATE
    // WHEN CROSSREF IS UPDATE FROM A PHONE (UI → ROOM, will need to sync later)
    suspend fun updateCrossRefFromUIForceSyncFalse(crossRef : PropertyPoiCrossEntity): String {
        updateCrossRef(crossRef.copy(isSynced = false))
        return "${crossRef.universalLocalPropertyId}_${crossRef.universalLocalPoiId}"
    }
    suspend fun updateAllCrossRefsFromUIForceSyncFalse(crossRefs : List<PropertyPoiCrossEntity>){
        crossRefs.forEach { crossRef ->
            updateCrossRef(crossRef.copy(isSynced = false))
        }
    }
    suspend fun updateCrossRefFromFirebaseForceSyncTrue(crossRef : PropertyPoiCrossEntity): String? {
        updateCrossRef(crossRef.copy(isSynced = true))
        return crossRef.firestoreDocumentId
    }
    suspend fun updateAllCrossRefFromFirebaseForceSyncTrue(crossRefs: List<PropertyPoiCrossEntity>){
        crossRefs.forEach { crossRef ->
            updateCrossRef(crossRef.copy(isSynced = true))
        }
    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCrossRef(crossRef: PropertyPoiCrossEntity)

    //SOFT DELETE
    //MARK FROM UI CROSSREFS AS DELETED BEFORE REAL DELETE
    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId AND poi_id = :poiId")
    suspend fun markCrossRefAsDeleted(propertyId: String, poiId: String, updatedAt: Long)
    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId")
    suspend fun markCrossRefsAsDeletedForProperty(propertyId: String, updatedAt: Long)
    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE poi_id = :poiId")
    suspend fun markCrossRefsAsDeletedForPoi(poiId: String, updatedAt: Long)
    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllCrossRefsAsDeleted(updatedAt: Long)

    //HARD DELETE
    // AFTER DELETE CROSSREFS FROM FIREBASE, DELETE CROSSREFS FROM ROOM
    @Delete
    suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity)
    @Query("DELETE FROM property_poi_cross_ref WHERE property_id = :propertyId")
    suspend fun deleteCrossRefsForProperty(propertyId: String)
    @Query("DELETE FROM property_poi_cross_ref WHERE poi_id = :poiId")
    suspend fun deleteCrossRefsForPoi(poiId: String)
    @Query("DELETE FROM property_poi_cross_ref WHERE is_deleted = 1")
    suspend fun clearAllDeleted()

    //FOR TEST CHECK HARD DELETE
    @Query("SELECT * FROM property_poi_cross_ref WHERE property_id = :propertyId AND poi_id = :poiId")
    fun getCrossRefsByIdsIncludedDeleted(propertyId: String, poiId: String): Flow<PropertyPoiCrossEntity?>
    @Query("SELECT * FROM property_poi_cross_ref WHERE property_id = :propertyId")
    fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PropertyPoiCrossEntity>>
    @Query("SELECT * FROM property_poi_cross_ref WHERE poi_id = :poiId")
    fun getCrossRefsByPoiIdIncludeDeleted(poiId: String): Flow<List<PropertyPoiCrossEntity>>
    @Query("SELECT * FROM property_poi_cross_ref")
    fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>>

    @RawQuery(observedEntities = [PropertyPoiCrossEntity::class])
    fun getAllCrossRefsAsCursor(query: SupportSQLiteQuery): Cursor

}