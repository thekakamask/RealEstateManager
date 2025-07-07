package com.dcac.realestatemanager.ui.googleMap

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.offlinedatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlinedatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlinedatabase.property.PropertyRepository

class GoogleMapViewModel(
    private val googleMapRepository: GoogleMapRepository,
    private val photoRepository: PhotoRepository,
    private val poiRepository: PoiRepository,
    private val propertyRepository: PropertyRepository
): ViewModel(), IGoogleMapViewModel {

}