package com.dcac.realestatemanager.data.offlineDatabase.photo

import com.dcac.realestatemanager.model.Photo
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {

    fun getPhotoById(id: Long): Flow<Photo?>
    fun getPhotosByPropertyId(propertyId: Long): Flow<List<Photo>>
    fun getAllPhotos(): Flow<List<Photo>>

    suspend fun insertPhotos(photos: List<Photo>)
    suspend fun insertPhoto(photo: Photo)
    suspend fun deletePhotosByPropertyId(propertyId: Long)
    suspend fun deletePhoto(photo: Photo)

    fun getUnSyncedPhotos(): Flow<List<Photo>>

}