package com.dcac.realestatemanager.data.offlineDatabase.poi

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing POIs associated with properties
@Dao
interface PoiDao {

    @Query("SELECT * FROM poi WHERE id = :id AND is_deleted = 0")
    fun getPoiById(id: Long): Flow<PoiEntity?>

    @Query("SELECT * FROM poi WHERE is_deleted = 0")
    fun getAllPoiS(): Flow<List<PoiEntity>>

    @Query("""
    INSERT OR REPLACE INTO poi (
        id, name, type, is_deleted, is_synced, updated_at
    ) VALUES (
        :id, :name, :type, :isDeleted, 0, :updatedAt
    )
""")
    suspend fun insertPoiForcedSyncFalse(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    //wrapper insert
    suspend fun insertPoi(poi: PoiEntity) {
        insertPoiForcedSyncFalse(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }

    //wrapper insert all
    suspend fun insertAllPoiS(poiS: List<PoiEntity>) {
        poiS.forEach { poi -> insertPoi(poi) }
    }

    @Query("""
    UPDATE poi SET 
        name = :name,
        type = :type,
        is_deleted = :isDeleted,
        is_synced = 0,
        updated_at = :updatedAt
    WHERE id = :id
""")
    suspend fun updatePoiForcedSyncFalse(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    //wrapper update
    suspend fun updatePoi(poi: PoiEntity) {
        updatePoiForcedSyncFalse(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }


    // ✅ Hard delete
    @Delete
    suspend fun deletePoi(poi: PoiEntity)

    //for unit test to check hard delete
    @Query("SELECT * FROM poi WHERE id = :id")
    fun getPoiByIdIncludeDeleted(id: Long): Flow<PoiEntity?>

    //for test check hard delete
    @Query("SELECT * FROM poi")
    fun getAllPoiIncludeDeleted(): Flow<List<PoiEntity>>

    // ✅ Soft delete
    @Query("UPDATE poi SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPoiAsDeleted(id: Long, updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM poi WHERE id = :poiId AND is_deleted = 0")
    fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation>

    @Query("SELECT * FROM poi WHERE is_synced = 0")
    fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>>

    @Query("""
    INSERT OR REPLACE INTO poi (
        id, name, type, is_deleted, is_synced, updated_at
    ) VALUES (
        :id, :name, :type, :isDeleted, 1, :updatedAt
    )
""")
    suspend fun savePoiFromFirebaseForcedSyncTrue(
        id: Long,
        name: String,
        type: String,
        isDeleted: Boolean,
        updatedAt: Long
    )

    //wrapper save
    suspend fun savePoiFromFirebase(poi: PoiEntity) {
        savePoiFromFirebaseForcedSyncTrue(
            id = poi.id,
            name = poi.name,
            type = poi.type,
            isDeleted = poi.isDeleted,
            updatedAt = poi.updatedAt
        )
    }

    @RawQuery(observedEntities = [PoiEntity::class])
    fun getAllPoiSAsCursor(query: SupportSQLiteQuery): Cursor

}