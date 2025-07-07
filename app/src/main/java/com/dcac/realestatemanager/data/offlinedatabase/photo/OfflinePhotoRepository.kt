package com.dcac.realestatemanager.data.offlinedatabase.photo

import com.dcac.realestatemanager.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePhotoRepository(
    private val photoDao: PhotoDao
): PhotoRepository {

    // Returns a flow of model Photos filtered by the given property ID.
    override fun getPhotosByPropertyId(propertyId: Long): Flow<List<Photo>> =
        photoDao.getPhotosByPropertyId(propertyId).map { list -> list.map { it.toModel() } }

    // Returns a flow of all model Photos stored in the database.
    override fun getAllPhotos(): Flow<List<Photo>> =
        photoDao.getAllPhotos().map { list -> list.map { it.toModel() } }

    // Inserts a list of PhotoEntities into the database.
    override suspend fun insertPhotos(photos: List<PhotoEntity>) = photoDao.insertPhotos(photos)

    override suspend fun insertPhoto(photo: PhotoEntity) = photoDao.insertPhoto(photo)

    // Deletes all photos associated with the specified property ID.
    override suspend fun deletePhotosByPropertyId(propertyId: Long) = photoDao.deletePhotosByPropertyId(propertyId)

    // Deletes a specific photo entity from the database.
    override suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
}