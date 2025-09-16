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

// DAO interface for accessing POIs associated with properties
@Dao
interface PoiDao {

    @Query("SELECT * FROM poi WHERE id = :id AND is_deleted = 0")
    fun getPoiById(id: Long): Flow<PoiEntity?>

    @Query("SELECT * FROM poi WHERE is_deleted = 0")
    fun getAllPoiS(): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPoiS(poiS: List<PoiEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PoiEntity)

    @Update
    suspend fun updatePoi(poi: PoiEntity)

    // ✅ Hard delete
    @Delete
    suspend fun deletePoi(poi: PoiEntity)

    // ✅ Soft delete
    @Query("UPDATE poi SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPoiAsDeleted(id: Long, updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM poi WHERE id = :poiId AND is_deleted = 0")
    fun getPoiWithProperties(poiId: Long): Flow<PoiWithPropertiesRelation>

    @Query("SELECT * FROM poi WHERE is_synced = 0")
    fun uploadUnSyncedPoiSToFirebase(): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePoiFromFirebase(poi: PoiEntity)

    @RawQuery(observedEntities = [PoiEntity::class])
    fun getAllPoiSAsCursor(query: SupportSQLiteQuery): Cursor

}