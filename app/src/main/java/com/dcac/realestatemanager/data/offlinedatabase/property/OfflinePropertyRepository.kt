package com.dcac.realestatemanager.data.offlinedatabase.property

import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.Flow
import com.dcac.realestatemanager.utils.toModel
import kotlinx.coroutines.flow.combine

class OfflinePropertyRepository(
    private val propertyDao: PropertyDao,
    private val poiRepository: PoiRepository,
    private val photoRepository: PhotoRepository
): PropertyRepository {

    private fun combinePropertiesWithDetails(
        propertiesFlow: Flow<List<PropertyEntity>>
    ): Flow<List<Property>> {
        val photosFlow = photoRepository.getAllPhotos()
        val poiSFlow = poiRepository.getAllPoiS()

        return combine(propertiesFlow, photosFlow, poiSFlow) { propertiesEntities, photos, poiS ->
            propertiesEntities.map { propertyEntity ->
                val propertyPhotos = photos.filter { it.propertyId == propertyEntity.id }
                val propertyPoiS = poiS.filter { it.propertyId == propertyEntity.id }
                propertyEntity.toModel(propertyPhotos, propertyPoiS)
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
        val poiSFlow = poiRepository.getPoiSByPropertyId(id)

        return combine(propertyFlow, photosFlow, poiSFlow) { propertyEntity, photos, poiS ->
            propertyEntity?.toModel(photos, poiS)
        }
    }

    override fun searchProperties(
        minSurface: Int?, maxSurface: Int?, minPrice: Int?,
        maxPrice: Int?, type: String?, isSold: Boolean?
    ): Flow<List<Property>> {
        val propertiesFlow = propertyDao.searchProperties(minSurface, maxSurface, minPrice, maxPrice, type, isSold)
        val photosFlow = photoRepository.getAllPhotos()
        val poiSFlow = poiRepository.getAllPoiS()

        return combine(propertiesFlow, photosFlow, poiSFlow) { propertiesEntities, photos, poiS ->
            propertiesEntities.map { propertyEntity ->
                val propertyPhotos = photos.filter { it.propertyId == propertyEntity.id }
                val propertyPoiS = poiS.filter { it.propertyId == propertyEntity.id }
                propertyEntity.toModel(propertyPhotos, propertyPoiS)
            }
        }
    }

    override suspend fun insertProperty(property: PropertyEntity): Long = propertyDao.insertProperty(property)
    override suspend fun updateProperty(property: PropertyEntity) = propertyDao.updateProperty(property)
    override suspend fun deleteProperty(property: PropertyEntity) = propertyDao.deleteProperty(property)
    override suspend fun markPropertyAsSold(propertyId: Long, saleDate: String) = propertyDao.markPropertyAsSold(propertyId, saleDate)
    override suspend fun clearAll() = propertyDao.clearAll()
}