package com.dcac.realestatemanager.data.offlineDatabase.property

import android.util.Log
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toFullModel
import kotlinx.coroutines.flow.Flow
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class OfflinePropertyRepository(
    private val propertyDao: PropertyDao,
    private val userRepository: UserRepository,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository,
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository
): PropertyRepository {

    //TODO PROBLEM WHEN RELOADING THE UI (IF A PROPERTY IS MODIFIED)
    // I RELOAD ALL PROPERTIES.
    // THIS IS NOT EFFICIENT IF THERE ARE MANY PROPERTIES.
    private fun combinePropertiesWithDetails(
        propertiesFlow: Flow<List<PropertyEntity>>
    ): Flow<List<Property>> {
        val photosFlow = photoRepository.getAllPhotos()
        val crossRefsFlow = propertyPoiCrossRepository.getAllCrossRefs()
        val poiSFlow = poiRepository.getAllPoiS()
        val usersFlow = userRepository.getAllUsers()

        return combine(propertiesFlow, usersFlow, photosFlow, crossRefsFlow, poiSFlow) {
                properties, users, photos, crossRefs, poiS ->
            properties.mapNotNull { property ->
                property.toFullModel(
                    allUsers = users,
                    photos = photos,
                    crossRefs = crossRefs,
                    allPoiS = poiS
                )
            }
        }
    }
    override fun getAllPropertiesByDate(): Flow<List<Property>> =
        combinePropertiesWithDetails(propertyDao.getAllPropertiesByDate())
    override fun getAllPropertiesByAlphabetic(): Flow<List<Property>> =
        combinePropertiesWithDetails(propertyDao.getAllPropertiesByAlphabetic())
    override fun getPropertyById(id: String): Flow<Property?> =
        propertyDao.getPropertyById(id).map { it?.toModel() }
    override fun getPropertiesByUserIdAlphabetic(userId: String): Flow<List<Property>> =
        propertyDao.getPropertyByUserIdAlphabetic(userId)
            .map { list ->
                list.map { it.toModel() }
            }
    override fun getFullPropertiesByUserIdAlphabetic(userId: String): Flow<List<Property>> {
        return combinePropertiesWithDetails(propertyDao.getPropertyByUserIdAlphabetic(userId))
    }
    override fun getPropertiesByUserIdDate(userId: String): Flow<List<Property>> =
        propertyDao.getPropertyByUserIdDate(userId)
            .map { list ->
                list.map { it.toModel() }
            }
    override fun getFullPropertiesByUserIdDate(userId: String): Flow<List<Property>> {
        return combinePropertiesWithDetails(propertyDao.getPropertyByUserIdDate(userId))
    }
    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?,
        sortOrder: PropertySortOrder
    ): Flow<List<Property>> {
        val baseFlow = when (sortOrder) {
            PropertySortOrder.DATE -> propertyDao.searchPropertiesByDate(
                minSurface, maxSurface, minPrice, maxPrice, type, isSold
            )
            PropertySortOrder.ALPHABETIC -> propertyDao.searchPropertiesByAlphabetic(
                minSurface, maxSurface, minPrice, maxPrice, type, isSold
            )
        }

        return combinePropertiesWithDetails(baseFlow)
    }

    override fun searchUserProperties(
        userId: String,
        filters: PropertyFilters
    ): Flow<List<Property>> {
        val baseFlow = when (filters.sortOrder) {
            PropertySortOrder.ALPHABETIC -> propertyDao.searchUserPropertiesByAlphabetic(
                userId,
                filters.minSurface,
                filters.maxSurface,
                filters.minPrice,
                filters.maxPrice,
                filters.selectedType,
                filters.isSold
            )
            PropertySortOrder.DATE -> propertyDao.searchUserPropertiesByDate(
                userId,
                filters.minSurface,
                filters.maxSurface,
                filters.minPrice,
                filters.maxPrice,
                filters.selectedType,
                filters.isSold
            )
        }
        return combinePropertiesWithDetails(baseFlow)
    }
    override suspend fun markPropertyAsSold(propertyId: String, saleDate: String, updatedAt: Long)
            = propertyDao.markPropertyAsSold(propertyId, saleDate, updatedAt)


    //SYNC
    override fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>> =
        propertyDao.uploadUnSyncedProperties()

    // INSERTIONS
    override suspend fun insertPropertyFromUI(property: Property) {
        Log.d("INSERT", "insertPropertyFromUI called for ID=${property.universalLocalId}")
        propertyDao.insertPropertyFromUi(property.toEntity())
    }
    override suspend fun insertPropertiesFromUI(properties: List<Property>) {
        propertyDao.insertPropertiesFromUi(properties.map { it.toEntity() })
    }
    //INSERTIONS FROM FIREBASE
    override suspend fun insertPropertyInsertFromFirebase(property: PropertyOnlineEntity, firebaseDocumentId: String) {
        propertyDao.insertPropertyFromFirebase(property.toEntity(firestoreId = firebaseDocumentId))
    }
    override suspend fun insertPropertiesInsertFromFirebase(properties: List<Pair<PropertyOnlineEntity, String>>) {
        val entities = properties.map {(property, firebaseDocumentId) ->
            property.toEntity(firestoreId = firebaseDocumentId)
        }
        propertyDao.insertAllPropertiesNotExistingFromFirebase(entities)
    }

    //UPDATES
    override suspend fun updatePropertyFromUI(property: Property) {
        propertyDao.updatePropertyFromUIForceSyncFalse(property.toEntity())
    }
    override suspend fun updatePropertyFromFirebase(property: PropertyOnlineEntity, firebaseDocumentId: String) {
        propertyDao.updatePropertyFromFirebaseForcesSyncTrue(property.toEntity(firestoreId = firebaseDocumentId))
    }

    override suspend fun updateAllPropertiesFromFirebase(properties: List<Pair<PropertyOnlineEntity, String>>) {
        val entities = properties.map { (property, firebaseDocumentId) ->
            property.toEntity(firestoreId = firebaseDocumentId)
            }
        propertyDao.updateAllPropertiesFromFirebaseForceSyncTrue(entities)
    }

    //SOFT DELETE
    override suspend fun markPropertyAsDeleted(property: Property) {
        propertyDao.markPropertyAsDeleted(property.universalLocalId, System.currentTimeMillis())
    }
    override suspend fun markAllPropertiesAsDeleted() {
        propertyDao.markAllPropertiesAsDeleted(System.currentTimeMillis())
    }

    //HARD DELETE
    override suspend fun deleteProperty(property: PropertyEntity) {
        propertyDao.deleteProperty(property)
    }
    override suspend fun clearAllDeleted() {
       propertyDao.clearAllDeleted()
    }


    //FOR TEST HARD DELETE
    override fun getPropertyByIdIncludeDeleted(id: String): Flow<PropertyEntity?> =
        propertyDao.getPropertyByIdIncludeDeleted(id)
    override fun getAllPropertyIncludeDeleted(): Flow<List<PropertyEntity>> =
        propertyDao.getAllPropertiesIncludeDeleted()

    override fun getPropertyWithPoiS(propertyId: String): Flow<PropertyWithPoiS> {
        return propertyDao.getPropertyWithPoiS(propertyId).map { relation ->
            relation.toModel()
        }
    }
}