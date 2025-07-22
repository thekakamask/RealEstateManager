package com.dcac.realestatemanager.ui.propertyCreation

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineStaticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository

class PropertyCreationViewModel(
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository,
    private val staticMapRepository: StaticMapRepository
): ViewModel(), IPropertyCreationViewModel {

}