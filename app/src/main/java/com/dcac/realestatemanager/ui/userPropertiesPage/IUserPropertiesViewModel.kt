package com.dcac.realestatemanager.ui.userPropertiesPage

import com.dcac.realestatemanager.ui.filter.PropertyFilters
import kotlinx.coroutines.flow.StateFlow


interface IUserPropertiesViewModel {

    val uiState: StateFlow<UserPropertiesUiState>
    fun getUserIdOrNull(): String?
    fun toggleFilterSheet(show: Boolean)
    fun applyFilters(filters: PropertyFilters)
    fun resetFilters()
    fun resetState()
}