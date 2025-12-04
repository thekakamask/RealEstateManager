package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder

sealed class PropertiesListUiState {

    data object Idle : PropertiesListUiState()
    data object Loading : PropertiesListUiState()

    @Immutable
    data class Success(
        val properties: List<Property>,
        val agentNames: Map<String, String> = emptyMap(),
        val isFiltered: Boolean = false,
        val activeFilters: PropertyFilters? = null,
        val sortOrder: PropertySortOrder = PropertySortOrder.ALPHABETIC
    ) : PropertiesListUiState()

    @Immutable
    data class Error(val message: String) : PropertiesListUiState()
}