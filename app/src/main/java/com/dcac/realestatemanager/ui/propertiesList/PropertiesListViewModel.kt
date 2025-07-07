package com.dcac.realestatemanager.ui.propertiesList

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyRepository

class PropertiesListViewModel(
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository
): ViewModel(), IPropertiesListViewModel {

}