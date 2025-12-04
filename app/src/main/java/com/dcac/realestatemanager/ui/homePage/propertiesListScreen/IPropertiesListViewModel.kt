package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import com.dcac.realestatemanager.ui.filter.PropertyFilters
import kotlinx.coroutines.flow.StateFlow


interface IPropertiesListViewModel {

    val uiState: StateFlow<PropertiesListUiState>
    fun applyFilters(filters: PropertyFilters)
    fun resetFilters()
    fun resetState()
}