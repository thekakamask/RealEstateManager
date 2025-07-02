package com.openclassrooms.realestatemanager.data.photo

import kotlinx.coroutines.flow.Flow

class OfflinePhotoRepository(
    private val photoDao: PhotoDao
): PhotoRepository {
    override fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>>
    = photoDao.getPhotosByPropertyId(propertyId)

    override suspend fun insertPhotos(photos: List<PhotoEntity>)
    = photoDao.insertPhotos(photos)

    override suspend fun deletePhotosByPropertyId(propertyId: Long)
    = photoDao.deletePhotosByPropertyId(propertyId)

    override suspend fun deletePhoto(photo: PhotoEntity)
    = photoDao.deletePhoto(photo)
}