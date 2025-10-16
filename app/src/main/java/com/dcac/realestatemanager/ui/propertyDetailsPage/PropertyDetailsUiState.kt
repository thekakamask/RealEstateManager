package com.dcac.realestatemanager.ui.propertyDetailsPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertyDetailsUiState {

    data object Loading : PropertyDetailsUiState

    @Immutable
    data class Success(
        val property: Property,
        val isMapAvailable: Boolean = property.staticMapPath != null
    ) : PropertyDetailsUiState

    @Immutable
    data class Error(
        val message: String
    ) : PropertyDetailsUiState
}