package com.dcac.realestatemanager.data.offlineDatabase.photo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing PhotoEntity data
@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePhotoFromFirebase(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE id = :id")
    fun getPhotoById(id: Long): Flow<PhotoEntity?>

    // Get all photos associated with a specific property ID
    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    // Insert a list of photos (used when creating/updating a property)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo : PhotoEntity)

    // Delete all photos linked to a specific property (e.g. when a property is deleted)
    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deletePhotosByPropertyId(propertyId: Long)

    // Delete a specific photo
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    //get All Photos
    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE is_synced = 0")
    fun getUnSyncedPhotos(): Flow<List<PhotoEntity>>


}