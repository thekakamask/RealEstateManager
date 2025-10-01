
package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
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
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PropertyPoiCrossRepositoryTest {

    private lateinit var fakePropertyPoiCrossDao: FakePropertyPoiCrossDao
    private lateinit var crossRepository: PropertyPoiCrossRepository

    private val crossRefEntity1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val crossRefEntity2 = FakePropertyPoiCrossEntity.propertyPoiCross2
    private val crossRefEntity3 = FakePropertyPoiCrossEntity.propertyPoiCross3
    private val crossRefEntity4 = FakePropertyPoiCrossEntity.propertyPoiCross4
    private val crossRefEntity5 = FakePropertyPoiCrossEntity.propertyPoiCross5
    private val crossRefEntity6 = FakePropertyPoiCrossEntity.propertyPoiCross6

    private val allCrossRefsEntityNotDeleted = FakePropertyPoiCrossEntity.allCrossRefsNotDeleted
    private val allCrossEntityRefs = FakePropertyPoiCrossEntity.allCrossRefs

    private val crossRefModel1 = FakePropertyPoiCrossModel.cross1
    private val crossRefModel2 = FakePropertyPoiCrossModel.cross2
    private val crossRefModel3 = FakePropertyPoiCrossModel.cross3
    private val crossRefModel4 = FakePropertyPoiCrossModel.cross4
    private val crossRefModel5 = FakePropertyPoiCrossModel.cross5
    private val crossRefModel6 = FakePropertyPoiCrossModel.cross6

    private val allCrossRefsModels = FakePropertyPoiCrossModel.allCrossRefs
    private val allCrossRefsModelsNotDeleted = FakePropertyPoiCrossModel.allCrossRefsNotDeleted

    @Before
    fun setup(){

        fakePropertyPoiCrossDao = FakePropertyPoiCrossDao()
        crossRepository = OfflinePropertyPoiCrossRepository(fakePropertyPoiCrossDao)

    }

    @Test
    fun getCrossRefsForProperty_returnsCorrectSubset() = runTest {
        val propertyId = crossRefEntity1.propertyId
        val expected = allCrossRefsModelsNotDeleted.filter { it.propertyId == propertyId }

        val result = crossRepository.getCrossRefsForProperty(propertyId).first()

        assertEquals(expected.size, result.size)
        expected.forEach { expectedItem ->
            val actual = result.find {
                it.propertyId == expectedItem.propertyId &&
                        it.poiId == expectedItem.poiId
            }
            assertNotNull(actual)
            assertEquals(expectedItem.propertyId, actual?.propertyId)
            assertEquals(expectedItem.poiId, actual?.poiId)
        }
    }

    @Test
    fun getAllCrossRefs_returnsAllCrossRefsNotDeleted() = runTest {
        val result = crossRepository.getAllCrossRefs().first()
        val expected = allCrossRefsModelsNotDeleted

        assertEquals(expected.sortedBy { it.updatedAt }, result.sortedBy { it.updatedAt })

        assertTrue(result.containsAll(expected))
        assertEquals(expected.size, result.size)
    }

    @Test
    fun getPoiIdsForProperty_returnsCorrectPoiIds() = runTest {
        val propertyId = crossRefModel2.propertyId
        val expected = allCrossRefsModelsNotDeleted.filter { it.propertyId == propertyId }.map { it.poiId }

        val result = crossRepository.getPoiIdsForProperty(propertyId).first()
        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun getPropertyIdsForPoi_returnsCorrectPropertyIds() = runTest {
        val poiId = crossRefModel2.poiId
        val expected = allCrossRefsModelsNotDeleted.filter { it.poiId == poiId }.map { it.propertyId }

        val result = crossRepository.getPropertyIdsForPoi(poiId).first()
        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun getCrossByIds_returnsCorrectCrossRef() = runTest {
        val result = crossRepository.getCrossByIds(crossRefModel1.propertyId, crossRefModel1.poiId).first()
        assertEquals(crossRefModel1, result)
    }


    @Test
    fun insertCrossRef_insertsNewCross() = runTest {
        val newCross = PropertyPoiCross(123L, 456L)
        crossRepository.insertCrossRef(newCross)

        // --- Verify DAO state (Entity level) ---
        val resultEntity = fakePropertyPoiCrossDao.entityMap[Pair(newCross.propertyId, newCross.poiId)]

        assertEquals(newCross.propertyId, resultEntity?.propertyId)
        assertEquals(newCross.poiId, resultEntity?.poiId)

        // --- Verify Repository result (Model level) ---
        val resultInserted = crossRepository.getCrossByIds(newCross.propertyId, newCross.poiId).first()

        Assert.assertNotNull(resultInserted)
        Assert.assertEquals(newCross.propertyId, resultInserted?.propertyId)
        Assert.assertEquals(newCross.poiId, resultInserted?.poiId)
    }

    @Test
    fun insertAllCrossRefs_insertsMultiple() = runTest {
        val newCrosses = listOf(
            PropertyPoiCross(111L, 222L),
            PropertyPoiCross(111L, 333L),
            PropertyPoiCross(444L, 555L)
        )

        crossRepository.insertAllCrossRefs(newCrosses)

        newCrosses.forEach { expected ->
            val entity = fakePropertyPoiCrossDao.entityMap[Pair(expected.propertyId, expected.poiId)]
            assertNotNull(entity)
            assertEquals(expected.propertyId, entity?.propertyId)
            assertEquals(expected.poiId, entity?.poiId)
        }

        val allCrosses = crossRepository.getAllCrossRefs().first()
        newCrosses.forEach { expected ->
            val actual = allCrosses.find { it.propertyId == expected.propertyId && it.poiId == expected.poiId }
            assertNotNull(actual)
            assertEquals(expected.propertyId, actual!!.propertyId)
            assertEquals(expected.poiId, actual.poiId)
        }
    }

    @Test
    fun updateCrossRef_onNonExistingCrossRef_shouldInsertIt() = runTest {
        val nonExistingCrossRef = PropertyPoiCross(
            propertyId = crossRefModel1.propertyId,
            poiId = crossRefModel1.poiId,
            isSynced = false,
            isDeleted = false,
            updatedAt = System.currentTimeMillis()
        )

        crossRepository.updateCrossRef(nonExistingCrossRef)

        val resultEntity = fakePropertyPoiCrossDao.entityMap[Pair(nonExistingCrossRef.propertyId, nonExistingCrossRef.poiId)]
        assertNotNull(resultEntity)
        assertEquals(nonExistingCrossRef.propertyId, resultEntity?.propertyId)

        val resultModel = crossRepository.getCrossByIds(nonExistingCrossRef.propertyId, nonExistingCrossRef.poiId).first()
        assertNotNull(resultModel)
        assertEquals(nonExistingCrossRef.propertyId, resultModel?.propertyId)
        assertEquals(nonExistingCrossRef.poiId, resultModel?.poiId)
    }

    @Test
    fun updateCrossRef_modifiesExistingCrossRef() = runTest {
        val oldEntity = fakePropertyPoiCrossDao.entityMap[Pair(crossRefModel2.propertyId, crossRefModel2.poiId)]
        assertNotNull(oldEntity)

        val updated = crossRefModel2.copy(
            isDeleted = true,
            updatedAt = System.currentTimeMillis()
        )

        crossRepository.updateCrossRef(updated)

        val entity = fakePropertyPoiCrossDao.entityMap[Pair(updated.propertyId, updated.poiId)]

        assertNotNull(entity)
        assertEquals(updated.propertyId, entity?.propertyId)
        assertEquals(updated.poiId, entity?.poiId)

        assertTrue(entity!!.updatedAt >= updated.updatedAt)

        assertFalse(entity.isSynced)
    }

    @Test
    fun markCrossRefAsDeleted_flagsAsDeleted() = runTest {
        crossRepository.markCrossRefAsDeleted(crossRefModel3.propertyId, crossRefModel3.poiId)

        val rawEntity = fakePropertyPoiCrossDao.entityMap[Pair(crossRefModel3.propertyId, crossRefModel3.poiId)]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        val result1 = crossRepository.getAllCrossRefs().first()
        assertFalse(result1.contains(crossRefModel3))

        val result2 = crossRepository
            .getCrossRefsByIdsIncludedDeleted(
                crossRefModel3.propertyId,
                crossRefModel3.poiId
            )
            .first()

        assertNotNull(result2)
        assertTrue(result2!!.isDeleted)
    }

    @Test
    fun markCrossRefAsDeletedForProperty_flagsAsDeleted() = runTest {
        val beforeDeletion = crossRepository.getCrossRefsForProperty(crossRefModel4.propertyId).first()
        assertTrue(beforeDeletion.isNotEmpty())

        crossRepository.markCrossRefsAsDeletedForProperty(crossRefModel4.propertyId)

        val remaining = fakePropertyPoiCrossDao.entityMap.values.filter { it.propertyId == crossRefModel4.propertyId }
        assertTrue(remaining.isNotEmpty())
        assertTrue(remaining.all { it.isDeleted })

        val result = crossRepository.getCrossRefsForProperty(crossRefModel4.propertyId).first()
        assertTrue(result.isEmpty())

        val result2 = crossRepository.getCrossRefsByPropertyIdIncludeDeleted(crossRefModel4.propertyId).first()
        assertTrue(result2.any { it.isDeleted })
    }

    @Test
    fun markCrossRefAsDeletedForPoi_flagsAsDeleted() = runTest {
       val beforeDeletion = crossRepository.getPropertyIdsForPoi(crossRefModel2.poiId).first()
        assertTrue(beforeDeletion.isNotEmpty())

        crossRepository.markCrossRefsAsDeletedForPoi(crossRefModel2.poiId)

        val remaining = fakePropertyPoiCrossDao.entityMap.values.filter { it.poiId == crossRefModel2.poiId }
        assertTrue(remaining.isNotEmpty())
        assertTrue(remaining.all { it.isDeleted })

        val result = crossRepository.getPropertyIdsForPoi(crossRefModel2.poiId).first()
        assertTrue(result.isEmpty())

        val result2 = crossRepository.getCrossRefsByPoiIdIncludeDeleted(crossRefModel2.poiId).first()
        assertTrue(result2.any { it.isDeleted })
    }

    @Test
    fun markAllCrossRefsAsDeleted_flagsAllAsDeleted() = runTest {
       crossRepository.markAllCrossRefsAsDeleted()

        fakePropertyPoiCrossDao.entityMap.values.forEach {
            assertTrue(it.isDeleted)
        }

        val result = crossRepository.getAllCrossRefs().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getCrossEntityByIds_returnsCorrectEntity() = runTest {
        val expected = crossRefEntity1

        val result = crossRepository.getCrossEntityByIds(expected.propertyId, expected.poiId).first()

        assertNotNull(result)
        assertEquals(expected.propertyId, result?.propertyId)
        assertEquals(expected.poiId, result?.poiId)
        assertEquals(expected.isDeleted, result?.isDeleted)
    }

    @Test
    fun deleteCrossRefsForProperty_removesCorrectCrossRefs() = runTest {
        val beforeDelete = crossRepository
            .getCrossRefsForProperty(crossRefModel5.propertyId)
            .first()
        assertNotNull(beforeDelete.isNotEmpty())

        crossRepository.deleteCrossRefsForProperty(crossRefModel5.propertyId)

        val afterDelete = crossRepository
            .getCrossRefsByPropertyIdIncludeDeleted(crossRefEntity5.propertyId)
            .first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun deleteCrossRefsForPoi_removesCorrectCrossRefs() = runTest {
        val beforeDelete = crossRepository
            .getPropertyIdsForPoi(crossRefModel6.poiId)
            .first()
        assertTrue(beforeDelete.isNotEmpty())

        crossRepository.deleteCrossRefsForPoi(crossRefModel6.poiId)

        val afterDelete = crossRepository
            .getCrossRefsByPoiIdIncludeDeleted(crossRefEntity6.poiId)
            .first()
        assertTrue(afterDelete.isEmpty())

    }

    @Test
    fun deleteCrossRef_removesCorrectCrossRef() = runTest {
        val beforeDelete = crossRepository
            .getCrossRefsByIdsIncludedDeleted(crossRefModel5.propertyId, crossRefModel5.poiId)
            .first()
        assertNotNull(beforeDelete)

        crossRepository.deleteCrossRef(crossRefEntity5)

        val afterDelete = crossRepository
            .getCrossRefsByIdsIncludedDeleted(crossRefEntity5.propertyId, crossRefEntity5.poiId)
            .first()
        assertNull(afterDelete)

    }

    @Test
    fun clearAllDeleted_removesOnlyDeletedOnes() = runTest {
        // --- Arrange ---
        val allCrossRefsBefore = crossRepository.getAllCrossRefsIncludeDeleted().first()

        val deletedCrossRefs = allCrossRefsBefore.filter { it.isDeleted }
        val notDeletedCrossRefs = allCrossRefsBefore.filterNot { it.isDeleted }

        deletedCrossRefs.forEach {
            assertTrue(it.isDeleted)
        }

        // --- Act ---
        crossRepository.clearAllDeleted()

        // --- Assert ---
        val afterClear = crossRepository.getAllCrossRefsIncludeDeleted().first()

        deletedCrossRefs.forEach { deleted ->
            val result = afterClear.find { it.propertyId == deleted.propertyId && it.poiId == deleted.poiId }
            assertNull(result)
        }

        notDeletedCrossRefs.forEach { notDeleted ->
            val result = afterClear.find { it.propertyId == notDeleted.propertyId && it.poiId == notDeleted.poiId }
            assertNotNull(result)
            assertFalse(result!!.isDeleted)
        }
    }

    @Test
    fun getUnSyncedPropertiesPoiSCross_returnsOnlyUnsynced() = runTest {
        val expected = allCrossEntityRefs.filter { !it.isSynced }
        val synced = allCrossEntityRefs.filter { it.isSynced }

        val result = crossRepository.uploadUnSyncedPropertiesPoiSCross().first()

        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun downloadCrossRefFromFirebase_savesCorrectly() = runTest {
        val existingPropertyId = crossRefModel1.propertyId
        val existingPoiId = crossRefModel1.poiId

        val firebaseCrossRef = PropertyPoiCrossOnlineEntity(
            propertyId = existingPropertyId,
            poiId = existingPoiId,
            roomId= 123L,
            updatedAt = System.currentTimeMillis()
        )

        crossRepository.downloadCrossRefFromFirebase(firebaseCrossRef)

        val result = crossRepository.getCrossEntityByIds(firebaseCrossRef.propertyId, firebaseCrossRef.poiId).first()


        assertNotNull(result)
        assertEquals(firebaseCrossRef.propertyId, result?.propertyId)
        assertEquals(firebaseCrossRef.poiId, result?.poiId)
        assertTrue(result?.isSynced == true)

    }

    @Test
    fun downloadPhotoFromFirebase_shouldUpdateExistingCrossRef() = runTest {
        val original = crossRefEntity1
        val firebaseCrossRef = PropertyPoiCrossOnlineEntity(
            propertyId = original.propertyId,
            poiId = original.poiId,
            roomId = 123L,
            updatedAt = original.updatedAt + 10_000
        )

        crossRepository.downloadCrossRefFromFirebase(firebaseCrossRef)

        val entity = fakePropertyPoiCrossDao.entityMap[
            Pair(original.propertyId, original.poiId)
        ]
        assertNotNull(entity)
        assertEquals(firebaseCrossRef.propertyId, entity?.propertyId)
        assertEquals(firebaseCrossRef.poiId, entity?.poiId)
        assertTrue(entity?.isSynced == true)

        val result = crossRepository.getCrossEntityByIds(original.propertyId, original.poiId).first()
        assertNotNull(result)
        assertEquals(firebaseCrossRef.propertyId, result?.propertyId)
        assertEquals(firebaseCrossRef.poiId, result?.poiId)
        assertTrue(result?.isSynced == true)
    }


}
