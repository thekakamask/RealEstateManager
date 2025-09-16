package com.dcac.realestatemanager.data.offlineDatabase.photo

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import kotlinx.coroutines.flow.Flow

// DAO interface for accessing PhotoEntity data
@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE id = :id AND is_deleted = 0")
    fun getPhotoById(id: Long): Flow<PhotoEntity?>

    @Query("SELECT * FROM photos WHERE property_id = :propertyId AND is_deleted = 0")
    fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE is_deleted = 0")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Update
    suspend fun updatePhoto(photo : PhotoEntity)

    // ✅ Hard delete
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    // ✅ Hard delete
    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deletePhotosByPropertyId(propertyId: Long)

    // ✅ Soft delete
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId")
    suspend fun markPhotosAsDeletedByProperty(propertyId: Long, updatedAt: Long)

    // ✅ Soft delete
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPhotoAsDeleted(id: Long, updatedAt: Long)

    @Query("SELECT * FROM photos WHERE is_synced = 0")
    fun getUnSyncedPhotos(): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePhotoFromFirebase(photo: PhotoEntity)

    @RawQuery(observedEntities = [PhotoEntity::class])
    fun getAllPhotosAsCursor(query: SupportSQLiteQuery): Cursor


}