package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.OfflineStaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapConfig
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapLocalDataSource
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRemoteDataSource
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.fakeData.fakeApiService.FakeStaticMapApiService
import com.dcac.realestatemanager.fakeData.fakeDao.FakeStaticMapDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeStaticMapEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakeStaticMapModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeStaticMapOnlineEntity
import com.dcac.realestatemanager.model.StaticMap
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

//saveStaticMapToLocal is tested in instrumented test of RemoteDataSource
class StaticMapRepositoryTest {

    private lateinit var fakeStaticMapDao: FakeStaticMapDao
    private lateinit var fakeStaticMapApiService: FakeStaticMapApiService
    private lateinit var staticMapRemoteDataSource: StaticMapRemoteDataSource
    private lateinit var staticMapLocalDataSource: StaticMapLocalDataSource
    private lateinit var staticMapRepository: StaticMapRepository

    private val staticMapEntity1 = FakeStaticMapEntity.staticMap1
    private val staticMapEntity3 = FakeStaticMapEntity.staticMap3
    private val allStaticMapsEntity = FakeStaticMapEntity.staticMapEntityList
    private val allStaticMapsEntityNotDeleted = FakeStaticMapEntity.staticMapEntityListNotDeleted
    private val staticMapOnlineEntity1 = FakeStaticMapOnlineEntity.staticMapOnline1
    private val staticMapOnlineEntity2 = FakeStaticMapOnlineEntity.staticMapOnline2
    private val staticMapOnlineEntity3 = FakeStaticMapOnlineEntity.staticMapOnline3
    private val staticMapModel1 = FakeStaticMapModel.staticMap1
    private val staticMapModel2 = FakeStaticMapModel.staticMap2
    private val staticMapModel3 = FakeStaticMapModel.staticMap3
    private val allStaticMapsModelNotDeleted = FakeStaticMapModel.staticMapListNotDeleted

    @Before
    fun setup() {
        fakeStaticMapDao = FakeStaticMapDao()
        fakeStaticMapApiService = FakeStaticMapApiService()

        staticMapRemoteDataSource = StaticMapRemoteDataSource(fakeStaticMapApiService)
        staticMapLocalDataSource = StaticMapLocalDataSource(fakeStaticMapDao)

        staticMapRepository = OfflineStaticMapRepository(
            staticMapRemoteDataSource, staticMapLocalDataSource
        )
    }

    @Test
    fun getStaticMapImage_whenApiSuccess_shouldReturnByteArray() = runTest {
        fakeStaticMapApiService.shouldSucceed = true

        val config = StaticMapConfig(
            center = "48.8566,2.3522",
            zoom = 15,
            size = "600x300",
            mapType = "roadmap",
            markers = listOf("48.8566,2.3522"),
            styles = emptyList()
        )

        val result = staticMapRepository.getStaticMapImage(config)

        assertNotNull(result)
    }

    @Test
    fun getStaticMapImage_whenApiFails_shouldReturnNull() = runTest {
        fakeStaticMapApiService.shouldSucceed = false

        val config = StaticMapConfig(
            center = "48.8566,2.3522",
            zoom = 15,
            size = "600x300",
            mapType = "roadmap",
            markers = listOf("48.8566,2.3522"),
            styles = emptyList()
        )

        val result = staticMapRepository.getStaticMapImage(config)

        assertNull(result)
    }

    //saveStaticMapToLocal is tested in instrumented test of RemoteDataSource

    @Test
    fun getStaticMapById_shouldReturnsCorrectStaticMap() = runTest {
        val result = staticMapRepository
            .getStaticMapByPropertyId(staticMapModel1.universalLocalPropertyId)
            .first()

        assertEquals(staticMapModel1, result)
    }

    @Test
    fun getStaticMapByPropertyId_shouldReturnsCorrectStaticMap() = runTest {
        val result = staticMapRepository
            .getStaticMapByPropertyId(staticMapModel2.universalLocalPropertyId)
            .first()

        val expected = allStaticMapsModelNotDeleted
            .firstOrNull { it.universalLocalPropertyId == staticMapModel2.universalLocalPropertyId}
        assertEquals(expected, result)
    }

    @Test
    fun getAllStaticMaps_shouldReturnsAllStaticMaps() = runTest {
        val result = staticMapRepository.getAllStaticMap().first()

        assertEquals(allStaticMapsModelNotDeleted, result)
    }

    @Test
    fun uploadUnSyncedStaticMaps_shouldReturnOnlyStaticMapsWithIsSyncedFalse() = runTest {
        val result = staticMapRepository.uploadUnSyncedStaticMapToFirebase().first()

        val expected = allStaticMapsEntity
            .filter { !it.isSynced }

        assertEquals(expected, result)
    }

