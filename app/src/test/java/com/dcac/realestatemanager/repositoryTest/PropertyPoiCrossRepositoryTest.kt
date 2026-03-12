
package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyPoiCrossDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyPoiCrossModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyPoiCrossOnlineEntity
import com.dcac.realestatemanager.model.PropertyPoiCross
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    private val allCrossRefEntities = FakePropertyPoiCrossEntity.allCrossRefs
    private val allCrossRefsEntityNotDeleted = FakePropertyPoiCrossEntity.allCrossRefsNotDeleted
    private val crossRefOnlineEntity1 = FakePropertyPoiCrossOnlineEntity.crossOnline1
    private val crossRefOnlineEntity2 = FakePropertyPoiCrossOnlineEntity.crossOnline2
    private val crossRefOnlineEntity3 = FakePropertyPoiCrossOnlineEntity.crossOnline3
    private val crossRefModel1 = FakePropertyPoiCrossModel.cross1
    private val crossRefModel2 = FakePropertyPoiCrossModel.cross2
    private val crossRefModel3 = FakePropertyPoiCrossModel.cross3
    private val crossRefModel5 = FakePropertyPoiCrossModel.cross5
    private val allCrossRefsModelsNotDeleted = FakePropertyPoiCrossModel.allCrossRefsNotDeleted

    @Before
    fun setup(){
        fakePropertyPoiCrossDao = FakePropertyPoiCrossDao()
        crossRepository = OfflinePropertyPoiCrossRepository(fakePropertyPoiCrossDao)
    }

    @Test
    fun getCrossRefsForProperty_returnsCorrectCrossRefs() = runTest {
        val result = crossRepository.getCrossRefsForProperty(
            crossRefModel1.universalLocalPropertyId)
            .first()

        val expected = allCrossRefsModelsNotDeleted
            .filter { it.universalLocalPropertyId == crossRefModel1.universalLocalPropertyId }

        assertEquals(expected, result)
    }

    @Test
    fun getPoiIdsForProperty_returnsCorrectPoiIds() = runTest {
        val result = crossRepository
            .getPoiIdsForProperty(crossRefModel1.universalLocalPropertyId)
            .first()

        val expected = allCrossRefsModelsNotDeleted
            .filter { it.universalLocalPropertyId == crossRefModel1.universalLocalPropertyId }
            .map { it.universalLocalPoiId }

        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun getPropertyIdsForPoi_returnsCorrectPropertyIds() = runTest {
        val result = crossRepository
            .getPropertyIdsForPoi(crossRefModel1.universalLocalPoiId)
            .first()

        val expected = allCrossRefsModelsNotDeleted
            .filter { it.universalLocalPoiId == crossRefModel1.universalLocalPoiId }
            .map { it.universalLocalPropertyId }

        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun getAllCrossRefs_returnsAllCrossRefsNotDeleted() = runTest {
        val result = crossRepository.getAllCrossRefs().first()
        val expected = allCrossRefsModelsNotDeleted

        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun getCrossByIds_returnsCorrectCrossRef() = runTest {
        val result = crossRepository.getCrossByIds(
            crossRefModel2.universalLocalPropertyId, crossRefModel2.universalLocalPoiId)
            .first()

        assertEquals(crossRefModel2, result)
    }

    @Test
    fun getCrossByIds_shouldReturnNull_whenCrossRefIsDeleted() = runTest {
        val result = crossRepository
            .getCrossByIds(
                crossRefModel5.universalLocalPropertyId,
                crossRefModel5.universalLocalPoiId
            )
            .first()

        assertNull(result)
    }

    @Test
    fun uploadUnSyncedCrossRefs_shouldReturnOnlyCrossRefsWithIsSyncedFalse() = runTest {
        val result = crossRepository.uploadUnSyncedCrossRefsToFirebase().first()

        val expected = allCrossRefEntities
            .filter { !it.isSynced }

        assertEquals(expected.toSet(), result.toSet())
    }

    @Test
    fun insertCrossRefInsertFromUI_shouldInsertWithIsSyncedFalse() = runTest {
        val newCrossRefModel = PropertyPoiCross(
            universalLocalPropertyId = crossRefModel3.universalLocalPropertyId,
            universalLocalPoiId = crossRefModel1.universalLocalPoiId,
            updatedAt = 1800000000000L
        )
        crossRepository.insertCrossRefInsertFromUI(newCrossRefModel)

        val key = Pair(
            newCrossRefModel.universalLocalPropertyId,
            newCrossRefModel.universalLocalPoiId
        )

        val resultEntity = fakePropertyPoiCrossDao.entityMap[key]

        assertNotNull(resultEntity)

        resultEntity!!.apply{
            assertEquals(newCrossRefModel.universalLocalPropertyId, resultEntity.universalLocalPropertyId)
            assertEquals(newCrossRefModel.universalLocalPoiId, resultEntity.universalLocalPoiId)
            assertFalse(resultEntity.isSynced)
            assertFalse(resultEntity.isDeleted)
            assertEquals(newCrossRefModel.updatedAt, updatedAt)
        }

        val resultInserted = crossRepository
            .getCrossByIds(newCrossRefModel.universalLocalPropertyId, newCrossRefModel.universalLocalPoiId)
            .first()

        assertEquals(newCrossRefModel, resultInserted)
    }

    @Test
    fun insertCrossRefInsertFromUI_shouldInsertAllWithIsSyncedFalse() = runTest {
        val insertedTimestamp = 1800000000000L
        val newCrossRefs = listOf(
            PropertyPoiCross(
                universalLocalPropertyId = crossRefModel3.universalLocalPropertyId,
                universalLocalPoiId = crossRefModel1.universalLocalPoiId,
                updatedAt = insertedTimestamp + 1
            ),
            PropertyPoiCross(
                universalLocalPropertyId = crossRefModel5.universalLocalPropertyId,
                universalLocalPoiId = crossRefModel2.universalLocalPoiId,
                updatedAt = insertedTimestamp + 2
            ),
            PropertyPoiCross(
                universalLocalPropertyId = "property-4",
                universalLocalPoiId = crossRefModel1.universalLocalPoiId,
                updatedAt = insertedTimestamp + 3
            )
        )

        crossRepository.insertAllCrossRefsInsertFromUI(newCrossRefs)

        newCrossRefs.forEach { expected ->
            val entity = fakePropertyPoiCrossDao.entityMap[
                    Pair(expected.universalLocalPropertyId,
                        expected.universalLocalPoiId)]
            assertNotNull(entity)
            entity!!.apply {
                assertEquals(expected.universalLocalPropertyId, entity.universalLocalPropertyId)
                assertEquals(expected.universalLocalPoiId, entity.universalLocalPoiId)
                assertFalse(entity.isSynced)
                assertFalse(entity.isDeleted)
                assertEquals(expected.updatedAt, entity.updatedAt)
            }
        }

        val allCrossRefs = crossRepository.getAllCrossRefs().first()

        newCrossRefs.forEach { expected ->
            val actual = allCrossRefs.find { it.universalLocalPropertyId == expected.universalLocalPropertyId
                    && it.universalLocalPoiId == expected.universalLocalPoiId }

            assertEquals(expected, actual)
        }
    }

    @Test
    fun insertCrossRefInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runTest {
        val firestoreId = "firestore-cross-7"
        val onlineCrossRef = PropertyPoiCrossOnlineEntity(
            ownerUid = "firebase_uid_1",
            universalLocalPropertyId = crossRefModel3.universalLocalPropertyId,
            universalLocalPoiId = crossRefModel1.universalLocalPoiId,
            updatedAt = 1900000000000L
        )

        crossRepository.insertCrossRefInsertFromFirebase(
            crossRef = onlineCrossRef,
            firebaseDocumentId = firestoreId
        )

        val resultEntity = fakePropertyPoiCrossDao.entityMap[
                Pair(onlineCrossRef.universalLocalPropertyId, onlineCrossRef.universalLocalPoiId)
        ]

        assertNotNull(resultEntity)
        resultEntity!!.apply {
            assertEquals(firestoreId, resultEntity.firestoreDocumentId)
            assertEquals(onlineCrossRef.universalLocalPropertyId, resultEntity.universalLocalPropertyId)
            assertEquals(onlineCrossRef.universalLocalPoiId, resultEntity.universalLocalPoiId)
            assertTrue(resultEntity.isSynced)
            assertEquals(onlineCrossRef.updatedAt, resultEntity.updatedAt)
        }


        val resultInserted = crossRepository
            .getCrossByIds(
                onlineCrossRef.universalLocalPropertyId,
                onlineCrossRef.universalLocalPoiId
            ).first()

        assertNotNull(resultInserted)
        assertEquals(firestoreId, resultInserted?.firestoreDocumentId)
        assertEquals(onlineCrossRef.universalLocalPropertyId, resultInserted?.universalLocalPropertyId)
        assertEquals(onlineCrossRef.universalLocalPoiId, resultInserted?.universalLocalPoiId)
        assertTrue(resultInserted!!.isSynced)
        assertEquals(onlineCrossRef.updatedAt, resultInserted.updatedAt)
    }

    @Test
    fun insertCrossRefsInsertFromFirebase_shouldInsertAllWithIsSyncedTrue() = runTest {
        val insertedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-cross-7",
            "firestore-cross-8",
            "firestore-cross-9"
        )

        val onlineCrossRefs = listOf(
            PropertyPoiCrossOnlineEntity(
                ownerUid = "firebase_uid_1",
                universalLocalPropertyId = crossRefModel3.universalLocalPropertyId,
                universalLocalPoiId = crossRefModel1.universalLocalPoiId,
                updatedAt = insertedTimestamp + 1
            ),
            PropertyPoiCrossOnlineEntity(
                ownerUid = "firebase_uid_2",
                universalLocalPropertyId = crossRefModel5.universalLocalPropertyId,
                universalLocalPoiId = crossRefModel2.universalLocalPoiId,
                updatedAt = insertedTimestamp + 2
            ),
            PropertyPoiCrossOnlineEntity(
                ownerUid = "firebase_uid_3",
                universalLocalPropertyId = "property-4",
                universalLocalPoiId = crossRefModel1.universalLocalPoiId,
                updatedAt = insertedTimestamp + 3
            )
        )

        val pairs = onlineCrossRefs.mapIndexed{ index, crossRef ->
            crossRef to firestoreIds[index]
        }

        crossRepository.insertAllCrossRefInsertFromFirebase(pairs)

        onlineCrossRefs.forEachIndexed { index, expected ->
            val resultEntity = fakePropertyPoiCrossDao.entityMap[
                    Pair(expected.universalLocalPropertyId, expected.universalLocalPoiId)
            ]

            assertNotNull(resultEntity)
            resultEntity!!.apply {
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertEquals(expected.universalLocalPropertyId, resultEntity.universalLocalPropertyId)
                assertEquals(expected.universalLocalPoiId, resultEntity.universalLocalPoiId)
                assertTrue(resultEntity.isSynced)
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
            }
        }

        val allCrossRefs = crossRepository.getAllCrossRefs().first()

        onlineCrossRefs.forEachIndexed { index, expected ->

            val resultInserted = allCrossRefs.find {
                it.universalLocalPropertyId == expected.universalLocalPropertyId
                        && it.universalLocalPoiId == expected.universalLocalPoiId
            }

            assertNotNull(resultInserted)
            resultInserted!!.apply {
                assertEquals(firestoreIds[index], resultInserted.firestoreDocumentId)
                assertEquals(expected.universalLocalPropertyId, resultInserted.universalLocalPropertyId)
                assertEquals(expected.universalLocalPoiId, resultInserted.universalLocalPoiId)
                assertTrue(resultInserted.isSynced)
                assertEquals(expected.updatedAt, resultInserted.updatedAt)
            }
        }
    }

    @Test
    fun updateCrossRefFromUI_shouldUpdateCrossRefAndForceSyncFalse() = runTest {
        val updatedTimestamp = 1800000000000L

        val updatedCrossRef = crossRefModel1.copy(
            updatedAt = updatedTimestamp
        )

        crossRepository.updateCrossRefFromUI(updatedCrossRef)

        val resultEntity = fakePropertyPoiCrossDao.entityMap[
            Pair(updatedCrossRef.universalLocalPropertyId, updatedCrossRef.universalLocalPoiId)
        ]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(updatedTimestamp, resultEntity.updatedAt)
            assertFalse(resultEntity.isSynced)
        }

        val resultUpdated = crossRepository
            .getCrossRefsByIdsIncludedDeleted(
                updatedCrossRef.universalLocalPropertyId,
                updatedCrossRef.universalLocalPoiId
            )
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals(updatedTimestamp, resultUpdated.updatedAt)
            assertFalse(resultUpdated.isSynced)
        }
    }

    @Test
    fun updateAllCrossRefsFromUI_shouldUpdateAllCrossRefsAndForceSyncFalse() = runTest {
        val updatedTimestamp = 1800000000000L

        val updatedCrossRefs = listOf(
            crossRefModel1.copy(updatedAt = updatedTimestamp + 1),
            crossRefModel2.copy(updatedAt = updatedTimestamp + 2 ),
            crossRefModel3.copy(updatedAt = updatedTimestamp + 3)
        )

        crossRepository.updateAllCrossRefsFromUI(updatedCrossRefs)

        updatedCrossRefs.forEach { expected ->

            val resultEntity = fakePropertyPoiCrossDao.entityMap[
                expected.universalLocalPropertyId to expected.universalLocalPoiId
            ]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
                assertFalse(resultEntity.isSynced)
            }
        }

        updatedCrossRefs.forEach { expected ->

            val resultUpdated = crossRepository
                .getCrossRefsByIdsIncludedDeleted(
                    expected.universalLocalPropertyId,
                    expected.universalLocalPoiId
                )
                .first()

            assertNotNull(resultUpdated)

            resultUpdated!!.apply {
                assertEquals(expected.updatedAt, resultUpdated.updatedAt)
                assertFalse(resultUpdated.isSynced)
            }
        }

    }

    @Test
    fun updateCrossRefFromFirebase_shouldUpdateCrossRefAndForceSyncTrue()=  runTest {
        val firestoreId = "firestore-cross-1"
        val updatedTimestamp = 1900000000000L
        val updatedCrossFromFirebase =
            crossRefOnlineEntity1.copy(
                updatedAt = updatedTimestamp
            )

        println("----- BEFORE UPDATE -----")
        println(fakePropertyPoiCrossDao.entityMap)

        crossRepository.updateCrossRefFromFirebase(
            crossRef = updatedCrossFromFirebase,
            firebaseDocumentId = firestoreId
        )

        println("----- AFTER UPDATE -----")
        println(fakePropertyPoiCrossDao.entityMap)

        val resultEntity = fakePropertyPoiCrossDao.entityMap[
            Pair(updatedCrossFromFirebase.universalLocalPropertyId,
                updatedCrossFromFirebase.universalLocalPoiId)
        ]

        println("----- ENTITY READ DIRECTLY FROM MAP -----")
        println(resultEntity)

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(firestoreId, resultEntity.firestoreDocumentId)
            assertEquals(updatedCrossFromFirebase.updatedAt, resultEntity.updatedAt)
            assertTrue(resultEntity.isSynced)
        }

        val resultUpdated = crossRepository.getCrossByIds(
            updatedCrossFromFirebase.universalLocalPropertyId,
            updatedCrossFromFirebase.universalLocalPoiId
        ).first()

        println("----- ENTITY READ FROM REPOSITORY -----")
        println(resultUpdated)

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals(firestoreId, resultUpdated.firestoreDocumentId)
            assertEquals(updatedCrossFromFirebase.updatedAt, resultUpdated.updatedAt)
            assertTrue(resultUpdated.isSynced)
        }
    }

    @Test
    fun updateCrossRefsFromFirebase_shouldUpdateAllCrossRefsAndForceSyncTrue() = runTest {
        val updatedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-cross-1",
            "firestore-cross-2",
            "firestore-cross-3"
        )
        val updatedOnlineCrossRefs = listOf(
            crossRefOnlineEntity1.copy(updatedAt = updatedTimestamp),
            crossRefOnlineEntity2.copy(updatedAt = updatedTimestamp),
            crossRefOnlineEntity3.copy(updatedAt = updatedTimestamp)
        )

        val pairs = updatedOnlineCrossRefs.mapIndexed { index, cross ->
            cross to firestoreIds[index]

        }

        crossRepository.updateAllCrossRefFromFirebase(pairs)

        updatedOnlineCrossRefs.forEachIndexed { index, expected ->

            val resultEntity = fakePropertyPoiCrossDao.entityMap[
                    Pair(expected.universalLocalPropertyId, expected.universalLocalPoiId)
            ]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.updatedAt, resultEntity.updatedAt)
                assertEquals(firestoreIds[index], resultEntity.firestoreDocumentId)
                assertTrue(resultEntity.isSynced)
            }
        }

        val allCrossRefs = crossRepository.getAllCrossRefs().first()

        updatedOnlineCrossRefs.forEachIndexed { index, expected ->
            val resultUpdated = allCrossRefs.find {
                it.universalLocalPropertyId == expected.universalLocalPropertyId
                        && it.universalLocalPoiId == expected.universalLocalPoiId
            }

            assertNotNull(resultUpdated)

            resultUpdated!!.apply {
                assertEquals(expected.updatedAt, resultUpdated.updatedAt)
                assertEquals(firestoreIds[index], resultUpdated.firestoreDocumentId)
                assertTrue(resultUpdated.isSynced)
            }
        }

    }

    @Test
    fun markCrossRefAsDelete_shouldHideCrossRefFromQueries() = runTest {
        crossRepository.markCrossRefAsDeleted(crossRefModel2.universalLocalPropertyId, crossRefModel2.universalLocalPoiId)

        val rawEntity = fakePropertyPoiCrossDao.entityMap[
                Pair(crossRefModel2.universalLocalPropertyId, crossRefModel2.universalLocalPoiId)
        ]

        assertNotNull(rawEntity)

        rawEntity!!.apply {
            assertTrue(rawEntity.isDeleted)
            assertFalse(rawEntity.isSynced)
        }

        val result = crossRepository.getAllCrossRefs().first()
        assertFalse(result.contains(crossRefModel2))
    }

    @Test
    fun markCrossRefsAsDeleteForProperty_shouldHideCrossRefsFromQueries() = runTest{
        crossRepository.markCrossRefsAsDeletedForProperty(
            crossRefModel2.universalLocalPropertyId
        )

        val rawEntities = fakePropertyPoiCrossDao.entityMap.values.filter {
            it.universalLocalPropertyId == crossRefModel2.universalLocalPropertyId
        }

        assertNotNull(rawEntities)

        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })
        }

        val result = crossRepository
            .getCrossRefsForProperty(crossRefModel2.universalLocalPropertyId)
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun markCrossRefsAsDeleteForPoi_shouldHideCrossRefsFromQueries() = runTest {

        crossRepository.markCrossRefsAsDeletedForPoi(
            crossRefModel2.universalLocalPoiId
        )

        val rawEntities = fakePropertyPoiCrossDao.entityMap.values.filter {
            it.universalLocalPoiId == crossRefModel2.universalLocalPoiId
        }

        assertNotNull(rawEntities)

        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })

        }

        val allCrossRefs = crossRepository
            .getAllCrossRefs()
            .first()

        val result = allCrossRefs.filter {
            it.universalLocalPoiId == crossRefModel2.universalLocalPoiId
        }

        assertTrue(result.isEmpty())
    }

    @Test
    fun markAllCrossRefsAsDeleted_shouldHideAllCrossRefsFromQueries() = runTest {
        crossRepository.markAllCrossRefsAsDeleted()

        val rawEntities = fakePropertyPoiCrossDao.entityMap.values

        assertNotNull(rawEntities)
        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })

        }

        val result = crossRepository
            .getAllCrossRefs()
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteCrossRefsByProperty_shouldDeleteCrossRefs() = runTest {
        val beforeDelete = fakePropertyPoiCrossDao.entityMap.values
            .filter {
                it.universalLocalPropertyId == crossRefEntity5.universalLocalPropertyId
            }
        assertTrue(beforeDelete.isNotEmpty())

        crossRepository.deleteCrossRefsForProperty(crossRefEntity5.universalLocalPropertyId)

        val resultEntity = fakePropertyPoiCrossDao.entityMap.values
            .filter { it.universalLocalPropertyId == crossRefEntity5.universalLocalPropertyId}

        assertTrue(resultEntity.isEmpty())

        val resultDeleted = crossRepository
            .getCrossRefsByPropertyIdIncludeDeleted(crossRefEntity5.universalLocalPropertyId)
            .first()

        assertTrue(resultDeleted.isEmpty())
    }

    @Test
    fun deleteCrossRefsByPoi_shouldDeleteCrossRefs() = runTest {

        val beforeDelete = fakePropertyPoiCrossDao.entityMap.values
            .filter {
                it.universalLocalPoiId == crossRefEntity6.universalLocalPoiId
            }

        assertTrue(beforeDelete.isNotEmpty())

        crossRepository.deleteCrossRefsForPoi(
            crossRefEntity6.universalLocalPoiId
        )

        val resultEntity = fakePropertyPoiCrossDao.entityMap.values
            .filter {
                it.universalLocalPoiId == crossRefEntity6.universalLocalPoiId
            }

        assertTrue(resultEntity.isEmpty())

        val allCrossRefs = crossRepository
            .getAllCrossRefsIncludeDeleted()
            .first()

        val resultDeleted = allCrossRefs.filter {
            it.universalLocalPoiId == crossRefEntity6.universalLocalPoiId
        }

        assertTrue(resultDeleted.isEmpty())
    }

    @Test
    fun deleteCrossRef_shouldDeleteCrossRef() = runTest {
        val beforeDelete = fakePropertyPoiCrossDao.entityMap.containsKey(
            Pair(
                crossRefEntity6.universalLocalPropertyId,
                crossRefEntity6.universalLocalPoiId
            )
        )
        assertTrue(beforeDelete)

        crossRepository.deleteCrossRef(crossRefEntity6)

        val resultEntity = fakePropertyPoiCrossDao.entityMap.containsKey(
            Pair(crossRefEntity6.universalLocalPropertyId,
                crossRefEntity6.universalLocalPoiId
            )
        )
        assertFalse(resultEntity)

        val resultDeleted = fakePropertyPoiCrossDao.getCrossRefsByIdsIncludedDeleted(
            crossRefEntity6.universalLocalPropertyId,
            crossRefEntity6.universalLocalPoiId
        ).first()

        assertNull(resultDeleted)

    }

    @Test
    fun clearAllDeleted_shouldDeleteAllDeleted() = runTest {
        crossRepository.markCrossRefAsDeleted(
            crossRefEntity1.universalLocalPropertyId,
            crossRefEntity1.universalLocalPoiId
        )
        crossRepository.markCrossRefAsDeleted(
            crossRefEntity2.universalLocalPropertyId,
            crossRefEntity2.universalLocalPoiId
        )
        crossRepository.markCrossRefAsDeleted(
            crossRefEntity3.universalLocalPropertyId,
            crossRefEntity3.universalLocalPoiId
        )
        crossRepository.markCrossRefAsDeleted(
            crossRefEntity4.universalLocalPropertyId,
            crossRefEntity4.universalLocalPoiId
        )

        val beforeDelete = fakePropertyPoiCrossDao.entityMap.values
        assertTrue(beforeDelete.isNotEmpty())
        assertTrue(beforeDelete.any { it.isDeleted })
        crossRepository.clearAllDeleted()

        val remainingEntities = fakePropertyPoiCrossDao.entityMap.values
        assertTrue(remainingEntities.all { !it.isDeleted })

        val result = crossRepository
            .getAllCrossRefsIncludeDeleted()
            .first()

        assertTrue(result.none {it.isDeleted})
    }

    @Test
    fun getCrossRefsByPropertyIdIncludeDeleted_shouldReturnDeletedCrossRefs() = runTest {
        val propertyId = crossRefModel1.universalLocalPropertyId

        crossRepository.markCrossRefAsDeleted(
            crossRefModel1.universalLocalPropertyId,
            crossRefModel1.universalLocalPoiId
        )

        crossRepository.markCrossRefAsDeleted(
            crossRefModel2.universalLocalPropertyId,
            crossRefModel2.universalLocalPoiId
        )

        val result = crossRepository
            .getCrossRefsByPropertyIdIncludeDeleted(propertyId)
            .first()

        assertNotNull(result)

        result.apply{
            assertEquals(2, result.size)
            assertTrue(result.all { it.isDeleted })
            assertTrue(
                result.any { it.universalLocalPoiId == crossRefModel1.universalLocalPoiId }
            )
            assertTrue(
                result.any { it.universalLocalPoiId == crossRefModel2.universalLocalPoiId }
            )
        }
    }

    @Test
    fun getCrossRefsByIdsIncludeDeleted_shouldReturnDeletedCrossRef() = runTest {
        val propertyId = crossRefModel1.universalLocalPropertyId
        val poiId = crossRefModel1.universalLocalPoiId

        crossRepository.markCrossRefAsDeleted(propertyId, poiId)

        val result = crossRepository
            .getCrossRefsByIdsIncludedDeleted(propertyId, poiId)
            .first()

        assertNotNull(result)

        result!!.apply {
            assertEquals(propertyId, result.universalLocalPropertyId)
            assertEquals(poiId, result.universalLocalPoiId)
            assertTrue(result.isDeleted)
        }
    }

    @Test
    fun getAllCrossRefsIncludeDeleted_shouldReturnAllCrossRefsEvenDeleted() = runTest {

        val totalBefore = fakePropertyPoiCrossDao.entityMap.size

        val result = crossRepository
            .getAllCrossRefsIncludeDeleted()
            .first()

        assertNotNull(result)

        result.apply {
            assertEquals(totalBefore, result.size)
            assertTrue(result.any { it.isDeleted })
            assertTrue(result.any { !it.isDeleted })
        }
    }
}
