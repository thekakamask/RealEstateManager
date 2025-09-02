package com.dcac.realestatemanager.daoTest

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

    private lateinit var propertyPoiCrossDao: PropertyPoiCrossDao

    private val poi1 = FakePoiEntity.poi1
    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val property1 = FakePropertyEntity.property1

    private val crossList = FakePropertyPoiCrossEntity.propertyPoiCrossEntityList

    @Before
    fun setup() = runBlocking {

        // Insert base required data for all tests
        db.userDao().saveUserFromFirebase(FakeUserEntity.user1)
        db.userDao().saveUserFromFirebase(FakeUserEntity.user2)
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }
        FakePoiEntity.poiEntityList.forEach{
            db.poiDao().insertPoi(it)
        }

        propertyPoiCrossDao = db.propertyCrossDao()

    }

    @Test
    fun insertCrossRef_shouldInsertOneCrossRef() = runBlocking {
        propertyPoiCrossDao.insertCrossRef(crossRef1)
        val result = propertyPoiCrossDao.getAllCrossRefs().first()
        assertTrue(result.contains(crossRef1))
    }

    @Test
    fun insertAllCrossRefs_shouldInsertMultipleCrossRefs() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        val result = propertyPoiCrossDao.getAllCrossRefs().first()
        assertEquals(crossList.size, result.size)
        assertTrue(result.containsAll(crossList))
    }

    @Test
    fun updateCrossRef_shouldUpdateCorrectly() = runBlocking {
        // Given
        val original = crossRef1.copy(isSynced = false)
        propertyPoiCrossDao.insertCrossRef(original)

        // When: update the isSynced flag
        val updated = original.copy(isSynced = true)
        propertyPoiCrossDao.updateCrossRef(updated)

        // Then: fetch and verify
        val result = propertyPoiCrossDao.getAllCrossRefs().first()
        val match = result.find { it.propertyId == updated.propertyId && it.poiId == updated.poiId }

        assertNotNull(match)
        assertTrue(match!!.isSynced)
    }

    @Test
    fun deleteCrossRefsForProperty_shouldDeleteCorrectRefs() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        propertyPoiCrossDao.deleteCrossRefsForProperty(property1.id)
        val result = propertyPoiCrossDao.getCrossRefsForProperty(property1.id).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteCrossRefsForPoi_shouldDeleteCorrectRefs() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        propertyPoiCrossDao.deleteCrossRefsForPoi(poi1.id)
        val result = propertyPoiCrossDao.getPropertyIdsForPoi(poi1.id).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getCrossRefsForProperty_shouldReturnCorrectRefs() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        val expected = crossList.filter { it.propertyId == property1.id }
        val result = propertyPoiCrossDao.getCrossRefsForProperty(property1.id).first()
        assertEquals(expected, result)
    }

    @Test
    fun getPoiIdsForProperty_shouldReturnCorrectPoiIds() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        val expected = crossList.filter { it.propertyId == property1.id }.map { it.poiId }
        val result = propertyPoiCrossDao.getPoiIdsForProperty(property1.id).first()
        assertEquals(expected, result)
    }

    @Test
    fun getPropertyIdsForPoi_shouldReturnCorrectPropertyIds() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        val expected = crossList.filter { it.poiId == poi1.id }.map { it.propertyId }
        val result = propertyPoiCrossDao.getPropertyIdsForPoi(poi1.id).first()
        assertEquals(expected, result)
    }

    @Test
    fun getAllCrossRefs_shouldReturnAllRefs() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        val result = propertyPoiCrossDao.getAllCrossRefs().first()
        assertEquals(crossList.size, result.size)
    }

    @Test
    fun clearAllCrossRefs_shouldRemoveAllData() = runBlocking {
        propertyPoiCrossDao.insertAllCrossRefs(crossList)
        propertyPoiCrossDao.clearAllCrossRefs()
        val result = propertyPoiCrossDao.getAllCrossRefs().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getUnSyncedPropertiesPoiSCross_shouldReturnOnlyUnSyncedRefs() = runBlocking {
        // Given: assume only the first N are synced, others are not
        propertyPoiCrossDao.insertAllCrossRefs(crossList)

        // When
        val result = propertyPoiCrossDao.getUnSyncedPropertiesPoiSCross().first()

        // Then
        assertTrue(result.none { it.isSynced })
    }

    @Test
    fun getCrossByIds_shouldReturnCorrectCrossRef() = runBlocking {
        // Given
        propertyPoiCrossDao.insertCrossRef(crossRef1)

        // When
        val result = propertyPoiCrossDao.getCrossByIds(crossRef1.propertyId, crossRef1.poiId).first()

        // Then
        assertNotNull(result)
        assertEquals(crossRef1.propertyId, result?.propertyId)
        assertEquals(crossRef1.poiId, result?.poiId)
    }

    @Test
    fun saveCrossRefFromFirebase_shouldInsertCorrectly() = runBlocking {
        // Given
        val firebaseCross = crossRef1.copy(isSynced = true)

        // When
        propertyPoiCrossDao.saveCrossRefFromFirebase(firebaseCross)

        // Then
        val result = propertyPoiCrossDao.getCrossByIds(firebaseCross.propertyId, firebaseCross.poiId).first()
        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }


}