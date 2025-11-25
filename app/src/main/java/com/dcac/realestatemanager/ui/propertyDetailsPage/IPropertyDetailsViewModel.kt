package com.dcac.realestatemanager.ui.propertyDetailsPage

import kotlinx.coroutines.flow.StateFlow

interface IPropertyDetailsViewModel {

    val uiState: StateFlow<PropertyDetailsUiState>
    fun loadPropertyDetails(propertyId: Long)
}