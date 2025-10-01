package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.OfflinePropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePoiDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyPoiCrossDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class PropertyRepositoryTest {

    private lateinit var fakePropertyDao: FakePropertyDao
    private lateinit var fakeUserDao: FakeUserDao
    private lateinit var userRepository: UserRepository
    private lateinit var propertyRepository: PropertyRepository
    private lateinit var poiRepository: PoiRepository
    private lateinit var photoRepository: PhotoRepository
    private lateinit var crossRepository: PropertyPoiCrossRepository

    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val property3 = FakePropertyEntity.property3
    private val allPropertyEntities = FakePropertyEntity.propertyEntityList
    private val allPropertyEntitiesNotDeleted = FakePropertyEntity.propertyEntityListNotDeleted

    private val propertyModel1 = FakePropertyModel.property1
    private val propertyModel2 = FakePropertyModel.property2
    private val propertyModel3 = FakePropertyModel.property3
    private val allPropertyModels = FakePropertyModel.propertyModelList
    private val allPropertyModelsNotDeleted = FakePropertyModel.propertyModelListNotDeleted

    private val poiEntityList = FakePoiEntity.poiEntityList
    private val allCrossRefs = FakePropertyPoiCrossEntity.allCrossRefs

    @Before
    fun setup() {
        fakePropertyDao = FakePropertyDao().apply {
            seedPois(poiEntityList)

            val mapByPoi = allCrossRefs
                .groupBy { it.propertyId }
                .mapValues { (_, list) -> list.map { it.poiId } }

            mapByPoi.forEach { (propertyId, poiIds) ->
                linkPropertyToPois(propertyId, *poiIds.toLongArray())
            }
        }

        fakeUserDao = FakeUserDao()
        userRepository = OfflineUserRepository(fakeUserDao)
        poiRepository = OfflinePoiRepository(FakePoiDao(), userRepository)
        photoRepository = OfflinePhotoRepository(FakePhotoDao())
        crossRepository = OfflinePropertyPoiCrossRepository(FakePropertyPoiCrossDao())

        propertyRepository = OfflinePropertyRepository(
            fakePropertyDao,
            userRepository,
            poiRepository,
            photoRepository,
            crossRepository)
    }

    @Test
    fun getAllPropertiesByDate_returnsAllSortedByDate() = runTest {
        // --- Act ---
        val result = propertyRepository.getAllPropertiesByDate().first()

        // --- Arrange ---
        val expected = allPropertyModelsNotDeleted.sortedBy { it.entryDate }

        // --- Assert ---
        assertEquals(expected, result)
    }

    @Test
    fun getAllPropertiesByAlphabetic_returnsAllSortedAlphabetically() = runTest {
        val result = propertyRepository.getAllPropertiesByAlphabetic().first()

        val expected = allPropertyModelsNotDeleted.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })

        assertEquals(expected, result)
    }

    @Test
    fun getPropertyById_returnsCorrectProperty() = runTest {
        val result = propertyRepository.getPropertyById(property1.id).first()

        assertNotNull(result)
        assertEquals(propertyModel1.id, result?.id)
        assertEquals(propertyModel1.user.id, result?.user?.id)
        assertEquals(propertyModel1.photos, result?.photos)
        assertEquals(propertyModel1.poiS.toSet(), result?.poiS?.toSet())
    }

    @Test
    fun getPropertyById_whenUnknownId_returnsNull() = runTest {
        val result = propertyRepository.getPropertyById(99999L).first()
        assertNull(result)
    }

    @Test
    fun getPropertiesByUserId_returnsCorrectProperties() = runTest {
        val userId = property1.userId
        val expectedProperties = allPropertyModelsNotDeleted.filter { it.user.id == userId }

        val result = propertyRepository.getPropertiesByUserId(userId).first()

        assertEquals(expectedProperties.size, result.size)
        expectedProperties.forEach { expected ->
            val actual = result.find { it.id == expected.id }
            assertNotNull(actual)
            assertEquals(expected.user.id, actual?.user?.id)
        }
    }

    @Test
    fun searchProperties_withMinSurfaceAndMaxPrice_returnsMatchingProperties() = runTest {
        // --- Arrange ---
        val minSurface = 60
        val maxPrice = 500000

        val expected = allPropertyModelsNotDeleted.filter {
            it.surface >= minSurface && it.price <= maxPrice
        }

        // --- Act ---
        val result = propertyRepository.searchProperties(
            minSurface = minSurface,
            maxSurface = null,
            minPrice = null,
            maxPrice = maxPrice,
            type = null,
            isSold = null
        ).first()

        // --- Assert ---
        assertEquals(expected.size, result.size)
        expected.forEach { expectedProperty ->
            val actual = result.find { it.id == expectedProperty.id }
            assertNotNull(actual)
            assertEquals(expectedProperty.surface, actual?.surface)
            assertEquals(expectedProperty.price, actual?.price)
        }
    }

    @Test
    fun searchProperties_withTypeFilter_returnsCorrectType() = runTest {
        // --- Arrange ---
        val type = "Appartement"
        val expected = allPropertyModelsNotDeleted.filter { it.type == type }

        // --- Act ---
        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = type,
            isSold = null
        ).first()

        // --- Assert ---
        assertEquals(expected.size, result.size)
        result.forEach {
            assertEquals(type, it.type)
        }
    }

    @Test
    fun searchProperties_withIsSoldTrue_returnsOnlySoldProperties() = runTest {
        val expected = allPropertyModelsNotDeleted.filter { it.isSold }

        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = true
        ).first()

        assertTrue(result.all { it.isSold })
        assertEquals(expected.map { it.id }.toSet(), result.map { it.id }.toSet())
    }


    @Test
    fun insertProperty_shouldInsertNewPropertyInDao() = runTest {
        // --- Arrange ---
        val newPropertyModel = Property(
            id = 888L,
            title = "Test Duplex",
            type = "Apartment",
            price = 420_000,
            surface = 75,
            rooms = 4,
            description = "Spacious duplex for testing insert.",
            address = "88 Rue de Test, 75000 Paris",
            isSold = false,
            entryDate = LocalDate.parse("2025-09-01"),
            saleDate = null,
            user = FakeUserModel.user1,
            staticMapPath = null,
            photos = emptyList(), // not handled by insert
            poiS = emptyList(),   // not handled by insert
            isSynced = false,
            isDeleted = false,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        val insertedId = propertyRepository.insertProperty(newPropertyModel)

        // --- Assert ---
        val insertedEntity = fakePropertyDao.entityMap[insertedId]
        assertNotNull(insertedEntity)
        assertEquals(newPropertyModel.title, insertedEntity?.title)
        assertEquals(newPropertyModel.type, insertedEntity?.type)
        assertEquals(newPropertyModel.price, insertedEntity?.price)
        assertEquals(newPropertyModel.surface, insertedEntity?.surface)
        assertEquals(newPropertyModel.rooms, insertedEntity?.rooms)
        assertEquals(newPropertyModel.description, insertedEntity?.description)
        assertEquals(newPropertyModel.address, insertedEntity?.address)
        assertEquals(newPropertyModel.isSold, insertedEntity?.isSold)
        assertEquals(newPropertyModel.entryDate.toString(), insertedEntity?.entryDate)
        assertEquals(newPropertyModel.saleDate?.toString(), insertedEntity?.saleDate)
        assertEquals(newPropertyModel.user.id, insertedEntity?.userId)
        assertEquals(newPropertyModel.staticMapPath, insertedEntity?.staticMapPath)
        assertEquals(newPropertyModel.isSynced, insertedEntity?.isSynced)
        assertEquals(newPropertyModel.isDeleted, insertedEntity?.isDeleted)
        assertEquals(newPropertyModel.updatedAt, insertedEntity?.updatedAt)
    }

    @Test
    fun updateProperty_shouldModifyExistingProperty() = runTest {
        // --- Arrange ---
        val existing = FakePropertyModel.property1

        val updated = existing.copy(
            title = "Updated Title",
            surface = existing.surface + 20,
            price = existing.price + 100_000,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        propertyRepository.updateProperty(updated)

        // --- Assert ---
        val entity = fakePropertyDao.entityMap[updated.id]
        assertNotNull(entity)
        assertEquals(updated.title, entity?.title)
        assertEquals(updated.surface, entity?.surface)
        assertEquals(updated.price, entity?.price)
        assertEquals(updated.updatedAt, entity?.updatedAt)
    }

    @Test
    fun updateProperty_onNonExistingProperty_shouldInsertIt() = runTest {
        // --- Arrange ---
        val ghostProperty = Property(
            id = 99999L,
            title = "Ghost Property",
            type = "Cave",
            price = 999_000,
            surface = 60,
            rooms = 2,
            description = "A mysterious cave property.",
            address = "99 Rue Souterraine, Paris",
            isSold = false,
            entryDate = LocalDate.parse("2025-10-01"),
            saleDate = null,
            user = FakeUserModel.user2,
            staticMapPath = null,
            isSynced = false,
            isDeleted = false,
            photos = emptyList(),
            poiS = emptyList(),
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        propertyRepository.updateProperty(ghostProperty)

        // --- Assert ---
        val inserted = fakePropertyDao.entityMap[ghostProperty.id]
        assertNotNull(inserted)
        assertEquals(ghostProperty.title, inserted?.title)

        val result = propertyRepository.getPropertyById(ghostProperty.id).first()
        assertNotNull(result)
        assertEquals(ghostProperty.title, result?.title)
    }

    @Test
    fun markPropertyAsDeleted_shouldHidePropertyFromQueries() = runTest {

        // --- Act ---
        propertyRepository.markPropertyAsDeleted(propertyModel2)

        // --- DAO-level: still present but flagged as deleted ---
        val rawEntity = fakePropertyDao.entityMap[propertyModel2.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // --- Repository-level: should no longer appear in visible results ---
        val result = propertyRepository.getAllPropertiesByDate().first()
        assertFalse(result.any { it.id == propertyModel2.id })
    }

    @Test
    fun markPropertyAsDeleted_calledTwice_staysDeleted() = runTest {
        // --- Arrange ---
        val property = FakePropertyModel.property2

        // --- Act ---
        propertyRepository.markPropertyAsDeleted(property)
        propertyRepository.markPropertyAsDeleted(property)

        // --- DAO-level
        val rawEntity = fakePropertyDao.entityMap[property.id]
        assertNotNull(rawEntity)
        assertTrue(rawEntity!!.isDeleted)

        // --- Repository-level
        val result = propertyRepository.getAllPropertiesByDate().first()
        assertFalse(result.any { it.id == property.id })
    }

    @Test
    fun markAllPropertyAsDeleted_shouldHideAllProperties() = runTest {
        // --- Act ---
        propertyRepository.markAllPropertyAsDeleted()

        // --- DAO-level: every property flagged as deleted ---
        fakePropertyDao.entityMap.values.forEach {
            assertTrue(it.isDeleted)
        }

        // --- Repository-level: should return empty visible list ---
        val result = propertyRepository.getAllPropertiesByDate().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getPropertyWithPoiS_whenUnlinked_returnsEmpty() = runTest {
        val propertyId = property1.id
        fakePropertyDao.unlinkAllForProperty(propertyId)

        val result = propertyRepository.getPropertyWithPoiS(propertyId).first()

        assertNotNull(result)
        assertEquals(propertyId, result!!.property.id)
        assertTrue(result.poiS.isEmpty())
    }

    @Test
    fun getPropertyWithPoiS_returnsLinkedPoiS() = runTest {
        val propertyId = property2.id

        val result = propertyRepository.getPropertyWithPoiS(propertyId).first()

        assertNotNull(result)
        assertEquals(propertyId, result!!.property.id)

        val expectedPoiIds = allCrossRefs
            .filter { it.propertyId == propertyId && !it.isDeleted }
            .map { it.poiId }
            .toSet()

        val actualPoiIds = result.poiS.map { it.id }.toSet()

        assertEquals(expectedPoiIds, actualPoiIds)
    }

    @Test
    fun getPropertyEntityById_returnsCorrectEntity() = runTest {
        // --- Arrange ---
        val expected = property1

        // --- Act ---
        val result = propertyRepository.getPropertyEntityById(expected.id).first()

        // --- Assert ---
        assertNotNull(result)
        assertEquals(expected.id, result?.id)
        assertEquals(expected.title, result?.title)
        assertEquals(expected.type, result?.type)
        assertEquals(expected.isDeleted, result?.isDeleted)
        assertEquals(expected.updatedAt, result?.updatedAt)
    }

    @Test
    fun deleteProperty_deletesEntity() = runTest {
        // --- Arrange ---
        val beforeDelete = propertyRepository
            .getPropertyByIdIncludeDeleted(property3.id)
            .first()
        assertNotNull(beforeDelete)

        // --- Act ---
        propertyRepository.deleteProperty(property3)

        // --- Assert ---
        val afterDelete = propertyRepository
            .getPropertyByIdIncludeDeleted(property3.id)
            .first()
        assertNull(afterDelete)
    }

    @Test
    fun clearAllDeleted_removesOnlyDeleted() = runTest {
        // --- Arrange ---
        val deletedProperty = property3
        assertTrue(deletedProperty.isDeleted)

        // --- Confirm it's present before clearing
        val beforeClear = propertyRepository.getPropertyByIdIncludeDeleted(deletedProperty.id).first()
        assertNotNull(beforeClear)

        // --- Act ---
        propertyRepository.clearAllDeleted()

        // --- Assert ---
        val afterClear = propertyRepository.getPropertyByIdIncludeDeleted(deletedProperty.id).first()
        assertNull(afterClear)

        // Ensure other (not deleted) properties are still there
        val stillPresent = propertyRepository.getPropertyEntityById(property1.id).first()
        assertNotNull(stillPresent)
    }

    @Test
    fun uploadUnSyncedPropertiesToFirebase_returnsOnlyUnSynced() = runTest {
        // --- Arrange ---
        val expected = allPropertyEntities.filter { !it.isSynced }
        val synced = allPropertyEntities.filter { it.isSynced }

        // --- Act ---
        val result = propertyRepository.uploadUnSyncedPropertiesToFirebase().first()

        // --- Assert ---
        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun downloadPropertyFromFirebase_savesNewPropertyCorrectly() = runTest {
        // --- Arrange ---
        val firebaseProperty = PropertyOnlineEntity(
            roomId = 888L,
            title = "Firebase Loft",
            type = "Loft",
            price = 450_000,
            surface = 70,
            rooms = 3,
            description = "Synced property from cloud",
            address = "123 Cloud St",
            isSold = false,
            entryDate = "2025-09-20",
            saleDate = null,
            userId = property1.userId,
            staticMapPath = null,
            updatedAt = System.currentTimeMillis()
        )

        // --- Act ---
        propertyRepository.downloadPropertyFromFirebase(firebaseProperty)

        // --- Assert ---
        val result = propertyRepository.getPropertyEntityById(firebaseProperty.roomId).first()

        assertNotNull(result)
        assertEquals(firebaseProperty.roomId, result?.id)
        assertEquals(firebaseProperty.title, result?.title)
        assertEquals(firebaseProperty.type, result?.type)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun downloadPropertyFromFirebase_updatesExisting() = runTest {
        // --- Arrange ---
        val original = property1
        val firebaseProperty = PropertyOnlineEntity(
            roomId = original.id,
            title = "Updated from Firebase",
            type = "Updated Type",
            price = original.price + 50_000,
            surface = original.surface,
            rooms = original.rooms,
            description = "Updated via sync",
            address = original.address,
            isSold = true,
            entryDate = original.entryDate,
            saleDate = "2025-09-29",
            userId = original.userId,
            staticMapPath = original.staticMapPath,
            updatedAt = original.updatedAt + 10_000
        )

        // --- Act ---
        propertyRepository.downloadPropertyFromFirebase(firebaseProperty)

        // --- Assert (DAO level) ---
        val entity = fakePropertyDao.entityMap[original.id]
        assertNotNull(entity)
        assertEquals(firebaseProperty.title, entity?.title)
        assertEquals(firebaseProperty.type, entity?.type)
        assertTrue(entity?.isSynced == true)

        // --- Assert (Repository level) ---
        val result = propertyRepository.getPropertyEntityById(original.id).first()
        assertNotNull(result)
        assertEquals(firebaseProperty.title, result?.title)
        assertEquals(firebaseProperty.type, result?.type)
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun getPropertyByIdIncludeDeleted_returnsDeletedProperty() = runTest {
        // --- Arrange ---
        val deletedProperty = property3
        assertTrue(deletedProperty.isDeleted)

        // --- Act ---
        val result = propertyRepository.getPropertyByIdIncludeDeleted(deletedProperty.id).first()

        // --- Assert ---
        assertNotNull(result)
        assertEquals(deletedProperty.id, result?.id)
        assertTrue(result?.isDeleted == true)
    }

    @Test
    fun getAllPropertyIncludeDeleted_returnsAllIncludingDeleted() = runTest {
        // --- Act ---
        val result = propertyRepository.getAllPropertyIncludeDeleted().first()

        // --- Assert ---
        assertEquals(allPropertyEntities.size, result.size)
        assertTrue(result.any { it.isDeleted })
    }


}
