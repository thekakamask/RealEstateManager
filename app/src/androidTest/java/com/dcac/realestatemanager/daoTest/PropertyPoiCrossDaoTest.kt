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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

@RunWith(AndroidJUnit4::class)
class PropertyPoiCrossDaoTest: DatabaseSetup() {

    private lateinit var crossRefDao: PropertyPoiCrossDao

    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRef2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRef3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRef4 = FakePropertyPoiCrossEntity.propertyPoiCross4
    private val crossRef5 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRef6 = FakePropertyPoiCrossEntity.propertyPoiCross6
    private val allCrossRefsNotDeleted = FakePropertyPoiCrossEntity.allCrossRefsNotDeleted
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs




    @Before
    fun setup() = runBlocking {

        FakeUserEntity.userEntityList.forEach {
            db.userDao().firstUserInsertForceSyncedTrue(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertPropertyFromUi(it)
        }
        FakePoiEntity.poiEntityList.forEach {
            db.poiDao().insertPoiInsertFromUi(it)
        }

        crossRefDao = db.propertyCrossDao()

    }

    @Test
    fun getCrossRefsForProperty_shouldReturnCorrectRefs() = runBlocking {
        val property1 = FakePropertyEntity.property1

        crossRefDao.insertAllCrossRefInsertFromUi(allCrossRefs)

        val result = crossRefDao.getCrossRefsForProperty(property1.id).first()

        val expected = allCrossRefsNotDeleted
            .filter { it.universalLocalPropertyId == property1.id }

        assertEquals(expected, result)
    }


    @Test
    fun getCrossRefsForPropertyIncludedDeleted_shouldReturnAllCrossRefsForPropertyIncludingDeleted() = runBlocking {
        val property3 = FakePropertyEntity.property3

        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val result = crossRefDao.getCrossRefsByPropertyIdIncludeDeleted(property3.id).first()

        val expected = allCrossRefs
            .filter { it.universalLocalPropertyId == property3.id}
            .map { it.copy(isSynced = true) }

        assertEquals(expected, result)
    }

    @Test
    fun getPoiIdsForProperty_shouldReturnCorrectPoiIds() = runBlocking {
        val property1 = FakePropertyEntity.property1
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val expected = allCrossRefsNotDeleted
            .filter {
                it.universalLocalPropertyId == property1.id }
            .map { it.universalLocalPoiId }

        val result = crossRefDao
            .getPoiIdsForProperty(property1.id)
            .first()

        assertEquals(expected, result)
    }

    @Test
    fun getPropertyIdsForPoi_shouldReturnCorrectPropertyIds() = runBlocking {
        val poi1 = FakePoiEntity.poi1
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val expected = allCrossRefsNotDeleted
            .filter {
                it.universalLocalPoiId == poi1.id }
            .map { it.universalLocalPropertyId }

        val result = crossRefDao
            .getPropertyIdsForPoi(poi1.id)
            .first()

        assertEquals(expected, result)
    }

    @Test
    fun getAllCrossRefs_shouldReturnAllRefsNotDeleted() = runBlocking {
        crossRefDao.insertAllCrossRefInsertFromUi(allCrossRefs)

        val result = crossRefDao.getAllCrossRefs().first()

        val expected = allCrossRefs
            .filter { !it.isDeleted }
            .map { it.universalLocalPropertyId to it.universalLocalPoiId }

        val resultKeys = result
            .map { it.universalLocalPropertyId to it.universalLocalPoiId }

        assertEquals(expected, resultKeys)
    }

    @Test
    fun getAllCrossRefsIncludeDeleted_shouldReturnAll() = runBlocking {
        crossRefDao.insertAllCrossRefInsertFromUi(allCrossRefs)

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()

        val expected = allCrossRefs
            .map { it.universalLocalPropertyId to it.universalLocalPoiId }

        val resultKeys = result
            .map { it.universalLocalPropertyId to it.universalLocalPoiId }

        assertEquals(expected, resultKeys)
    }

    @Test
    fun getCrossByIds_shouldReturnCorrectCrossRef() = runBlocking {
        crossRefDao.insertCrossRefInsertFromUI(crossRef1)

        val result = crossRefDao.getCrossByIds(
            crossRef1.universalLocalPropertyId,
            crossRef1.universalLocalPoiId)
            .first()

        assertEquals(crossRef1.universalLocalPropertyId, result?.universalLocalPropertyId)
        assertEquals(crossRef1.universalLocalPoiId, result?.universalLocalPoiId)
    }

    @Test
    fun getCrossByIds_shouldNotReturnDeletedCrossRef() = runBlocking {
        crossRefDao.insertCrossRefInsertFromUI(crossRef5)

        val result = crossRefDao.getCrossByIds(
            crossRef5.universalLocalPropertyId,
            crossRef5.universalLocalPoiId)
            .first()

        assertNull(result)
    }

    @Test
    fun getCrossByIdsIncludeDeleted_shouldReturnCrossRefDeleted() = runBlocking {
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef6)

        val result = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef6.universalLocalPropertyId,
            crossRef6.universalLocalPoiId)
            .first()

