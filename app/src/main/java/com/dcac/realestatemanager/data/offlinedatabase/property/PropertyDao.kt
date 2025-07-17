package com.dcac.realestatemanager.data.offlinedatabase.property

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing PropertyEntity data
@Dao
interface PropertyDao {

    @Query("SELECT * FROM properties ORDER BY entry_date DESC")
    fun getAllPropertiesByDate(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties ORDER BY title ASC")
    fun getAllPropertiesByAlphabetic(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    @Query("""
        SELECT * FROM properties
        WHERE
          (:minSurface IS NULL OR surface >= :minSurface) AND
          (:maxSurface IS NULL OR surface <= :maxSurface) AND
          (:minPrice IS NULL OR price >= :minPrice) AND
          (:maxPrice IS NULL OR price <= :maxPrice) AND
          (:type IS NULL OR type = :type) AND
          (:isSold IS NULL OR is_sold = :isSold)
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

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)

    @Query("UPDATE properties SET is_sold = 1, sale_date = :saleDate WHERE id = :propertyId")
    suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)

    @Query("DELETE FROM properties")
    suspend fun clearAll()

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :propertyId")
    fun getPropertyWithPoiS(propertyId: Long): Flow<PropertyWithPoiSRelation>


}