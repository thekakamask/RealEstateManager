package com.dcac.realestatemanager.data.offlineDatabase.property

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toFullModel
import kotlinx.coroutines.flow.Flow
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.combine

class OfflinePropertyRepository(
    private val propertyDao: PropertyDao,
    private val userRepository: UserRepository,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository,
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository
): PropertyRepository {

    // FOR UI

    private fun combinePropertiesWithDetails(
        propertiesFlow: Flow<List<PropertyEntity>>
    ): Flow<List<Property>> {
        val photosFlow = photoRepository.getAllPhotos()
        val crossRefsFlow = propertyPoiCrossRepository.getAllCrossRefs()
        val poiSFlow = poiRepository.getAllPoiS()
        val usersFlow = userRepository.getAllUsers()

        return combine(propertiesFlow, usersFlow, photosFlow, crossRefsFlow, poiSFlow) {
                properties, users, photos, crossRefs, poiS ->
            properties.map { property ->
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

    override fun getPropertyById(id: Long): Flow<Property?> {
        val propertyFlow = propertyDao.getPropertyById(id)
        val photosFlow = photoRepository.getPhotosByPropertyId(id)
        val poiRelationFlow = getPropertyWithPoiS(id)
        val usersFlow = userRepository.getAllUsers()

        return combine(propertyFlow, photosFlow, poiRelationFlow, usersFlow) { propertyEntity, photos, propertyWithPoiS, users ->
            propertyEntity?.toModel(
                user = users.first { it.id == propertyEntity.userId },
                photos = photos,
                poiS = propertyWithPoiS.poiS
            )
        }
    }

    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ): Flow<List<Property>> {
        val propertiesFlow = propertyDao.searchProperties(
            minSurface, maxSurface, minPrice, maxPrice, type, isSold
        )
        val photosFlow = photoRepository.getAllPhotos()
        val crossRefsFlow = propertyPoiCrossRepository.getAllCrossRefs()
        val poiSFlow = poiRepository.getAllPoiS()
        val usersFlow = userRepository.getAllUsers()

        return combine(propertiesFlow, photosFlow, crossRefsFlow, poiSFlow, usersFlow) { properties, photos, crossRefs, poiS, users ->
            properties.map { property ->
                property.toFullModel(
                    allUsers = users,
                    photos = photos,
                    crossRefs = crossRefs,
                    allPoiS = poiS
                )
            }
        }
    }

    override suspend fun insertProperty(property: Property): Long
    = propertyDao.insertProperty(property.toEntity())

    override suspend fun updateProperty(property: Property)
    = propertyDao.updateProperty(property.toEntity())

    override suspend fun markPropertyAsDeleted(property: Property) =
        propertyDao.markPropertyAsDeleted(property.id, System.currentTimeMillis())

    override suspend fun markPropertyAsSold(propertyId: Long, saleDate: String)
    = propertyDao.markPropertyAsSold(propertyId, saleDate)

    override suspend fun markAllPropertyAsDeleted()
    = propertyDao.markAllPropertiesAsDeleted(System.currentTimeMillis())

    override fun getPropertyWithPoiS(id: Long): Flow<PropertyWithPoiS> {
        val usersFlow = userRepository.getAllUsers()
        val propertyWithPoisFlow = propertyDao.getPropertyWithPoiS(id)

        return combine(propertyWithPoisFlow, usersFlow) { relation, users ->
            relation.toModel(allUsers = users)
        }
    }

    //FOR FIREBASE SYNC

    override fun getPropertyEntityById(id: Long): Flow<PropertyEntity?> =
        propertyDao.getPropertyById(id)

    override suspend fun deleteProperty(property: PropertyEntity)
            = propertyDao.deleteProperty(property)

    override suspend fun clearAll()
            = propertyDao.clearAll()

    override fun uploadUnSyncedPropertiesToFirebase(): Flow<List<PropertyEntity>>
    = propertyDao.uploadUnSyncedPropertiesToFirebase()

    override suspend fun downloadPropertyFromFirebase(property: PropertyOnlineEntity)
    = propertyDao.savePropertyFromFirebase(property.toEntity(propertyId = property.roomId))

}