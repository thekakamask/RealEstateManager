
package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePoiDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePoiOnlineEntity
import com.dcac.realestatemanager.model.Poi
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PoiRepositoryTest {

    private lateinit var fakePoiDao: FakePoiDao
    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var userRepository: UserRepository
    private lateinit var poiRepository: PoiRepository

    private val poiEntity1 = FakePoiEntity.poi1
    private val poiEntity3 = FakePoiEntity.poi3
    private val poiEntityList = FakePoiEntity.poiEntityList
    private val poiEntityListNotDeleted = FakePoiEntity.poiEntityListNotDeleted
    private val propertyEntityList = FakePropertyEntity.propertyEntityList
    private val poiOnlineEntity1 = FakePoiOnlineEntity.poiOnline1
    private val poiOnlineEntity2 = FakePoiOnlineEntity.poiOnline2
    private val poiOnlineEntity3 = FakePoiOnlineEntity.poiOnline3
    private val poiModel1 = FakePoiModel.poi1
    private val poiModel2 = FakePoiModel.poi2
    private val poiModel3 = FakePoiModel.poi3
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs
    private val allPoiModelsNotDeleted = FakePoiModel.poiModelListNotDeleted

    @Before
    fun setup() {
        // In-memory Poi DAO with properties + cross-refs in local stores
        fakePoiDao = FakePoiDao().apply {
            // 1) Seed PropertyEntity snapshot used to build relations
            seedProperties(propertyEntityList)

            // 2) Build links poiId -> propertyIds from the cross table
            //    Cross = (propertyId, poiId)-
            val mapByPoi = allCrossRefs
                .groupBy { it.universalLocalPoiId } // group by poiId
                .mapValues { (_, list) -> list.map { it.universalLocalPropertyId } } // -> List<propertyId>

            mapByPoi.forEach { (poiId, propertyIds) ->
                linkPoiToProperties(poiId, *propertyIds.toTypedArray())
            }
        }

        // Real repository for users backed by a FakeUserDao (seeded in its init)
        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)

        // Repository under test
        poiRepository = OfflinePoiRepository(fakePoiDao)
    }

    @Test
    fun getPoiById_shouldReturnsCorrectPoi() = runTest {
        val result = poiRepository.getPoiById(poiModel1.universalLocalId).first()

        assertEquals(poiModel1, result)
    }

    @Test
    fun getAllPoiS_shouldReturnsAll() = runTest {
        val result = poiRepository.getAllPoiS().first()

        assertEquals(allPoiModelsNotDeleted, result)
    }

    @Test
    fun uploadUnSyncedPoi_shouldReturnOnlyPoiWithIsSyncedFalse() = runTest {
        val result = poiRepository.uploadUnSyncedPoiSToFirebase().first()

        val expected = poiEntityList
            .filter { !it.isSynced }

        assertEquals(expected, result)
    }

    @Test
    fun insertPoiInsertFromUI_shouldInsertWithIsSyncedFalse() = runTest {
        val newPoiModel = Poi(
            universalLocalId = "poi-4",
            name = "New poi 4",
            type = "New type",
            address = "New address",
            updatedAt = 1800000000000L
        )

        val before = System.currentTimeMillis()

        poiRepository.insertPoiInsertFromUI(newPoiModel)

        val after = System.currentTimeMillis()

        val resultEntity = fakePoiDao.entityMap[newPoiModel.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(newPoiModel.universalLocalId, id)
            assertEquals(newPoiModel.name, name)
            assertEquals(newPoiModel.type, type)
            assertEquals(newPoiModel.address, address)
            assertFalse(isSynced)
            assertFalse(isDeleted)
            assertTrue(updatedAt in before..after)
        }

        val resultInserted = poiRepository
            .getPoiById(newPoiModel.universalLocalId)
            .first()

        assertNotNull(resultInserted)
    }

    @Test
    fun insertPoiInsertFromUI_shouldReturnExistingPoiIfAlreadyExists() = runTest {
        val duplicatePoi = Poi(
            universalLocalId = "poi-4",
            name = poiModel1.name,
            type = poiModel1.type,
            address = poiModel1.address
        )

        val sizeBefore = fakePoiDao.entityMap.size

        val result = poiRepository.insertPoiInsertFromUI(duplicatePoi)

        val sizeAfter = fakePoiDao.entityMap.size

        assertEquals(sizeBefore, sizeAfter)

        assertEquals(poiModel1.universalLocalId, result.universalLocalId)
        assertEquals(poiModel1.name, result.name)
        assertEquals(poiModel1.address, result.address)
        assertEquals(poiModel1.isSynced, result.isSynced)
        assertEquals(poiModel1.isDeleted, result.isDeleted)
        assertEquals(poiModel1.updatedAt, result.updatedAt)
    }

    @Test
    fun insertPoiInsertFromFirebase_shouldInsertWithIsSyncedTrue()= runTest {
        val firestoreId = "firestore-poi-4"
        val onlinePoi = PoiOnlineEntity(
            ownerUid = "firebase_uid_1",
            universalLocalId = "poi-4",
            name = "New poi 4",
            type = "New type",
            address = "New address",
            updatedAt = 1900000000000L
        )

        poiRepository.insertPoiInsertFromFirebase(
            poi = onlinePoi,
            firebaseDocumentId = firestoreId
        )

        val resultEntity = fakePoiDao.entityMap[onlinePoi.universalLocalId]

        assertNotNull(resultEntity)
        resultEntity!!.apply {
            assertEquals(onlinePoi.universalLocalId, resultEntity.id)
            assertEquals(firestoreId, resultEntity.firestoreDocumentId)
            assertEquals(onlinePoi.name, resultEntity.name)
            assertEquals(onlinePoi.type, resultEntity.type)
            assertEquals(onlinePoi.address, resultEntity.address)
            assertTrue(resultEntity.isSynced)
            assertEquals(onlinePoi.updatedAt, resultEntity.updatedAt)
        }

        val resultInserted = poiRepository
            .getPoiById(onlinePoi.universalLocalId)
            .first()

        assertNotNull(resultInserted)
        assertEquals(firestoreId, resultInserted!!.firestoreDocumentId)
        assertEquals(onlinePoi.universalLocalId, resultInserted.universalLocalId)
        assertEquals(onlinePoi.name, resultInserted.name)
        assertEquals(onlinePoi.type, resultInserted.type)
        assertTrue(resultInserted.isSynced)
        assertEquals(onlinePoi.updatedAt, resultInserted.updatedAt)
    }

    @Test
    fun insertPoiSInsertFromFirebase_shouldInsertAllWithIsSyncedTrue() = runTest {
        val insertedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-poi-4",
            "firestore-poi-5",
            "firestore-poi-6"
            )
        val onlinePoiS = listOf(
            PoiOnlineEntity(
                ownerUid = "firebase_uid_1",
                universalLocalId = "poi-4",
                name = "New poi 4",
                type = "New type 4",
                address = "New address 4",
                updatedAt = insertedTimestamp + 1
            ),
            PoiOnlineEntity(
                ownerUid = "firebase_uid_2",
                universalLocalId = "poi-5",
                name = "New poi 5",
                type = "New type 5",
                address = "New address 5",
                updatedAt = insertedTimestamp + 2
            ),
            PoiOnlineEntity(
                ownerUid = "firebase_uid_1",
                universalLocalId = "poi-6",
                name = "New poi 6",
                type = "New type 6",
                address = "New address 6",
                updatedAt = insertedTimestamp + 3
            )
        )

        val pairs = onlinePoiS.mapIndexed { index, poi ->
            Pair(poi, firestoreIds[index])
        }

        poiRepository.insertPoiSInsertFromFirebase(pairs)

        onlinePoiS.forEachIndexed { index, expected ->

            val resultEntity = fakePoiDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.universalLocalId, resultEntity.id)
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertEquals(expected.name, resultEntity.name)
                assertEquals(expected.type, resultEntity.type)
                assertEquals(expected.address, resultEntity.address)
                assertTrue(resultEntity.isSynced)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
            }
        }


        val allPoiS = poiRepository.getAllPoiS().first()

        onlinePoiS.forEachIndexed { index, expected ->

            val resultInserted = allPoiS.find {
                it.universalLocalId == expected.universalLocalId
            }

            assertNotNull(resultInserted)
            assertEquals(firestoreIds[index], resultInserted!!.firestoreDocumentId)
            assertEquals(expected.universalLocalId, resultInserted.universalLocalId)
            assertEquals(expected.name, resultInserted.name)
            assertEquals(expected.type, resultInserted.type)
            assertEquals(expected.address, resultInserted.address)
            assertTrue(resultInserted.isSynced)
            assertEquals(expected.updatedAt, resultInserted.updatedAt)
        }
    }

    @Test
    fun updatePoiFromUI_shouldUpdatePoiAndForceSyncFalse() = runTest {
        val updatedTimestamp = 1800000000000L
        val updatedPoi = poiModel1.copy(
            name = "Updated name",
            type = "Updated type",
            updatedAt = updatedTimestamp,
            isSynced = true
        )

        poiRepository.updatePoiFromUI(updatedPoi)

        val resultEntity = fakePoiDao.entityMap[updatedPoi.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated name", resultEntity.name)
            assertEquals("Updated type", resultEntity.type)
            assertFalse(resultEntity.isSynced)
            assertEquals(updatedTimestamp, resultEntity.updatedAt)
        }

        val resultUpdated = poiRepository
            .getPoiById(updatedPoi.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated name", resultUpdated.name)
            assertEquals("Updated type", resultUpdated.type)
            assertFalse(resultUpdated.isSynced)
            assertEquals(updatedTimestamp, resultUpdated.updatedAt)
        }
    }

    @Test
    fun updatePoiFromFirebase_shouldUpdatePoiAndForceSyncTrue() = runTest {
        val firestoreId = "firestore-poi-1"
        val updatedTimestamp = 1900000000000L

        val updatedPoiFromFirebase =
            poiOnlineEntity1.copy(
                name = "Updated from Firebase",
                type = "Updated from Firebase",
                updatedAt = updatedTimestamp
            )

        poiRepository.updatePoiFromFirebase(
            poi = updatedPoiFromFirebase,
            firebaseDocumentId = firestoreId
        )

        val resultEntity = fakePoiDao.entityMap[updatedPoiFromFirebase.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated from Firebase", resultEntity.name)
            assertEquals("Updated from Firebase", resultEntity.type)
            assertTrue(resultEntity.isSynced)
            assertEquals(updatedTimestamp, resultEntity.updatedAt)
        }

        val resultUpdated = poiRepository
            .getPoiById(poiModel1.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated from Firebase", resultUpdated.name)
            assertEquals("Updated from Firebase", resultUpdated.type)
            assertTrue(resultUpdated.isSynced)
            assertEquals(updatedTimestamp, resultUpdated.updatedAt)
        }
    }

    @Test
    fun updateAllPoiSFromFirebase_shouldUpdateAllPoiS() = runTest {
        val updatedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-poi-1",
            "firestore-poi-2",
            "firestore-poi-3"
        )
        val updatedPoiSFromFirebase = listOf(
            poiOnlineEntity1.copy(
                name = "Updated from Firebase 1",
                type = "Updated from Firebase 1",
                updatedAt = updatedTimestamp + 1
            ),
            poiOnlineEntity2.copy(
                name = "Updated from Firebase 2",
                address = "Updated from Firebase 2",
                updatedAt = updatedTimestamp + 2
            ),
            poiOnlineEntity3.copy(
                name = "Updated from Firebase 3",
                type = "Updated from Firebase 3",
                updatedAt = updatedTimestamp + 3
            )
        )

        val pairs = updatedPoiSFromFirebase.mapIndexed { index, poi ->
            poi to firestoreIds[index]
        }

        poiRepository.updateAllPoiSFromFirebase(pairs)

        updatedPoiSFromFirebase.forEachIndexed { index, expected ->
            val resultEntity = fakePoiDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.universalLocalId, resultEntity.id)
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertEquals(expected.name, resultEntity.name)
                assertEquals(expected.type, resultEntity.type)
                assertEquals(expected.address, resultEntity.address)
                assertTrue(resultEntity.isSynced)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
            }
        }

        val allPoiS = poiRepository.getAllPoiIncludeDeleted().first()

        updatedPoiSFromFirebase.forEach { expected ->
            val resultUpdated = allPoiS.find {
                it.id == expected.universalLocalId
            }

            assertNotNull(resultUpdated)

            resultUpdated!!.apply {
                assertEquals(expected.universalLocalId, resultUpdated.id)
                assertEquals(expected.name, resultUpdated.name)
                assertEquals(expected.type, resultUpdated.type)
                assertEquals(expected.address, resultUpdated.address)
                assertTrue(resultUpdated.isSynced)
                assertEquals(expected.updatedAt, resultUpdated.updatedAt)
            }
        }
    }

    @Test
    fun markPoiAsDelete_shouldHidePoiFromQueries() = runTest {
        poiRepository.markPoiAsDeleted(poiModel2)

        val rawEntity = fakePoiDao.entityMap[poiModel2.universalLocalId]
        assertNotNull(rawEntity)
        rawEntity!!.apply {
            assertTrue(rawEntity.isDeleted)
            assertFalse(rawEntity.isSynced)
        }

        val result = poiRepository.getAllPoiS().first()
        assertFalse(result.contains(poiModel2))
    }

    @Test
    fun deletePoi_shouldDeletePoi() =  runTest {
        val existsBefore = fakePoiDao.entityMap.containsKey(poiEntity3.id)
        assertTrue(existsBefore)

        poiRepository.deletePoi(poiEntity3)

        val resultEntity = fakePoiDao.entityMap.containsKey(poiEntity3.id)
        assertFalse(resultEntity)

        val resultDeleted = poiRepository.getPoiByIdIncludeDeleted(poiEntity3.id).first()
        assertNull(resultDeleted)
    }

    @Test
    fun clearAllPoiSDeleted_shouldDeleteOnlyDeletedPoiS() = runTest {
        poiRepository.markPoiAsDeleted(poiModel1)

        assertTrue(fakePoiDao.entityMap[poiModel1.universalLocalId]!!.isDeleted)
        assertTrue(fakePoiDao.entityMap[poiModel3.universalLocalId]!!.isDeleted)
        assertFalse(fakePoiDao.entityMap[poiModel2.universalLocalId]!!.isDeleted)

        poiRepository.clearAllPoiSDeleted()

        assertFalse(fakePoiDao.entityMap.containsKey(poiModel1.universalLocalId))
        assertFalse(fakePoiDao.entityMap.containsKey(poiModel3.universalLocalId))
        assertTrue(fakePoiDao.entityMap.containsKey(poiModel2.universalLocalId))

        val allPoiS = poiRepository.getAllPoiIncludeDeleted().first()

        assertFalse(allPoiS.any { it.id == poiModel1.universalLocalId })
        assertFalse(allPoiS.any { it.id == poiModel3.universalLocalId })
        assertTrue(allPoiS.any { it.id == poiModel2.universalLocalId })
    }

    @Test
    fun getPoiByIdIncludeDeleted_returnsDeletedPoi() = runTest {
        val result = poiRepository.getPoiByIdIncludeDeleted(poiEntity3.id).first()

        assertNotNull(result)

        result!!.apply {
            assertEquals(poiEntity3.id, result.id)
            assertTrue(result.isDeleted)
        }
    }

    @Test
    fun getAllPoiIncludeDeleted_returnsAllIncludingDeleted() = runTest {
        val result = poiRepository.getAllPoiIncludeDeleted().first()

        assertEquals(poiEntityList.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }

    @Test
    fun getPoiWithProperties_shouldReturnPoiWithLinkedProperties() = runTest {
        val result = poiRepository
            .getPoiWithProperties(poiEntity1.id)
            .first()

        assertEquals(poiEntity1.id, result.poi.universalLocalId)
        assertEquals(poiEntity1.name, result.poi.name)

        val expectedPropertyIds = allCrossRefs
            .filter {
                it.universalLocalPoiId == poiEntity1.id && !it.isDeleted
            }
            .map { it.universalLocalPropertyId }
            .toSet()

        val resultPropertyIds = result.properties
            .map { it.universalLocalId }
            .toSet()

        assertEquals(expectedPropertyIds, resultPropertyIds)
    }









}
