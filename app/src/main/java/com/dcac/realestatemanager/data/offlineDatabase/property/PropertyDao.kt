package com.dcac.realestatemanager.data.offlineDatabase.property

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
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

    @Query("SELECT * FROM properties WHERE user_id = :userId AND is_deleted = 0")
    fun getPropertyByUserId(userId: Long): Flow<List<PropertyEntity>>

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

    @Query("""
        INSERT OR REPLACE INTO properties (
            id, title, type, price, surface, rooms, description,
            address, is_sold, entry_date, sale_date, user_id,
            static_map_path, is_deleted, is_synced, updated_at
        ) VALUES (
            :id, :title, :type, :price, :surface, :rooms, :description,
            :address, :isSold, :entryDate, :saleDate, :userId,
            :staticMapPath, :isDeleted, 0, :updatedAt
        )
    """)
    suspend fun insertPropertyForcedSyncFalse(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // Wrapper insert
    suspend fun insertProperty(property: PropertyEntity): Long {
        insertPropertyForcedSyncFalse(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
        return property.id
    }

    @Query("""
        UPDATE properties SET
            title = :title,
            type = :type,
            price = :price,
            surface = :surface,
            rooms = :rooms,
            description = :description,
            address = :address,
            is_sold = :isSold,
            entry_date = :entryDate,
            sale_date = :saleDate,
            user_id = :userId,
            static_map_path = :staticMapPath,
            is_deleted = :isDeleted,
            is_synced = 0,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updatePropertyForcedSyncFalse(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    // Wrapper update
    suspend fun updateProperty(property: PropertyEntity) {
        updatePropertyForcedSyncFalse(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
    }

    // ✅ Hard delete
    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    // ✅ Soft delete
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPropertyAsDeleted(id: Long, updatedAt: Long)

    //for test check hard delete
    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyByIdIncludeDeleted(id: Long): Flow<PropertyEntity?>

    /*@Query("UPDATE properties SET is_sold = 1, sale_date = :saleDate, updated_at = :updatedAt WHERE id = :propertyId")
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String, updatedAt: Long)
*/
    // ✅ Hard delete
    @Query("DELETE FROM properties WHERE is_deleted = 1")
    suspend fun clearAllDeleted()

    // ✅ Soft delete
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllPropertiesAsDeleted(updatedAt: Long)

    //for test check hard delete
    @Query("SELECT * FROM properties")
    fun getAllPropertiesIncludeDeleted(): Flow<List<PropertyEntity>>

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :propertyId AND is_deleted = 0")
    fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation>

    @Query("SELECT * FROM properties WHERE is_synced = 0")
    fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>>

    @Query("""
        INSERT OR REPLACE INTO properties (
            id, title, type, price, surface, rooms, description,
            address, is_sold, entry_date, sale_date, user_id,
            static_map_path, is_deleted, is_synced, updated_at
        ) VALUES (
            :id, :title, :type, :price, :surface, :rooms, :description,
            :address, :isSold, :entryDate, :saleDate, :userId,
            :staticMapPath, :isDeleted, 1, :updatedAt
        )
    """)
    suspend fun savePropertyFromFirebaseForcedSyncTrue(
        id: Long,
        title: String,
        type: String,
        price: Int,
        surface: Int,
        rooms: Int,
        description: String,
        address: String,
        isSold: Boolean,
        entryDate: String,
        saleDate: String?,
        userId: Long,
        staticMapPath: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    suspend fun savePropertyFromFirebase(property: PropertyEntity) {
        savePropertyFromFirebaseForcedSyncTrue(
            id = property.id,
            title = property.title,
            type = property.type,
            price = property.price,
            surface = property.surface,
            rooms = property.rooms,
            description = property.description,
            address = property.address,
            isSold = property.isSold,
            entryDate = property.entryDate,
            saleDate = property.saleDate,
            userId = property.userId,
            staticMapPath = property.staticMapPath,
            isDeleted = property.isDeleted,
            updatedAt = property.updatedAt
        )
    }

    @RawQuery(observedEntities = [PropertyEntity::class])
    fun getAllPropertiesAsCursor(query: SupportSQLiteQuery): Cursor
}