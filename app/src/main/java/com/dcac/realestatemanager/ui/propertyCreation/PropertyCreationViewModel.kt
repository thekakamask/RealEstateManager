package com.dcac.realestatemanager.ui.propertyCreation

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyRepository
import com.dcac.realestatemanager.ui.propertiesList.IPropertiesListViewModel

class PropertyCreationViewModel(
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository,
    private val staticMapRepository: StaticMapRepository
): ViewModel(), IPropertyCreationViewModel {

}