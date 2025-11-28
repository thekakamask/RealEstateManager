package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import com.dcac.realestatemanager.ui.homePage.PropertyFilters
import kotlinx.coroutines.flow.StateFlow

interface IGoogleMapViewModel {

    val uiState: StateFlow<GoogleMapUiState>
    fun loadMapData()
    fun selectProperty(propertyId: String)
    fun clearSelectedProperty()
    fun applyFilters(filters: PropertyFilters)
    fun resetFilters()
    fun resetState()
}