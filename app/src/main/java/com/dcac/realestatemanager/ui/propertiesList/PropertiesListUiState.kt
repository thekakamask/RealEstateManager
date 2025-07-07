package com.dcac.realestatemanager.ui.propertiesList

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertiesListUiState {
    @Immutable
    data class Success(val properties: List<Property>) : PropertiesListUiState

    data class Error(val message: String) : PropertiesListUiState

    data object Loading : PropertiesListUiState
}