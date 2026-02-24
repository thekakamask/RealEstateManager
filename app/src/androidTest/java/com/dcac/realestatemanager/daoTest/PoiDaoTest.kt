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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PoiDaoTest: DatabaseSetup() {

    private lateinit var poiDao: PoiDao

    private val poi1 = FakePoiEntity.poi1
    private val poi2 = FakePoiEntity.poi2
    private val poi3 = FakePoiEntity.poi3
    private val crossref1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val allPoiSNotDeleted = FakePoiEntity.poiEntityListNotDeleted
    private val allPoiS = FakePoiEntity.poiEntityList

    @Before
    fun setup() = runBlocking {
        // Insert base required data for all tests
        FakeUserEntity.userEntityList.forEach {
            db.userDao().firstUserInsertForceSyncedTrue(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertPropertyFromUi(it)
        }

        poiDao = db.poiDao()
    }

    @Test
    fun getPoiById_shouldReturnCorrectPoi() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)

        val result = poiDao.getPoiById(poi1.id).first()

        assertEquals(poi1, result)
    }

    @Test
    fun getPoiById_shouldNotReturnDeletedPoi() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi3)

        val result = poiDao.getPoiById(poi3.id).first()

        assertNull(result)
    }

    @Test
    fun getPoiByIdIncludeDeleted_shouldReturnPoiIncludeDeleted() = runBlocking {
        poiDao.insertPoiInsertFromFirebase(poi3)

        val result = poiDao.getPoiByIdIncludeDeleted(poi3.id).first()

        assertEquals(poi3.id, result?.id)
    }

    @Test
    fun getAllPoiS_shouldReturnAllNotDeleted() = runBlocking {
        allPoiS.forEach { poi ->
            poiDao.insertPoiInsertFromUi(poi)
        }

        val result = poiDao.getAllPoiS().first()

        val expectedIds = allPoiSNotDeleted.map { it.id }
        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getAllPoiIncludeDeleted_shouldReturnAll() = runBlocking {
        poiDao.insertAllPoiSNotExistingFromFirebase(allPoiS)

        val result = poiDao.getAllPoiSIncludeDeleted().first()

        assertEquals(allPoiS.size, result.size)
    }

    @Test
    fun uploadUnSyncedPoiS_shouldReturnOnlyPoiSWithIsSyncedFalse() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)
        poiDao.insertPoiInsertFromFirebase(poi2)

        val result = poiDao.uploadUnSyncedPoiS().first()

        assertEquals(1, result.size)
        assertTrue(result.all { !it.isSynced })
        assertEquals(poi1.id, result.first().id)
    }

    @Test
    fun insertPoiInsertFromUI_shouldInsertWithIsSyncedFalse() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi2)

        val result = poiDao.getPoiByIdIncludeDeleted(poi2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
    }

    @Test
    fun insertPoiSInsertFromUI_shouldInsertAllWithIsSyncedFalse() = runBlocking {
        allPoiS.forEach { poi ->
            poiDao.insertPoiInsertFromUi(poi)
        }

        val result = poiDao.getAllPoiSIncludeDeleted().first()

        assertEquals(allPoiS.size, result.size)
        assertTrue(result.all { !it.isSynced })
    }

    @Test
    fun insertPoiInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runBlocking {
        poiDao.insertPoiInsertFromFirebase(poi1)

        val result = poiDao.getPoiByIdIncludeDeleted(poi1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertAllPoiSNotExistingFromFirebase_shouldInsertAllWithIsSyncedTrue() = runBlocking {
        poiDao.insertAllPoiSNotExistingFromFirebase(allPoiS)

        val result = poiDao.getAllPoiSIncludeDeleted().first()

        assertEquals(allPoiS.size, result.size)
        assertTrue(result.all {it.isSynced})
    }

    @Test
    fun updatePoiFromUIForceSyncFalse_shouldSetIsSyncedFalse() = runBlocking {
        poiDao.insertPoiInsertFromFirebase(poi2)

        val updated = poi2.copy(name = "Updated name")
        poiDao.updatePoiFromUIForceSyncFalse(updated)

        val result = poiDao.getPoiByIdIncludeDeleted(poi2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals(updated.name ,result.name)
    }

    @Test
    fun updatePoiFromFirebaseForceSyncTrue_shouldSetIsSyncedTrue() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)

        val updated = poi1.copy(name = "From Firebase")
        poiDao.updatePoiFromFirebaseForceSyncTrue(updated)

        val result = poiDao.getPoiByIdIncludeDeleted(poi1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
        assertEquals(updated.name, result.name)
    }

    @Test
    fun updateAllPoiSFromFirebaseForceSyncTrue_shouldUpdateAll() = runBlocking {
        allPoiS.forEach { poi ->
            poiDao.insertPoiInsertFromUi(poi)
        }

        poiDao.updateAllPoiFromFirebaseForceSyncTrue(allPoiS)

        val result = poiDao.getAllPoiSIncludeDeleted().first()

        assertEquals(allPoiS.size, result.size)
        assertTrue(result.all { it.isSynced })

    }

    @Test
    fun markPoiAsDeleted_shouldHideFromQueries() = runBlocking {
        poiDao.insertPoiInsertFromFirebase(poi2)

        poiDao.markPoiAsDeleted(poi2.id, System.currentTimeMillis())

        val uiResult = poiDao.getPoiById(poi2.id).first()
        assertNull(uiResult)

        val dbResult = poiDao.getPoiByIdIncludeDeleted(poi2.id).first()
        assertNotNull(dbResult)
        assertTrue(dbResult!!.isDeleted)
        assertFalse(dbResult.isSynced)
    }


    @Test
    fun deletePoi_shouldRemovePoi() = runBlocking {
        poiDao.insertPoiInsertFromFirebase(poi3)

        val inserted = poiDao.getPoiByIdIncludeDeleted(poi3.id).first()
        poiDao.deletePoi(inserted!!)

        val result = poiDao.getPoiByIdIncludeDeleted(poi3.id).first()

        assertNull(result)
    }

    @Test
    fun clearAllPoiSDeleted_shouldRemoveOnlyDeletedPoiS() = runBlocking {
        poiDao.insertAllPoiSNotExistingFromFirebase(allPoiS)

        poiDao.markPoiAsDeleted(poi1.id, System.currentTimeMillis())
        poiDao.markPoiAsDeleted(poi2.id, System.currentTimeMillis())

        poiDao.clearAllPoiSDeleted()

        val result = poiDao.getAllPoiSIncludeDeleted().first()

        assertEquals(0, result.size)
    }

    @Test
    fun findExistingPoi_shouldReturnPoi_withNameAndAddressMatchIgnoringCase_andNotDeleted() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)

        val found = poiDao.findExistingPoi(
            name = poi1.name.uppercase(),
            address = poi1.address.lowercase()
        )
        assertNotNull(found)
        assertEquals(poi1.id, found?.id)
    }

    @Test
    fun findExistingPoi_shouldReturnNull_whenNameMatchesButAddressDifferent() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)

        val found = poiDao.findExistingPoi(
            name = poi1.name,
            address = "Different address"
        )

        assertNull(found)
    }

    @Test
    fun findExistingPoi_shouldReturnNull_whenPoiIsDeleted() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)
        poiDao.markPoiAsDeleted(poi1.id, System.currentTimeMillis())

        val found = poiDao.findExistingPoi(
            name = poi1.name,
            address = poi1.address
        )
        assertNull(found)
    }

    @Test
    fun getPoiWithProperties_shouldReturnPoiWithLinkedProperties() = runBlocking {
        poiDao.insertPoiInsertFromUi(poi1)

        db.propertyCrossDao().insertCrossRefInsertFromUI(crossref1)

        val relation = poiDao.getPoiWithProperties(poi1.id).first()

        assertEquals(poi1.id, relation.poi.id)
        assertTrue(relation.properties.isNotEmpty())
        assertTrue(relation.properties.any { it.id == crossref1.universalLocalPropertyId })
    }

    @Test
    fun getAllPoiSAsCursor_shouldReturnValidCursor() = runBlocking {
        allPoiS.forEach { poi ->
            poiDao.insertPoiInsertFromUi(poi)
        }
        val query = SimpleSQLiteQuery("SELECT * FROM poi")
        val cursor = poiDao.getAllPoiSAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}