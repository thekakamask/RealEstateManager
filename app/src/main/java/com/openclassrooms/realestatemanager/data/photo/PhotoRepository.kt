package com.openclassrooms.realestatemanager.data.photo

import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>>
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    suspend fun deletePhotosByPropertyId(propertyId: Long)
    suspend fun deletePhoto(photo: PhotoEntity)

}