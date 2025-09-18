package com.dcac.realestatemanager.data.offlineDatabase.photo

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
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

    //wrapper insert all
    suspend fun insertPhotos(photos: List<PhotoEntity>) {
        photos.forEach { photo ->
            insertPhoto(photo)
        }
    }

    //wrapper insert
    suspend fun insertPhoto(photo: PhotoEntity) {
        insertPhotoForcedSyncFalse(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    @Query("""
        INSERT OR REPLACE INTO photos (
            id, property_id, uri, description, is_deleted, is_synced, updated_at
        ) VALUES (
            :id, :propertyId, :uri, :description, :isDeleted, 0, :updatedAt
        )
    """)
    suspend fun insertPhotoForcedSyncFalse(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    @Query("""
        UPDATE photos SET
            property_id = :propertyId,
            uri = :uri,
            description = :description,
            is_deleted = :isDeleted,
            is_synced = 0,
            updated_at = :updatedAt
        WHERE id = :id
    """)
    suspend fun updatePhotoForcedSyncFalse(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    //wrapper update
    suspend fun updatePhoto(photo: PhotoEntity) {
        updatePhotoForcedSyncFalse(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    // ✅ Hard delete
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    // ✅ Hard delete
    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deletePhotosByPropertyId(propertyId: Long)

    //for test check hard delete
    @Query("SELECT * FROM photos WHERE id = :id")
    fun getPhotoByIdIncludeDeleted(id: Long): Flow<PhotoEntity?>

    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PhotoEntity>>

    // ✅ Soft delete
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId")
    suspend fun markPhotosAsDeletedByProperty(propertyId: Long, updatedAt: Long)

    // ✅ Soft delete
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPhotoAsDeleted(id: Long, updatedAt: Long)

    @Query("SELECT * FROM photos WHERE is_synced = 0")
    fun getUnSyncedPhotos(): Flow<List<PhotoEntity>>

    @Query("""
    INSERT OR REPLACE INTO photos (
        id, property_id, uri, description, is_deleted, is_synced, updated_at
    ) VALUES (
        :id, :propertyId, :uri, :description, :isDeleted, 1, :updatedAt
    )
""")
    suspend fun savePhotoFromFirebaseForcedSyncTrue(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    )

    suspend fun savePhotoFromFirebase(photo: PhotoEntity) {
        savePhotoFromFirebaseForcedSyncTrue(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    @RawQuery(observedEntities = [PhotoEntity::class])
    fun getAllPhotosAsCursor(query: SupportSQLiteQuery): Cursor


}