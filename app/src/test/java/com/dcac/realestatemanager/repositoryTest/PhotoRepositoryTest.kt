package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
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

    // Fake DAO simulating Room database
    private lateinit var fakePhotoDao : FakePhotoDao
    // Repository under test
    private lateinit var photoRepository : PhotoRepository

    private val photoEntity1 = FakePhotoEntity.photo1
    private val photoEntity2 = FakePhotoEntity.photo2
    private val photoEntity3 = FakePhotoEntity.photo3
    private val allPhotosEntity = FakePhotoEntity.photoEntityList
    private val allPhotosEntityNotDeleted = FakePhotoEntity.photoEntityListNotDeleted

    private val photoModel1 = FakePhotoModel.photo1
    private val photoModel2 = FakePhotoModel.photo2
    private val photoModel3 = FakePhotoModel.photo3
    private val allPhotosModel = FakePhotoModel.photoModelList
    private val allPhotosModelNotDeleted = FakePhotoModel.photoModelListNotDeleted

    @Before
    fun setup() {
        // Initialize fake DAO and repository before each test
        fakePhotoDao = FakePhotoDao()
        photoRepository = OfflinePhotoRepository(fakePhotoDao)
    }

    @Test
    fun getPhotoById_returnsCorrectPhoto() = runTest {
        val result = photoRepository.getPhotoById(photoModel1.id).first()
        // Verify that result matches expected
        assertEquals(photoModel1, result)
    }

    @Test
    fun getPhotosByPropertyId_returnsCorrectPhotos() = runTest {
        val result = photoRepository.getPhotosByPropertyId(photoModel2.propertyId).first()

        val expected = allPhotosModelNotDeleted
            .filter { it.propertyId == photoModel2.propertyId }
        assertEquals(expected, result)
    }

    @Test
    fun getAllPhotos_returnsAllPhotos() = runTest {
        // Fetch all photos via repository
        val result = photoRepository.getAllPhotos().first()
        // Expected full list of fake photos
        val expected = allPhotosModelNotDeleted
        // Verify full dataset matches
        assertEquals(expected, result)
    }


    @Test
    fun insertPhoto_insertsPhoto() = runTest {
        // --- Arrange ---
        val newPhotoModel = Photo(
            id = 9999L,
            propertyId = photoModel1.propertyId,
            uri = "content://photo/new",
            description = "New Photo",
            isDeleted = false,
            isSynced = false,
        )
        // --- Act ---
        photoRepository.insertPhoto(newPhotoModel)

        // --- Verify DAO state (Entity level) ---
        val resultEntity = fakePhotoDao.entityMap[newPhotoModel.id]

        assertEquals(newPhotoModel.id, resultEntity?.id)
        assertEquals(newPhotoModel.propertyId, resultEntity?.propertyId)
        assertEquals(newPhotoModel.uri, resultEntity?.uri)

        // --- Verify Repository result (Model level) ---
        val resultInserted = photoRepository.getPhotoById(newPhotoModel.id).first()

        assertNotNull(resultInserted)
        assertEquals(newPhotoModel.id, resultInserted?.id)
        assertEquals(newPhotoModel.uri, resultInserted?.uri)
    }

    @Test
    fun updatePhoto_onNonExistingPhoto_shouldInsertIt() = runTest {
        // --- Arrange ---
        val nonExistingPhoto = Photo(
            id = 7777L,
            propertyId = photoModel1.propertyId,
            uri = "file://new_photo.jpg",
            description = "Inserted via update",
            isDeleted = false,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        photoRepository.updatePhoto(nonExistingPhoto)

        // --- Assert ---
        val resultEntity = fakePhotoDao.entityMap[nonExistingPhoto.id]
        assertNotNull(resultEntity)
        assertEquals(nonExistingPhoto.id, resultEntity?.id)

        val resultModel = photoRepository.getPhotoById(nonExistingPhoto.id).first()
        assertNotNull(resultModel)
        assertEquals(nonExistingPhoto.uri, resultModel?.uri)
        assertEquals(nonExistingPhoto.description, resultModel?.description)
    }

    @Test
    fun insertPhotos_insertsNewPhotos() = runTest {
        // --- Arrange
        val newPhotos = listOf(
            Photo(
                id = 5001L,
                propertyId = photoModel1.propertyId,
                uri = "content://photo/new/1",
                description = "Nouvelle photo 1",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            Photo(
                id = 5002L,
                propertyId = photoModel2.propertyId,
                uri = "content://photo/new/2",
                description = "Nouvelle photo 2",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            Photo(
                id = 5003L,
                propertyId = photoModel3.propertyId,
                uri = "content://photo/new/3",
                description = "Nouvelle photo 3",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        // --- Act ---
        photoRepository.insertPhotos(newPhotos)

        // --- Verify DAO state ---
        newPhotos.forEach { expected ->
            val entity = fakePhotoDao.entityMap[expected.id]
            assertNotNull(entity)
            assertEquals(expected.id, entity?.id)
            assertEquals(expected.propertyId, entity?.propertyId)
            assertEquals(expected.uri, entity?.uri)
        }

        // --- Verify Repository state ---
        val allPhotos = photoRepository.getAllPhotos().first()
        newPhotos.forEach { expected ->
            val actual = allPhotos.find { it.id == expected.id }
            assertNotNull(actual)
            assertEquals(expected.id, actual!!.id)
            assertEquals(expected.propertyId, actual.propertyId)
            assertEquals(expected.uri, actual.uri)
            assertEquals(expected.description, actual.description)
        }
    }

    @Test
    fun updatePhoto_shouldModifyExistingPhoto() = runTest {
        val updated = photoModel2.copy(
            uri = "file://updated_uri.jpg",
            description = "Updated description",
            updatedAt = System.currentTimeMillis()
        )

        photoRepository.updatePhoto(updated)

        val result = photoRepository.getPhotoById(photoModel2.id).first()

        assertNotNull(result)
        assertEquals(updated.uri, result?.uri)
        assertEquals(updated.description, result?.description)
        assertFalse(result?.isSynced ?: true)
    }

    @Test
    fun markPhotoAsDelete_shouldHidePhotoFromQueries() = runTest {
        photoRepository.markPhotoAsDelete(photoModel2)

        // Still in entityMap (not hard deleted)
        val rawEntity = fakePhotoDao.entityMap[photoModel2.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // Should not appear in getAllPhotos anymore
        val result = photoRepository.getAllPhotos().first()
        assertFalse(result.contains(photoModel2))
    }

    @Test
    fun markPhotoAsDelete_calledTwice_shouldRemainDeleted() = runTest {
        // --- Act ---
        photoRepository.markPhotoAsDelete(photoModel2)
        photoRepository.markPhotoAsDelete(photoModel2)

        // --- DAO-level ---
        val rawEntity = fakePhotoDao.entityMap[photoModel2.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // --- Repository-level ---
        val result = photoRepository.getAllPhotos().first()
        assertFalse(result.contains(photoModel2))
    }

    @Test
    fun markPhotosAsDeletedByProperty_shouldHidePhotoFromQueries() = runTest {
        val beforeDeletion = photoRepository.getPhotosByPropertyId(photoModel2.propertyId).first()
        assertTrue(beforeDeletion.isNotEmpty())

        // --- Act ---
        photoRepository.markPhotosAsDeletedByProperty(photoModel2.propertyId)

        // --- Assert ---

        val remaining = fakePhotoDao.entityMap.values.filter { it.propertyId == photoModel2.propertyId }
        assertTrue(remaining.isNotEmpty())
        assertTrue(remaining.all { it.isDeleted })

        val result = photoRepository.getPhotosByPropertyId(photoModel2.propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getPhotoEntityById_returnsCorrectEntity() = runTest {
        // --- Arrange ---
        val expected = photoEntity1

        // --- Act ---
        val result = photoRepository.getPhotoEntityById(expected.id).first()

        // --- Assert ---
        assertNotNull(result)
        assertEquals(expected.id, result?.id)
        assertEquals(expected.uri, result?.uri)
        assertEquals(expected.propertyId, result?.propertyId)
        assertEquals(expected.description, result?.description)
        assertEquals(expected.isDeleted, result?.isDeleted)
        assertEquals(expected.updatedAt, result?.updatedAt)
    }

    @Test
    fun deletePhotosByPropertyId_deletesPhotos() = runTest {
        // --- Arrange ---
        val beforeDelete = photoRepository
            .getPhotosByPropertyIdIncludeDeleted(photoEntity3.propertyId)
            .first()
        assertTrue(beforeDelete.isNotEmpty())

        // --- Act ---
        photoRepository.deletePhotosByPropertyId(photoEntity3.propertyId)

        // --- Assert ---
        val afterDelete = photoRepository
            .getPhotosByPropertyIdIncludeDeleted(photoEntity3.propertyId)
            .first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun deletePhoto_deletesPhoto() = runTest {
        // --- Arrange ---
        val beforeDelete = photoRepository
            .getPhotoByIdIncludeDeleted(photoEntity3.id)
            .first()
        assertNotNull(beforeDelete)

        // --- Act ---
        photoRepository.deletePhoto(photoEntity3)

        // --- Assert ---
        val afterDelete = photoRepository
            .getPhotoByIdIncludeDeleted(photoEntity3.id)
            .first()
        assertNull(afterDelete)
    }

    @Test
    fun getUnSyncedPhotoEntities_returnsOnlyUnSynced() = runTest {
        // --- Arrange ---
        val expected = allPhotosEntity.filter { !it.isSynced }
        val synced = allPhotosEntity.filter { it.isSynced }

        // --- Act ---
        val result = photoRepository.uploadUnSyncedPhotosToFirebase().first()

        // --- Assert ---
        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun downloadPhotoFromFirebase_savesPhotoCorrectly() = runTest {
        // --- Arrange ---
        val firebasePhoto = PhotoOnlineEntity(
            roomId = 999L,
            propertyId = photoEntity1.propertyId,
            storageUrl = "https://firebase/photo.jpg",
            description = "Synced from Firebase",
            updatedAt = 1700000009999L
        )
        val localUri = "file://local/photo.jpg"

        // --- Act ---
        photoRepository.downloadPhotoFromFirebase(firebasePhoto, localUri)

        // --- Assert ---
        val result = photoRepository.getPhotoById(firebasePhoto.roomId).first()
        assertNotNull(result)
        assertEquals(firebasePhoto.roomId, result?.id)
        assertEquals(localUri, result?.uri)
        assertEquals(firebasePhoto.description, result?.description)
        assertEquals(true, result?.isSynced)
    }

    @Test
    fun downloadPhotoFromFirebase_shouldUpdateExistingPhoto() = runTest {
        // --- Arrange: photo déjà présente ---
        val existingId = photoEntity1.id
        val firebasePhoto = PhotoOnlineEntity(
            roomId = existingId,
            propertyId = photoEntity1.propertyId,
            storageUrl = "https://firebase/updated.jpg",
            description = "Updated from Firebase",
            updatedAt = 1800000000000L
        )
        val localUri = "file://local/updated_photo.jpg"

        // --- Act ---
        photoRepository.downloadPhotoFromFirebase(firebasePhoto, localUri)

        // --- Assert ---
        val result = photoRepository.getPhotoById(existingId).first()
        assertNotNull(result)
        assertEquals(existingId, result?.id)
        assertEquals(localUri, result?.uri) // le localUri doit remplacer l'ancien
        assertEquals(firebasePhoto.description, result?.description)
        assertTrue(result?.isSynced == true)
        assertEquals(firebasePhoto.updatedAt, result?.updatedAt)
    }

}