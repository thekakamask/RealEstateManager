package com.dcac.realestatemanager.ui.googleMap

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository

class GoogleMapViewModel(
    private val googleMapRepository: GoogleMapRepository,
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository
): ViewModel(), IGoogleMapViewModel {

}