package com.dcac.realestatemanager.ui.propertyCreation

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertyCreationUiState {

    @Immutable
    data class Success(val createdProperty: Property? = null) : PropertyCreationUiState

    data class Error(val message: String) : PropertyCreationUiState

    data object Loading : PropertyCreationUiState
}