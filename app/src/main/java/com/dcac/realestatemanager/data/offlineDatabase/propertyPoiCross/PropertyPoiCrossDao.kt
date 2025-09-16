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

    // Queries filtered on is_deleted = 0
    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId AND is_deleted = 0")
    fun getCrossRefsForProperty(propertyId: Long): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT poiId FROM property_poi_cross_ref WHERE propertyId = :propertyId AND is_deleted = 0")
    fun getPoiIdsForProperty(propertyId: Long): Flow<List<Long>>

    @Query("SELECT propertyId FROM property_poi_cross_ref WHERE poiId = :poiId AND is_deleted = 0")
    fun getPropertyIdsForPoi(poiId: Long): Flow<List<Long>>

    @Query("SELECT * FROM property_poi_cross_ref WHERE is_deleted = 0")
    fun getAllCrossRefs(): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId AND poiId = :poiId AND is_deleted = 0")
    fun getCrossByIds(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PropertyPoiCrossEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCrossEntity>)

    @Update
    suspend fun updateCrossRef(propertyPoiCrossEntity: PropertyPoiCrossEntity)

    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE propertyId = :propertyId AND poiId = :poiId")
    suspend fun markCrossRefAsDeleted(propertyId: Long, poiId: Long, updatedAt: Long)


    // Soft delete
    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE propertyId = :propertyId")
    suspend fun markCrossRefsAsDeletedForProperty(propertyId: Long, updatedAt: Long)

    @Delete
    suspend fun deleteCrossRef(crossRef: PropertyPoiCrossEntity)

    // ✅ Hard delete
    @Query("DELETE FROM property_poi_cross_ref WHERE propertyId = :propertyId")
    suspend fun deleteCrossRefsForProperty(propertyId: Long)

    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE poiId = :poiId")
    suspend fun markCrossRefsAsDeletedForPoi(poiId: Long, updatedAt: Long)

    @Query("DELETE FROM property_poi_cross_ref WHERE poiId = :poiId")
    suspend fun deleteCrossRefsForPoi(poiId: Long)


    @Query("UPDATE property_poi_cross_ref SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllCrossRefsAsDeleted(updatedAt: Long)

    @Query("DELETE FROM property_poi_cross_ref")
    suspend fun clearAllCrossRefs()

    // Sync
    @Query("SELECT * FROM property_poi_cross_ref WHERE is_synced = 0")
    fun uploadUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCrossRefFromFirebase(crossRef: PropertyPoiCrossEntity)

    // ContentProvider support
    @RawQuery(observedEntities = [PropertyPoiCrossEntity::class])
    fun getAllCrossRefsAsCursor(query: SupportSQLiteQuery): Cursor

}