package com.dcac.realestatemanager.data.firebaseDatabase.photo

interface PhotoOnlineRepository {

    suspend fun uploadPhoto(photo: PhotoOnlineEntity, firebasePhotoId: String): PhotoOnlineEntity
    suspend fun getPhoto(firebasePhotoId: String): PhotoOnlineEntity?
    suspend fun getPhotosByPropertyId(firebasePropertyId: String): List<PhotoOnlineEntity>
    suspend fun getAllPhotos(): List<FirestorePhotoDocument>
    suspend fun deletePhoto(firebasePhotoId: String)
    suspend fun deletePhotosByPropertyId(firebasePropertyId: String)
    suspend fun downloadImageLocally(storageUrl: String): String
}