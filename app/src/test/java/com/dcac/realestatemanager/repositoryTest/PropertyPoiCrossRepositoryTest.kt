package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyPoiCrossDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyPoiCrossModel
import com.dcac.realestatemanager.model.PropertyPoiCross
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PropertyPoiCrossRepositoryTest {

    private lateinit var fakePropertyPoiCrossDao: FakePropertyPoiCrossDao
    private lateinit var propertyPoiCrossRepository: PropertyPoiCrossRepository

    @Before
    fun setup(){
        fakePropertyPoiCrossDao = FakePropertyPoiCrossDao()
        propertyPoiCrossRepository = OfflinePropertyPoiCrossRepository(fakePropertyPoiCrossDao)

    }

    @Test
    fun getAllCrossRefs_returnsAll() = runTest {
        val result = propertyPoiCrossRepository.getAllCrossRefs().first()
        val expected = FakePropertyPoiCrossModel.propertyPoiCrossModelList

        assertEquals(
            expected.sortedWith(compareBy({ it.propertyId }, { it.poiId })),
            result.sortedWith(compareBy({ it.propertyId }, { it.poiId }))
        )
    }

    @Test
    fun insertCrossRef_insertsAndIsRetrievable() = runTest {
        val newCross = PropertyPoiCross(propertyId = 99L, poiId = 199L)

        propertyPoiCrossRepository.insertCrossRef(newCross)

        // DAO-level
        val entity = fakePropertyPoiCrossDao.entityMap[Pair(99L, 199L)]
        assertNotNull(entity)

        // Repo-level
        val all = propertyPoiCrossRepository.getAllCrossRefs().first()
        assertTrue(all.contains(newCross))
    }

    @Test
    fun insertAllCrossRefs_insertsBatch() = runTest {
        val batch = listOf(
            PropertyPoiCross(101L, 201L),
            PropertyPoiCross(102L, 202L)
        )

        propertyPoiCrossRepository.insertAllCrossRefs(batch)

        // DAO-level
        assertNotNull(fakePropertyPoiCrossDao.entityMap[Pair(101L, 201L)])
        assertNotNull(fakePropertyPoiCrossDao.entityMap[Pair(102L, 202L)])

        // Repo-level
        val all = propertyPoiCrossRepository.getAllCrossRefs().first()
        assertTrue(all.containsAll(batch))
    }

    @Test
    fun updateCrossRef_updatesEntityCorrectly() = runTest {
        // Given
        val original = FakePropertyPoiCrossModel.cross1.copy(isSynced = false)
        propertyPoiCrossRepository.insertCrossRef(original)

        // When
        val updated = original.copy(isSynced = true)
        propertyPoiCrossRepository.updateCrossRef(updated)

        // Then (DAO-level)
        val stored = fakePropertyPoiCrossDao.entityMap[Pair(updated.propertyId, updated.poiId)]
        assertNotNull(stored)
        assertTrue(stored!!.isSynced)

        // Then (Repo-level)
        val fetched = propertyPoiCrossRepository.getAllCrossRefs().first()
            .find { it.propertyId == updated.propertyId && it.poiId == updated.poiId }

        assertNotNull(fetched)
        assertTrue(fetched!!.isSynced)
    }

    @Test
    fun deleteCrossRefsForProperty_removesAll() = runTest {
        val propertyId = FakePropertyPoiCrossEntity.propertyPoiCross1.propertyId

        propertyPoiCrossRepository.deleteCrossRefsForProperty(propertyId)

        // DAO-level: no cross with this propertyId
        assertFalse(fakePropertyPoiCrossDao.entityMap.values.any { it.propertyId == propertyId })

        // Repo-level
        val result = propertyPoiCrossRepository.getCrossRefsForProperty(propertyId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteCrossRefsForPoi_removesAll() = runTest {
        val poiId = FakePropertyPoiCrossEntity.propertyPoiCross1.poiId

        propertyPoiCrossRepository.deleteCrossRefsForPoi(poiId)

        // DAO-level: no cross with this poiId
        assertFalse(fakePropertyPoiCrossDao.entityMap.values.any { it.poiId == poiId })

        // Repo-level
        val result = propertyPoiCrossRepository.getPropertyIdsForPoi(poiId).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun clearAllCrossRefs_removesEverything() = runTest {
        propertyPoiCrossRepository.clearAllCrossRefs()

        // DAO-level
        assertTrue(fakePropertyPoiCrossDao.entityMap.isEmpty())

        // Repo-level
        val all = propertyPoiCrossRepository.getAllCrossRefs().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getPoiIdsForProperty_returnsCorrectIds() = runTest {
        val propertyId = FakePropertyPoiCrossEntity.propertyPoiCross1.propertyId
        val result = propertyPoiCrossRepository.getPoiIdsForProperty(propertyId).first()

        val expected = FakePropertyPoiCrossModel.propertyPoiCrossModelList
            .filter { it.propertyId == propertyId }
            .map { it.poiId }

        assertEquals(expected, result)
    }

    @Test
    fun getPropertyIdsForPoi_returnsCorrectIds() = runTest {
        val poiId = FakePropertyPoiCrossEntity.propertyPoiCross1.poiId
        val result = propertyPoiCrossRepository.getPropertyIdsForPoi(poiId).first()

        val expected = FakePropertyPoiCrossModel.propertyPoiCrossModelList
            .filter { it.poiId == poiId }
            .map { it.propertyId }

        assertEquals(expected, result)
    }

    @Test
    fun getUnSyncedPropertiesPoiSCross_returnsOnlyUnSynced() = runTest {
        // Expected cross-refs: those not synced
        val expected = FakePropertyPoiCrossModel.propertyPoiCrossModelList.filter { !it.isSynced }
        val synced = FakePropertyPoiCrossModel.propertyPoiCrossModelList.filter { it.isSynced }

        // When
        val result = propertyPoiCrossRepository.getUnSyncedPropertiesPoiSCross().first()

        // Then
        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun cacheCrossRefFromFirebase_insertsSyncedCrossRef() = runTest {
        // Given
        val syncedCrossRef = PropertyPoiCross(propertyId = 888L, poiId = 999L, isSynced = true)

        // When
        propertyPoiCrossRepository.cacheCrossRefFromFirebase(syncedCrossRef)

        // Then (DAO-level)
        val entity = fakePropertyPoiCrossDao.entityMap[Pair(888L, 999L)]
        assertNotNull(entity)
        assertTrue(entity!!.isSynced)

        // Then (Repo-level)
        val result = propertyPoiCrossRepository.getAllCrossRefs().first()
        assertTrue(result.any { it.propertyId == 888L && it.poiId == 999L && it.isSynced })
    }

    @Test
    fun getCrossByIds_returnsCorrectCrossRef() = runTest {
        // Given: one known cross-ref
        val cross = PropertyPoiCross(propertyId = 777L, poiId = 222L)
        propertyPoiCrossRepository.insertCrossRef(cross)

        // When
        val result = propertyPoiCrossRepository.getCrossByIds(777L, 222L).first()

        // Then
        assertNotNull(result)
        assertEquals(cross.propertyId, result?.propertyId)
        assertEquals(cross.poiId, result?.poiId)
    }



}