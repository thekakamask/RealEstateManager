package com.dcac.realestatemanager.databaseTest.daoTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import com.dcac.realestatemanager.databaseTest.DatabaseSetup
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import kotlinx.coroutines.flow.first

@RunWith(AndroidJUnit4::class)
class PropertyDaoTest: DatabaseSetup() {

    private lateinit var propertyDao: PropertyDao

    private val property1 = FakePropertyEntity.property1
    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val poi1 = FakePoiEntity.poi1

    private val propertyList = FakePropertyEntity.propertyEntityList

    @Before
    fun setup() = runBlocking {

        db.userDao().saveUserFromFirebase(FakeUserEntity.user1)
        db.userDao().saveUserFromFirebase(FakeUserEntity.user2)
        FakePoiEntity.poiEntityList.forEach {
            db.poiDao().insertPoi(it)
        }

        propertyDao = db.propertyDao()
    }

    @Test
    fun insertProperty_shouldInsertCorrectly() = runBlocking {
        propertyDao.insertProperty(property1)
        db.propertyCrossDao().insertCrossRef(crossRef1)
        val result = propertyDao.getPropertyById(property1.id).first()
        assertEquals(property1, result)
    }

    @Test
    fun getAllPropertiesByDate_shouldReturnSorted() = runBlocking {
        propertyList.forEach { propertyDao.insertProperty(it) }
        val result = propertyDao.getAllPropertiesByDate().first()
        val sorted = propertyList.sortedByDescending { it.entryDate }
        assertEquals(sorted, result)
    }

    @Test
    fun getAllPropertiesByAlphabetic_shouldReturnSorted() = runBlocking {
        propertyList.forEach { propertyDao.insertProperty(it) }
        val result = propertyDao.getAllPropertiesByAlphabetic().first()
        val sorted = propertyList.sortedBy { it.title }
        assertEquals(sorted, result)
    }

    @Test
    fun getPropertyById_shouldReturnCorrectProperty() = runBlocking {
        propertyDao.insertProperty(property1)
        val result = propertyDao.getPropertyById(property1.id).first()
        assertEquals(property1, result)
    }

    @Test
    fun updateProperty_shouldUpdateCorrectly() = runBlocking {
        propertyDao.insertProperty(property1)
        val updated = property1.copy(title = "New Title")
        propertyDao.updateProperty(updated)

        val result = propertyDao.getPropertyById(updated.id).first()
        assertEquals("New Title", result?.title)
    }

    @Test
    fun deleteProperty_shouldRemoveProperty() = runBlocking {
        propertyDao.insertProperty(property1)
        propertyDao.deleteProperty(property1)

        val result = propertyDao.getPropertyById(property1.id).first()
        assertNull(result)
    }

    @Test
    fun markPropertyAsSold_shouldUpdateFields() = runBlocking {
        propertyDao.insertProperty(property1)
        val saleDate = "2025-08-06"
        propertyDao.markPropertyAsSold(property1.id, saleDate)

        val result = propertyDao.getPropertyById(property1.id).first()
        assertTrue(result?.isSold ?: false)
        assertEquals(saleDate, result?.saleDate)
    }

    @Test
    fun getPropertyWithPoiS_shouldReturnAssociatedPoiS() = runBlocking {
        db.propertyDao().insertProperty(property1)
        db.propertyCrossDao().insertCrossRef(crossRef1)

        val result = propertyDao.getPropertyWithPoiS(property1.id).first()
        assertEquals(property1, result.property)
        assertEquals(listOf(poi1), result.poiS)
    }

    @Test
    fun searchProperties_shouldFilterCorrectly() = runBlocking {
        propertyList.forEach { propertyDao.insertProperty(it) }

        val result = propertyDao.searchProperties(
            minSurface = 50,
            maxSurface = 200,
            minPrice = 300000,
            maxPrice = 1000000,
            type = null,
            isSold = false
        ).first()

        val expected = propertyList.filter {
            it.surface in 50..200 &&
                    it.price >= 300000 && it.price <= 1000000 &&
                    !it.isSold
        }

        assertEquals(expected, result)
    }

    @Test
    fun clearAll_shouldRemoveAllProperties() = runBlocking {
        propertyList.forEach { propertyDao.insertProperty(it) }
        propertyDao.clearAll()
        val result = propertyDao.getAllPropertiesByDate().first()
        assertTrue(result.isEmpty())
    }
}