package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePoiEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropertyDaoTest: DatabaseSetup() {

    private lateinit var propertyDao: PropertyDao

    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val property3 = FakePropertyEntity.property3
    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRef2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val poi1 = FakePoiEntity.poi1
    private val poi2 = FakePoiEntity.poi2
    private val allPropertiesNotDeleted = FakePropertyEntity.propertyEntityListNotDeleted
    private val allProperties = FakePropertyEntity.propertyEntityList


    @Before
    fun setup() = runBlocking {

        FakeUserEntity.userEntityList.forEach {
            db.userDao().insertUser(it)
        }
        FakePoiEntity.poiEntityList.forEach {
            db.poiDao().insertPoi(it)
        }

        propertyDao = db.propertyDao()
    }

    @Test
    fun getPropertyById_shouldReturnCorrectProperty() = runBlocking {
        propertyDao.insertProperty(property1)
        val result = propertyDao.getPropertyById(property1.id).first()
        assertEquals(property1, result)
    }

    @Test
    fun getPropertyByUserID_shouldReturnCorrectProperty() = runBlocking {
        propertyDao.insertProperty(property2)

        val result = propertyDao.getPropertyByUserId(property2.userId).first()
        val expected = listOf(property2.copy(isSynced = false))

        assertEquals(expected, result)
    }

    @Test
    fun getAllPropertiesByDate_shouldReturnSorted() = runBlocking {
        allProperties.forEach {
            propertyDao.insertProperty(it)
        }
        val result = propertyDao.getAllPropertiesByDate().first()
        val expectedSorted = allPropertiesNotDeleted
            .map { it.copy(isSynced = false) }
            .sortedByDescending { it.entryDate }
        assertEquals(expectedSorted, result)
    }

    @Test
    fun getAllPropertiesByAlphabetic_shouldReturnSorted() = runBlocking {
        allProperties.forEach {
            propertyDao.insertProperty(it)
        }

        val result = propertyDao.getAllPropertiesByAlphabetic().first()
        val expectedSorted = allPropertiesNotDeleted
            .map { it.copy(isSynced = false) }
            .sortedBy { it.title }
        assertEquals(expectedSorted, result)
    }

    @Test
    fun getPropertyByIdIncludeDeleted_shouldReturnCorrectProperty() = runBlocking {
        propertyDao.insertProperty(property3)
        val result = propertyDao.getPropertyByIdIncludeDeleted(property3.id).first()
        assertEquals(property3, result)
    }

    @Test
    fun getAllPropertiesIncludeDeleted_shouldReturnAllProperties() = runBlocking {
        allProperties.forEach {
            propertyDao.insertProperty(it)
        }
        val result = propertyDao.getAllPropertiesIncludeDeleted().first()
        val expected = allProperties.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_shouldFilterCorrectly() = runBlocking {
        allProperties.forEach {
            propertyDao.insertProperty(it)
        }

        val result = propertyDao.searchProperties(
            minSurface = 50,
            maxSurface = 300,
            minPrice = 200000,
            maxPrice = 1000000,
            type = null,
            isSold = false
        ).first()

        val expected = allPropertiesNotDeleted
            .map { it.copy(isSynced = false) }
            .filter {
            it.surface in 50..300 &&
                    it.price >= 200000 && it.price <= 1000000 &&
                    !it.isSold
        }

        assertEquals(expected, result)
    }

    @Test
    fun insertProperty_shouldInsertCorrectly() = runBlocking {
        propertyDao.insertProperty(property1)
        val result = propertyDao.getPropertyById(property1.id).first()
        assertEquals(property1, result)
    }

    @Test
    fun updateProperty_shouldUpdateCorrectly() = runBlocking {
        propertyDao.insertProperty(property2)

        val updatedProperty = property2.copy(
            title = "Updated Title",
            price = 150000,
            updatedAt = System.currentTimeMillis()
        )
        propertyDao.updateProperty(updatedProperty)

        val result = propertyDao.getPropertyById(property2.id).first()

        assertEquals("Updated Title", result?.title)
        assertEquals(150000, result?.price)
        assertEquals(false, result?.isSynced)
        assertEquals(updatedProperty.updatedAt, result?.updatedAt)
    }

    @Test
    fun markPropertyAsSold_shouldUpdateFields() = runBlocking {
        propertyDao.insertProperty(property2)
        val saleDate = "2025-08-06"

        val updatedProperty = property2.copy(
            saleDate = saleDate,
            updatedAt = System.currentTimeMillis()
        )

        propertyDao.updateProperty(updatedProperty)

        val result = propertyDao.getPropertyById(property2.id).first()

        assertEquals(saleDate, result?.saleDate)
        assertEquals(false, result?.isSynced)
        assertEquals(updatedProperty.updatedAt, result?.updatedAt)
    }

    @Test
    fun markPropertyAsDeleted_shouldHideFromQueries() = runBlocking {
        propertyDao.insertProperty(property2)
        propertyDao.markPropertyAsDeleted(property2.id, System.currentTimeMillis())

        val result = propertyDao.getPropertyById(property2.id).first()
        assertEquals(null, result)
    }

    @Test
    fun deleteProperty_shouldRemovePropertyFromDatabase() = runBlocking {
        propertyDao.savePropertyFromFirebase(property3)
        propertyDao.deleteProperty(property3)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property3.id).first()
        assertNull(result)
    }

    @Test
    fun markAllPropertiesAsDeleted_shouldHideFromQueries() = runBlocking {
        allPropertiesNotDeleted.forEach {
            propertyDao.insertProperty(it)
        }
        propertyDao.markAllPropertiesAsDeleted(System.currentTimeMillis())

        val result = propertyDao.getAllPropertiesByDate().first()
        assertTrue(result.all { it.isDeleted })

    }

    @Test
    fun clearAllDeleted_shouldRemoveOnlySoftDeletedProperties() = runBlocking {
        // Insert all properties
        allProperties.forEach {
            propertyDao.savePropertyFromFirebase(it)
        }

        propertyDao.markPropertyAsDeleted(allProperties[0].id, System.currentTimeMillis())

        propertyDao.clearAllDeleted()

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertFalse(result.any { it.id == allProperties[0].id })
        assertFalse(result.any { it.id == allProperties[2].id })

        assertTrue(result.any { it.id == allProperties[1].id })

        assertEquals(1 , result.size)
    }

    @Test
    fun getPropertyWithPoiS_shouldReturnAssociatedPoiS() = runBlocking {
        propertyDao.insertProperty(property1)
        db.propertyCrossDao().insertCrossRef(crossRef1)
        db.propertyCrossDao().insertCrossRef(crossRef2)

        val result = propertyDao.getPropertyWithPoiS(property1.id).first()

        assertNotNull(result)
        assertEquals(property1, result.property)

        val expectedPoiS = listOf(
            poi1,
            poi2.copy(isSynced = false)
        )
        assertEquals(expectedPoiS.toSet(), result.poiS.toSet())
        assertEquals(2, result.poiS.size)

    }

    @Test
    fun getUnSyncedProperties_shouldReturnOnlyUnSyncedProperties() = runBlocking {
        allProperties.forEach {
            propertyDao.insertProperty(it)
        }

        val result = propertyDao.uploadUnSyncedPropertiesToFirebase().first()
        val expected = allProperties.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun savePropertyFromFirebase_shouldInsertPropertyCorrectly() = runBlocking {
        propertyDao.savePropertyFromFirebase(property1)
        val result = propertyDao.getPropertyById(property1.id).first()
        val expected = property1.copy(isSynced = true)
        assertEquals(expected, result)
    }

    //This test ensures that:
    //the Cursor is not null,
    //it contains data (when the database is not empty),
    //it is closed correctly (good practice).
    @Test
    fun getAllPropertiesAsCursor_shouldReturnValidCursor() = runBlocking {
        allProperties.forEach { propertyDao.insertProperty(it) }
        val query = SimpleSQLiteQuery("SELECT * FROM properties")
        val cursor = propertyDao.getAllPropertiesAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}