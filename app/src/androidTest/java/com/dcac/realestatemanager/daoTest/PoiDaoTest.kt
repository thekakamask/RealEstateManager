package com.dcac.realestatemanager.daoTest

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
import org.junit.Assert.assertFalse
import kotlinx.coroutines.flow.first

@RunWith(AndroidJUnit4::class)
class PoiDaoTest: DatabaseSetup() {

    private lateinit var poiDao: PoiDao

    private val poi1 = FakePoiEntity.poi1
    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val property1 = FakePropertyEntity.property1

    private val poiList = FakePoiEntity.poiEntityList
    private val crossRefList = FakePropertyPoiCrossEntity.propertyPoiCrossEntityList

    @Before
    fun setup() = runBlocking {
        // Insert base required data for all tests
        db.userDao().saveUserFromFirebase(FakeUserEntity.user1)
        db.userDao().saveUserFromFirebase(FakeUserEntity.user2)
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertProperty(it)
        }

        poiDao = db.poiDao()
    }

    @Test
    fun insertPoi_shouldInsertSinglePoi() = runBlocking {
        poiDao.insertPoi(poi1)
        db.propertyCrossDao().insertCrossRef(crossRef1)

        // WHEN
        val result = poiDao.getAllPoiS().first()

        // THEN
        assertEquals(listOf(poi1), result)
    }

    @Test
    fun insertAllPoiS_shouldInsertMultiplePoi() = runBlocking {
        poiDao.insertAllPoiS(poiList)
        db.propertyCrossDao().insertAllCrossRefs(crossRefList)

        val result = poiDao.getAllPoiS().first()
        assertEquals(poiList, result)
    }

    @Test
    fun updatePoi_shouldUpdateExistingPoi() = runBlocking {
        val updatedPoi = poi1.copy(name = "Updated Name", type = "Updated Type")

        poiDao.insertPoi(poi1)
        poiDao.updatePoi(updatedPoi)

        val result = poiDao.getAllPoiS().first().first()
        assertEquals("Updated Name", result.name)
        assertEquals("Updated Type", result.type)
    }

    @Test
    fun deletePoi_shouldRemovePoiFromDatabase() = runBlocking {
        poiDao.insertAllPoiS(poiList)
        db.propertyCrossDao().insertAllCrossRefs(crossRefList)

        db.propertyCrossDao().deleteCrossRefsForPoi(poi1.id)
        poiDao.deletePoi(poi1)

        val result = poiDao.getAllPoiS().first()
        assertFalse(result.contains(poi1))
    }

    @Test
    fun getPoiWithProperties_shouldReturnAssociatedProperties() = runBlocking {
        poiDao.insertPoi(poi1)
        db.propertyCrossDao().insertCrossRef(crossRef1)

        val result = poiDao.getPoiWithProperties(poi1.id).first()
        assertEquals(poi1, result.poi)
        assertEquals(listOf(property1), result.properties)
    }

    @Test
    fun getUnSyncedPoiS_shouldReturnOnlyUnSyncedPoi() = runBlocking {
        // Suppose poi1 isSynced = true, poi2 & poi3 are false
        poiDao.insertAllPoiS(listOf(poi1, FakePoiEntity.poi2, FakePoiEntity.poi3))

        val result = poiDao.getUnSyncedPoiS().first()

        assertFalse(result.contains(poi1))
        assertEquals(2, result.size)
    }

}