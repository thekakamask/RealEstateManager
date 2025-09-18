package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiDao
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePoiEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Assert.assertEquals
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PoiDaoTest: DatabaseSetup() {

    private lateinit var poiDao: PoiDao

    private val poi1 = FakePoiEntity.poi1
    private val poi2 = FakePoiEntity.poi2
    private val poi3 = FakePoiEntity.poi3
    private val crossRef2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRef3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val allPoiSNotDeleted = FakePoiEntity.poiEntityListNotDeleted
    private val allPoiS = FakePoiEntity.poiEntityList

    @Before
    fun setup() = runBlocking {
        // Insert base required data for all tests
        FakeUserEntity.userEntityList.forEach {
            db.userDao().insertUser(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }

        poiDao = db.poiDao()
    }

    @Test
    fun getPoiById_shouldReturnCorrectPoi() = runBlocking {
        // GIVEN: a POI inserted in DB
        poiDao.insertPoi(poi1)

        // WHEN: we query it by ID
        val result = poiDao.getPoiById(poi1.id).first()

        // THEN: we get the expected POI
        assertEquals(poi1, result)
    }

    @Test
    fun getAllPoiS_shouldReturnAllInsertedPoiSNotMarkAsDeleted() = runBlocking {
        poiDao.insertAllPoiS(allPoiS)
        val result = poiDao.getAllPoiS().first()
        val expected = allPoiSNotDeleted.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun getPoiIncludeDeletedById_shouldReturnCorrectPoi() = runBlocking {
        poiDao.insertPoi(poi3)
        val result = poiDao.getPoiByIdIncludeDeleted(poi3.id).first()
        assertEquals(poi3, result)
    }

    @Test
    fun getAllPoiIncludeDeleted_shouldReturnAllInsertedPoiS() = runBlocking {
        poiDao.insertAllPoiS(allPoiS)
        val result = poiDao.getAllPoiIncludeDeleted().first()
        val expected = allPoiS.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun insertPoi_shouldInsertSinglePoi() = runBlocking {
        poiDao.insertPoi(poi1)
        val result = poiDao.getAllPoiS().first()
        assertEquals(listOf(poi1), result)
    }

    @Test
    fun insertAllPoiS_shouldInsertMultiplePoi() = runBlocking {
        poiDao.insertAllPoiS(allPoiS)

        val result = poiDao.getAllPoiS().first()
        val expected = allPoiSNotDeleted.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun updatePoi_shouldUpdateExistingPoi() = runBlocking {
        poiDao.insertPoi(poi2)

        val updatedPoi = poi2.copy(
            name = "Updated Name",
            type = "Updated Type",
            updatedAt = System.currentTimeMillis()
        )

        poiDao.updatePoi(updatedPoi)

        val result = poiDao.getPoiById(poi2.id).first()

        assertEquals("Updated Name", result?.name)
        assertEquals("Updated Type", result?.type)
        assertEquals(false, result?.isSynced)
        assertEquals(updatedPoi.updatedAt, result?.updatedAt)
    }

    @Test
    fun markPoiAsDeleted_shouldHidePhotoFromQueries() = runBlocking {
        poiDao.insertPoi(poi2)
        poiDao.markPoiAsDeleted(poi2.id, System.currentTimeMillis())

        val result = poiDao.getPoiById(poi2.id).first()
        assertEquals(null, result)
    }

    @Test
    fun deletePoi_shouldRemovePoiFromDatabase() = runBlocking {
        poiDao.savePoiFromFirebase(poi3)
        poiDao.deletePoi(poi3)
        val result = poiDao.getPoiByIdIncludeDeleted(poi3.id).first()
        assertEquals(null, result)
    }

    @Test
    fun getPoiWithProperties_shouldReturnAssociatedProperties() = runBlocking {
        poiDao.insertPoi(poi2)
        db.propertyCrossDao().insertCrossRef(crossRef2)
        db.propertyCrossDao().insertCrossRef(crossRef3)

        val result = poiDao.getPoiWithProperties(poi2.id).first()

        assertNotNull(result)
        assertEquals(poi2.copy(isSynced = false), result.poi)

        val expectedProperties = listOf(
            property1,
            property2.copy(isSynced = false)
        )
        assertEquals(expectedProperties.toSet(), result.properties.toSet())
        assertEquals(2, result.properties.size)

    }


    @Test
    fun getUnSyncedPoiS_shouldReturnOnlyUnSyncedPoi() = runBlocking {
        poiDao.insertAllPoiS(allPoiS)
        val result = poiDao.uploadUnSyncedPoiSToFirebase().first()
        val expected = allPoiS.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun savePoiFromFirebase_shouldInsertPoiCorrectly() = runBlocking {
        poiDao.savePoiFromFirebase(poi1)
        val result = poiDao.getPoiById(poi1.id).first()
        val expected = poi1.copy(isSynced = true)
        assertEquals(expected, result)
    }

    //This test ensures that:
    //the Cursor is not null,
    //it contains data (when the database is not empty),
    //it is closed correctly (good practice).
    @Test
    fun getAllPoiSAsCursor_shouldReturnValidCursor() = runBlocking {
        poiDao.insertAllPoiS(allPoiS)
        val query = SimpleSQLiteQuery("SELECT * FROM poi")
        val cursor = poiDao.getAllPoiSAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }


}