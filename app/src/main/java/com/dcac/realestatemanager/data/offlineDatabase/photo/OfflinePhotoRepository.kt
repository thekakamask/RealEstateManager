package com.dcac.realestatemanager.data.offlineDatabase.photo

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.model.Photo
import com.dcac.realestatemanager.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dcac.realestatemanager.utils.toModel

class OfflinePhotoRepository(
    private val photoDao: PhotoDao
) : PhotoRepository {

    //FOR UI

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

    override suspend fun updatePhoto(photo: Photo) =
        photoDao.updatePhoto(photo.toEntity())

    override suspend fun markPhotosAsDeletedByProperty(propertyId: Long) =
        photoDao.markPhotosAsDeletedByProperty(propertyId, System.currentTimeMillis())

    override suspend fun markPhotoAsDelete(photo: Photo) {
        photoDao.markPhotoAsDeleted(photo.id, System.currentTimeMillis())
    }

    //FOR FIREBASE SYNC

    override fun getPhotoEntityById(id: Long): Flow<PhotoEntity?> =
        photoDao.getPhotoById(id)

    override suspend fun deletePhoto(photo: PhotoEntity) =
        photoDao.deletePhoto(photo)

    override suspend fun deletePhotosByPropertyId(propertyId: Long) =
        photoDao.deletePhotosByPropertyId(propertyId)

    override fun uploadUnSyncedPhotosToFirebase(): Flow<List<PhotoEntity>> =
        photoDao.getUnSyncedPhotos()

    override suspend fun downloadPhotoFromFirebase(photo: PhotoOnlineEntity, localUri: String) {
        val entity = photo.toEntity(photoId = photo.roomId).copy(uri = localUri)
        photoDao.savePhotoFromFirebase(entity)
    }

    // --- FOR TEST / HARD DELETE CHECK ---
    override fun getPhotoByIdIncludeDeleted(id: Long): Flow<PhotoEntity?> =
        photoDao.getPhotoByIdIncludeDeleted(id)

    override fun getPhotosByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PhotoEntity>> =
        photoDao.getPhotosByPropertyIdIncludeDeleted(propertyId)
}