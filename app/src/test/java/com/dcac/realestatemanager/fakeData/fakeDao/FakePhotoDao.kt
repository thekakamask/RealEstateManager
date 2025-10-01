package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePhotoDao : PhotoDao,
    BaseFakeDao<Long, PhotoEntity>({ it.id }) {

    init {
        seed(FakePhotoEntity.photoEntityList)
    }

    override fun getPhotoById(id: Long): Flow<PhotoEntity?> =
        entityFlow.map { list -> list.find { it.id == id && !it.isDeleted } }

    override fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId && !it.isDeleted } }

    override fun getAllPhotos(): Flow<List<PhotoEntity>> =
        entityFlow.map { list -> list.filter { !it.isDeleted } }

    override suspend fun insertPhotos(photos: List<PhotoEntity>) {
        photos.forEach { insertPhoto(it) }
    }

    override suspend fun insertPhoto(photo: PhotoEntity) {
        insertPhotoForcedSyncFalse(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    override suspend fun insertPhotoForcedSyncFalse(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PhotoEntity(
                id = id,
                propertyId = propertyId,
                uri = uri,
                description = description,
                isDeleted = isDeleted,
                isSynced = false,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun updatePhotoForcedSyncFalse(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        insertPhotoForcedSyncFalse(id, propertyId, uri, description, isDeleted, updatedAt)
    }

    override suspend fun updatePhoto(photo: PhotoEntity) {
        updatePhotoForcedSyncFalse(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    override suspend fun savePhotoFromFirebaseForcedSyncTrue(
        id: Long,
        propertyId: Long,
        uri: String,
        description: String?,
        isDeleted: Boolean,
        updatedAt: Long
    ) {
        upsert(
            PhotoEntity(
                id = id,
                propertyId = propertyId,
                uri = uri,
                description = description,
                isDeleted = isDeleted,
                isSynced = true,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun savePhotoFromFirebase(photo: PhotoEntity) {
        savePhotoFromFirebaseForcedSyncTrue(
            id = photo.id,
            propertyId = photo.propertyId,
            uri = photo.uri,
            description = photo.description,
            isDeleted = photo.isDeleted,
            updatedAt = photo.updatedAt
        )
    }

    override suspend fun markPhotosAsDeletedByProperty(propertyId: Long, updatedAt: Long) {
        val updated = entityMap.values.map {
            if (it.propertyId == propertyId)
                it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            else it
        }
        seed(updated)
    }

    override suspend fun markPhotoAsDeleted(id: Long, updatedAt: Long) {
        entityMap[id]?.let {
            val updated = it.copy(isDeleted = true, isSynced = false, updatedAt = updatedAt)
            upsert(updated)
        }
    }

    override suspend fun deletePhoto(photo: PhotoEntity) {
        delete(photo)
    }

    override suspend fun deletePhotosByPropertyId(propertyId: Long) {
        val toDelete = entityMap.values.filter { it.propertyId == propertyId }
        toDelete.forEach { delete(it) }
    }

    override fun getPhotoByIdIncludeDeleted(id: Long): Flow<PhotoEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override fun getPhotosByPropertyIdIncludeDeleted(propertyId: Long): Flow<List<PhotoEntity>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId } }

    override fun getUnSyncedPhotos(): Flow<List<PhotoEntity>> =
        entityFlow.map { list -> list.filter { !it.isSynced } }

    override fun getAllPhotosAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("getAllPhotosAsCursor is not used in unit tests.")
    }
}
