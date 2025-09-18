package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePhotoEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PhotoDaoTest: DatabaseSetup() {

    private lateinit var photoDao: PhotoDao

    private val photo1 = FakePhotoEntity.photo1
    private val photo2 = FakePhotoEntity.photo2
    private val photo3 = FakePhotoEntity.photo3
    private val allPhotosNotDeleted = FakePhotoEntity.photoEntityListNotDeleted
    private val allPhotos = FakePhotoEntity.photoEntityList


    @Before
    fun setup() = runBlocking {
        // insert related users and properties before using PhotoDao
        FakeUserEntity.userEntityList.forEach{
            db.userDao().insertUser(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }
        photoDao = db.photoDao()
    }

    @Test
    fun getPhotoById_shouldReturnCorrectPhoto() = runBlocking {
        // Given
        photoDao.insertPhoto(photo1)
        // When
        val result = photoDao.getPhotoById(photo1.id).first()
        // Then
        assertEquals(photo1, result)
    }

    @Test
    fun insert_and_getPhotosByPropertyId_shouldReturnCorrectPhotos() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val result = photoDao.getPhotosByPropertyId(photo2.propertyId).first()
        val expected = allPhotos
            .filter { it.propertyId == photo2.propertyId }
            .map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun insertPhoto_shouldInsertSinglePhoto() = runBlocking {
        photoDao.insertPhoto(photo1)
        val result = photoDao.getAllPhotos().first()
        assertEquals(listOf(photo1), result)
    }

    @Test
    fun getAllPhotos_shouldReturnAllInsertedPhotosNotMarkAsDeleted() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val result = photoDao.getAllPhotos().first()
        val expected = allPhotosNotDeleted.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun getPhotoIncludeDeletedById_shouldReturnCorrectPhoto() = runBlocking {
        photoDao.insertPhoto(photo3)
        val result = photoDao.getPhotoByIdIncludeDeleted(photo3.id).first()
        assertEquals(photo3, result)
    }

    @Test
    fun getPhotosByPropertyIdIncludeDeleted_shouldReturnCorrectPhotos() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val result = photoDao.getPhotosByPropertyIdIncludeDeleted(photo3.propertyId).first()
        val expected = allPhotos.filter { it.propertyId == photo3.propertyId }
        assertEquals(expected, result)
    }

    @Test
    fun updatePhoto_shouldModifyPhotoCorrectly() = runBlocking {
        photoDao.insertPhoto(photo2)

        val updated = photo2.copy(
            uri = "file://updated_photo.jpg",
            description = "Updated description",
            updatedAt = System.currentTimeMillis()
        )
        photoDao.updatePhoto(updated)

        val result = photoDao.getPhotoById(photo2.id).first()

        assertEquals(updated.uri, result?.uri)
        assertEquals(updated.description, result?.description)
        assertEquals(false, result?.isSynced)
        assertEquals(updated.updatedAt, result?.updatedAt)
    }

    @Test
    fun markPhotoAsDeleted_shouldHidePhotoFromQueries() = runBlocking {
        photoDao.insertPhoto(photo2)
        photoDao.markPhotoAsDeleted(photo2.id, System.currentTimeMillis())

        val result = photoDao.getPhotoById(photo2.id).first()

        assertEquals(null, result)
    }

    @Test
    fun markPhotosAsDeletedByProperty_shouldHidePhotosFromQueries() = runBlocking {
        photoDao.insertPhotos(allPhotosNotDeleted)
        photoDao.markPhotosAsDeletedByProperty(photo2.propertyId, System.currentTimeMillis())

        val result = photoDao.getAllPhotos().first()
        assertEquals(listOf(photo1), result)
    }

    @Test
    fun deletePhoto_shouldRemoveDeletedPhotoFromDatabase() = runBlocking {
        photoDao.savePhotoFromFirebase(photo3)
        photoDao.deletePhoto(photo3)
        val result = photoDao.getPhotoByIdIncludeDeleted(photo3.id).first()
        assertEquals(null, result)
    }

    @Test
    fun deletePhotosByPropertyId_shouldRemoveDeletedPhotosFromDatabase() = runBlocking {
        allPhotos.forEach {
            photoDao.savePhotoFromFirebase(it)
        }
        photoDao.deletePhotosByPropertyId(photo3.propertyId)
        val result = photoDao.getPhotosByPropertyIdIncludeDeleted(photo3.propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getUnSyncedPhotos_shouldReturnUnSyncedPhotos() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val result = photoDao.getUnSyncedPhotos().first()
        val expected = allPhotos.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun savePhotoFromFirebase_shouldSetIsSyncedToTrue() = runBlocking {
        photoDao.savePhotoFromFirebase(photo1)
        val result = photoDao.getPhotoById(photo1.id).first()
        val expected = photo1.copy(isSynced = true)
        assertEquals(expected, result)
    }

    //This test ensures that:
    //the Cursor is not null,
    //it contains data (when the database is not empty),
    //it is closed correctly (good practice).
    @Test
    fun getAllPhotosAsCursor_shouldReturnValidCursor() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val query = SimpleSQLiteQuery("SELECT * FROM photos")
        val cursor = photoDao.getAllPhotosAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }


}