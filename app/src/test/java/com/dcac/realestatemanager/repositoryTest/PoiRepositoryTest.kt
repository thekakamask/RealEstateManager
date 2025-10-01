
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
    private val poiEntity2 = FakePoiEntity.poi2
    private val poiEntity3 = FakePoiEntity.poi3
    private val allPoiEntities = FakePoiEntity.poiEntityList
    private val allPoiEntitiesNotDeleted = FakePoiEntity.poiEntityListNotDeleted

    private val poiModel1 = FakePoiModel.poi1
    private val poiModel2 = FakePoiModel.poi2
    private val poiModel3 = FakePoiModel.poi3
    private val allPoiModels = FakePoiModel.poiModelList
    private val allPoiModelsNotDeleted = FakePoiModel.poiModelListNotDeleted
    private val propertyEntityList = FakePropertyEntity.propertyEntityList
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs

    @Before
    fun setup() {
        // In-memory Poi DAO with properties + cross-refs in local stores
        fakePoiDao = FakePoiDao().apply {
            // 1) Seed PropertyEntity snapshot used to build relations
            seedProperties(propertyEntityList)

            // 2) Build links poiId -> propertyIds from the cross table
            //    Cross = (propertyId, poiId)
            val mapByPoi = allCrossRefs
                .groupBy { it.poiId } // group by poiId
                .mapValues { (_, list) -> list.map { it.propertyId } } // -> List<propertyId>

            mapByPoi.forEach { (poiId, propertyIds) ->
                linkPoiToProperties(poiId, *propertyIds.toLongArray())
            }
        }

        // Real repository for users backed by a FakeUserDao (seeded in its init)
        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)

        // Repository under test
        poiRepository = OfflinePoiRepository(fakePoiDao, userRepository)
    }

    @Test
    fun getAllPoiS_returnsAll() = runTest {
        val result = poiRepository.getAllPoiS().first()
        assertEquals(allPoiModelsNotDeleted, result)
    }

    @Test
    fun getPoiById_returnsCorrectPoi() = runTest {
        val result = poiRepository.getPoiById(poiModel1.id).first()

        assertNotNull(result)
        assertEquals(poiModel1, result)
    }

    @Test
    fun insertPoi_insertsPoi() = runTest {
        val newPoiModel = Poi(
            id = 999L,
            name = "Test Market",
            type = "Supermarché",
            isDeleted = false,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        poiRepository.insertPoi(newPoiModel)

        // --- Verify DAO state (Entity level) ---
        val resultEntity = fakePoiDao.entityMap[newPoiModel.id]

        assertEquals(newPoiModel.id, resultEntity?.id)
        assertEquals(newPoiModel.name, resultEntity?.name)
        assertEquals(newPoiModel.type, resultEntity?.type)

        // --- Verify Repository result (Model level) ---
        val resultInserted = poiRepository.getPoiById(newPoiModel.id).first()

        assertNotNull(resultInserted)
        assertEquals(newPoiModel.id, resultInserted?.id)
        assertEquals(newPoiModel.name, resultInserted?.name)
        assertEquals(newPoiModel.type, resultInserted?.type)
    }

    @Test
    fun insertAllPoiS_insertsNewPoiS() = runTest {
        val newPoiS = listOf(
            Poi(
                id = 6001L,
                name = "New School",
                type = "École",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            Poi(
                id = 6002L,
                name = "New Pharmacy",
                type = "Pharmacie",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            ),
            Poi(
                id = 6003L,
                name = "New Market",
                type = "Supermarché",
                isDeleted = false,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
        )

        // --- Act ---
        poiRepository.insertAllPoiS(newPoiS)

        // --- Verify DAO state (Entity level) ---
        newPoiS.forEach { expected ->
            val entity = fakePoiDao.entityMap[expected.id]
            assertEquals(expected.id, entity?.id)
            assertEquals(expected.name, entity?.name)
            assertEquals(expected.type, entity?.type)
        }

        // --- Verify Repository state (Model level) ---
        val allPois = poiRepository.getAllPoiS().first()
        newPoiS.forEach { expected ->
            val actual = allPois.find { it.id == expected.id }
            assertNotNull(actual)
            assertEquals(expected.id, actual!!.id)
            assertEquals(expected.name, actual.name)
            assertEquals(expected.type, actual.type)
        }
    }

    @Test
    fun updatePoi_shouldModifyExistingPoi() = runTest {
        // --- Arrange ---
        val updated = poiModel2.copy(
            name = "Updated POI Name",
            type = "Updated Type",
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        poiRepository.updatePoi(updated)

        // --- Assert (Repository level) ---
        val result = poiRepository.getPoiById(poiModel2.id).first()

        assertNotNull(result)
        assertEquals(updated.name, result?.name)
        assertEquals(updated.type, result?.type)
        assertFalse(result?.isSynced ?: true)
    }

    @Test
    fun updatePoi_onNonExistingPoi_shouldInsertIt() = runTest {
        // --- Arrange ---
        val nonExistingPoi = Poi(
            id = 99999L,
            name = "Ghost POI",
            type = "Undefined",
            isDeleted = false,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        poiRepository.updatePoi(nonExistingPoi)

        // --- Assert ---
        val entity = fakePoiDao.entityMap[nonExistingPoi.id]
        assertNotNull(entity)
        assertEquals(nonExistingPoi.name, entity?.name)

        // --- Repository-level
        val result = poiRepository.getPoiById(nonExistingPoi.id).first()
        assertNotNull(result)
        assertEquals(nonExistingPoi.name, result?.name)
    }



    @Test
    fun markPoiAsDeleted_shouldHidePoiFromQueries() = runTest {
        // --- Act ---
        poiRepository.markPoiAsDeleted(poiModel2)

        // --- DAO-level: still present in entityMap but flagged as deleted ---
        val rawEntity = fakePoiDao.entityMap[poiModel2.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // --- Repository-level: should no longer appear in visible results ---
        val result = poiRepository.getAllPoiS().first()
        assertFalse(result.contains(poiModel2))
    }

    @Test
    fun markPoiAsDeleted_calledTwice_staysDeleted() = runTest {
        // --- Act ---
        poiRepository.markPoiAsDeleted(poiModel1)
        poiRepository.markPoiAsDeleted(poiModel1)

        // --- DAO-level
        val rawEntity = fakePoiDao.entityMap[poiModel1.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // --- Repository-level
        val result = poiRepository.getAllPoiS().first()
        assertFalse(result.any { it.id == poiModel1.id })
    }

    @Test
    fun getPoiWithProperties_whenUnlinked_returnsEmpty() = runTest {
        val poiId = poiEntity2.id
        fakePoiDao.unlinkAllForPoi(poiId)

        val result = poiRepository.getPoiWithProperties(poiId).first()

        assertEquals(poiId, result.poi.id)
        assertTrue(result.properties.isEmpty())
    }

    @Test
    fun getPoiWithProperties_returnsLinkedProperties() = runTest {
        val poiId = poiEntity2.id

        // --- Act ---
        val result = poiRepository.getPoiWithProperties(poiId).first()

        // --- Assert ---
        assertEquals(poiId, result.poi.id)

        val expectedIds = allCrossRefs
            .filter { it.poiId == poiId }
            .map { it.propertyId }
            .toSet()
        val actualIds = result.properties.map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)

        result.properties.forEach { property ->
            assertNotNull(property.user)
            val entity = propertyEntityList.first { it.id == property.id }
            assertEquals(entity.userId, property.user.id)
        }
    }

    @Test
    fun getPoiEntityById_returnsCorrectEntity() = runTest {
        // --- Arrange ---
        val expected = poiEntity1

        // --- Act ---
        val result = poiRepository.getPoiEntityById(expected.id).first()

        // --- Assert ---
        assertNotNull(result)
        assertEquals(expected.id, result?.id)
        assertEquals(expected.name, result?.name)
        assertEquals(expected.type, result?.type)
        assertEquals(expected.isDeleted, result?.isDeleted)
        assertEquals(expected.updatedAt, result?.updatedAt)
    }


    @Test
    fun deletePoi_deletesPoi() = runTest {
        // --- Arrange ---
        val beforeDelete = poiRepository
            .getPoiByIdIncludeDeleted(poiEntity3.id)
            .first()
        assertNotNull(beforeDelete)

        // --- Act ---
        poiRepository.deletePoi(poiEntity3)

        // --- Assert ---
        val afterDelete = poiRepository
            .getPoiByIdIncludeDeleted(poiEntity3.id)
            .first()
        assertNull(afterDelete)
    }

    @Test
    fun getUnSyncedPoiS_returnsOnlyUnSynced() = runTest {
        // --- Arrange ---
        val expected = allPoiEntities.filter { !it.isSynced }
        val synced = allPoiEntities.filter { it.isSynced }

        // --- Act ---
        val result = poiRepository.uploadUnSyncedPoiSToFirebase().first()

        // --- Assert ---
        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun downloadPoiFromFirebase_savesPoiCorrectly() = runTest {
        // --- Arrange ---
        val firebasePoi = PoiOnlineEntity(
            roomId = 888L,
            name = "Synced from Firestore",
            type = "Transport",
            updatedAt = 1700000009999L
        )

        // --- Act ---
        poiRepository.downloadPoiFromFirebase(firebasePoi)

        // --- Assert ---
        val result = poiRepository.getPoiById(firebasePoi.roomId).first()
        assertNotNull(result)
        assertEquals(firebasePoi.roomId, result?.id)
        assertEquals(firebasePoi.name, result?.name)
        assertEquals(firebasePoi.type, result?.type)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun downloadPoiFromFirebase_updatesExistingPoi() = runTest {
        // --- Arrange ---
        val original = poiEntity1
        val firebasePoi = PoiOnlineEntity(
            roomId = original.id,
            name = "Updated from Firebase",
            type = "Updated Type",
            updatedAt = original.updatedAt + 1000
        )

        // --- Act ---
        poiRepository.downloadPoiFromFirebase(firebasePoi)

        // --- Assert ---
        val entity = fakePoiDao.entityMap[original.id]
        assertNotNull(entity)
        assertEquals(firebasePoi.name, entity?.name)
        assertEquals(firebasePoi.type, entity?.type)
        assertTrue(entity?.isSynced == true)

        // --- Repository-level
        val result = poiRepository.getPoiById(original.id).first()
        assertNotNull(result)
        assertEquals(firebasePoi.name, result?.name)
        assertEquals(firebasePoi.type, result?.type)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun getAllPoiIncludeDeleted_returnsAllIncludingDeleted() = runTest {
        val result = poiRepository.getAllPoiIncludeDeleted().first()

        // all 3 POIs are in FakePoiEntity.poiEntityList (poi3 isDeleted = true)
        assertEquals(allPoiEntities.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }

    @Test
    fun getPoiByIdIncludeDeleted_returnsDeletedPoi() = runTest {
        // --- Arrange ---
        val deletedPoi = poiEntity3
        assertTrue(deletedPoi.isDeleted)

        // --- Act ---
        val result = poiRepository.getPoiByIdIncludeDeleted(deletedPoi.id).first()

        // --- Assert ---
        assertNotNull(result)
        assertEquals(deletedPoi.id, result?.id)
        assertTrue(result?.isDeleted == true)
    }




}
