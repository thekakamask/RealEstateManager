package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import kotlinx.coroutines.flow.StateFlow

interface IGoogleMapViewModel {

    val uiState: StateFlow<GoogleMapUiState>
    fun loadMapData()
    fun selectProperty(propertyId: String)
    fun clearSelectedProperty()
}