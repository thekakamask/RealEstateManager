package com.dcac.realestatemanager.data.offlineDatabase.property

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.model.PropertyWithPoiS
import com.dcac.realestatemanager.utils.toEntity
import com.dcac.realestatemanager.utils.toFullModel
import kotlinx.coroutines.flow.Flow
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class OfflinePropertyRepository(
    private val propertyDao: PropertyDao,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository,
    private val propertyPoiCrossRepository: PropertyPoiCrossRepository
): PropertyRepository {

    private fun combinePropertiesWithDetails(
        propertiesFlow: Flow<List<PropertyEntity>>
    ): Flow<List<Property>> {
        val photosFlow = photoRepository.getAllPhotos()
        val crossRefsFlow = propertyPoiCrossRepository.getAllCrossRefs()
        val poiSFlow = poiRepository.getAllPoiS()

        return combine(propertiesFlow, photosFlow, crossRefsFlow, poiSFlow) { properties, photos, crossRefs, poiS ->
            properties.map { property ->
                property.toFullModel(photos, crossRefs, poiS)
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

        return combine(propertyFlow, photosFlow, poiRelationFlow) { propertyEntity, photos, propertyWithPoiS ->
            propertyEntity?.toModel(
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

        return combine(propertiesFlow, photosFlow, crossRefsFlow, poiSFlow) { properties, photos, crossRefs, poiS ->
            properties.map { property ->
                property.toFullModel(photos, crossRefs, poiS)
            }
        }
    }

    override suspend fun insertProperty(property: Property): Long = propertyDao.insertProperty(property.toEntity())
    override suspend fun updateProperty(property: Property) = propertyDao.updateProperty(property.toEntity())
    override suspend fun deleteProperty(property: Property) = propertyDao.deleteProperty(property.toEntity())
    override suspend fun markPropertyAsSold(propertyId: Long, saleDate: String) = propertyDao.markPropertyAsSold(propertyId, saleDate)
    override suspend fun clearAll() = propertyDao.clearAll()

    override fun getPropertyWithPoiS(id: Long): Flow<PropertyWithPoiS> =
        propertyDao.getPropertyWithPoiS(id).map { it.toModel() }
}