    @Test
    fun insertStaticMapInsertFromUI_shouldInsertWithIsSyncedFalse() = runTest {
        val newStaticMapModel = StaticMap(
            universalLocalId = "static-map-4",
            universalLocalPropertyId = staticMapModel1.universalLocalPropertyId,
            uri = "file://static_map_4.jpg",
            isDeleted = false,
            isSynced = false,
            updatedAt = 1800000000000L
        )
        staticMapRepository.insertStaticMapInsertFromUI(newStaticMapModel)

        val resultEntity = fakeStaticMapDao.entityMap[newStaticMapModel.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(newStaticMapModel.universalLocalId, id)
            assertEquals(newStaticMapModel.universalLocalPropertyId, universalLocalPropertyId)
            assertEquals(newStaticMapModel.uri, uri)
            assertFalse(isSynced)
            assertFalse(isDeleted)
            assertEquals(newStaticMapModel.updatedAt, updatedAt)
        }

        val resultInserted = staticMapRepository
            .getStaticMapById(newStaticMapModel.universalLocalId)
            .first()

        assertEquals(newStaticMapModel, resultInserted)
    }

    @Test
    fun insertStaticMapInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runTest {
        val firestoreId = "firestore-static-map-4"
        val localUri = "file://static_map_4.jpg"

        val onlineStaticMap = StaticMapOnlineEntity(
            ownerUid = "firebase_uid_1",
            universalLocalId = "static-map-4",
            universalLocalPropertyId = staticMapModel1.universalLocalPropertyId,
            storageUrl = "https://firebase.storage/static_map_4.jpg",
            isDeleted = false,
            updatedAt = 1900000000000L
        )

        staticMapRepository.insertStaticMapInsertFromFirebase(
            staticMap = onlineStaticMap,
            firestoreId = firestoreId,
            localUri = localUri
        )

        val resultEntity = fakeStaticMapDao.entityMap[onlineStaticMap.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(onlineStaticMap.universalLocalId, id)
            assertEquals(firestoreId, firestoreDocumentId)
            assertEquals(localUri, uri)
            assertTrue(isSynced)
            assertEquals(onlineStaticMap.updatedAt, updatedAt)
        }

        val resultInserted = staticMapRepository
            .getStaticMapById(onlineStaticMap.universalLocalId)
            .first()

        assertNotNull(resultInserted)

        resultInserted!!.apply{
            assertEquals(firestoreId, resultInserted.firestoreDocumentId)
            assertEquals(onlineStaticMap.universalLocalId, resultInserted.universalLocalId)
            assertTrue(resultInserted.isSynced)
            assertEquals(onlineStaticMap.updatedAt, resultInserted.updatedAt)
        }
    }


    @Test
    fun updateStaticMapFromUI_shouldUpdateStaticMapAndForceSyncFalse() = runTest {
        val updatedTimeStamp = 1800000000000L
        val updatedStaticMap = staticMapModel1.copy(
            updatedAt = updatedTimeStamp,
            isSynced = true
        )

        staticMapRepository.updateStaticMapFromUI(updatedStaticMap)

        val resultEntity = fakeStaticMapDao.entityMap[updatedStaticMap.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(updatedTimeStamp, updatedAt)
            assertFalse(isSynced)
        }

        val resultUpdated = staticMapRepository
            .getStaticMapById(updatedStaticMap.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals(updatedTimeStamp, updatedAt)
            assertFalse(isSynced)
        }
    }

    @Test
    fun updateStaticMapFromFirebase_shouldUpdateStaticMapForceSyncTrue() = runTest {
        val firestoreId = "firestore-static-map-1"
        val updatedTimestamp = 1900000000000L
        val updatedOnlineStaticMap = staticMapOnlineEntity1.copy(
            updatedAt = updatedTimestamp
        )

        staticMapRepository.updateStaticMapFromFirebase(
            staticMap = updatedOnlineStaticMap,
            firestoreId = firestoreId
        )

        val resultEntity = fakeStaticMapDao.entityMap[updatedOnlineStaticMap.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(firestoreId, firestoreDocumentId)
            assertTrue(isSynced)
            assertEquals(updatedTimestamp, updatedAt)
        }

        val resultUpdated = staticMapRepository
            .getStaticMapById(updatedOnlineStaticMap.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals(firestoreId, firestoreDocumentId)
            assertTrue(isSynced)
            assertEquals(updatedTimestamp,updatedAt)
        }
    }

    @Test
    fun markStaticMapAsDelete_shouldHideStaticMapFromQueries() = runTest {
        staticMapRepository.markStaticMapAsDeleted(staticMapModel2)

        val rawEntity = fakeStaticMapDao.entityMap[staticMapModel2.universalLocalId]
        assertNotNull(rawEntity)
        rawEntity!!.apply {
            assertTrue(isDeleted)
            assertFalse(isSynced)
        }

        val result = staticMapRepository.getAllStaticMap().first()
        assertFalse(result.contains(staticMapModel2))
    }

