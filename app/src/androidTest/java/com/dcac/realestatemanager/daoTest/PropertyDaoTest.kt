package com.dcac.realestatemanager.daoTest

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dcac.realestatemanager.daoTest.fakeData.DatabaseSetup
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePoiEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakePropertyPoiCrossEntity
import com.dcac.realestatemanager.daoTest.fakeData.fakeEntities.FakeUserEntity
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropertyDaoTest: DatabaseSetup() {

    private lateinit var propertyDao: PropertyDao

    private val property1 = FakePropertyEntity.property1
    private val property2 = FakePropertyEntity.property2
    private val property3 = FakePropertyEntity.property3
    private val property4 = FakePropertyEntity.property4
    private val property5 = FakePropertyEntity.property5
    private val crossRef1 = FakePropertyPoiCrossEntity.propertyPoiCross1
    private val allPropertiesNotDeleted = FakePropertyEntity.propertyEntityListNotDeleted
    private val allProperties = FakePropertyEntity.propertyEntityList


    @Before
    fun setup() = runBlocking {

        FakeUserEntity.userEntityList.forEach {
            db.userDao().firstUserInsertForceSyncedTrue(it)
        }
        FakePoiEntity.poiEntityList.forEach {
            db.poiDao().insertPoiInsertFromUi(it)
        }

        propertyDao = db.propertyDao()
    }

    @Test
    fun getAllPropertiesByDate_shouldReturnAllPropertiesNotDeleted() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getAllPropertiesByDate().first()

        val expectedIds = allPropertiesNotDeleted
            .sortedByDescending { it.entryDate }
            .map { it.id }

        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getAllPropertiesByAlphabetic_shouldReturnAllPropertiesNotDeleted() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getAllPropertiesByAlphabetic().first()

        val expectedIds = allPropertiesNotDeleted
            .sortedBy { it.title }
            .map { it.id }

        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getAllPropertiesIncludeDeleted_shouldReturnAll() = runBlocking {
        propertyDao.insertAllPropertiesNotExistingFromFirebase(allProperties)

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertEquals(allProperties.size, result.size)

    }

    @Test
    fun getPropertyById_shouldReturnCorrectProperty() = runBlocking {
        propertyDao.insertPropertyFromUi(property1)

        val result = propertyDao.getPropertyById(property1.id).first()

        assertEquals(property1, result)
    }

    @Test
    fun getPropertyById_shouldNotReturnDeleteProperty() = runBlocking {
        propertyDao.insertPropertyFromUi(property3)

        val result = propertyDao.getPropertyById(property3.id).first()

        assertNull(result)
    }

    @Test
    fun getPropertyByIdIncludeDeleted_shouldReturnPropertyIncludeDeleted() = runBlocking {
        propertyDao.insertPropertyFromFirebase(property3)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property3.id).first()

        assertEquals(property3.id, result?.id)
    }

    @Test
    fun getPropertyByUserIdDate_shouldReturnCorrectProperty() = runBlocking {
        val user1 = FakeUserEntity.user1

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getPropertyByUserIdDate(user1.id).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1.id }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getPropertyByUserIdDate_shouldNotReturnDeletedProperty() = runBlocking {
        val user2 = FakeUserEntity.user2

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getPropertyByUserIdDate(user2.id).first()

        assertTrue(result.none { it.isDeleted })
        assertTrue(result.all { it.universalLocalUserId == user2.id })
    }

    @Test
    fun getPropertyByUserIdAlphabetic_shouldReturnCorrectProperty() = runBlocking {
        val user1 = FakeUserEntity.user1

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getPropertyByUserIdAlphabetic(user1.id).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1.id }
            .sortedBy { it.title }
            .map { it.id }

        val resultIds = result.map { it.id }

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun getPropertyByUserIdAlphabetic_shouldNotReturnDeletedProperty() = runBlocking {
        val user2 = FakeUserEntity.user2

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.getPropertyByUserIdAlphabetic(user2.id).first()

        assertTrue(result.none { it.isDeleted })
        assertTrue(result.all { it.universalLocalUserId == user2.id })
    }

    @Test
    fun searchPropertiesByDate_withoutFilters_shouldReturnAllNotDeletedSortedByDate() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByDate(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchPropertiesByDate_shouldFilterByMinSurface() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByDate(
            minSurface = 100,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.surface >= 100 }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchPropertiesByDate_shouldFilterByType() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByDate(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = "Loft",
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.type == "Loft" }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchPropertiesByDate_shouldFilterByIsSold() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByDate(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = true
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.isSold }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchPropertiesByDate_shouldFilterByMultipleCriteria() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByDate(
            minSurface = 80,
            maxSurface = 150,
            minPrice = 200_000,
            maxPrice = 500_000,
            type = "Apartment",
            isSold = false
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.surface in 80..150 &&
                        it.price >= 200_000 &&
                        it.price <= 500_000 &&
                        it.type == "Apartment" &&
                        !it.isSold
            }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchPropertiesByAlphabetic_withoutFilters_shouldReturnAllNotDeletedSortedByAlphabetic() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByAlphabetic(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchPropertiesByAlphabetic_shouldFilterByMinSurface() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByAlphabetic(
            minSurface = 100,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.surface >= 100 }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchPropertiesByAlphabetic_shouldFilterByType() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByAlphabetic(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = "Loft",
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.type == "Loft" }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchPropertiesByAlphabetic_shouldFilterByIsSold() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByAlphabetic(
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = true
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.isSold }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchPropertiesByAlphabetic_shouldFilterByMultipleCriteria() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchPropertiesByAlphabetic(
            minSurface = 80,
            maxSurface = 150,
            minPrice = 200_000,
            maxPrice = 500_000,
            type = "Apartment",
            isSold = false
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.surface in 80..150 &&
                        it.price >= 200_000 &&
                        it.price <= 500_000 &&
                        it.type == "Apartment" &&
                        !it.isSold
            }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByDate_withoutFilters_shouldReturnUserPropertiesSortedByDate() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1 }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByDate_shouldFilterByMinSurface() = runBlocking {
        val user2 = FakeUserEntity.user2.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user2,
            minSurface = 100,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.universalLocalUserId == user2 &&
                        it.surface >= 100
            }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByDate_shouldFilterByIsSold() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = true
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.universalLocalUserId == user1 &&
                        it.isSold
            }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByDate_shouldFilterByMultipleCriteria() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user1,
            minSurface = 80,
            maxSurface = 150,
            minPrice = 200_000,
            maxPrice = 600_000,
            type = "Apartment",
            isSold = false
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.universalLocalUserId == user1 &&
                        it.surface >= 80 &&
                        it.surface <= 150 &&
                        it.price >= 200_000 &&
                        it.price <= 600_000 &&
                        it.type == "Apartment" &&
                        !it.isSold
            }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByDate_shouldFilterByType() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = "Apartment",
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1 && it.type == "Apartment" }
            .sortedByDescending { it.entryDate }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }


    @Test
    fun searchUserPropertiesByDate_shouldNotReturnOtherUsersProperties() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByDate(
            userId = user1,
            null, null, null, null, null, null
        ).first()

        assertTrue(result.all { it.universalLocalUserId == user1 })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_withoutFilters_shouldReturnUserPropertiesSortedByTitle() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1 }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_shouldFilterByMinSurface() = runBlocking {
        val user2 = FakeUserEntity.user2.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user2,
            minSurface = 100,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user2 && it.surface >= 100 }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_shouldFilterByType() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)


        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = "Apartment",
            isSold = null
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1 && it.type == "Apartment" }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_shouldFilterByIsSold() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user1,
            minSurface = null,
            maxSurface = null,
            minPrice = null,
            maxPrice = null,
            type = null,
            isSold = true
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter { it.universalLocalUserId == user1 && it.isSold }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_shouldFilterByMultipleCriteria() = runBlocking {
        val user1 = FakeUserEntity.user1.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user1,
            minSurface = 80,
            maxSurface = 150,
            minPrice = 200_000,
            maxPrice = 600_000,
            type = "Apartment",
            isSold = false
        ).first()

        val expectedIds = allPropertiesNotDeleted
            .filter {
                it.universalLocalUserId == user1 &&
                        it.surface >= 80 &&
                        it.surface <= 150 &&
                        it.price >= 200_000 &&
                        it.price <= 600_000 &&
                        it.type == "Apartment" &&
                        !it.isSold
            }
            .sortedBy { it.title }
            .map { it.id }

        assertEquals(expectedIds, result.map { it.id })
    }

    @Test
    fun searchUserPropertiesByAlphabetic_shouldNotReturnOtherUsersProperties() = runBlocking {
        val user2 = FakeUserEntity.user2.id

        propertyDao.insertPropertiesFromUi(allProperties)

        val result = propertyDao.searchUserPropertiesByAlphabetic(
            userId = user2,
            null, null, null, null, null, null
        ).first()

        assertTrue(result.all { it.universalLocalUserId == user2 })
    }


    @Test
    fun markPropertyAsSold_shouldUpdateIsSoldSaleDateAndUpdatedAt() = runBlocking {
        propertyDao.insertPropertyFromUi(property2)

        val newSaleDate = "2025-09-01"
        val newUpdatedAt = System.currentTimeMillis()

        propertyDao.markPropertyAsSold(
            property2.id,
            saleDate = newSaleDate,
            updatedAt = newUpdatedAt
        )

        val result = propertyDao.getPropertyByIdIncludeDeleted(property2.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSold)
        assertEquals(newSaleDate, result.saleDate)
        assertEquals(newUpdatedAt, result.updatedAt)
    }

    @Test
    fun uploadUnSyncedProperties_shouldReturnOnlyPropertiesWithIsSyncedFalse() = runBlocking{
        propertyDao.insertPropertyFromUi(property1)
        propertyDao.insertPropertyFromFirebase(property2)

        val result = propertyDao.uploadUnSyncedProperties().first()

        assertEquals(1, result.size)
        assertTrue(result.all { !it.isSynced })
        assertEquals(property1.id, result.first().id)
    }

    @Test
    fun insertPropertyInsertFromUI_shouldInsertWithIsSyncedFalse() = runBlocking {
        propertyDao.insertPropertyFromUi(property2)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
    }

    @Test
    fun insertPropertiesFromUI_shouldInsertAllWithIsSyncedFalse() = runBlocking {
        allProperties.forEach {
            propertyDao.insertPropertyFromUi(it)
        }

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertEquals(allProperties.size, result.size)
        assertTrue(result.all { !it.isSynced })
    }

    @Test
    fun insertPropertyFromFirebase_shouldInsertWithIsSyncedTrue() = runBlocking {
        propertyDao.insertPropertyFromFirebase(property1)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
    }

    @Test
    fun insertAllPropertiesNotExistingFromFirebase_shouldInsertAllWithIsSyncedTrue() = runBlocking {
        propertyDao.insertAllPropertiesNotExistingFromFirebase(allProperties)

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertEquals(allProperties.size, result.size)
        assertTrue(result.all {it.isSynced })
    }

    @Test
    fun updatePropertyFromUIForceSyncFalse_shouldSetIsSyncedFalse() = runBlocking {
        propertyDao.insertPropertyFromFirebase(property2)

        val updated = property2.copy(
            title = "Updated title",
            description = "Updated description",
            updatedAt = System.currentTimeMillis()
        )

        propertyDao.updatePropertyFromUIForceSyncFalse(updated)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property2.id).first()

        assertNotNull(result)
        assertFalse(result!!.isSynced)
        assertEquals(updated.title, result.title)
        assertEquals(updated.description, result.description)
        assertEquals(updated.updatedAt, result.updatedAt)
    }

    @Test
    fun updatePropertyFromFirebaseForceSyncTrue_shouldSetIsSyncedTrue() = runBlocking {
        propertyDao.insertPropertyFromUi(property1)

        val updated = property1.copy(
            title = "Updated title",
            description = "From Firebase",
            updatedAt = System.currentTimeMillis()
        )

        propertyDao.updatePropertyFromFirebaseForcesSyncTrue(updated)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property1.id).first()

        assertNotNull(result)
        assertTrue(result!!.isSynced)
        assertEquals(updated.title, result.title)
        assertEquals(updated.description, result.description)
        assertEquals(updated.updatedAt, result.updatedAt)
    }

    @Test
    fun updateAllPropertiesFromFirebaseForceSyncTrue_shouldUpdateAllAndSetIsSyncedTrue() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val updatedList = allProperties.map {
            it.copy(
                title = it.title + "Updated",
                updatedAt = System.currentTimeMillis()
            )
        }

        propertyDao.updateAllPropertiesFromFirebaseForceSyncTrue(updatedList)

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertEquals(updatedList.size, result.size)
        assertTrue(result.all { it.isSynced })

        val resultIds = result.map { it.title }.sorted()
        val expectedIds = updatedList.map { it.title }.sorted()

        assertEquals(expectedIds, resultIds)
    }

    @Test
    fun markAllPropertiesAsDeleted_shouldHideFromQueries() = runBlocking {
        propertyDao.insertAllPropertiesNotExistingFromFirebase(allProperties)

        val updatedAt = System.currentTimeMillis()

        propertyDao.markAllPropertiesAsDeleted(updatedAt)

        val uiResult = propertyDao.getAllPropertiesByDate().first()
        assertTrue(uiResult.isEmpty())

        val dbResult = propertyDao.getAllPropertiesIncludeDeleted().first()
        assertEquals(allProperties.size, dbResult.size)
        assertTrue(dbResult.all { it.isDeleted })
        assertTrue(dbResult.all { !it.isSynced })
        assertTrue(dbResult.all { it.updatedAt == updatedAt })
    }

    @Test
    fun deleteProperty_shouldRemoveProperty() = runBlocking {
        propertyDao.insertPropertyFromUi(property3)

        val inserted = propertyDao.getPropertyByIdIncludeDeleted(property3.id).first()
        propertyDao.deleteProperty(inserted!!)

        val result = propertyDao.getPropertyByIdIncludeDeleted(property3.id).first()

        assertNull(result)

    }

    @Test
    fun clearAllPropertiesDeleted_shouldRemoveOnlyDeletedProperties() = runBlocking {
        propertyDao.insertAllPropertiesNotExistingFromFirebase(allProperties)

        propertyDao.markPropertyAsDeleted(property1.id, System.currentTimeMillis())
        propertyDao.markPropertyAsDeleted(property2.id, System.currentTimeMillis())
        propertyDao.markPropertyAsDeleted(property4.id, System.currentTimeMillis())
        propertyDao.markPropertyAsDeleted(property5.id, System.currentTimeMillis())

        propertyDao.clearAllDeleted()

        val result = propertyDao.getAllPropertiesIncludeDeleted().first()

        assertEquals(0, result.size)
    }

    @Test
    fun getPropertyWithPoiS_shouldReturnPropertyWithLinkedPoiS() = runBlocking {
        propertyDao.insertPropertyFromUi(property1)

        db.propertyCrossDao().insertCrossRefInsertFromUI(crossRef1)

        val relation = propertyDao.getPropertyWithPoiS(property1.id).first()

        assertEquals(property1.id, relation.property.id)
        assertTrue(relation.poiS.isNotEmpty())
        assertTrue(relation.poiS.any { it.id == crossRef1.universalLocalPoiId })
    }

    @Test
    fun getAllPropertiesAsCursor_shouldReturnValidCursor() = runBlocking {
        propertyDao.insertPropertiesFromUi(allProperties)

        val query = SimpleSQLiteQuery("SELECT * FROM properties")
        val cursor = propertyDao.getAllPropertiesAsCursor(query)
        assertNotNull(cursor)
        assertTrue(cursor.count > 0)
        cursor.close()
    }
}
