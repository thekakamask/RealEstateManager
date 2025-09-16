package com.dcac.realestatemanager.data.offlineDatabase.property

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

// DAO interface for accessing PropertyEntity data
@Dao
interface PropertyDao {

    @Query("SELECT * FROM properties WHERE is_deleted = 0 ORDER BY entry_date DESC")
    fun getAllPropertiesByDate(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE is_deleted = 0 ORDER BY title ASC")
    fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Query("""
        SELECT * FROM properties
        WHERE is_deleted = 0
          AND (:minSurface IS NULL OR surface >= :minSurface)
          AND (:maxSurface IS NULL OR surface <= :maxSurface)
          AND (:minPrice IS NULL OR price >= :minPrice)
          AND (:maxPrice IS NULL OR price <= :maxPrice)
          AND (:type IS NULL OR type = :type)
          AND (:isSold IS NULL OR is_sold = :isSold)
    """)
    fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    // ✅ Hard delete
    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    // ✅ Soft delete
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPropertyAsDeleted(id: Long, updatedAt: Long)

    @Query("UPDATE properties SET is_sold = 1, sale_date = :saleDate WHERE id = :propertyId")
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)

    // ✅ Hard delete
    @Query("DELETE FROM properties")
    suspend fun clearAll()

    // ✅ Soft delete
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllPropertiesAsDeleted(updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :propertyId AND is_deleted = 0")
    fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation>

    @Query("SELECT * FROM properties WHERE is_synced = 0")
    fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePropertyFromFirebase(property: PropertyEntity)

    @RawQuery(observedEntities = [PropertyEntity::class])
    fun getAllPropertiesAsCursor(query: SupportSQLiteQuery): Cursor
}