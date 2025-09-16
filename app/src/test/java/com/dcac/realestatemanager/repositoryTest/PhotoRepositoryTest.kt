package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
import com.dcac.realestatemanager.model.Photo
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class PhotoRepositoryTest {

    // Fake DAO simulating Room database
    private lateinit var fakePhotoDao : FakePhotoDao
    // Repository under test
    private lateinit var photoRepository : PhotoRepository

    @Before
    fun setup() {
        // Initialize fake DAO and repository before each test
        fakePhotoDao = FakePhotoDao()
        photoRepository = OfflinePhotoRepository(fakePhotoDao)
    }

    @Test
    fun getPhotoById_returnsCorrectPhoto() = runTest {
        // Expected model result
        val expected = FakePhotoModel.photo1
        // Query repository by ID
        val result = photoRepository.getPhotoById(FakePhotoEntity.photo1.id).first()
        // Verify that result matches expected
        assertEquals(expected, result)
    }

    @Test
    fun getPhotosByPropertyId_returnsCorrectPhotos() = runTest {
        // Take propertyId from a fake entity
        val propertyId = FakePhotoEntity.photo1.propertyId

        // Expected: all fake photos belonging to the same propertyId
        val expected = FakePhotoModel.photoModelList
            .filter { it.propertyId == propertyId }

        // Query repository
        val result = photoRepository.getPhotosByPropertyId(propertyId).first()

        // Verify repository returns correct subset
        assertEquals(expected, result)
    }

    @Test
    fun getAllPhotos_returnsAllPhotos() = runTest {
        // Fetch all photos via repository
        val result = photoRepository.getAllPhotos().first()
        // Expected full list of fake photos
        val expected = FakePhotoModel.photoModelList
        // Verify full dataset matches
        assertEquals(expected, result)
    }

    @Test
    fun insertPhoto_insertsPhoto() = runTest {
        // Create a new fake photo model with a unique ID
        val expectedNewPhoto = FakePhotoModel.photo1.copy(
            id = 99L,
            propertyId = FakePhotoEntity.photo1.propertyId,
            uri = "file://new_photo.jpg",
            description = "New inserted photo"
        )
        // Insert into repository
        photoRepository.insertPhoto(expectedNewPhoto)

        // --- Verify DAO state (Entity level) ---
        val resultEntity = fakePhotoDao.entityMap[expectedNewPhoto.id]

        assertEquals(expectedNewPhoto.id, resultEntity?.id)
        assertEquals(expectedNewPhoto.propertyId, resultEntity?.propertyId)
        assertEquals(expectedNewPhoto.uri, resultEntity?.uri)

        // --- Verify Repository result (Model level) ---
        val resultInserted = photoRepository.getPhotoById(expectedNewPhoto.id).first()

        // Ensure photo exists
        assertNotNull(resultInserted)
        assertEquals(expectedNewPhoto.id, resultInserted?.id)
        assertEquals(expectedNewPhoto.uri, resultInserted?.uri)
    }

    @Test
    fun insertPhotos_insertsPhotos() = runTest {
        // Create two new fake photos with different IDs
        val newPhotos = listOf(
            FakePhotoModel.photo1.copy(
                id = 101L,
                propertyId = FakePhotoEntity.photo1.propertyId,
                uri = "file://new_photo_1.jpg",
                description = "First new inserted photo"
            ),
            FakePhotoModel.photo2.copy(
                id = 102L,
                propertyId = FakePhotoEntity.photo2.propertyId,
                uri = "file://new_photo_2.jpg",
                description = "Second new inserted photo"
            )
        )

        // Insert both via repository
        photoRepository.insertPhotos(newPhotos)

        // --- Verify DAO state (Entities exist in map) ---
        newPhotos.forEach { expected ->
            val entity = fakePhotoDao.entityMap[expected.id]
            assertEquals(expected.id, entity?.id)
            assertEquals(expected.propertyId, entity?.propertyId)
            assertEquals(expected.uri, entity?.uri)
        }

        // --- Verify Repository state (Models are retrievable) ---
        val allPhotos = photoRepository.getAllPhotos().first()
        // Ensure both are included
        assertTrue(allPhotos.containsAll(newPhotos))
    }

    @Test
    fun updatePhoto_shouldModifyExistingPhoto() = runTest {
        val original = FakePhotoModel.photo1
        val updated = original.copy(
            uri = "file://updated_uri.jpg",
            description = "Updated description",
            isSynced = true
        )

        photoRepository.updatePhoto(updated)

        val result = photoRepository.getPhotoById(updated.id).first()

        assertNotNull(result)
        assertEquals(updated.uri, result?.uri)
        assertEquals(updated.description, result?.description)
        assertEquals(updated.isSynced, result?.isSynced)
    }

    @Test
    fun cachePhotoFromFirebase_savesPhotoCorrectly() = runTest {
        // Given
        val firebasePhoto = FakePhotoModel.photo1.copy(
            id = 999L,
            uri = "",
            description = "From Firebase",
            isSynced = true,
            storageUrl = "https://example.com/photo_1.jpg"
        )

        // When
        photoRepository.cachePhotoFromFirebase(firebasePhoto)
        val result = photoRepository.getPhotoById(firebasePhoto.id).first()

        // Then
        assertThat(result).isNotNull()
        result!!                       // safe unwrap

        assertThat(result.uri).isEqualTo(firebasePhoto.uri)
        assertThat(result.description).isEqualTo(firebasePhoto.description)
        assertThat(result.isSynced).isEqualTo(firebasePhoto.isSynced)
        assertThat(result.updatedAt).isEqualTo(firebasePhoto.updatedAt)
    }



    @Test
    fun deletePhotosByPropertyId_deletesPhotos() = runTest {
        // Property whose photos will be deleted
        val propertyId = FakePhotoEntity.photo1.propertyId

        // Ensure photos exist before deletion
        val initialPhotos = photoRepository.getPhotosByPropertyId(propertyId).first()
        assertTrue(initialPhotos.isNotEmpty())

        // Delete all photos linked to propertyId
        photoRepository.deletePhotosByPropertyId(propertyId)

        // --- Verify DAO state (Entities removed) ---
        val resultEntities = fakePhotoDao.entityMap.values.filter { it.propertyId == propertyId }
        assertTrue(resultEntities.isEmpty())

        // --- Verify Repository state (No models returned) ---
        val resultModels = photoRepository.getPhotosByPropertyId(propertyId).first()
        assertTrue(resultModels.isEmpty())
    }

    @Test
    fun deletePhoto_deletesPhoto() = runTest {
        // Photo to delete
        val targetPhoto = FakePhotoModel.photo1

        // Ensure the photo exists before deletion
        val beforeDelete = photoRepository.getPhotoById(targetPhoto.id).first()
        assertNotNull(beforeDelete)

        // Delete the photo
        photoRepository.deletePhoto(targetPhoto)

        // --- Verify DAO state (Entity removed) ---
        val resultEntity = fakePhotoDao.entityMap[targetPhoto.id]
        assertEquals(null, resultEntity)

        // --- Verify Repository state (Model not retrievable) ---
        val afterDelete = photoRepository.getPhotoById(targetPhoto.id).first()
        assertEquals(null, afterDelete)
    }

    @Test
    fun getUnSyncedPhotos_returnsOnlyUnSynced() = runTest {
        val expected = FakePhotoModel.photoModelList.filter { !it.isSynced }


        val result = photoRepository.getUnSyncedPhotos().first()
        val synced = FakePhotoModel.photoModelList.filter { it.isSynced }

        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

}