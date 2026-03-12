package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.OfflinePropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.OfflineStaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapLocalDataSource
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRemoteDataSource
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePoiDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyPoiCrossDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakeStaticMapDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeApiService.FakeStaticMapApiService
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakePropertyOnlineEntity
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class PropertyRepositoryTest {

    private lateinit var fakePropertyDao: FakePropertyDao
    private lateinit var fakeStaticMapDao: FakeStaticMapDao
    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var userRepository: UserRepository
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var poiRepository: PoiRepository
    private lateinit var photoRepository: PhotoRepository
    private lateinit var crossRepository: PropertyPoiCrossRepository
    private lateinit var fakeStaticMapApiService: FakeStaticMapApiService
    private lateinit var staticMapRemoteDataSource: StaticMapRemoteDataSource
    private lateinit var staticMapLocalDataSource: StaticMapLocalDataSource
    private lateinit var staticMapRepository: StaticMapRepository

    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val property3 = FakePropertyEntity.property3
    private val allPropertyEntities = FakePropertyEntity.propertyEntityList
    private val allPropertyEntitiesNotDeleted = FakePropertyEntity.propertyEntityListNotDeleted
    private val propertyModel1 = FakePropertyModel.property1
    private val propertyModel2 = FakePropertyModel.property2
    private val propertyModel3 = FakePropertyModel.property3
    private val allPropertyModels = FakePropertyModel.propertyModelList
    private val allPropertiesModelsNotDeleted = FakePropertyModel.propertyModelListNotDeleted
    private val onlineProperty1 = FakePropertyOnlineEntity.propertyOnline1
    private val onlineProperty2 = FakePropertyOnlineEntity.propertyOnline2
    private val onlineProperty3 = FakePropertyOnlineEntity.propertyOnline3
    private val allOnlineProperties = FakePropertyOnlineEntity.propertyOnlineEntityList
    private val poiEntityList = FakePoiEntity.poiEntityList
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs

    @Before
    fun setup() {
        fakePropertyDao = FakePropertyDao().apply {
            seedPois(poiEntityList)

            val mapByPoi = allCrossRefs
                .groupBy { it.universalLocalPropertyId }
                .mapValues { (_, list) -> list.map { it.universalLocalPoiId } }

            mapByPoi.forEach { (propertyId, poiIds) ->
                linkPropertyToPois(propertyId, *poiIds.toTypedArray())
            }
        }

        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)
        photoRepository = OfflinePhotoRepository(FakePhotoDao())
        crossRepository = OfflinePropertyPoiCrossRepository(FakePropertyPoiCrossDao())
        poiRepository = OfflinePoiRepository(FakePoiDao())
        fakeStaticMapDao = FakeStaticMapDao()
        fakeStaticMapApiService = FakeStaticMapApiService()
        staticMapLocalDataSource = StaticMapLocalDataSource(fakeStaticMapDao)
        staticMapRemoteDataSource = StaticMapRemoteDataSource(fakeStaticMapApiService)
        staticMapRepository = OfflineStaticMapRepository(staticMapRemoteDataSource, staticMapLocalDataSource)

        propertyRepository = OfflinePropertyRepository(
            fakePropertyDao,
            userRepository,
            poiRepository,
            photoRepository,
            crossRepository,
            staticMapRepository)
    }

    @Test
    fun getAllPropertiesByDate_shouldReturnsAllSortedByDate() = runTest {

        val result = propertyRepository
            .getAllPropertiesByDate()
            .first()

        val sanitizedResult = result
            .map {
                it.copy(
                    staticMap = null,
                    photos = emptyList(),
                    poiS = emptyList()
                )
            }
            .sortedBy { it.entryDate }

        val sanitizedExpected = allPropertiesModelsNotDeleted
            .sortedBy { it.entryDate }
            .map {
                it.copy(
                    staticMap = null,
                    photos = emptyList(),
                    poiS = emptyList()
                )
            }

        assertEquals(sanitizedExpected, sanitizedResult)
    }

    @Test
    fun getAllPropertiesByAlphabetic_shouldReturnsAllSortedAlphabetically() = runTest {
        val result = propertyRepository.getAllPropertiesByAlphabetic().first()

        assertEquals(
            allPropertiesModelsNotDeleted.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }),
            result
        )
    }

    @Test
    fun getPropertyById_shouldReturnsCorrectProperty() = runTest {
        val result = propertyRepository
            .getPropertyById(propertyModel1.universalLocalId)
            .first()

        val sanitizedResult = result?.copy(
            staticMap = null,
            photos = emptyList(),
            poiS = emptyList()
        )

        val sanitizedExpected = propertyModel1.copy(
            staticMap = null,
            photos = emptyList(),
            poiS = emptyList()
        )

        assertEquals(sanitizedExpected, sanitizedResult)
    }

    @Test
    fun getPropertiesByUserIdAlphabetic_shouldReturnsCorrectProperties() = runTest {
        val result = propertyRepository
            .getPropertiesByUserIdAlphabetic(propertyModel2.universalLocalUserId)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.universalLocalUserId == propertyModel2.universalLocalUserId }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })

        val sanitizedResult = result.map {
            it.copy(
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList()
            )
        }

        val sanitizedExpected = expected.map {
            it.copy(
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList()
            )
        }

        assertEquals(sanitizedExpected, sanitizedResult)
    }

    @Test
    fun getFullPropertiesByUserIdAlphabetic_shouldReturnsCorrectProperties() = runTest {

        val result = propertyRepository
            .getFullPropertiesByUserIdAlphabetic(propertyModel2.universalLocalUserId)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.universalLocalUserId == propertyModel2.universalLocalUserId }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })

        assertEquals(expected, result)

        result.forEach { property ->
            assertNotNull(property.poiS)
            assertNotNull(property.photos)
        }
    }

    @Test
    fun getPropertiesByUserIdDate_shouldReturnsCorrectProperties() = runTest {
        val result = propertyRepository
            .getPropertiesByUserIdDate(propertyModel2.universalLocalUserId)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter {it.universalLocalUserId == propertyModel2.universalLocalUserId }
            .sortedBy { it.entryDate }

        val sanitizedResult = result.map {
            it.copy(
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList()
            )
        }

        val sanitizedExpected = expected.map {
            it.copy(
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList()
            )
        }

        assertEquals(sanitizedExpected, sanitizedResult)
    }

    @Test
    fun getFullPropertiesByUserIdDate_shouldReturnsCorrectProperties() = runTest {

        val result = propertyRepository
            .getFullPropertiesByUserIdDate(propertyModel2.universalLocalUserId)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.universalLocalUserId == propertyModel2.universalLocalUserId }
            .sortedBy { it.entryDate }

        assertEquals(expected, result)

        result.forEach { property ->
            assertNotNull(property.poiS)
            assertNotNull(property.photos)
        }
    }

    @Test
    fun searchProperties_dateSort_withoutFilters_returnsAllSortedByDate() = runTest {

        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null,
            sortOrder = PropertySortOrder.DATE
        ).first()

        val expected = allPropertiesModelsNotDeleted
            .sortedByDescending  { it.entryDate }

        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_alphabeticSort_withoutFilters_returnsAllSortedAlphabetically() = runTest {

        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null,
            sortOrder = PropertySortOrder.ALPHABETIC
        ).first()

        val expected = allPropertiesModelsNotDeleted
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })

        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_withFilters_returnsCorrectFilteredList() = runTest {

        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = 150,
            minPrice = 200_000,
            maxPrice = null,
            type = null,
            isSold = false,
            sortOrder = PropertySortOrder.DATE
        ).first()

        val expected = allPropertiesModelsNotDeleted
            .filter {
                it.surface <= 150 &&
                        it.price >= 200_000 &&
                        !it.isSold
            }
            .sortedBy { it.entryDate }

        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_filterByType_returnsCorrectProperties() = runTest {

        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = "House",
            isSold = null,
            sortOrder = PropertySortOrder.DATE
        ).first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.type == "House" }
            .sortedBy { it.entryDate }

        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_dateAndAlphabetic_shouldReturnDifferentOrder() = runTest {

        val dateSorted = propertyRepository.searchProperties(
            null, null, null, null, null, null,
            PropertySortOrder.DATE
        ).first()

        val alphaSorted = propertyRepository.searchProperties(
            null, null, null, null, null, null,
            PropertySortOrder.ALPHABETIC
        ).first()

        assertNotEquals(dateSorted, alphaSorted)
    }

    @Test
    fun searchUserProperties_withoutFilters_returnsAllUserPropertiesSortedByDate() = runTest {

        val filters = PropertyFilters(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            selectedType = null,
            isSold = null,
            sortOrder = PropertySortOrder.DATE
        )

        val result = propertyRepository
            .searchUserProperties("user-2", filters)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.universalLocalUserId == "user-2" }
            .sortedBy { it.entryDate }

        assertEquals(expected, result)
    }

    @Test
    fun searchUserProperties_alphabeticSort_returnsSortedUserProperties() = runTest {

        val filters = PropertyFilters(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            selectedType = null,
            isSold = null,
            sortOrder = PropertySortOrder.ALPHABETIC
        )

        val result = propertyRepository
            .searchUserProperties("user-2", filters)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter { it.universalLocalUserId == "user-2" }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })

        assertEquals(expected, result)
    }

    @Test
    fun searchUserProperties_withFilters_returnsCorrectFilteredUserProperties() = runTest {

        val filters = PropertyFilters(
            minSurface = null,
            maxSurface = 250,
            minPrice = 300_000,
            maxPrice = null,
            selectedType = "House",
            isSold = false,
            sortOrder = PropertySortOrder.DATE
        )

        val result = propertyRepository
            .searchUserProperties("user-2", filters)
            .first()

        val expected = allPropertiesModelsNotDeleted
            .filter {
                it.universalLocalUserId == "user-2" &&
                        it.surface <= 250 &&
                        it.price >= 300_000 &&
                        it.type == "House" &&
                        !it.isSold
            }
            .sortedBy { it.entryDate }

        assertEquals(expected, result)
    }

    @Test
    fun searchUserProperties_noMatch_returnsEmptyList() = runTest {

        val filters = PropertyFilters(
            minSurface = 1000,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            selectedType = null,
            isSold = null,
            sortOrder = PropertySortOrder.DATE
        )

        val result = propertyRepository
            .searchUserProperties("user-2", filters)
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun markPropertyAsSold_shouldUpdatePropertyCorrectly() = runTest {

        val propertyId = property2.id
        val saleDate = "2025-09-01"
        val updatedAt = 1800000000000L

        propertyRepository.markPropertyAsSold(propertyId, saleDate, updatedAt)

        val entity = fakePropertyDao.entityMap[propertyId]

        assertNotNull(entity)
        assertTrue(entity!!.isSold)
        assertEquals(saleDate, entity.saleDate)
        assertEquals(updatedAt, entity.updatedAt)

        val result = propertyRepository
            .getPropertyById(propertyId)
            .first()

        assertNotNull(result)
        assertTrue(result!!.isSold)
        assertEquals(LocalDate.parse(saleDate), result.saleDate)
    }

    @Test
    fun markPropertyAsSold_withInvalidId_shouldDoNothing() = runTest {
        val invalidId = "unknown-id"

        propertyRepository.markPropertyAsSold(
            invalidId,
            "2025-09-01",
            1800000000000L
        )

        val result = propertyRepository
            .getPropertyById(invalidId)
            .first()

        assertNull(result)
    }

    @Test
    fun uploadUnSyncedProperties_shouldReturnOnlyPropertiesWithIsSyncedFalse() = runTest {
        val result = propertyRepository.uploadUnSyncedPropertiesToFirebase().first()

        val expected = allPropertyEntities
            .filter { !it.isSynced }

        assertEquals(expected, result)
    }

    @Test
    fun insertPropertyInsertFromUi_shouldInsertWithIsSyncedFalse() = runTest {
        val newPropertyModel = Property(
            universalLocalId = "property-4",
            universalLocalUserId = propertyModel1.universalLocalUserId,
            title = "New Property",
            type = "House",
            price = 500_000,
            surface = 150,
            rooms = 3,
            description = "New property from UI",
            address = "123 New St",
            isSold = false,
            entryDate = LocalDate.parse("2025-10-01"),
            saleDate = null,
            staticMap = null,
            photos = emptyList(),
            poiS = emptyList(),
            isSynced = false,
            isDeleted = false,
            updatedAt = 1800000000000L
        )

        propertyRepository.insertPropertyFromUI(newPropertyModel)

        val resultEntity = fakePropertyDao.entityMap[newPropertyModel.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals(newPropertyModel.universalLocalId, id)
            assertEquals(newPropertyModel.universalLocalUserId, universalLocalUserId)
            assertEquals(newPropertyModel.title, title)
            assertEquals(newPropertyModel.type, type)
            assertEquals(newPropertyModel.price, price)
            assertEquals(newPropertyModel.surface, surface)
            assertEquals(newPropertyModel.rooms, rooms)
            assertEquals(newPropertyModel.description, description)
            assertEquals(newPropertyModel.address, address)
            assertEquals(newPropertyModel.entryDate.toString(), entryDate)
            assertEquals(newPropertyModel.saleDate?.toString(), saleDate)
            assertFalse(isSynced)
            assertFalse(isDeleted)
            assertEquals(newPropertyModel.updatedAt, updatedAt)
        }

        val resultInserted = propertyRepository
            .getPropertyById(newPropertyModel.universalLocalId)
            .first()

        assertEquals(newPropertyModel, resultInserted)
    }

    @Test
    fun insertPropertiesInsertFromUi_shouldInsertAllWithIsSyncedFalse() = runTest {
        val insertedTimestamp = 1800000000000L
        val newProperties = listOf(
            Property(
                universalLocalId = "property-4",
                universalLocalUserId = propertyModel1.universalLocalUserId,
                title = "New Property 4",
                type = "House",
                price = 500_000,
                surface = 150,
                rooms = 4,
                description = "Inserted from UI 4",
                address = "4 New Street",
                isSold = false,
                entryDate = LocalDate.parse("2025-10-01"),
                saleDate = null,
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList(),
                isSynced = false,
                isDeleted = false,
                updatedAt = insertedTimestamp + 1
            ),
            Property(
                universalLocalId = "property-5",
                universalLocalUserId = propertyModel1.universalLocalUserId,
                title = "New Property 5",
                type = "Apartment",
                price = 350_000,
                surface = 90,
                rooms = 3,
                description = "Inserted from UI 5",
                address = "5 New Street",
                isSold = false,
                entryDate = LocalDate.parse("2025-10-02"),
                saleDate = null,
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList(),
                isSynced = false,
                isDeleted = false,
                updatedAt = insertedTimestamp + 2
            ),
            Property(
                universalLocalId = "property-6",
                universalLocalUserId = propertyModel1.universalLocalUserId,
                title = "New Property 6",
                type = "Studio",
                price = 200_000,
                surface = 45,
                rooms = 1,
                description = "Inserted from UI 6",
                address = "6 New Street",
                isSold = false,
                entryDate = LocalDate.parse("2025-10-03"),
                saleDate = null,
                staticMap = null,
                photos = emptyList(),
                poiS = emptyList(),
                isSynced = false,
                isDeleted = false,
                updatedAt = insertedTimestamp + 3
            )
        )

        propertyRepository.insertPropertiesFromUI(newProperties)


        newProperties.forEach { expected ->
            val entity = fakePropertyDao.entityMap[expected.universalLocalId]
            assertNotNull(entity)
            entity!!.apply {
                assertEquals(expected.universalLocalId, id)
                assertEquals(expected.title, title)
                assertEquals(expected.price, price)
                assertEquals(expected.surface, surface)
                assertFalse(isSynced)
                assertFalse(isDeleted)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }

        newProperties.forEach { expected ->
            val result = propertyRepository
                .getPropertyById(expected.universalLocalId)
                .first()

            assertEquals(expected, result)
        }
    }

    @Test
    fun insertPropertyInsertFromFirebase_shouldInsertWithIsSyncedTrue() = runTest {
        val firestoreId = "firestore-property-4"
        val onlineProperty = PropertyOnlineEntity(
            ownerUid = "firebase_uid_1",
            universalLocalId = "property-4",
            universalLocalUserId = "user-1",
            title = "Firebase Property",
            type = "House",
            price = 600_000,
            surface = 180,
            rooms = 5,
            description = "Inserted from Firebase",
            address = "4 Firebase Street",
            latitude = 48.860,
            longitude = 2.350,
            isSold = false,
            entryDate = "2025-11-01",
            saleDate = null,
            updatedAt = 1900000000000L
        )

        propertyRepository.insertPropertyInsertFromFirebase(
            onlineProperty,
            firestoreId
        )

        val resultEntity = fakePropertyDao.entityMap[onlineProperty.universalLocalId]

        assertNotNull(resultEntity)
        resultEntity!!.apply {
            assertEquals(onlineProperty.universalLocalId, id)
            assertEquals(firestoreId, firestoreDocumentId)
            assertEquals(onlineProperty.title, title)
            assertEquals(onlineProperty.price, price)
            assertEquals(onlineProperty.surface, surface)
            assertEquals(onlineProperty.description, description)
            assertEquals(onlineProperty.address, address)
            assertTrue(isSynced)
            assertEquals(onlineProperty.updatedAt, updatedAt)
        }

        val resultInserted = propertyRepository
            .getPropertyById(onlineProperty.universalLocalId)
            .first()

        assertNotNull(resultInserted)
        assertEquals(firestoreId, resultInserted!!.firestoreDocumentId)
        assertEquals(onlineProperty.universalLocalId, resultInserted.universalLocalId)
        assertEquals(onlineProperty.title, resultInserted.title)
        assertTrue(resultInserted.isSynced)
        assertEquals(onlineProperty.updatedAt, resultInserted.updatedAt)
    }

    @Test
    fun insertPropertiesInsertFromFirebase_shouldInsertAllWithIsSyncedTrue() = runTest {
        val insertedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-property-4",
            "firestore-property-5",
            "firestore-property-6"
        )
        val onlineProperties = listOf(
            PropertyOnlineEntity(
                ownerUid = "firebase_uid_1",
                universalLocalId = "property-4",
                universalLocalUserId = "user-1",
                title = "Firebase Property 4",
                type = "House",
                price = 600_000,
                surface = 180,
                rooms = 5,
                description = "Inserted from Firebase 4",
                address = "4 Firebase Street",
                latitude = 48.860,
                longitude = 2.350,
                isSold = false,
                entryDate = "2025-11-01",
                saleDate = null,
                updatedAt = insertedTimestamp + 1
            ),
            PropertyOnlineEntity(
                ownerUid = "firebase_uid_2",
                universalLocalId = "property-5",
                universalLocalUserId = "user-2",
                title = "Firebase Property 5",
                type = "Apartment",
                price = 350_000,
                surface = 90,
                rooms = 3,
                description = "Inserted from Firebase 5",
                address = "5 Firebase Street",
                latitude = 48.861,
                longitude = 2.351,
                isSold = false,
                entryDate = "2025-11-02",
                saleDate = null,
                updatedAt = insertedTimestamp + 2
            ),
            PropertyOnlineEntity(
                ownerUid = "firebase_uid_3",
                universalLocalId = "property-6",
                universalLocalUserId = "user-3",
                title = "Firebase Property 6",
                type = "Studio",
                price = 200_000,
                surface = 45,
                rooms = 1,
                description = "Inserted from Firebase 6",
                address = "6 Firebase Street",
                latitude = 48.862,
                longitude = 2.352,
                isSold = false,
                entryDate = "2025-11-03",
                saleDate = null,
                updatedAt = insertedTimestamp + 3
            )
        )

        val pairs = onlineProperties.mapIndexed { index, property ->
            Pair(property, firestoreIds[index])
        }

        propertyRepository.insertPropertiesInsertFromFirebase(pairs)

        onlineProperties.forEachIndexed { index, expected ->

            val resultEntity = fakePropertyDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.universalLocalId, id)
                assertEquals(firestoreIds[index], firestoreDocumentId)
                assertEquals(expected.title, title)
                assertEquals(expected.price, price)
                assertEquals(expected.surface, surface)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }

        }

        onlineProperties.forEachIndexed { index, expected ->

            val resultInserted = propertyRepository
                .getPropertyById(expected.universalLocalId)
                .first()

            assertNotNull(resultInserted)

            resultInserted!!.apply {
                assertEquals(expected.universalLocalId, universalLocalId)
                assertEquals(firestoreIds[index], firestoreDocumentId)
                assertEquals(expected.title, title)
                assertEquals(expected.price, price)
                assertEquals(expected.surface, surface)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }
    }

    @Test
    fun updatePropertyFromUI_shouldUpdatePropertyAndForceSyncFalse() = runTest {
        val updatedTimestamp = 1800000000000L
        val updatedProperty = propertyModel1.copy(
            title = "Updated title UI",
            price = 999_999,
            surface = 250,
            updatedAt = updatedTimestamp,
            isSynced = true
        )

        propertyRepository.updatePropertyFromUI(updatedProperty)

        val resultEntity = fakePropertyDao.entityMap[updatedProperty.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated title UI", title)
            assertEquals(999_999, price)
            assertEquals(250, surface)
            assertEquals(updatedTimestamp, updatedAt)
            assertFalse(isSynced)
        }

        val resultUpdated = propertyRepository
            .getPropertyById(updatedProperty.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated title UI", title)
            assertEquals(999_999, price)
            assertEquals(250, surface)
            assertFalse(isSynced)
            assertEquals(updatedTimestamp, updatedAt)
        }
    }

    @Test
    fun updatePropertyFromFirebase_shouldUpdatePropertyAndForceSyncTrue() = runTest {
        val firestoreId = "firestore-property-1"
        val updatedTimestamp = 1900000000000L
        val updatedOnlineProperty = onlineProperty1.copy(
            universalLocalId = propertyModel1.universalLocalId,
            universalLocalUserId = propertyModel1.universalLocalUserId,
            title = "Updated from firebase",
            price = 888_888,
            surface = 300,
            isSold = false,
            saleDate = "2025-12-01",
            updatedAt = updatedTimestamp
        )

        propertyRepository.updatePropertyFromFirebase(
            updatedOnlineProperty,
            firestoreId
        )

        val resultEntity = fakePropertyDao.entityMap[propertyModel1.universalLocalId]

        assertNotNull(resultEntity)

        resultEntity!!.apply {
            assertEquals("Updated from firebase", title)
            assertEquals(888_888, price)
            assertEquals(300, surface)
            assertEquals(false, isSold)
            assertEquals("2025-12-01", saleDate)
            assertEquals(firestoreId, firestoreDocumentId)
            assertTrue(isSynced)
            assertEquals(updatedTimestamp, updatedAt)
        }

        val resultUpdated = propertyRepository
            .getPropertyById(propertyModel1.universalLocalId)
            .first()

        assertNotNull(resultUpdated)

        resultUpdated!!.apply {
            assertEquals("Updated from firebase", title)
            assertEquals(888_888, price)
            assertEquals(300, surface)
            assertEquals(false, isSold)
            assertEquals(
                LocalDate.parse("2025-12-01"),
                saleDate
            )
            assertEquals(firestoreId, firestoreDocumentId)
            assertTrue(isSynced)
            assertEquals(updatedTimestamp, updatedAt)
        }
    }

    @Test
    fun updateAllPropertiesFromFirebase_shouldUpdateAllAndForceSyncTrue() = runTest {
        val updatedTimestamp = 1900000000000L
        val firestoreIds = listOf(
            "firestore-property-1",
            "firestore-property-2",
            "firestore-property-3",
        )
        val updatedOnlineProperties = listOf(
            onlineProperty1.copy(
                title = "Updated from firebase 1",
                price = 777_000,
                updatedAt = updatedTimestamp + 1
            ),
            onlineProperty2.copy(
                title = "Updated from firebase 2",
                isSold = true,
                saleDate = "2025-12-15",
                updatedAt = updatedTimestamp + 2

            ),
            onlineProperty3.copy(
                title = "Updated from firebase 3",
                surface = 200,
                updatedAt = updatedTimestamp + 3
            )
        )

        val pairs = updatedOnlineProperties.mapIndexed { index, property ->
            property to firestoreIds[index]
        }

        propertyRepository.updateAllPropertiesFromFirebase(pairs)

        updatedOnlineProperties.forEachIndexed { index, expected ->
            val resultEntity = fakePropertyDao.entityMap[expected.universalLocalId]

            assertNotNull(resultEntity)

            resultEntity!!.apply {
                assertEquals(expected.price, price)
                assertEquals(expected.isSold, isSold)
                assertEquals(expected.saleDate, saleDate)
                assertEquals(expected.title, title)
                assertEquals(expected.surface, surface)
                assertEquals(firestoreIds[index], firestoreDocumentId)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }

        updatedOnlineProperties.forEachIndexed { index, expected ->

            val resultUpdated = propertyRepository
                .getPropertyByIdIncludeDeleted(expected.universalLocalId)
                .first()

            assertNotNull(resultUpdated)



            resultUpdated!!.apply {
                assertEquals(expected.price, price)
                assertEquals(expected.isSold, isSold)
                assertEquals(
                    expected.saleDate,
                    saleDate
                )
                assertEquals(expected.title, title)
                assertEquals(expected.surface, surface)
                assertEquals(firestoreIds[index], firestoreDocumentId)
                assertTrue(isSynced)
                assertEquals(expected.updatedAt, updatedAt)
            }
        }
    }

    @Test
    fun markPropertyAsDeleted_shouldHidePropertyFromQueries() = runTest {
        propertyRepository.markPropertyAsDeleted(propertyModel2)

        val rawEntity = fakePropertyDao.entityMap[propertyModel2.universalLocalId]

        assertNotNull(rawEntity)

        rawEntity!!.apply {
            assertTrue(rawEntity.isDeleted)
            assertFalse(rawEntity.isSynced)
        }

        val result = propertyRepository.getPropertyById(propertyModel2.universalLocalId)
            .first()

        assertNull(result)

    }

    @Test
    fun markAllPropertiesAsDeleted_shouldHideAllProperties() = runTest {
        propertyRepository.markAllPropertiesAsDeleted()

        val rawEntities = fakePropertyDao.entityMap.values

        assertNotNull(rawEntities)

        rawEntities.apply {
            assertTrue(rawEntities.isNotEmpty())
            assertTrue(rawEntities.all { it.isDeleted })
            assertTrue(rawEntities.all { !it.isSynced })
        }

        val result = propertyRepository
            .getAllPropertiesByDate()
            .first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteProperty_shouldDeleteProperty() = runTest {
        val existsBefore = fakePropertyDao.entityMap.containsKey(property3.id)
        assertTrue(existsBefore)

        propertyRepository.deleteProperty(property3)

        val resultEntity = fakePropertyDao.entityMap.containsKey(property3.id)
        assertFalse(resultEntity)

        val resultDeleted = propertyRepository.getPropertyByIdIncludeDeleted(property3.id).first()
        assertNull(resultDeleted)
    }

    @Test
    fun clearAllPropertiesDeleted_shouldDeleteOnlyDeletedProperties() = runTest {
        propertyRepository.markPropertyAsDeleted(propertyModel1)

        assertTrue(fakePropertyDao.entityMap[propertyModel1.universalLocalId]!!.isDeleted)
        assertTrue(fakePropertyDao.entityMap[propertyModel3.universalLocalId]!!.isDeleted)
        assertFalse(fakePropertyDao.entityMap[propertyModel2.universalLocalId]!!.isDeleted)

        propertyRepository.clearAllDeleted()

        assertFalse(fakePropertyDao.entityMap.containsKey(propertyModel1.universalLocalId))
        assertFalse(fakePropertyDao.entityMap.containsKey(propertyModel3.universalLocalId))
        assertTrue(fakePropertyDao.entityMap.containsKey(propertyModel2.universalLocalId))

        val allProperties = propertyRepository.getAllPropertyIncludeDeleted().first()

        assertFalse(allProperties.any { it.id == propertyModel1.universalLocalId })
        assertFalse(allProperties.any { it.id == propertyModel3.universalLocalId })
        assertTrue(allProperties.any { it.id == propertyModel2.universalLocalId })
    }

    @Test
    fun getPropertyByIdIncludeDeleted_returnsDeletedProperty() = runTest {
        val result = propertyRepository.getPropertyByIdIncludeDeleted(property3.id).first()

        assertNotNull(result)

        result!!.apply {
            assertEquals(property3.id, result.id)
            assertTrue(result.isDeleted)
        }
    }

    @Test
    fun getAllPropertiesIncludeDeleted_returnsAllIncludingDeleted() = runTest {
        val result = propertyRepository.getAllPropertyIncludeDeleted().first()

        assertEquals(allPropertyEntities.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }

    @Test
    fun getPropertyWithPoiS_shouldReturnPropertyWithLinkedPoiS() = runTest {
        val result = propertyRepository
            .getPropertyWithPoiS(property1.id)
            .first()

        assertEquals(property1.id, result.property.universalLocalId)
        assertEquals(property1.title, result.property.title)

        val expectedPoiIds = allCrossRefs
            .filter {
                it.universalLocalPropertyId == property1.id && !it.isDeleted
            }
            .map { it.universalLocalPoiId }
            .toSet()

        val resultPoiSIds = result.poiS
            .map { it.universalLocalId }
            .toSet()

        assertEquals(expectedPoiIds, resultPoiSIds)
    }





}
