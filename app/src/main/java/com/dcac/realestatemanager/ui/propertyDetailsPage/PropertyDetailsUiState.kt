package com.dcac.realestatemanager.ui.propertyDetailsPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed class PropertyDetailsUiState {

    data object Loading : PropertyDetailsUiState()

    @Immutable
    data class Success(
        val property: Property,
        val userName: String
    ) : PropertyDetailsUiState()

    @Immutable
    data class Error(
        val message: String
    ) : PropertyDetailsUiState()
}