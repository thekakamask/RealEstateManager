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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
            db.userDao().firstUserInsertForceSyncedTrue(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertPropertyFromUi(it)
        }
        photoDao = db.photoDao()
    }

    @Test
    fun getPhotoById_shouldReturnCorrectPhoto() = runBlocking {
        photoDao.insertPhotoInsertFromUI(photo1)

        val result = photoDao.getPhotoById(photo1.id).first()

        assertEquals(photo1, result)
    }

    @Test
    fun getPhotoById_shouldNotReturnDeletedPhoto() = runBlocking {
        photoDao.insertPhotoInsertFromUI(photo3)

        val result = photoDao.getPhotoById(photo3.id).first()

        assertNull(result)
    }

    @Test
    fun getPhotoByIdIncludeDeleted_shouldReturnPhotoIncludeDeleted() = runBlocking {
        photoDao.insertPhotoInsertFromFirebase(photo3)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo3.id).first()

        assertEquals(photo3.id, result?.id)
    }

    @Test
    fun getPhotosByPropertyId_shouldReturnCorrectPhotos() = runBlocking {
        val property1 = FakePropertyEntity.property1

        photoDao.insertPhotosInsertFromUI(allPhotos)

        val result = photoDao.getPhotosByPropertyId(property1.id).first()
        val expected = allPhotosNotDeleted
            .filter { it.universalLocalPropertyId == property1.id }

        assertEquals(expected, result)
    }

    @Test
    fun getPhotosByPropertyId_shouldNotReturnDeletedPhotos() = runBlocking {
        val property3 = FakePropertyEntity.property3

        photoDao.insertPhotosInsertFromUI(allPhotos)

        val result = photoDao.getPhotosByPropertyId(property3.id).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getPhotosByPropertyIdIncludeDeleted_shouldReturnAllPhotosForPropertyIncludingDeleted() = runBlocking {
        val property3 = FakePropertyEntity.property3
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        val result = photoDao.getPhotosByPropertyIdIncludeDeleted(property3.id).first()
        val expected = allPhotos
            .filter { it.universalLocalPropertyId == property3.id }
            .map { it.copy(isSynced = true) }

        assertEquals(expected, result)
    }

    @Test
    fun getAllPhotos_shouldReturnAllPhotosNotDeleted() = runBlocking {
        photoDao.insertPhotosInsertFromUI(allPhotos)

        val result = photoDao.getAllPhotos().first()

        val expectedIds = allPhotosNotDeleted.map { it.id }
        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getAllPhotosIncludeDeleted_shouldReturnAll() = runBlocking {
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        val result = photoDao.getAllPhotosIncludeDeleted().first()

        assertEquals(allPhotos.size, result.size)
    }

    @Test
    fun uploadUnSyncedPhotos_shouldReturnOnlyPhotosWithIsSyncedFalse() = runBlocking {
        photoDao.insertPhotoInsertFromUI(photo1)
        photoDao.insertPhotoInsertFromFirebase(photo2)

        val result = photoDao.uploadUnSyncedPhotos().first()

        assertEquals(1, result.size)
        assertTrue(result.all { !it.isSynced })
        assertEquals(photo1.id, result.first().id)
    }

    @Test
    fun insertPhotoInsertFromUI_shouldInsertWithIsSyncedFalse() = runBlocking {
        photoDao.insertPhotoInsertFromUI(photo2)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
    }


    @Test
    fun insertPhotosInsertFromUI_shouldInsertAllWithIsSyncedFalse() = runBlocking {
        photoDao.insertPhotosInsertFromUI(allPhotos)

        val result = photoDao.getAllPhotosIncludeDeleted().first()

        assertEquals(allPhotos.size, result.size)
        assertTrue(result.all { !it.isSynced })
    }

    @Test
    fun insertPhotoInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runBlocking {
        photoDao.insertPhotoInsertFromFirebase(photo1)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertAllPhotosNotExistingFromFirebase_shouldInsertAllWithIsSyncedTrue() = runBlocking {
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        val result = photoDao.getAllPhotosIncludeDeleted().first()

        assertEquals(allPhotos.size, result.size)
        assertTrue(result.all { it.isSynced })
    }

    @Test
    fun updatePhotoFromUIForceSyncFalse_shouldSetIsSyncedFalse() = runBlocking {
        photoDao.insertPhotoInsertFromFirebase(photo2)

        val updated = photo2.copy(description = "Updated")
        photoDao.updatePhotoFromUIForceSyncFalse(updated)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals("Updated", result.description)
    }

    @Test
    fun updatePhotoFromFirebaseForceSyncTrue_shouldSetIsSyncedTrue() = runBlocking {
        photoDao.insertPhotoInsertFromUI(photo1)

        val updated = photo1.copy(description = "From Firebase")
        photoDao.updatePhotoFromFirebaseForceSyncTrue(updated)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
        assertEquals("From Firebase", result.description)
    }

    @Test
    fun updateAllPhotosFromFirebaseForceSyncTrue_shouldUpdateAll() = runBlocking {
        photoDao.insertPhotosInsertFromUI(allPhotos)

        photoDao.updateAllPhotosFromFirebaseForceSyncTrue(allPhotos)

        val result = photoDao.getAllPhotosIncludeDeleted().first()

        assertEquals(allPhotos.size, result.size)
        assertTrue(result.all { it.isSynced })
    }

    @Test
    fun markPhotoAsDeleted_shouldHideFromQueries() = runBlocking {
        photoDao.insertPhotoInsertFromFirebase(photo2)

        photoDao.markPhotoAsDeleted(photo2.id, System.currentTimeMillis())

        val uiResult = photoDao.getPhotoById(photo2.id).first()
        assertEquals(null, uiResult)

        val dbResult = photoDao.getPhotoByIdIncludeDeleted(photo2.id).first()

        assertNotNull(dbResult)
        assertTrue(dbResult!!.isDeleted)
        assertFalse(dbResult.isSynced)
    }

    @Test
    fun markPhotosAsDeletedByProperty_shouldHideAllFromQueries() = runBlocking {
        val property2 = FakePropertyEntity.property2
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        photoDao.markPhotosAsDeletedByProperty(property2.id, System.currentTimeMillis())

        val uiResult = photoDao.getPhotosByPropertyId(property2.id).first()
        assertTrue(uiResult.isEmpty())

        val dbResult = photoDao.getPhotosByPropertyIdIncludeDeleted(property2.id).first()
        assertTrue(dbResult.all { it.isDeleted })
        assertTrue(dbResult.all { !it.isSynced })
    }

    @Test
    fun deletePhoto_shouldRemovePhoto() = runBlocking {
        photoDao.insertPhotoInsertFromFirebase(photo3)

        val inserted = photoDao.getPhotoByIdIncludeDeleted(photo3.id).first()
        photoDao.deletePhoto(inserted!!)

        val result = photoDao.getPhotoByIdIncludeDeleted(photo3.id).first()

        assertNull(result)
    }

    @Test
    fun deletePhotosByPropertyId_shouldRemoveAllForProperty() = runBlocking {
        val property3 = FakePropertyEntity.property3
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        photoDao.deletePhotosByPropertyId(property3.id)

        val result = photoDao.getPhotosByPropertyIdIncludeDeleted(property3.id).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun clearAllPhotosDeleted_shouldRemoveOnlyDeletedPhotos() = runBlocking {
        photoDao.insertAllPhotosNotExistingFromFirebase(allPhotos)

        photoDao.markPhotoAsDeleted(photo1.id, System.currentTimeMillis())
        photoDao.markPhotoAsDeleted(photo2.id, System.currentTimeMillis())

        photoDao.clearAllPhotosDeleted()

        val result = photoDao.getAllPhotosIncludeDeleted().first()
        assertEquals(0, result.size)
    }

    @Test
    fun getAllPhotosAsCursor_shouldReturnValidCursor() = runBlocking {
        photoDao.insertPhotosInsertFromUI(allPhotos)
        val query = SimpleSQLiteQuery("SELECT * FROM photos")
        val cursor = photoDao.getAllPhotosAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }
}