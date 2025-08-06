package com.dcac.realestatemanager.databaseTest.daoTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoDao
import com.dcac.realestatemanager.databaseTest.DatabaseSetup
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePhotoEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PhotoDaoTest: DatabaseSetup() {

    private lateinit var photoDao: PhotoDao

    private val photo1 = FakePhotoEntity.photo1
    private val photo2 = FakePhotoEntity.photo2
    private val photo3 = FakePhotoEntity.photo3
    private val photoList = listOf(photo1, photo2, photo3)
    private val allPhotos = FakePhotoEntity.photoEntityList


    @Before
    fun setup() = runBlocking {
        // insert related users and properties before using PhotoDao
        db.userDao().saveUserFromFirebase(FakeUserEntity.user1)
        db.userDao().saveUserFromFirebase(FakeUserEntity.user2)
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }

        photoDao = db.photoDao()
    }

    @Test
    fun insert_and_getPhotosByPropertyId_shouldReturnCorrectPhotos() = runBlocking {
        photoDao.insertPhotos(photoList)
        val result = photoDao.getPhotosByPropertyId(photo1.propertyId).first()
        val expected = listOf(photo1, photo2) // photo1 & photo2 share same propertyId
        assertEquals(expected, result)
    }

    @Test
    fun insertPhoto_shouldInsertSinglePhoto() = runBlocking {
        photoDao.insertPhoto(photo1)
        val result = photoDao.getPhotosByPropertyId(photo1.propertyId).first()
        assertEquals(listOf(photo1), result)
    }

    @Test
    fun getAllPhotos_shouldReturnAllInsertedPhotos() = runBlocking {
        photoDao.insertPhotos(allPhotos)
        val result = photoDao.getAllPhotos().first()
        assertEquals(allPhotos, result)
    }

    @Test
    fun deletePhotosByPropertyId_shouldRemoveAllPhotosForProperty() = runBlocking {
        photoDao.insertPhotos(photoList)
        photoDao.deletePhotosByPropertyId(photo1.propertyId)
        val result = photoDao.getPhotosByPropertyId(photo1.propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deletePhoto_shouldRemoveSinglePhoto() = runBlocking {
        photoDao.insertPhotos(photoList)
        photoDao.deletePhoto(photo2)
        val result = photoDao.getPhotosByPropertyId(photo2.propertyId).first()
        assertEquals(listOf(photo1), result)
    }

}