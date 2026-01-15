package com.dcac.realestatemanager.ui.propertyDetailsPage

import com.dcac.realestatemanager.model.Property
import kotlinx.coroutines.flow.StateFlow

interface IPropertyDetailsViewModel {

    val uiState: StateFlow<PropertyDetailsUiState>
    fun loadPropertyDetails(propertyId: String)
    fun deleteProperty(property: Property, onDeleted: () -> Unit)
}