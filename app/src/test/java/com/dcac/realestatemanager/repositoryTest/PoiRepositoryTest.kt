package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
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
    private lateinit var userRepository: OfflineUserRepository
    private lateinit var poiRepository: OfflinePoiRepository

    @Before
    fun setup() {
        // In-memory Poi DAO with properties + cross-refs in local stores
        fakePoiDao = FakePoiDao().apply {
            // 1) Seed PropertyEntity snapshot used to build relations
            seedProperties(FakePropertyEntity.propertyEntityList)

            // 2) Build links poiId -> propertyIds from the cross table
            //    Cross = (propertyId, poiId)
            val mapByPoi = FakePropertyPoiCrossEntity.propertyPoiCrossEntityList
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
        val expected = FakePoiModel.poiModelList
        assertEquals(expected, result)
    }

    @Test
    fun insertPoi_insertsAndIsRetrievable() = runTest {
        val newPoi = Poi(id = 999L, name = "Test Market", type = "Supermarché")

        poiRepository.insertPoi(newPoi)

        // DAO-level check
        val entity = fakePoiDao.entityMap[newPoi.id]
        assertNotNull(entity)
        assertEquals(newPoi.name, entity?.name)

        // Model-level check
        val all = poiRepository.getAllPoiS().first()
        assertTrue(all.any { it.id == 999L && it.name == "Test Market" })
    }

    @Test
    fun insertAllPoiS_insertsBatch() = runTest {
        val batch = listOf(
            Poi(id = 1001L, name = "Batch A", type = "École"),
            Poi(id = 1002L, name = "Batch B", type = "Pharmacie")
        )

        poiRepository.insertAllPoiS(batch)

        assertNotNull(fakePoiDao.entityMap[1001L])
        assertNotNull(fakePoiDao.entityMap[1002L])

        val names = poiRepository.getAllPoiS().first().map { it.name }
        assertTrue("Batch A should be present", "Batch A" in names)
        assertTrue("Batch B should be present", "Batch B" in names)
    }

    @Test
    fun updatePoi_shouldReflectChange() = runTest {
        val existing = FakePoiModel.poi1
        val updated = existing.copy(name = "Updated POI Name")

        // Act
        poiRepository.updatePoi(updated)

        // Assert DAO level
        val entity = fakePoiDao.entityMap[updated.id]
        assertEquals("Updated POI Name", entity?.name)

        // Assert repository level
        val result = poiRepository.getAllPoiS().first()
        assertTrue(result.any { it.id == updated.id && it.name == "Updated POI Name" })
    }

    @Test
    fun deletePoi_removesPoi_andUnlinks() = runTest {
        val toDelete = FakePoiModel.poi3
        assertNotNull(fakePoiDao.entityMap[toDelete.id]) // exists before

        poiRepository.deletePoi(toDelete)

        // DAO-level: entity removed
        assertNull(fakePoiDao.entityMap[toDelete.id])

        // Links cleaned for this POI
        assertTrue(fakePoiDao.poiToProperty.value[toDelete.id].isNullOrEmpty())

        // Model-level: no longer listed
        val all = poiRepository.getAllPoiS().first()
        assertFalse(all.any { it.id == toDelete.id })
    }

    @Test
    fun getPoiWithProperties_poi1_hasOnlyProperty1() = runTest {
        val poiId = FakePoiEntity.poi1.id

        val result = poiRepository.getPoiWithProperties(poiId).first()

        // poi1 is linked only to property1 in the provided cross list
        assertEquals(poiId, result.poi.id)
        val expectedIds = setOf(FakePropertyEntity.property1.id)
        val actualIds = result.properties.map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)

        // users resolved
        result.properties.forEach { p ->
            assertNotNull(p.user)
            val entity = FakePropertyEntity.propertyEntityList.first { it.id == p.id }
            assertEquals(entity.userId, p.user.id)
        }
    }

    @Test
    fun getPoiWithProperties_poi16_hasProperty4_and_5() = runTest {
        val poiId = FakePoiEntity.poi16.id

        val result = poiRepository.getPoiWithProperties(poiId).first()

        // poi16 is linked to property4 and property5 in your cross list
        val expectedIds = setOf(FakePropertyEntity.property4.id, FakePropertyEntity.property5.id)
        val actualIds = result.properties.map { it.id }.toSet()

        assertEquals(poiId, result.poi.id)
        assertEquals(expectedIds, actualIds)

        // users resolved
        result.properties.forEach { p ->
            assertNotNull(p.user)
            val entity = FakePropertyEntity.propertyEntityList.first { it.id == p.id }
            assertEquals(entity.userId, p.user.id)
        }
    }

    @Test
    fun getPoiWithProperties_whenUnlinked_returnsEmpty() = runTest {
        // Pick a POI and unlink it on purpose
        val poiId = FakePoiEntity.poi2.id
        fakePoiDao.unlinkAllForPoi(poiId)

        val result = poiRepository.getPoiWithProperties(poiId).first()

        assertEquals(poiId, result.poi.id)
        assertTrue("Properties should be empty after unlink", result.properties.isEmpty())
    }

}