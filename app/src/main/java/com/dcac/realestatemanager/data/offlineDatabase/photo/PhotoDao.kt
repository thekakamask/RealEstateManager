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
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    //QUERIES (FILTERED ON IS_DELETE = 0)
    //FOR UI
    @Query("SELECT * FROM photos WHERE id = :id AND is_deleted = 0")
    fun getPhotoById(id: String): Flow<PhotoEntity?>
    @Query("SELECT * FROM photos WHERE property_id = :propertyId AND is_deleted = 0")
    fun getPhotosByPropertyId(propertyId: String): Flow<List<PhotoEntity>>
    @Query("SELECT * FROM photos WHERE is_deleted = 0")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    //SYNC
    @Query("SELECT * FROM photos WHERE is_synced = 0")
    fun uploadUnSyncedPhotos(): Flow<List<PhotoEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPhotoInsertFromUI(photo: PhotoEntity): String{
        firstPhotoInsert(photo.copy(isSynced = false))
        return photo.id
    }
    suspend fun insertPhotosInsertFromUI(photos: List<PhotoEntity>){
        photos.forEach { photo ->
            firstPhotoInsert(photo.copy(isSynced = false))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun firstPhotoInsert(photo: PhotoEntity)

    //INSERTIONS FROM FIREBASE
    suspend fun insertPhotoInsertFromFirebase(photo: PhotoEntity): String? {
        insertPhotoIfNotExists(photo.copy(isSynced = true))
        return photo.firestoreDocumentId
    }
    suspend fun insertAllPhotosNotExistingFromFirebase(photos: List<PhotoEntity>){
        photos.forEach { photo ->
            insertPhotoIfNotExists(photo.copy(isSynced = true))
        }
    }
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhotoIfNotExists(photo: PhotoEntity)

    //UPDATE
    // WHEN PHOTO IS UPDATE FROM A PHONE (UI → ROOM, will need to sync later)
    suspend fun updatePhotoFromUIForceSyncFalse(photo: PhotoEntity): String {
        updatePhoto(photo.copy(isSynced = false))
        return photo.id
    }
    // WHEN FIREBASE SENDS AN UPDATED SINGLE OR MULTIPLE PHOTOS TO ROOM
    suspend fun updatePhotoFromFirebaseForceSyncTrue(photo: PhotoEntity): String? {
        updatePhoto(photo.copy(isSynced = true))
        return photo.firestoreDocumentId
    }
    suspend fun updateAllPhotosFromFirebaseForceSyncTrue(photos: List<PhotoEntity>) {
        photos.forEach { photo ->
            updatePhoto(photo.copy(isSynced = true))
        }
    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePhoto(photo: PhotoEntity)

    //SOFT DELETE
    //MARK FROM UI PHOTOS AS DELETED BEFORE REAL DELETE
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE property_id = :propertyId")
    suspend fun markPhotosAsDeletedByProperty(propertyId: String, updatedAt: Long)
    @Query("UPDATE photos SET is_deleted = 1, is_synced = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun markPhotoAsDeleted(id: String, updatedAt: Long)

    //HARD DELETE
    // AFTER MARK PHOTO AS DELETE IN FIREBASE, DELETE PHOTO FROM ROOM
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    @Query("DELETE FROM photos WHERE property_id = :propertyId")
    suspend fun deletePhotosByPropertyId(propertyId: String)
    @Query("DELETE FROM photos WHERE is_deleted = 1")
    suspend fun clearAllPhotosDeleted()

    // FOR SYNC AND TEST CHECK
    @Query("SELECT * FROM photos WHERE id = :id")
    fun getPhotoByIdIncludeDeleted(id: String): Flow<PhotoEntity?>
    @Query("SELECT * FROM photos WHERE property_id = :propertyId")
    fun getPhotosByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PhotoEntity>>
    @Query("SELECT * FROM photos")
    fun getAllPhotosIncludeDeleted(): Flow<List<PhotoEntity>>

    @RawQuery(observedEntities = [PhotoEntity::class])
    fun getAllPhotosAsCursor(query: SupportSQLiteQuery): Cursor

}