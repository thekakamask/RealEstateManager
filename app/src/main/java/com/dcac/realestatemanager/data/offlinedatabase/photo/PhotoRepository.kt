package com.dcac.realestatemanager.data.offlinedatabase.photo

import com.dcac.realestatemanager.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    fun getPhotosByPropertyId(propertyId: Long): Flow<List<Photo>>
    fun getAllPhotos(): Flow<List<Photo>>

    suspend fun insertPhotos(photos: List<PhotoEntity>)
    suspend fun insertPhoto(photo: PhotoEntity)
    suspend fun deletePhotosByPropertyId(propertyId: Long)
    suspend fun deletePhoto(photo: PhotoEntity)

}