package com.dcac.realestatemanager.data.offlinedatabase.photo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing PhotoEntity data
@Dao
interface PhotoDao {

    // Get all photos associated with a specific property ID
    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    // Insert a list of photos (used when creating/updating a property)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    // Delete all photos linked to a specific property (e.g. when a property is deleted)
    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deletePhotosByPropertyId(propertyId: Long)

    // Delete a specific photo by entity
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}