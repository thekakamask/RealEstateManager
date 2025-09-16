package com.dcac.realestatemanager.data.firebaseDatabase.photo

interface PhotoOnlineRepository {

    suspend fun uploadPhoto(photo: PhotoOnlineEntity, photoId: String): PhotoOnlineEntity
    suspend fun getPhoto(photoId: String): PhotoOnlineEntity?
    suspend fun getPhotosByPropertyId(propertyId: Long): List<PhotoOnlineEntity>
    suspend fun getAllPhotos(): List<PhotoOnlineEntity>
    suspend fun deletePhoto(photoId: String)
    suspend fun deletePhotosByPropertyId(propertyId: Long)
}