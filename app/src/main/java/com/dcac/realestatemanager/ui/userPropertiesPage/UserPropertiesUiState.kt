package com.dcac.realestatemanager.ui.userPropertiesPage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder

sealed class UserPropertiesUiState {
    data object Idle : UserPropertiesUiState()
    data object Loading : UserPropertiesUiState()

    @Immutable
    data class Success(
        val properties: List<Property>,
        val filters: PropertyFilters = PropertyFilters(),
        val isFiltered: Boolean = false,
        val activeFilters: PropertyFilters? = null,
        val showFilterSheet: Boolean = false,
        val sortOrder: PropertySortOrder = PropertySortOrder.ALPHABETIC
    ) : UserPropertiesUiState()

    @Immutable
    data class Error(val message: String) : UserPropertiesUiState()
}