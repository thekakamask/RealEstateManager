package com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
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

    @Query("""
        INSERT OR REPLACE INTO property_poi_cross_ref (
            propertyId, poiId, is_deleted, is_synced, updated_at
        ) VALUES (
            :propertyId, :poiId, :isDeleted, 0, :updatedAt
        )
    """)
    suspend fun insertCrossRefForcedSyncFalse(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper insert ---
    suspend fun insertCrossRef(crossRef: PropertyPoiCrossEntity) {
        insertCrossRefForcedSyncFalse(
            propertyId = crossRef.propertyId,
            poiId = crossRef.poiId,
            isDeleted = crossRef.isDeleted,
            updatedAt = crossRef.updatedAt
        )
    }

    // --- Wrapper insert all ---
    suspend fun insertAllCrossRefs(crossRefs: List<PropertyPoiCrossEntity>) {
        crossRefs.forEach { insertCrossRef(it) }
    }

    @Query("""
        UPDATE property_poi_cross_ref SET 
            is_deleted = :isDeleted,
            is_synced = 0,
            updated_at = :updatedAt
        WHERE propertyId = :propertyId AND poiId = :poiId
    """)
    suspend fun updateCrossRefForcedSyncFalse(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper update ---
    suspend fun updateCrossRef(propertyPoiCrossEntity: PropertyPoiCrossEntity) {
        updateCrossRefForcedSyncFalse(
            propertyId = propertyPoiCrossEntity.propertyId,
            poiId = propertyPoiCrossEntity.poiId,
            isDeleted = propertyPoiCrossEntity.isDeleted,
            updatedAt = propertyPoiCrossEntity.updatedAt
        )
    }

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

    //for test check hard delete
    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId")
    fun getCrossRefsByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT * FROM property_poi_cross_ref WHERE poiId = :poiId")
    fun getCrossRefsByPoiIdIncludeDeleted(poiId: Long): Flow<List<PropertyPoiCrossEntity>>

    @Query("SELECT * FROM property_poi_cross_ref WHERE propertyId = :propertyId AND poiId = :poiId")
    fun getCrossRefsByIdsIncludedDeleted(propertyId: Long, poiId: Long): Flow<PropertyPoiCrossEntity?>

    @Query("SELECT * FROM property_poi_cross_ref")
    fun getAllCrossRefsIncludeDeleted(): Flow<List<PropertyPoiCrossEntity>>

    @Query("DELETE FROM property_poi_cross_ref WHERE is_deleted = 1")
    suspend fun clearAllDeleted()

    // Sync
    @Query("SELECT * FROM property_poi_cross_ref WHERE is_synced = 0")
    fun uploadUnSyncedPropertiesPoiSCross(): Flow<List<PropertyPoiCrossEntity>>

    @Query("""
        INSERT OR REPLACE INTO property_poi_cross_ref (
            propertyId, poiId, is_deleted, is_synced, updated_at
        ) VALUES (
            :propertyId, :poiId, :isDeleted, 1, :updatedAt
        )
    """)
    suspend fun saveCrossRefFromFirebaseForcedSyncTrue(
        propertyId: Long,
        poiId: Long,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // --- Wrapper Firebase insert ---
    suspend fun saveCrossRefFromFirebase(crossRef: PropertyPoiCrossEntity) {
        saveCrossRefFromFirebaseForcedSyncTrue(
            propertyId = crossRef.propertyId,
            poiId = crossRef.poiId,
            isDeleted = crossRef.isDeleted,
            updatedAt = crossRef.updatedAt
        )
    }

    // ContentProvider support
    @RawQuery(observedEntities = [PropertyPoiCrossEntity::class])
    fun getAllCrossRefsAsCursor(query: SupportSQLiteQuery): Cursor

}