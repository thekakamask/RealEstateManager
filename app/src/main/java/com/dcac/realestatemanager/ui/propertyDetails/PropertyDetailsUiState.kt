package com.dcac.realestatemanager.ui.propertyDetails

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertyDetailsUiState {

    @Immutable
    data class Success(val property: Property) : PropertyDetailsUiState

    data class Error(val message: String) : PropertyDetailsUiState

    data object Loading : PropertyDetailsUiState
}