package com.dcac.realestatemanager.ui.propertiesList

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository

class PropertiesListViewModel(
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository
): ViewModel(), IPropertiesListViewModel {

}