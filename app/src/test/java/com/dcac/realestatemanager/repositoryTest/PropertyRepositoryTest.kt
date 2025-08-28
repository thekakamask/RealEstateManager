package com.dcac.realestatemanager.repositoryTest

import com.dcac.realestatemanager.data.offlineDatabase.photo.OfflinePhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.OfflinePoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.OfflinePropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.OfflinePropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.OfflineUserRepository
import com.dcac.realestatemanager.fakeData.fakeDao.FakePhotoDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePoiDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakePropertyPoiCrossDao
import com.dcac.realestatemanager.fakeData.fakeDao.FakeUserDao
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyPoiCrossEntity
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
    private lateinit var fakePoiDao: FakePoiDao
    private lateinit var fakePhotoDao: FakePhotoDao
    private lateinit var fakePropertyPoiCrossDao: FakePropertyPoiCrossDao

    private lateinit var userRepository: OfflineUserRepository
    private lateinit var propertyRepository: OfflinePropertyRepository
    private lateinit var poiRepository: OfflinePoiRepository
    private lateinit var photoRepository: OfflinePhotoRepository
    private lateinit var propertyPoiCrossRepository: OfflinePropertyPoiCrossRepository

    @Before
    fun setup() {
        // In-memory Property DAO with local POIs + cross-refs
        fakePropertyDao = FakePropertyDao().apply {
            // 1) Seed POIs snapshot used to build relations
            seedPois(FakePoiEntity.poiEntityList)

            // 2) Build links propertyId -> poiIds from the cross table (propertyId, poiId)
            val mapByProperty = FakePropertyPoiCrossEntity.propertyPoiCrossEntityList
                .groupBy { it.propertyId }                       // propertyId -> List<Cross>
                .mapValues { (_, list) -> list.map { it.poiId } } // -> List<poiId>

            mapByProperty.forEach { (propertyId, poiIds) ->
                linkPropertyToPois(propertyId, *poiIds.toLongArray())
            }
        }

        // User repo backed by FakeUserDao (seeded in its init)
        fakeUserDao = FakeUserDao()
        fakePoiDao = FakePoiDao()
        fakePhotoDao = FakePhotoDao()
        fakePropertyPoiCrossDao = FakePropertyPoiCrossDao()

        userRepository = OfflineUserRepository(fakeUserDao)
        poiRepository = OfflinePoiRepository(fakePoiDao, userRepository)
        photoRepository = OfflinePhotoRepository(fakePhotoDao)
        propertyPoiCrossRepository = OfflinePropertyPoiCrossRepository(fakePropertyPoiCrossDao)

        // Repository under test
        propertyRepository = OfflinePropertyRepository(fakePropertyDao, userRepository, poiRepository, photoRepository, propertyPoiCrossRepository)
    }

    @Test
    fun getAllPropertiesByDate_returnsSortedDesc() = runTest {
        // When
        val result = propertyRepository.getAllPropertiesByDate().first()
        // Then (expected sorted by entryDate desc)
        val expected = FakePropertyModel.propertyModelList.sortedByDescending { it.entryDate }
        assertEquals(expected, result)
    }

    @Test
    fun getAllPropertiesByAlphabetic_returnsSortedByTitle() = runTest {
        val result = propertyRepository.getAllPropertiesByAlphabetic().first()
        val expected = FakePropertyModel.propertyModelList.sortedBy { it.title }
        assertEquals(expected, result)
        println("Expected:\n$expected")
        println("Actual:\n$result")
    }

    @Test
    fun getPropertyById_returnsCorrectProperty() = runTest {
        val id = FakePropertyEntity.property1.id
        val result = propertyRepository.getPropertyById(id).first()
        val expected = FakePropertyModel.property1
        assertEquals(expected, result)
    }

    @Test
    fun searchProperties_filtersCorrectly() = runTest {
        // Example: type = "Apartment", maxPrice = 600_000, isSold = false
        val result = propertyRepository.searchProperties(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = 600_000,
            type = "Apartment",
            isSold = false
        ).first()

        val expected = FakePropertyModel.propertyModelList.filter {
            it.type == "Apartment" && it.price <= 600_000 && !it.isSold
        }
        assertEquals(expected, result)
    }

    @Test
    fun insertProperty_insertsAndIsRetrievable() = runTest {
        // Given a new Property domain model
        val newProp = Property(
            id = 99L,
            title = "New Place",
            type = "Apartment",
            price = 420_000,
            surface = 72,
            rooms = 3,
            description = "Brand new test property.",
            address = "1 Test St, 75000 Paris",
            isSold = false,
            entryDate = LocalDate.parse("2025-08-21"),
            saleDate = null,
            user = FakeUserModel.user1,
            staticMapPath = null
        )

        // When
        val insertedId = propertyRepository.insertProperty(newProp)

        // Then (DAO-level)
        val entity = fakePropertyDao.entityMap[insertedId]
        assertNotNull(entity)
        assertEquals(newProp.title, entity?.title)

        // Then (Model-level)
        val fetched = propertyRepository.getPropertyById(insertedId).first()
        assertEquals(newProp.copy(id = insertedId), fetched)
    }

    @Test
    fun updateProperty_updatesFields() = runTest {
        val updated = FakePropertyModel.property2.copy(title = "Villa Montmartre (Updated)")
        propertyRepository.updateProperty(updated)

        val entity = fakePropertyDao.entityMap[updated.id]
        assertEquals("Villa Montmartre (Updated)", entity?.title)

        val model = propertyRepository.getPropertyById(updated.id).first()
        assertEquals(updated, model)
    }

    @Test
    fun deleteProperty_removesEntity_andUnlinks() = runTest {
        val toDelete = FakePropertyModel.property3

        // Ensure exists
        assertNotNull(fakePropertyDao.entityMap[toDelete.id])

        // When
        propertyRepository.deleteProperty(toDelete)

        // Then (DAO-level)
        assertNull(fakePropertyDao.entityMap[toDelete.id])
        // Cross-refs cleaned for this property
        assertTrue(fakePropertyDao.propertyToPoi.value[toDelete.id].isNullOrEmpty())

        // Then (Model-level)
        val allByDate = propertyRepository.getAllPropertiesByDate().first()
        assertFalse(allByDate.any { it.id == toDelete.id })
    }

    @Test
    fun markPropertyAsSold_setsFlagAndDate() = runTest {
        val propId = FakePropertyEntity.property1.id
        propertyRepository.markPropertyAsSold(propId, saleDate = "2025-08-25")

        val entity = fakePropertyDao.entityMap[propId]
        assertEquals(true, entity?.isSold)
        assertEquals("2025-08-25", entity?.saleDate)
    }

    @Test
    fun getPropertyWithPoiS_property1_hasPoi1_to_5() = runTest {
        val propId = FakePropertyEntity.property1.id
        val result = propertyRepository.getPropertyWithPoiS(propId).first()

        // property1 linked to poi1..poi5 (see FakePropertyPoiCrossEntity)
        val expectedPoiIds = setOf(
            FakePoiEntity.poi1.id, FakePoiEntity.poi2.id, FakePoiEntity.poi3.id,
            FakePoiEntity.poi4.id, FakePoiEntity.poi5.id
        )
        val actualPoiIds = result.poiS.map { it.id }.toSet()

        assertEquals(propId, result.property.id)
        assertEquals(expectedPoiIds, actualPoiIds)

        // Property.user must be resolved via OfflinePropertyRepository (combine with users)
        assertNotNull(result.property.user)
        val backing = FakePropertyEntity.propertyEntityList.first { it.id == propId }
        assertEquals(backing.userId, result.property.user.id)
    }

    @Test
    fun getPropertyWithPoiS_property4_hasPoi16_to_20() = runTest {
        val propId = FakePropertyEntity.property4.id
        val result = propertyRepository.getPropertyWithPoiS(propId).first()

        val expectedPoiIds = setOf(
            FakePoiEntity.poi16.id, FakePoiEntity.poi17.id, FakePoiEntity.poi18.id,
            FakePoiEntity.poi19.id, FakePoiEntity.poi20.id
        )
        val actualPoiIds = result.poiS.map { it.id }.toSet()

        assertEquals(propId, result.property.id)
        assertEquals(expectedPoiIds, actualPoiIds)
        assertNotNull(result.property.user)
        val backing = FakePropertyEntity.propertyEntityList.first { it.id == propId }
        assertEquals(backing.userId, result.property.user.id)
    }

    @Test
    fun clearAll_removesEveryProperty_andLinks() = runTest {
        propertyRepository.clearAll()

        // DAO-level
        assertTrue(fakePropertyDao.entityMap.isEmpty())
        assertTrue(fakePropertyDao.propertyToPoi.value.isEmpty())

        // Model-level
        val all = propertyRepository.getAllPropertiesByDate().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getUnSyncedProperties_returnsOnlyUnSynced() = runTest {
        // Expected: only properties where isSynced == false
        val expected = FakePropertyModel.propertyModelList.filter { !it.isSynced }
        val synced = FakePropertyModel.propertyModelList.filter { it.isSynced }

        // When
        val result = propertyRepository.getUnSyncedProperties().first()

        // Then
        assertTrue(result.none { synced.contains(it) })
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }
}