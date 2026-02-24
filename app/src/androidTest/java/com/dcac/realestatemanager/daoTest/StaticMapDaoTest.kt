package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeStaticMapEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapDao
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaticMapDaoTest: DatabaseSetup() {

    private lateinit var staticMapDao : StaticMapDao

    private val staticMap1 = FakeStaticMapEntity.staticMap1
    private val staticMap2 = FakeStaticMapEntity.staticMap2
    private val staticMap3 = FakeStaticMapEntity.staticMap3
    private val allStaticMapNotDeleted = FakeStaticMapEntity.staticMapEntityListNotDeleted
    private val allStaticMap = FakeStaticMapEntity.staticMapEntityList


    @Before
    fun setup() = runBlocking {

        FakeUserEntity.userEntityList.forEach {
            db.userDao().firstUserInsertForceSyncedTrue(it)
        }
        FakePropertyEntity.propertyEntityList.forEach {
            db.propertyDao().insertPropertyFromUi(it)
        }
        staticMapDao = db.staticMapDao()
        }

    @Test
    fun getStaticMapById_shouldReturnCorrectStaticMap() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap1)

        val result = staticMapDao.getStaticMapById(staticMap1.id).first()

        assertEquals(staticMap1, result)
    }

    @Test
    fun getStaticMapById_shouldNotReturnDeletedStaticMap() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap3)

        val result = staticMapDao.getStaticMapByPropertyId(staticMap3.id).first()

        assertNull(result)
    }

    @Test
    fun getStaticMapByIdIncludeDeleted_shouldReturnStaticMapIncludeDeleted() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap3)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap3.id).first()

        assertEquals(staticMap3.id, result?.id)
    }

    @Test
    fun getStaticMapByPropertyId_shouldReturnCorrectStaticMap() = runBlocking {
        val property1 = FakePropertyEntity.property1

        staticMapDao.insertStaticMapInsertFromUI(staticMap1)

        val result = staticMapDao.getStaticMapByPropertyId(property1.id).first()

        assertEquals(staticMap1, result)
    }

    @Test
    fun getStaticMapByPropertyId_shouldNotReturnDeletedStaticMap() = runBlocking {
        val property3 = FakePropertyEntity.property3

        staticMapDao.insertStaticMapInsertFromUI(staticMap3)

        val result = staticMapDao.getStaticMapByPropertyId(property3.id).first()

        assertNull(result)
    }

    @Test
    fun getStaticMapByPropertyIdIncludeDeleted_shouldReturnStaticMapIncludeDeleted() = runBlocking {
        val property3 = FakePropertyEntity.property3

        staticMapDao.insertStaticMapInsertFromUI(staticMap3)

        val result = staticMapDao.getStaticMapByPropertyIdIncludeDeleted(property3.id).first()

        assertEquals(staticMap3, result)
    }

    @Test
    fun getAllStaticMap_shouldReturnAllStaticMapNotDeleted() = runBlocking {
        allStaticMap.forEach {
            staticMapDao.insertStaticMapInsertFromUI(it)
        }

        val result = staticMapDao.getAllStaticMap().first()

        val expectedIds = allStaticMapNotDeleted.map {it.id}
        val resultId = result.map {it.id}

        assertEquals(expectedIds, resultId)
    }

    @Test
    fun getAllStaticMapIncludeDeleted_shouldReturnAll() = runBlocking {
        allStaticMap.forEach {
            staticMapDao.insertStaticMapInsertFromUI(it)
        }

        val result = staticMapDao.getAllStaticMapIncludeDeleted().first()

        assertEquals(allStaticMap.size, result.size)
    }

    @Test
    fun uploadUnSyncedStaticMap_shouldReturnOnlyStaticMapWithIsSyncedFalse() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap1)
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap2)

        val result = staticMapDao.uploadUnSyncedStaticMap().first()

        assertEquals(1, result.size)
        assertTrue(result.all { !it.isSynced })
        assertEquals(staticMap1.id, result.first().id)
    }

    @Test
    fun insertStaticMapInsertFromUI_shouldInsertWithIsSyncedFalse() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap2)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
    }

    @Test
    fun insertStaticMapInsertFromFirebase_shouldInsertWithIsSyncedTrue()= runBlocking {
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap1)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun updateStaticMapFromUIForceSyncFalse_shouldSetIsSyncedFalse()= runBlocking {
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap2)

        val updated = staticMap2.copy(uri = "Updated uri")
        staticMapDao.updateStaticMapFromUIForceSyncFalse(updated)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals("Updated uri", result.uri)
    }

    @Test
    fun updateStaticMapFromFirebaseForceSyncTrue_shouldSetIsSyncedTrue() = runBlocking {
        staticMapDao.insertStaticMapInsertFromUI(staticMap1)

        val updated = staticMap1.copy(uri = "Updated uri from firebase")
        staticMapDao.updateStateMapFromFirebaseForceSyncTrue(updated)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
        assertEquals("Updated uri from firebase", result.uri)
    }

    @Test
    fun markStaticMapAsDeleted_shouldHideFromQueries() = runBlocking {
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap2)

        staticMapDao.markStaticMapAsDeleted(staticMap2.id, System.currentTimeMillis())

        val uiResult = staticMapDao.getStaticMapById(staticMap2.id).first()
        assertEquals(null, uiResult)

        val dbResult = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap2.id).first()

        assertNotNull(dbResult)
        assertTrue(dbResult!!.isDeleted)
        assertFalse(dbResult.isSynced)
    }

    @Test
    fun markStaticMapAsDeletedByProperty_shouldHideAllFromQueries()= runBlocking {
        val property2 = FakePropertyEntity.property2

        staticMapDao.insertStaticMapInsertFromFirebase(staticMap2)

        staticMapDao.markStaticMapsAsDeletedByProperty(property2.id, System.currentTimeMillis())

        val uiResult = staticMapDao.getStaticMapByPropertyId(property2.id).first()
        assertNull(uiResult)

        val dbResult = staticMapDao.getStaticMapByPropertyIdIncludeDeleted(property2.id).first()
        assertTrue(dbResult!!.isDeleted )
        assertFalse(dbResult.isSynced )
    }

    @Test
    fun deleteStaticMap_shouldRemoveStaticMap() = runBlocking {
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap3)

        val inserted = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap3.id).first()
        staticMapDao.deleteStaticMap(inserted!!)

        val result = staticMapDao.getStaticMapByIdIncludeDeleted(staticMap3.id).first()

        assertNull(result)
    }

    @Test
    fun deleteStaticMapByProperty_shouldRemoveStaticMap()= runBlocking {
        val property3 = FakePropertyEntity.property3
        staticMapDao.insertStaticMapInsertFromFirebase(staticMap3)

        staticMapDao.deleteStaticMapByPropertyId(property3.id)

        val result = staticMapDao.getStaticMapByPropertyIdIncludeDeleted(property3.id).first()

        assertNull(result)
    }

    @Test
    fun clearAllStaticMapDeleted_shouldRemoveOnlyDeletedStaticMap() = runBlocking {
        allStaticMap.forEach {
            staticMapDao.insertStaticMapInsertFromUI(it)
        }

        staticMapDao.markStaticMapAsDeleted(staticMap1.id, System.currentTimeMillis())
        staticMapDao.markStaticMapAsDeleted(staticMap2.id, System.currentTimeMillis())

        staticMapDao.clearAllStaticMapsDeleted()

        val result = staticMapDao.getAllStaticMapIncludeDeleted().first()
        assertEquals(0, result.size)

        }

    @Test
    fun getAllStaticMapAsCursor_shouldReturnValideCursor() = runBlocking {
        allStaticMap.forEach {
            staticMapDao.insertStaticMapInsertFromUI(it)
        }
        val query = SimpleSQLiteQuery("SELECT * FROM static_map")
        val cursor = staticMapDao.getAllStaticMapAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }


}