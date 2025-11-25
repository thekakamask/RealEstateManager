package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import com.dcac.realestatemanager.ui.homePage.PropertyFilters
import com.dcac.realestatemanager.ui.homePage.PropertySortOrder
import kotlinx.coroutines.flow.StateFlow


interface IPropertiesListViewModel {

    val uiState: StateFlow<PropertiesListUiState>
    fun loadProperties()
    fun applyFilters(filters: PropertyFilters)

   /* fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    )
    fun sortProperties(order: PropertySortOrder)
    fun clearSort()*/
    fun resetFilters()
    fun resetState()
}