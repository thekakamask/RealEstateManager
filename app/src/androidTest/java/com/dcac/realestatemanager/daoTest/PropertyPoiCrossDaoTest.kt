package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossDao
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePoiEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertNotNull

@RunWith(AndroidJUnit4::class)
class PropertyPoiCrossDaoTest: DatabaseSetup() {

    private lateinit var crossRefDao: PropertyPoiCrossDao

    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRef2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRef3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRef4 = FakePropertyPoiCrossEntity.propertyPoiCross4
    private val crossRef5 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRef6 = FakePropertyPoiCrossEntity.propertyPoiCross6
    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val poi2 = FakePoiEntity.poi2
    private val allCrossRefsNotDeleted = FakePropertyPoiCrossEntity.allCrossRefsNotDeleted
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs




    @Before
    fun setup() = runBlocking {

        FakeUserEntity.userEntityList.forEach {
            db.userDao().insertUser(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }
        FakePoiEntity.poiEntityList.forEach {
            db.poiDao().insertPoi(it)
        }

        crossRefDao = db.propertyCrossDao()

    }

    @Test
    fun getCrossRefsForProperty_shouldReturnCorrectRefs() = runBlocking {
        // Insert all crossRefs
        allCrossRefs.forEach {
            crossRefDao.insertCrossRef(it)
        }

        val result = crossRefDao.getCrossRefsForProperty(property1.id).first()

        val expected = allCrossRefsNotDeleted.filter { it.propertyId == property1.id }

        assertEquals(expected, result)
    }

    @Test
    fun getPoiIdsForProperty_shouldReturnCorrectPoiIds() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)


