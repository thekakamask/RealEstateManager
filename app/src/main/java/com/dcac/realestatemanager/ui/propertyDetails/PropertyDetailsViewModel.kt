package com.dcac.realestatemanager.ui.propertyDetails

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyRepository

class PropertyDetailsViewModel(
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository,
    private val staticMapRepository: StaticMapRepository
): ViewModel(), IPropertyDetailsViewModel {

}