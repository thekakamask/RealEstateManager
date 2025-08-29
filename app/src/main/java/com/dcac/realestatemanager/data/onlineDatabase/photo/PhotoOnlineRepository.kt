package com.dcac.realestatemanager.data.onlineDatabase.photo

import com.dcac.realestatemanager.model.Photo

interface PhotoOnlineRepository {

    suspend fun uploadPhoto(photo: Photo, photoId: String) : Photo
    suspend fun getPhoto(photoId: String) : Photo?
    suspend fun getPhotosByPropertyId(propertyId: Long) : List<Photo>
    suspend fun deletePhoto(photoId: String)
    suspend fun deletePhotosByPropertyId(propertyId: Long)

}