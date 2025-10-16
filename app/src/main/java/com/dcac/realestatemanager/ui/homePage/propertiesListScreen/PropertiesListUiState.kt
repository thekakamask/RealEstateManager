package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property

sealed interface PropertiesListUiState {

    data object Idle : PropertiesListUiState
    data object Loading : PropertiesListUiState

    @Immutable
    data class Success(
        val properties: List<Property>,
        val isFiltered: Boolean = false,
        val activeFilters: PropertyFilters? = null,
        val sortOrder: PropertySortOrder = PropertySortOrder.ALPHABETIC
    ) : PropertiesListUiState

    @Immutable
    data class Error(val message: String) : PropertiesListUiState
}