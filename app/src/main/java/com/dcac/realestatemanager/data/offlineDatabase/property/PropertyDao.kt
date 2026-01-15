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

@Dao
interface PropertyDao {

    //QUERIES (FILTERED ON IS_DELETE = 0)
    //FOR UI
    @Query("SELECT * FROM properties WHERE is_deleted = 0 ORDER BY entry_date DESC")
    fun getAllPropertiesByDate(): Flow<List<PropertyEntity>>
    @Query("SELECT * FROM properties WHERE is_deleted = 0 ORDER BY title ASC")
    fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>>
    @Query("SELECT * FROM properties WHERE id = :id AND is_deleted = 0 LIMIT 1")
    fun getPropertyById(id: String): Flow<PropertyEntity?>
    @Query("SELECT * FROM properties WHERE user_id = :userId AND is_deleted = 0 ORDER BY title ASC")
    fun getPropertyByUserIdAlphabetic(userId: String): Flow<List<PropertyEntity>>
    @Query("SELECT * FROM properties WHERE user_id = :userId AND is_deleted = 0 ORDER BY entry_date DESC")
    fun getPropertyByUserIdDate(userId: String): Flow<List<PropertyEntity>>
    @Query("""
    SELECT * FROM properties
    WHERE is_deleted = 0
      AND (:minSurface IS NULL OR surface >= :minSurface)
      AND (:maxSurface IS NULL OR surface <= :maxSurface)
      AND (:minPrice IS NULL OR price >= :minPrice)
      AND (:maxPrice IS NULL OR price <= :maxPrice)
      AND (:type IS NULL OR type = :type)
      AND (:isSold IS NULL OR is_sold = :isSold)
    ORDER BY entry_date DESC
""")
    fun searchPropertiesByDate(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>
    @Query("""
    SELECT * FROM properties
    WHERE is_deleted = 0
      AND (:minSurface IS NULL OR surface >= :minSurface)
      AND (:maxSurface IS NULL OR surface <= :maxSurface)
      AND (:minPrice IS NULL OR price >= :minPrice)
      AND (:maxPrice IS NULL OR price <= :maxPrice)
      AND (:type IS NULL OR type = :type)
      AND (:isSold IS NULL OR is_sold = :isSold)
    ORDER BY title ASC
""")
    fun searchPropertiesByAlphabetic(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>

    @Query("""
    SELECT * FROM properties
    WHERE user_id = :userId AND is_deleted = 0
      AND (:minSurface IS NULL OR surface >= :minSurface)
      AND (:maxSurface IS NULL OR surface <= :maxSurface)
      AND (:minPrice IS NULL OR price >= :minPrice)
      AND (:maxPrice IS NULL OR price <= :maxPrice)
      AND (:type IS NULL OR type = :type)
      AND (:isSold IS NULL OR is_sold = :isSold)
    ORDER BY title ASC
""")
    fun searchUserPropertiesByAlphabetic(
        userId: String,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>

    @Query("""
    SELECT * FROM properties
    WHERE user_id = :userId AND is_deleted = 0
      AND (:minSurface IS NULL OR surface >= :minSurface)
      AND (:maxSurface IS NULL OR surface <= :maxSurface)
      AND (:minPrice IS NULL OR price >= :minPrice)
      AND (:maxPrice IS NULL OR price <= :maxPrice)
      AND (:type IS NULL OR type = :type)
      AND (:isSold IS NULL OR is_sold = :isSold)
    ORDER BY entry_date DESC
""")
    fun searchUserPropertiesByDate(
        userId: String,
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<PropertyEntity>>

    @Query("UPDATE properties SET is_sold = 1, sale_date = :saleDate, updated_at = :updatedAt WHERE id = :propertyId")
    suspend fun markPropertyAsSold(propertyId: String, saleDate: String, updatedAt: Long)

    //SYNC
    @Query("SELECT * FROM properties WHERE is_synced = 0")
    fun uploadUnSyncedProperties(): Flow<List<PropertyEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPropertyFromUi(property: PropertyEntity): String {
        firstPropertyInsert(property.copy(isSynced = false))
        return property.id
    }
    suspend fun insertPropertiesFromUi(properties: List<PropertyEntity>) {
        properties.forEach { property ->
            firstPropertyInsert(property.copy(isSynced = false))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstPropertyInsert(property: PropertyEntity)

    //INSERTIONS FROM FIREBASE
    suspend fun insertPropertyFromFirebase(property: PropertyEntity): String? {
        insertPropertyIfNotExists(property.copy(isSynced = true))
        return property.firestoreDocumentId
    }
    suspend fun insertAllPropertiesNotExistingFromFirebase(properties: List<PropertyEntity>) {
        properties.forEach { property ->
            insertPropertyIfNotExists(property.copy(isSynced = true))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPropertyIfNotExists(property: PropertyEntity)

    //UPDATE
    // WHEN PROPERTY IS UPDATE FROM A PHONE (UI → ROOM, will need to sync later)
    suspend fun updatePropertyFromUIForceSyncFalse(property: PropertyEntity): String{
        updateProperty(property.copy(isSynced = false))
        return property.id
    }
    suspend fun updatePropertyFromFirebaseForcesSyncTrue(property: PropertyEntity): String? {
        updateProperty(property.copy(isSynced = true))
        return property.firestoreDocumentId
    }
    suspend fun updateAllPropertiesFromFirebaseForceSyncTrue(properties: List<PropertyEntity>) {
        properties.forEach { property ->
            updateProperty(property.copy(isSynced = true))
        }
    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProperty(property: PropertyEntity)

    //SOFT DELETE
    //MARK FROM UI PROPERTIES AS DELETED BEFORE REAL DELETE
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPropertyAsDeleted(id: String, updatedAt: Long)
    @Query("UPDATE properties SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt")
    suspend fun markAllPropertiesAsDeleted(updatedAt: Long)

    //HARD DELETE
    //AFTER MARK DELETE PROPERTY IN FIREBASE, DELETE PROPERTY FROM ROOM
    @Delete
    suspend fun deleteProperty(property: PropertyEntity)
    @Query("DELETE FROM properties WHERE is_deleted = 1")
    suspend fun clearAllDeleted()

    // FOR SYNC AND TEST CHECK
    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyByIdIncludeDeleted(id: String): Flow<PropertyEntity?>
    @Query("SELECT * FROM properties")
    fun getAllPropertiesIncludeDeleted(): Flow<List<PropertyEntity>>

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :propertyId AND is_deleted = 0")
    fun getPropertyWithPoiS(propertyId: String): Flow<PropertyWithPoiSRelation>

    @RawQuery(observedEntities = [PropertyEntity::class])
    fun getAllPropertiesAsCursor(query: SupportSQLiteQuery): Cursor
}