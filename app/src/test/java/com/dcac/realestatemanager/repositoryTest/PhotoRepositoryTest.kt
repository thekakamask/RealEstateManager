package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePhotoOnlineEntity
import com.dcac.realestatemanager.model.Photo
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class PhotoRepositoryTest {

    private lateinit var fakePhotoDao : FakePhotoDao
    private lateinit var photoRepository : PhotoRepository

    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity3 = FakePhotoEntity.photo3
    private val allPhotosEntity = FakePhotoEntity.photoEntityList
    private val allPhotosEntityNotDeleted = FakePhotoEntity.photoEntityListNotDeleted
    private val photoOnlineEntity1 = FakePhotoOnlineEntity.photoOnline1
    private val photoOnlineEntity2 = FakePhotoOnlineEntity.photoOnline2
    private val photoOnlineEntity3 = FakePhotoOnlineEntity.photoOnline3
    private val photoModel1 = FakePhotoModel.photo1
    private val photoModel2 = FakePhotoModel.photo2
    private val photoModel3 = FakePhotoModel.photo3
    private val allPhotosModelNotDeleted = FakePhotoModel.photoModelListNotDeleted

    @Before
    fun setup() {
        fakePhotoDao = FakePhotoDao()
        photoRepository = OfflinePhotoRepository(fakePhotoDao)
    }

    @Test
    fun getPhotoById_shouldReturnsCorrectPhoto() = runTest {
        val result = photoRepository.getPhotoById(photoModel1.universalLocalId).first()

        assertEquals(photoModel1, result)
    }

    @Test
    fun getPhotosByPropertyId_shouldReturnsCorrectPhotos() = runTest {
        val result = photoRepository.getPhotosByPropertyId(photoModel2.universalLocalPropertyId).first()

        val expected = allPhotosModelNotDeleted
            .filter { it.universalLocalPropertyId == photoModel2.universalLocalPropertyId }
        assertEquals(expected, result)
    }

    @Test
    fun getAllPhotos_shouldReturnsAllPhotos() = runTest {
        val result = photoRepository.getAllPhotos().first()

        assertEquals(allPhotosModelNotDeleted, result)
    }

    @Test
    fun uploadUnSyncedPhotos_shouldReturnOnlyPhotosWithIsSyncedFalse() = runTest {
        val result = photoRepository.uploadUnSyncedPhotosToFirebase().first()

        val expected = allPhotosEntity
            .filter { !it.isSynced }

        assertEquals(expected, result)
    }

    @Test
    fun insertPhotoInsertFromUI_shouldInsertWithIsSyncedFalse() = runTest {
        val newPhotoModel = Photo(
            universalLocalId = "photo-4",
            universalLocalPropertyId = photoModel1.universalLocalPropertyId,
            uri = "file://photo_4.jpg",
            description = "New Photo 4",
            isDeleted = false,
            isSynced = false,
            updatedAt = 1800000000000L
        )
        photoRepository.insertPhotoInsertFromUI(newPhotoModel)

        val resultEntity = fakePhotoDao.entityMap[newPhotoModel.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(newPhotoModel.universalLocalId, id)
            assertEquals(newPhotoModel.universalLocalPropertyId, universalLocalPropertyId)
            assertEquals(newPhotoModel.uri, uri)
            assertEquals(newPhotoModel.description, description)
            assertFalse(isSynced)
            assertFalse(isDeleted)
            assertEquals(newPhotoModel.updatedAt, updatedAt)
        }

        val resultInserted = photoRepository
            .getPhotoById(newPhotoModel.universalLocalId)
            .first()

        assertEquals(newPhotoModel, resultInserted)
    }

    @Test
    fun insertPhotosInsertFromUI_shouldInsertAllWithIsSyncedFalse() = runTest {
        val insertedTimestamp = 1800000000000L
        val newPhotos = listOf(
            Photo(
                universalLocalId = "photo-4",
                universalLocalPropertyId = photoModel1.universalLocalPropertyId,
                uri = "file://photo_4.jpg",
                description = "New photo 4",
                isDeleted = false,
                isSynced = false,
                updatedAt = insertedTimestamp + 1
            ),
            Photo(
                universalLocalId = "photo-5",
                universalLocalPropertyId = photoModel2.universalLocalPropertyId,
                uri = "file://photo_5.jpg",
                description = "New photo 5",
                isDeleted = false,
                isSynced = false,
                updatedAt = insertedTimestamp + 2
            ),
            Photo(
                universalLocalId = "photo-6",
                universalLocalPropertyId = photoModel3.universalLocalPropertyId,
                uri = "file://photo_6.jpg",
                description = "New photo 6",
                isDeleted = false,
                isSynced = false,
                updatedAt = insertedTimestamp + 3
            )
        )

        photoRepository.insertPhotosInsertFromUI(newPhotos)

        newPhotos.forEach { expected ->
            val entity = fakePhotoDao.entityMap[expected.universalLocalId]
            assertNotNull(entity)
            entity!!.apply {
                assertEquals(expected.universalLocalId, id)
                assertEquals(expected.universalLocalPropertyId, universalLocalPropertyId)
                assertEquals(expected.uri, uri)
                assertEquals(expected.description, description)
                assertFalse(isSynced)
                assertFalse(isDeleted)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }

        newPhotos.forEach { expected ->
            val result = photoRepository
                .getPhotoById(expected.universalLocalId)
                .first()

            assertEquals(expected, result)
        }
    }

    @Test
    fun insertPhotoInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runTest {
        val firestoreId = "firestore-photo-4"
        val localUri= "file://photo_4.jpg"

        val onlinePhoto = PhotoOnlineEntity(
            ownerUid = "firebase_uid_1",
            universalLocalId = "photo-4",
            universalLocalPropertyId = photoModel1.universalLocalPropertyId,
            description = "Photo from firebase",
            storageUrl = "https://firebase.storage/photo_4.jpg",
            isDeleted = false,
            updatedAt = 1900000000000L
        )

        photoRepository.insertPhotoInsertFromFirebase(
            photo = onlinePhoto,
            firestoreId = firestoreId,
            localUri = localUri
        )

        val resultEntity = fakePhotoDao.entityMap[onlinePhoto.universalLocalId]

        assertNotNull(resultEntity)
        resultEntity!!.apply {
            assertEquals(onlinePhoto.universalLocalId, resultEntity.id)
            assertEquals(firestoreId, resultEntity.firestoreDocumentId)
            assertEquals(localUri, resultEntity.uri)
            assertTrue(resultEntity.isSynced)
            assertEquals(onlinePhoto.updatedAt, resultEntity.updatedAt)
        }

        val resultInserted = photoRepository
            .getPhotoById(onlinePhoto.universalLocalId)
            .first()

        assertNotNull(resultInserted)
        resultInserted!!.apply{
            assertEquals(firestoreId, resultInserted.firestoreDocumentId)
            assertEquals(onlinePhoto.universalLocalId, resultInserted.universalLocalId)
            assertEquals(onlinePhoto.description, resultInserted.description)
            assertTrue(resultInserted.isSynced)
            assertEquals(onlinePhoto.updatedAt, resultInserted.updatedAt)
        }
    }

    @Test
    fun insertPhotosInsertFromFirebase_shouldInsertAllWithIsSyncedTrue() = runTest {
        val insertedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-photo-4",
            "firestore-photo-5",
            "firestore-photo-6"
        )
        val localUris = listOf(
            "file://photo_4.jpg",
            "file://photo_5.jpg",
            "file://photo_6.jpg"
        )

        val onlinePhotos = listOf(
            PhotoOnlineEntity(
                ownerUid = "firebase_uid_1",
                universalLocalId = "photo-4",
                universalLocalPropertyId = photoModel1.universalLocalPropertyId,
                description = "Photo from firebase",
                storageUrl = "https://firebase.storage/photo4.jpg",
                isDeleted = false,
                updatedAt = insertedTimestamp + 1
            ),
            PhotoOnlineEntity(
                ownerUid = "firebase_uid_2",
                universalLocalId = "photo-5",
                universalLocalPropertyId = photoModel2.universalLocalPropertyId,
                description = "Photo from firebase",
                storageUrl = "https://firebase.storage/photo5.jpg",
                isDeleted = false,
                updatedAt = insertedTimestamp + 2
            ),
            PhotoOnlineEntity(
                ownerUid = "firebase_uid_3",
                universalLocalId = "photo-6",
                universalLocalPropertyId = photoModel3.universalLocalPropertyId,
                description = "Photo from firebase",
                storageUrl = "https://firebase.storage/photo6.jpg",
                isDeleted = false ,
                updatedAt = insertedTimestamp + 3
            )
        )

        val triples = onlinePhotos.mapIndexed { index, photo ->
            Triple(photo, firestoreIds[index], localUris[index])
        }

        photoRepository.insertPhotosInsertFromFirebase(triples)

        onlinePhotos.forEachIndexed { index, expected ->

            val resultEntity = fakePhotoDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)
            resultEntity!!.apply{
                assertEquals(expected.universalLocalId, resultEntity.id)
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertEquals(localUris[index], resultEntity.uri)
                assertEquals(expected.description, resultEntity.description)
                assertTrue(resultEntity.isSynced)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
            }
        }

        val allPhotos = photoRepository.getAllPhotos().first()

        onlinePhotos.forEachIndexed { index, expected ->

            val resultInserted = allPhotos.find {
                it.universalLocalId == expected.universalLocalId
            }

            assertNotNull(resultInserted)

            resultInserted!!.apply {
                assertEquals(firestoreIds[index], resultInserted.firestoreDocumentId)
                assertEquals(expected.universalLocalId, resultInserted.universalLocalId)
                assertEquals(localUris[index], resultInserted.uri)
                assertEquals(expected.description, resultInserted.description)
                assertTrue(resultInserted.isSynced)
                assertEquals(expected.updatedAt, resultInserted.updatedAt)
            }
        }
    }

    @Test
    fun updatePhotoFromUI_shouldUpdatePhotoAndForceSyncFalse() = runTest {
        val updatedTimeStamp = 1800000000000L
        val updatedPhoto = photoModel1.copy(
            description = "Updated description",
            updatedAt = updatedTimeStamp,
            isSynced = true
        )

        photoRepository.updatePhotoFromUI(updatedPhoto)

        val resultEntity = fakePhotoDao.entityMap[updatedPhoto.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated description", resultEntity.description)
            assertFalse(resultEntity.isSynced)
            assertEquals(updatedTimeStamp, updatedAt)
        }

        val resultUpdated = photoRepository
            .getPhotoById(updatedPhoto.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated description", resultUpdated.description)
            assertFalse(resultUpdated.isSynced)
            assertEquals(updatedTimeStamp, resultUpdated.updatedAt)
        }
    }

    @Test
    fun updatePhotoFromFirebase_shouldUpdatePhotoAndForceSyncTrue() = runTest {
        val firestoreId = "firestore-photo-1"
        val updatedTimestamp = 1900000000000L
        val updatedOnlinePhoto = photoOnlineEntity1.copy(
                description = "Updated from Firebase",
                updatedAt = updatedTimestamp
        )

        photoRepository.updatePhotoFromFirebase(
            photo = updatedOnlinePhoto,
            firestoreId = firestoreId
        )

        val resultEntity = fakePhotoDao.entityMap[updatedOnlinePhoto.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated from Firebase", resultEntity.description)
            assertEquals(firestoreId, resultEntity.firestoreDocumentId)
            assertTrue(resultEntity.isSynced)
            assertEquals(updatedTimestamp, resultEntity.updatedAt)
        }

        val resultUpdated = photoRepository
            .getPhotoById(updatedOnlinePhoto.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated from Firebase", resultUpdated.description)
            assertEquals(firestoreId, resultUpdated.firestoreDocumentId)
            assertTrue(resultUpdated.isSynced)
            assertEquals(updatedTimestamp, resultUpdated.updatedAt)
        }
    }

    @Test
    fun updateAllPhotosFromFirebase_shouldUpdateAllPhotos() = runTest {
        val updatedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-photo-1",
            "firestore-photo-2",
            "firestore-photo-3"
        )
        val updatedPhotosFromFirebase = listOf(
            photoOnlineEntity1.copy(
                description = "Updated from Firebase 1",
                updatedAt = updatedTimestamp + 1
            ),
            photoOnlineEntity2.copy(
                description = "Updated from Firebase 2",
                updatedAt = updatedTimestamp + 2
            ),
            photoOnlineEntity3.copy(
                description = "Updated from Firebase 3",
                updatedAt = updatedTimestamp + 3
            )
        )

        val pairs = updatedPhotosFromFirebase.mapIndexed { index, photo ->
            photo to firestoreIds[index]
        }

        photoRepository.updateAllPhotosFromFirebase(pairs)

        updatedPhotosFromFirebase.forEachIndexed { index, expected ->

            val resultEntity = fakePhotoDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.description, resultEntity.description)
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
                assertTrue(resultEntity.isSynced)
            }
        }
        val allPhotos = photoRepository.getAllPhotosIncludeDeleted().first()

        updatedPhotosFromFirebase.forEachIndexed { index, expected ->
            val resultUpdated = allPhotos.find {
                it.id == expected.universalLocalId
            }
            assertNotNull(resultUpdated)
            resultUpdated!!.apply {
                assertEquals(firestoreIds[index], resultUpdated.firestoreDocumentId)
                assertEquals(expected.description, resultUpdated.description)
                assertEquals(expected.updatedAt, resultUpdated.updatedAt)
                assertTrue(resultUpdated.isSynced)
            }
        }
    }

    @Test
    fun markPhotoAsDelete_shouldHidePhotoFromQueries() = runTest {
        photoRepository.markPhotoAsDeleted(photoModel2)

        val rawEntity = fakePhotoDao.entityMap[photoModel2.universalLocalId]
        assertNotNull(rawEntity)
        rawEntity!!.apply {
            assertTrue(rawEntity.isDeleted)
            assertFalse(rawEntity.isSynced)
        }

        val result = photoRepository.getAllPhotos().first()
        assertFalse(result.contains(photoModel2))
    }

    @Test
    fun markPhotosAsDeletedByProperty_shouldHidePhotoFromQueries() = runTest {
        photoRepository.markPhotosAsDeletedByProperty(photoModel2.universalLocalPropertyId)

        val rawEntities = fakePhotoDao.entityMap.values.filter {
            it.universalLocalPropertyId == photoModel2.universalLocalPropertyId
        }

        assertNotNull(rawEntities)

        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })
        }


        val result = photoRepository
            .getPhotosByPropertyId(photoModel2.universalLocalPropertyId)
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deletePhotosByPropertyId_shouldDeletePhotos() = runTest {
        val beforeDelete = fakePhotoDao.entityMap.values
            .filter { it.universalLocalPropertyId == photoEntity3.universalLocalPropertyId }
        assertTrue(beforeDelete.isNotEmpty())

        photoRepository.deletePhotosByPropertyId(photoEntity3.universalLocalPropertyId)

        val resultEntity = fakePhotoDao.entityMap.values
            .filter { it.universalLocalPropertyId == photoEntity3.universalLocalPropertyId }

        assertTrue(resultEntity.isEmpty())

        val resultDeleted = photoRepository
            .getPhotosByPropertyIdIncludeDeleted(photoEntity3.universalLocalPropertyId)
            .first()

        assertTrue(resultDeleted.isEmpty())
    }

    @Test
    fun deletePhoto_shouldDeletePhoto() = runTest {
        val beforeDelete = fakePhotoDao.entityMap.containsKey(photoEntity3.id)
        assertTrue(beforeDelete)

        photoRepository.deletePhoto(photoEntity3)

        val resultEntity = fakePhotoDao.entityMap.containsKey(photoEntity3.id)
        assertFalse(resultEntity)

        val resultDeleted = photoRepository
            .getPhotoByIdIncludeDeleted(photoEntity3.id).first()
        assertNull(resultDeleted)
    }

    @Test
    fun clearAllPhotosDeleted_shouldDeleteOnlyDeletedPhotos() = runTest {
        photoRepository.markPhotoAsDeleted(photoModel1)

        assertTrue(fakePhotoDao.entityMap[photoModel1.universalLocalId]!!.isDeleted)
        assertTrue(fakePhotoDao.entityMap[photoModel3.universalLocalId]!!.isDeleted)
        assertFalse(fakePhotoDao.entityMap[photoModel2.universalLocalId]!!.isDeleted)

        photoRepository.clearAllPhotosDeleted()

        assertFalse(fakePhotoDao.entityMap.containsKey(photoModel1.universalLocalId))
        assertFalse(fakePhotoDao.entityMap.containsKey(photoModel3.universalLocalId))
        assertTrue(fakePhotoDao.entityMap.containsKey(photoModel2.universalLocalId))

        val allPhotos = photoRepository.getAllPhotosIncludeDeleted().first()

        assertFalse(allPhotos.any { it.id == photoModel1.universalLocalId })
        assertFalse(allPhotos.any { it.id == photoModel3.universalLocalId })
        assertTrue(allPhotos.any { it.id == photoModel2.universalLocalId })
    }

    @Test
    fun getPhotoByIdIncludeDeleted_shouldReturnDeletedPhoto() = runTest {
        photoRepository.markPhotoAsDeleted(photoModel1)

        val result = photoRepository
            .getPhotoByIdIncludeDeleted(photoModel1.universalLocalId)
            .first()

        assertNotNull(result)
        result!!.apply {
            assertEquals(photoModel1.universalLocalId, result.id)
            assertTrue(result.isDeleted)
        }
    }

    @Test
    fun getPhotosByPropertyIdIncludeDeleted_shouldReturnDeletedPhotos() = runTest {
        val propertyId = photoModel1.universalLocalPropertyId
        photoRepository.markPhotoAsDeleted(photoModel1)

        val result = photoRepository
            .getPhotosByPropertyIdIncludeDeleted(propertyId)
            .first()

        assertTrue(result.any { it.id == photoModel1.universalLocalId })
        val deletedPhoto = result.find { it.id == photoModel1.universalLocalId }
        assertTrue(deletedPhoto!!.isDeleted)
    }

    @Test
    fun getAllPhotosIncludeDeleted_shouldReturnAllPhotosEvenDeleted() = runTest {

        photoRepository.markPhotoAsDeleted(photoModel2)

        val totalBefore = fakePhotoDao.entityMap.size

        val result = photoRepository
            .getAllPhotosIncludeDeleted()
            .first()

        assertEquals(totalBefore, result.size)
        assertTrue(result.any { it.id == photoModel2.universalLocalId && it.isDeleted })
    }
}