    @Test
    fun markStaticMapAsDeletedByProperty_shouldHideStaticMapFromQueries() = runTest {
        staticMapRepository.markStaticMapAsDeletedByProperty(staticMapModel2.universalLocalPropertyId)

        val rawEntities = fakeStaticMapDao.entityMap.values.filter {
            it.universalLocalPropertyId == staticMapModel2.universalLocalPropertyId
        }

        assertNotNull(rawEntities)

        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })
        }

        val result = staticMapRepository
            .getStaticMapByPropertyId(staticMapModel2.universalLocalPropertyId)
            .first()

        assertNull(result)
    }

    @Test
    fun deleteStaticMapByPropertyId_shouldDeleteStaticMap() = runTest {
        val beforeDelete = fakeStaticMapDao.entityMap.values
            .filter {
                it.universalLocalPropertyId == staticMapEntity3.universalLocalPropertyId
            }
        assertTrue(beforeDelete.isNotEmpty())

        staticMapRepository.deleteStaticMapByPropertyId(staticMapEntity3.universalLocalPropertyId)

        val resultEntity = fakeStaticMapDao.entityMap.values
            .filter {
                it.universalLocalPropertyId == staticMapEntity3.universalLocalPropertyId
            }

        assertTrue(resultEntity.isEmpty())

        val resultDeleted = staticMapRepository
            .getStaticMapByPropertyIdIncludeDeleted(staticMapEntity3.universalLocalPropertyId)
            .first()

        assertNull(resultDeleted)
    }

    @Test
    fun deleteStaticMap_shouldDeleteStaticMap() = runTest{
        val beforeDelete = fakeStaticMapDao.entityMap.containsKey(staticMapEntity3.id)
        assertTrue(beforeDelete)

        staticMapRepository.deleteStaticMap(staticMapEntity3)

        val resultEntity = fakeStaticMapDao.entityMap.containsKey(staticMapEntity3.id)
        assertFalse(resultEntity)

        val resultDeleted = staticMapRepository
            .getStaticMapByIdIncludeDeleted(staticMapEntity3.id).first()
        assertNull(resultDeleted)
    }

    @Test
    fun clearAllStaticMapsDeleted_shouldDeleteOnlyDeletedStaticMaps() = runTest {
        staticMapRepository.markStaticMapAsDeleted(staticMapModel1)

        assertTrue(fakeStaticMapDao.entityMap[staticMapModel1.universalLocalId]!!.isDeleted)
        assertTrue(fakeStaticMapDao.entityMap[staticMapModel3.universalLocalId]!!.isDeleted)
        assertFalse(fakeStaticMapDao.entityMap[staticMapModel2.universalLocalId]!!.isDeleted)

        staticMapRepository.clearAllStaticMapsDeleted()

        assertFalse(fakeStaticMapDao.entityMap.containsKey(staticMapModel1.universalLocalId))
        assertFalse(fakeStaticMapDao.entityMap.containsKey(staticMapModel3.universalLocalId))
        assertTrue(fakeStaticMapDao.entityMap.containsKey(staticMapModel2.universalLocalId))

        val allPhotos = staticMapRepository.getAllStaticMapIncludeDeleted().first()

        assertFalse(allPhotos.any { it.id == staticMapModel1.universalLocalId })
        assertFalse(allPhotos.any { it.id == staticMapModel3.universalLocalId })
        assertTrue(allPhotos.any { it.id == staticMapModel2.universalLocalId })
    }

    @Test
    fun getStaticMapByIdIncludeDeleted_shouldReturnDeletedStaticMap() = runTest {
        staticMapRepository.markStaticMapAsDeleted(staticMapModel3)

        val result = staticMapRepository
            .getStaticMapByIdIncludeDeleted(staticMapModel3.universalLocalId)
            .first()

        assertNotNull(result)
        result!!.apply {
            assertEquals(staticMapModel3.universalLocalId, id)
            assertTrue(isDeleted)
        }
    }

    @Test
    fun getStaticMapByPropertyIdIncludeDeleted_shouldReturnDeletedStaticMap()= runTest {
        val propertyId = staticMapModel1.universalLocalPropertyId
        staticMapRepository.markStaticMapAsDeleted(staticMapModel1)

        val result = staticMapRepository
            .getStaticMapByIdIncludeDeleted(staticMapModel1.universalLocalId)
            .first()

        assertNotNull(result)
        result!!.apply {
            assertEquals(staticMapModel1.universalLocalId, result.id)
            assertEquals(propertyId, universalLocalPropertyId)
            assertTrue(isDeleted)
        }
    }

    @Test
    fun getAllStaticMapsIncludeDeleted_shouldReturnAllStaticMapEvenDeleted() = runTest {
        staticMapRepository.markStaticMapAsDeleted(staticMapModel2)

        val totalBefore = fakeStaticMapDao.entityMap.size

        val result = staticMapRepository
            .getAllStaticMapIncludeDeleted()
            .first()

        Assert.assertEquals(totalBefore, result.size)
        Assert.assertTrue(result.any { it.id == staticMapModel2.universalLocalId && it.isDeleted })
    }
}