package com.dcac.realestatemanager.ui.propertyCreationPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertyCreationUiState {

    @Immutable
    data class Editing(
        val property: Property
    ) : PropertyCreationUiState
    // -> Loaded property for modifying (pre-filling the form)

    @Immutable
    data class Success(
        val createdOrUpdatedProperty: Property,
        val isUpdate: Boolean
    ) : PropertyCreationUiState
    // -> Created or updated property with success

    data class Error(val message: String) : PropertyCreationUiState

    data object Loading : PropertyCreationUiState

    data object Idle : PropertyCreationUiState
}