        assertEquals(crossRef6.universalLocalPropertyId, result?.universalLocalPropertyId)
        assertEquals(crossRef6.universalLocalPoiId, result?.universalLocalPoiId)
    }

    @Test
    fun uploadUnSyncedCrossRefs_shouldReturnOnlyCrossRefsWithIsSyncedFalse() = runBlocking {
        crossRefDao.insertCrossRefInsertFromUI(crossRef1)
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef2)

        val result = crossRefDao.uploadUnSyncedCrossRefs().first()

        assertEquals(1, result.size)
        assertTrue(result.all {!it.isSynced})
        assertEquals(crossRef1.universalLocalPropertyId, result.first().universalLocalPropertyId)
        assertEquals(crossRef1.universalLocalPoiId, result.first().universalLocalPoiId)
    }

    @Test
    fun insertCrossRefFromUI_shouldInsertWithIsSyncedFalse() = runBlocking {
        crossRefDao.insertCrossRefInsertFromUI(crossRef2)

        val result = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef2.universalLocalPropertyId,
            crossRef2.universalLocalPoiId)
            .first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
    }

    @Test
    fun insertAllCrossRefsInsertFromUI_shouldInsertAllWithIsSyncedFalse() = runBlocking {
        crossRefDao.insertAllCrossRefInsertFromUi(allCrossRefs)

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()

        assertEquals(allCrossRefs.size, result.size)
        assertTrue(result.all { !it.isSynced })
    }

    @Test
    fun insertCrossRefFromFirebase_shouldInsertWithIsSyncedTrue() = runBlocking {
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef1)

        val result = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef1.universalLocalPropertyId,
            crossRef1.universalLocalPoiId)
            .first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertAllCrossRefsNotExistingFromFirebase_shouldInsertAllWithIsSyncedTrue() = runBlocking {
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()

        assertEquals(allCrossRefs.size, result.size)
        assertTrue(result.all { it.isSynced })
    }

    @Test
    fun updateCrossRefFromUIForceSyncFalse_shouldSetIsSyncedFalse() = runBlocking {
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef2)

        val newUpdatedAt = System.currentTimeMillis()

        val updated = crossRef2.copy(
            updatedAt = newUpdatedAt
        )

        crossRefDao.updateCrossRefFromUIForceSyncFalse(updated)

        val result = crossRefDao
            .getCrossRefsByIdsIncludedDeleted(
                crossRef2.universalLocalPropertyId,
                crossRef2.universalLocalPoiId
            ).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals(newUpdatedAt, result.updatedAt)
    }

    @Test
    fun updateAllCrossRefsFromUIForceSyncFalse_shouldSetIsSyncedFalse() = runBlocking {
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val newUpdatedAt = System.currentTimeMillis()

        val updatedList = allCrossRefs.map {
            it.copy(updatedAt = newUpdatedAt)
        }

        crossRefDao.updateAllCrossRefsFromUIForceSyncFalse(updatedList)

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()

        assertEquals(allCrossRefs.size, result.size)
        assertTrue(result.all { !it.isSynced })
        assertTrue(result.all { it.updatedAt == newUpdatedAt })
    }

    @Test
    fun updateAllCrossRefsFromFirebaseForceSyncTrue_shouldUpdateAll() = runBlocking {
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        crossRefDao.updateAllCrossRefFromFirebaseForceSyncTrue(allCrossRefs)

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()

        assertEquals(allCrossRefs.size, result.size)
        assertTrue(result.all { it.isSynced })
    }

    @Test
    fun markCrossRefAsDeleted_shouldHideFromQueries() = runBlocking {
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef2)

        crossRefDao.markCrossRefAsDeleted(
            crossRef2.universalLocalPropertyId,
            crossRef2.universalLocalPoiId,
            System.currentTimeMillis())

        val uiResult = crossRefDao.getCrossByIds(
            crossRef2.universalLocalPropertyId,
            crossRef2.universalLocalPoiId)
            .first()
        assertEquals(null, uiResult)

        val dbResult = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef2.universalLocalPropertyId,
            crossRef2.universalLocalPoiId)
            .first()

        assertNotNull(dbResult)
        assertTrue(dbResult!!.isDeleted)
        assertFalse(dbResult.isSynced)

    }

    @Test
    fun markCrossRefAsDeletedByProperty_shouldHideAllFromQueries() = runBlocking {
        val property2 = FakePropertyEntity.property2
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        crossRefDao.markCrossRefsAsDeletedForProperty(
            property2.id,
            System.currentTimeMillis()
        )

        val uiResult = crossRefDao.getCrossRefsForProperty(property2.id).first()
        assertTrue(uiResult.isEmpty())

        val dbResult = crossRefDao.getCrossRefsByPropertyIdIncludeDeleted(property2.id).first()
        assertTrue(dbResult.all { it.isDeleted })
        assertTrue(dbResult.all { !it.isSynced })
    }

    @Test
    fun markCrossRefsAsDeletedForPoi_shouldHideAllFromQueries() = runBlocking {
        val poi2 = FakePoiEntity.poi2

        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        crossRefDao.markCrossRefsAsDeletedForPoi(poi2.id, System.currentTimeMillis())

        val uiResult = crossRefDao.getPropertyIdsForPoi(poi2.id).first()
        assertTrue(uiResult.isEmpty())

        val all = crossRefDao.getAllCrossRefsIncludeDeleted().first()
        val affected = all.filter { it.universalLocalPoiId == poi2.id }

        assertTrue(affected.isNotEmpty())
        assertTrue(affected.all { it.isDeleted })
        assertTrue(affected.all { !it.isSynced })
    }

    @Test
    fun markAllCrossRefsAsDeleted_shouldHideAllFromQueries() = runBlocking {
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        val newUpdatedAt = System.currentTimeMillis()
        crossRefDao.markAllCrossRefsAsDeleted(newUpdatedAt)

        val uiResult = crossRefDao.getAllCrossRefs().first()
        assertTrue(uiResult.isEmpty())

        val dbResult = crossRefDao.getAllCrossRefsIncludeDeleted().first()
        assertEquals(allCrossRefs.size, dbResult.size)
        assertTrue(dbResult.all { it.isDeleted })
        assertTrue(dbResult.all { !it.isSynced })
        assertTrue(dbResult.all { it.updatedAt == newUpdatedAt })
    }

    @Test
    fun deleteCrossRef_shouldRemoveCrossRef() = runBlocking {
        crossRefDao.insertCrossRefInsertFromFirebase(crossRef5)

        val inserted = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef5.universalLocalPropertyId,
            crossRef5.universalLocalPoiId)
            .first()

        crossRefDao.deleteCrossRef(inserted!!)

        val result = crossRefDao.getCrossRefsByIdsIncludedDeleted(
            crossRef5.universalLocalPropertyId,
            crossRef5.universalLocalPoiId)
            .first()

        assertNull(result)
    }

    @Test
    fun deleteCrossRefsByProperty_shouldRemoveAllForProperty() = runBlocking {
        val property3 = FakePropertyEntity.property3
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        crossRefDao.deleteCrossRefsForProperty(property3.id)

        val result = crossRefDao.getCrossRefsByPropertyIdIncludeDeleted(property3.id).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun clearAllCrossRefsDeleted_shouldRemoveOnlyDeletedCrossRefs() = runBlocking {
        crossRefDao.insertAllCrossRefNotExistingFromFirebase(allCrossRefs)

        crossRefDao.markCrossRefAsDeleted(
            crossRef1.universalLocalPropertyId,
            crossRef1.universalLocalPoiId,
            System.currentTimeMillis()
        )
        crossRefDao.markCrossRefAsDeleted(
            crossRef2.universalLocalPropertyId,
            crossRef2.universalLocalPoiId,
            System.currentTimeMillis()
        )
        crossRefDao.markCrossRefAsDeleted(
            crossRef3.universalLocalPropertyId,
            crossRef3.universalLocalPoiId,
            System.currentTimeMillis()
        )
        crossRefDao.markCrossRefAsDeleted(
            crossRef4.universalLocalPropertyId,
            crossRef4.universalLocalPoiId,
            System.currentTimeMillis()
        )

        crossRefDao.clearAllDeleted()

        val result = crossRefDao.getAllCrossRefsIncludeDeleted().first()
        assertEquals(0, result.size)
    }

    @Test
    fun getAllCrossRefsAsCursor_shouldReturnValidCursor() = runBlocking {
        crossRefDao.insertAllCrossRefInsertFromUi(allCrossRefs)
        val query = SimpleSQLiteQuery("SELECT * FROM property_poi_cross_ref")
        val cursor = crossRefDao.getAllCrossRefsAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }

}
