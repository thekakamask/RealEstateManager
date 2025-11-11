package com.dcac.realestatemanager.data.offlineDatabase.photo

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    // FOR UI
    fun getPhotoById(id: String): Flow<Photo?>
    fun getPhotosByPropertyId(propertyId: String): Flow<List<Photo>>
    fun getAllPhotos(): Flow<List<Photo>>

    //SYNC
    fun uploadUnSyncedPhotosToFirebase(): Flow<List<PhotoEntity>>

    //INSERTIONS
    //INSERTIONS FROM UI
    suspend fun insertPhotoInsertFromUI(photo: Photo): String
    suspend fun insertPhotosInsertFromUI(photos: List<Photo>)
    //INSERTIONS FROM FIREBASE
    suspend fun insertPhotoInsertFromFirebase(photo: PhotoOnlineEntity,firestoreId: String, localUri: String)
    suspend fun insertPhotosInsertFromFirebase(photos: List<Triple<PhotoOnlineEntity,String, String>>)

    //UPDATE
    suspend fun updatePhotoFromUI(photo: Photo)
    suspend fun updatePhotoFromFirebase(photo: PhotoOnlineEntity, firestoreId: String)
    suspend fun updateAllPhotosFromFirebase(photos: List<Pair<PhotoOnlineEntity, String>>)

    //SOFT DELETE
    suspend fun markPhotoAsDeleted(photo: Photo)
    suspend fun markPhotosAsDeletedByProperty(propertyId: String)

    //HARD DELETE
    suspend fun deletePhotosByPropertyId(propertyId: String)
    suspend fun deletePhoto(photo: PhotoEntity)
    suspend fun clearAllPhotosDeleted()

    // FOR TEST HARD DELETE CHECK
    fun getPhotoByIdIncludeDeleted(id: String): Flow<PhotoEntity?>
    fun getPhotosByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PhotoEntity>>
    fun getAllPhotosIncludeDeleted(): Flow<List<PhotoEntity>>
}