        val expected = allCrossRefsNotDeleted.filter { it.propertyId == property2.id }.map { it.poiId }
        val result = crossRefDao.getPoiIdsForProperty(property2.id).first()
        assertEquals(expected, result)
    }

    @Test
    fun getPropertyIdsForPoi_shouldReturnCorrectPropertyIds() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)

        val expected = allCrossRefsNotDeleted.filter { it.poiId == poi2.id }.map { it.propertyId }
        val result = crossRefDao.getPropertyIdsForPoi(poi2.id).first()
        assertEquals(expected, result)
    }

    @Test
    fun getAllCrossRefs_shouldReturnAllRefs() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)

        val result = crossRefDao.getAllCrossRefs().first()

        val expected = allCrossRefsNotDeleted.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun getCrossByIds_shouldReturnCorrectCrossRef() = runBlocking {
        crossRefDao.insertCrossRef(crossRef1)

        val result = crossRefDao.getCrossByIds(crossRef1.propertyId, crossRef1.poiId).first()

        assertEquals(crossRef1.propertyId, result?.propertyId)
        assertEquals(crossRef1.poiId, result?.poiId)
    }

    @Test
    fun getAllCrossRefsIncludeDeleted_shouldReturnAllRefs() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)
        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()
        val expected = allCrossRefs.map { it.copy(isSynced = false) }
        assertEquals(expected, result)
    }

    @Test
    fun insertCrossRef_shouldInsertOneCrossRef() = runBlocking {
        crossRefDao.insertCrossRef(crossRef2)

        val result = crossRefDao.getAllCrossRefs().first()
        assertEquals(listOf(crossRef2), result)
    }

    @Test
    fun insertAllCrossRefs_shouldInsertMultipleCrossRefs() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)

        val result = crossRefDao.getAllCrossRefs().first()
        val expected = allCrossRefsNotDeleted.map { it.copy(isSynced = false) }

        assertEquals(expected, result)
    }

    @Test
    fun updateCrossRef_shouldUpdateCorrectly() = runBlocking {
        crossRefDao.insertCrossRef(crossRef3)

        val updated = crossRef3.copy(
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        crossRefDao.updateCrossRef(updated)

        val result = crossRefDao.getCrossByIds(crossRef3.propertyId, crossRef3.poiId).first()

        assertEquals(updated.isSynced, result?.isSynced)
        assertEquals(updated.updatedAt, result?.updatedAt)

    }

    @Test
    fun markCrossRefAsDeleted_shouldHideCrossRefFromQueries() = runBlocking {
        crossRefDao.insertCrossRef(crossRef4)
        crossRefDao.markCrossRefAsDeleted(
            crossRef4.propertyId,
            crossRef4.poiId,
            System.currentTimeMillis()
            )
        val result = crossRefDao.getCrossByIds(
            crossRef4.propertyId,
            crossRef4.poiId).first()

        assertEquals(null, result)
    }

    @Test
    fun markCrossRefsAsDeletedForProperty_shouldHideCrossRefsFromQueries() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)
        crossRefDao.markCrossRefsAsDeletedForProperty(
            crossRef1.propertyId,
            System.currentTimeMillis()
        )

        val result = crossRefDao.getCrossRefsForProperty(crossRef1.propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun markCrossRefsAsDeletedForPoi_shouldHideCrossRefsFromQueries() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)
        crossRefDao.markCrossRefsAsDeletedForPoi(
            crossRef2.poiId,
            System.currentTimeMillis()
        )
        val result = crossRefDao.getPropertyIdsForPoi(crossRef2.poiId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun markAllCrossRefsAsDeleted_shouldHideCrossRefsFromQueries() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefsNotDeleted)
        crossRefDao.markAllCrossRefsAsDeleted(System.currentTimeMillis())
        val result = crossRefDao.getAllCrossRefs().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteCrossRef_shouldDeleteCorrectly() = runBlocking {
        crossRefDao.saveCrossRefFromFirebase(crossRef5)
        crossRefDao.deleteCrossRef(crossRef5)
        val result = crossRefDao.getCrossByIds(crossRef5.propertyId, crossRef5.poiId).first()
        assertEquals(null, result)
    }

    @Test
    fun deleteCrossRefsForProperty_shouldDeleteCorrectRefs() = runBlocking {
        allCrossRefs.forEach {
            crossRefDao.saveCrossRefFromFirebase(it)
        }
        crossRefDao.deleteCrossRefsForProperty(crossRef6.propertyId)
        val result = crossRefDao.getCrossRefsForProperty(crossRef6.propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteCrossRefsForPoi_shouldDeleteCorrectRefs() = runBlocking {
        crossRefDao.saveCrossRefFromFirebase(crossRef4)
        val updated = crossRef4.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis()
        )
        crossRefDao.updateCrossRef(updated)
        crossRefDao.saveCrossRefFromFirebase(crossRef6)

        crossRefDao.deleteCrossRefsForPoi(crossRef6.poiId)
        val result = crossRefDao.getPropertyIdsForPoi(crossRef6.poiId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun clearAllCrossRefsDeleted_shouldRemoveAllData() = runBlocking {
        allCrossRefs.forEach {
            crossRefDao.saveCrossRefFromFirebase(it)
        }

        crossRefDao.markCrossRefsAsDeletedForProperty(crossRef1.propertyId, System.currentTimeMillis())
        crossRefDao.markCrossRefsAsDeletedForProperty(crossRef3.propertyId, System.currentTimeMillis())

        crossRefDao.clearAllDeleted()

        val result = crossRefDao.getAllCrossRefs().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun getUnSyncedPropertiesPoiSCross_shouldReturnOnlyUnSyncedRefs() = runBlocking {
        // Given: assume only the first N are synced, others are not
        crossRefDao.insertAllCrossRefs(allCrossRefs)

        // When
        val result = crossRefDao.uploadUnSyncedPropertiesPoiSCross().first()
        val expected = allCrossRefs.map { it.copy(isSynced = false) }
        // Then
        assertEquals(expected, result)
    }

    @Test
    fun saveCrossRefFromFirebase_shouldInsertCorrectly() = runBlocking {
        // Given
        crossRefDao.saveCrossRefFromFirebase(crossRef1)
        val result = crossRefDao.getCrossByIds(crossRef1.propertyId, crossRef1.poiId).first()
        val expected = crossRef1.copy(isSynced = true)
        assertEquals(expected, result)
    }

    @Test
    fun getCrossRefsByPropertyIdIncludeDeleted_shouldReturnDeletedAndNotDeleted() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)

        val result = crossRefDao.getCrossRefsByPropertyIdIncludeDeleted(crossRef5.propertyId).first()

        assertTrue(result.contains(crossRef5.copy(isSynced = false)))
    }

    @Test
    fun markCrossRefsAsDeletedForProperty_shouldKeepRefsInIncludeDeleted() = runBlocking {
        crossRefDao.insertCrossRef(crossRef1)

        crossRefDao.markCrossRefsAsDeletedForProperty(crossRef1.propertyId, System.currentTimeMillis())

        val result = crossRefDao.getCrossRefsByPropertyIdIncludeDeleted(crossRef1.propertyId).first()
        assertTrue(result.any { it.isDeleted })
    }

    //This test ensures that:
    //the Cursor is not null,
    //it contains data (when the database is not empty),
    //it is closed correctly (good practice).
    @Test
    fun getAllCrossRefsAsCursor_shouldReturnValidCursor() = runBlocking {
        crossRefDao.insertAllCrossRefs(allCrossRefs)
        val query = SimpleSQLiteQuery("SELECT * FROM property_poi_cross_ref")
        val cursor = crossRefDao.getAllCrossRefsAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}