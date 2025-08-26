package com.dcac.realestatemanager.data.offlineDatabase.photo

import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePhotoRepository(
    private val photoDao: PhotoDao
) : PhotoRepository {

    override fun getPhotoById(id: Long): Flow<Photo?> =
        photoDao.getPhotoById(id).map { it?.toModel() }

    override fun getPhotosByPropertyId(propertyId: Long): Flow<List<Photo>> =
        photoDao.getPhotosByPropertyId(propertyId).map { list -> list.map { it.toModel() } }

    override fun getAllPhotos(): Flow<List<Photo>> =
        photoDao.getAllPhotos().map { list -> list.map { it.toModel() } }

    override suspend fun insertPhotos(photos: List<Photo>) =
        photoDao.insertPhotos(photos.map { it.toEntity() })

    override suspend fun insertPhoto(photo: Photo) =
        photoDao.insertPhoto(photo.toEntity())

    override suspend fun deletePhotosByPropertyId(propertyId: Long) =
        photoDao.deletePhotosByPropertyId(propertyId)

    override suspend fun deletePhoto(photo: Photo) =
        photoDao.deletePhoto(photo.toEntity())
}