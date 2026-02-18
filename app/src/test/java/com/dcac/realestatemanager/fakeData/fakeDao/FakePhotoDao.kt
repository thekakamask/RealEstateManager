package com.dcac.realestatemanager.fakeData.fakeDao

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteQuery
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePhotoDao : PhotoDao,
    BaseFakeDao<String, PhotoEntity>({ it.id }) {

    init {
        seed(FakePhotoEntity.photoEntityList)
    }

    override fun getPhotoById(id: String): Flow<PhotoEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id && !it.isDeleted }
        }

    override fun getPhotosByPropertyId(propertyId: String): Flow<List<PhotoEntity>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPropertyId == propertyId && !it.isDeleted }
        }

    override fun getAllPhotos(): Flow<List<PhotoEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isDeleted }
        }

    override fun uploadUnSyncedPhotos(): Flow<List<PhotoEntity>> =
        entityFlow.map { list ->
            list.filter { !it.isSynced }
        }

    override suspend fun firstPhotoInsert(photo: PhotoEntity) {
        if (!entityMap.containsKey(photo.id)) {
            upsert(photo)
        }
    }

    override suspend fun insertPhotoIfNotExists(photo: PhotoEntity) {
        if (!entityMap.containsKey(photo.id)) {
            upsert(photo)
        }
    }

    override suspend fun updatePhoto(photo: PhotoEntity) {
        upsert(photo)
    }

    override suspend fun markPhotosAsDeletedByProperty(propertyId: String, updatedAt: Long) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .forEach {
                upsert(
                    it.copy(
                        isDeleted = true,
                        isSynced = false,
                        updatedAt = updatedAt
                    )
                )
            }
    }

    override suspend fun markPhotoAsDeleted(id: String, updatedAt: Long) {
        entityMap[id]?.let {
            upsert(
                it.copy(
                    isDeleted = true,
                    isSynced = false,
                    updatedAt = updatedAt
                )
            )
        }
    }

    override suspend fun deletePhoto(photo: PhotoEntity) {
        delete(photo)
    }

    override suspend fun deletePhotosByPropertyId(propertyId: String) {
        entityMap.values
            .filter { it.universalLocalPropertyId == propertyId }
            .toList()
            .forEach { delete(it) }
    }

    override suspend fun clearAllPhotosDeleted() {
        entityMap.values
            .filter { it.isDeleted }
            .toList()
            .forEach { delete(it) }
    }

    override fun getPhotoByIdIncludeDeleted(id: String): Flow<PhotoEntity?> =
        entityFlow.map { list ->
            list.find { it.id == id }
        }

    override fun getPhotosByPropertyIdIncludeDeleted(propertyId: String): Flow<List<PhotoEntity>> =
        entityFlow.map { list ->
            list.filter { it.universalLocalPropertyId == propertyId }
        }

    override fun getAllPhotosIncludeDeleted(): Flow<List<PhotoEntity>> =
        entityFlow

    override fun getAllPhotosAsCursor(query: SupportSQLiteQuery): Cursor {
        throw NotImplementedError("Cursor not needed for unit tests")
    }
}
