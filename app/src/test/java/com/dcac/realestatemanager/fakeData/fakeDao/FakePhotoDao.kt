package com.dcac.realestatemanager.fakeData.fakeDao

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FakePhotoDao : PhotoDao,
    BaseFakeDao<Long, PhotoEntity>({ it.id }) {


    // pre-filling with photos
    init {
        seed(FakePhotoEntity.photoEntityList)
    }

    // Return a single photo by its ID, or null if not found
    override fun getPhotoById(id: Long): Flow<PhotoEntity?> =
        entityFlow.map { list -> list.find { it.id == id } }

    override fun getPhotosByPropertyId(propertyId: Long): Flow<List<PhotoEntity>> =
        entityFlow.map { list -> list.filter { it.propertyId == propertyId } }

    override suspend fun insertPhotos(photos: List<PhotoEntity>) {
        photos.forEach { upsert(it) }
    }


    override suspend fun insertPhoto(photo: PhotoEntity) {
        upsert(photo)
    }


    override suspend fun deletePhotosByPropertyId(propertyId: Long) {
        val toDelete = entityMap.values.filter { it.propertyId == propertyId }
        toDelete.forEach { delete(it) }
    }


    override suspend fun deletePhoto(photo: PhotoEntity) {
        delete(photo)
    }


    override fun getAllPhotos(): Flow<List<PhotoEntity>> =
        entityFlow
}