package com.dcac.realestatemanager.data.offlineDatabase.photo

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    //FOR UI

    fun getPhotoById(id: Long): Flow<Photo?>
    fun getPhotosByPropertyId(propertyId: Long): Flow<List<Photo>>
    fun getAllPhotos(): Flow<List<Photo>>
    suspend fun insertPhotos(photos: List<Photo>)
    suspend fun insertPhoto(photo: Photo)
    suspend fun updatePhoto(photo: Photo)
    suspend fun markPhotoAsDelete(photo: Photo)
    suspend fun markPhotosAsDeletedByProperty(propertyId: Long)

    //FOR FIREBASE SYNC

    fun getPhotoEntityById(id: Long): Flow<PhotoEntity?>
    suspend fun deletePhotosByPropertyId(propertyId: Long)
    suspend fun deletePhoto(photo: PhotoEntity)
    fun uploadUnSyncedPhotosToFirebase(): Flow<List<PhotoEntity>>
    suspend fun downloadPhotoFromFirebase(photo: PhotoOnlineEntity, localUri: String)

}