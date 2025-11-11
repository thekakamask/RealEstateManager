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
    override fun getPhotoById(id: String): Flow<Photo?> =
        photoDao.getPhotoById(id).map { it?.toModel() }
    override fun getPhotosByPropertyId(propertyId: String): Flow<List<Photo>> =
        photoDao.getPhotosByPropertyId(propertyId).map { list -> list.map { it.toModel() } }
    override fun getAllPhotos(): Flow<List<Photo>> =
        photoDao.getAllPhotos().map { list -> list.map { it.toModel() } }

    //SYNC
    override fun uploadUnSyncedPhotosToFirebase(): Flow<List<PhotoEntity>> =
        photoDao.uploadUnSyncedPhotos()

    //INSERTIONS
    //INSERTIONS FROM UI
    override suspend fun insertPhotoInsertFromUI(photo: Photo): String =
        photoDao.insertPhotoInsertFromUI(photo.toEntity())
    override suspend fun insertPhotosInsertFromUI(photos: List<Photo>) =
        photoDao.insertPhotosInsertFromUI(photos.map { it.toEntity() })

    //INSERTIONS FROM FIREBASE
    override suspend fun insertPhotoInsertFromFirebase(
        photo: PhotoOnlineEntity,
        firestoreId: String,
        localUri: String
    ) {
        val entity = photo.toEntity(firestoreId = firestoreId).copy(uri = localUri)
        photoDao.insertPhotoInsertFromFirebase(entity)
    }
    override suspend fun insertPhotosInsertFromFirebase(
        photos: List<Triple<PhotoOnlineEntity, String, String>>
    ) {
        val entities = photos.map { (photo, firestoreId, localUri) ->
            photo.toEntity(firestoreId = firestoreId).copy(uri = localUri)
        }
        photoDao.insertAllPhotosNotExistingFromFirebase(entities)
    }

    //UPDATE
    override suspend fun updatePhotoFromUI(photo: Photo) {
        photoDao.updatePhotoFromUIForceSyncFalse(photo.toEntity())
    }
    override suspend fun updatePhotoFromFirebase(photo: PhotoOnlineEntity, firestoreId: String) {
        photoDao.updatePhotoFromFirebaseForceSyncTrue(photo.toEntity(firestoreId = firestoreId))
    }
    override suspend fun updateAllPhotosFromFirebase(photos: List<Pair<PhotoOnlineEntity, String>>) {
        val entities = photos.map { (photo, firestoreId) ->
            photo.toEntity(firestoreId = firestoreId)
        }
        photoDao.updateAllPhotosFromFirebaseForceSyncTrue(entities)
    }

    //SOFT DELETE
    override suspend fun markPhotoAsDeleted(photo: Photo) {
        photoDao.markPhotoAsDeleted(photo.universalLocalId, System.currentTimeMillis())
    }
    override suspend fun markPhotosAsDeletedByProperty(propertyId: String) {
        photoDao.markPhotosAsDeletedByProperty(propertyId, System.currentTimeMillis())
    }

    //HARD DELETE
    override suspend fun deletePhotosByPropertyId(propertyId: String) {
        photoDao.deletePhotosByPropertyId(propertyId)
    }
    override suspend fun deletePhoto(photo: PhotoEntity) {
        photoDao.deletePhoto(photo)
    }
    override suspend fun clearAllPhotosDeleted() {
        photoDao.clearAllPhotosDeleted()
    }

    // FOR TEST HARD DELETE CHECK
    override fun getPhotoByIdIncludeDeleted(id: String): Flow<PhotoEntity?> =
        photoDao.getPhotoByIdIncludeDeleted(id)
    override fun getPhotosByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PhotoEntity>> =
        photoDao.getPhotosByPropertyIdIncludeDeleted(propertyId)
    override fun getAllPhotosIncludeDeleted(): Flow<List<PhotoEntity>> =
        photoDao.getAllPhotosIncludeDeleted()
}