package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import android.location.Location
import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.ui.filter.PropertyFilters

sealed class GoogleMapUiState {

    data object Idle : GoogleMapUiState()
    data object Loading : GoogleMapUiState()

    @Immutable
    data class Partial(
        val userLocation: Location?
    ) : GoogleMapUiState()

    @Immutable
    data class Success(
        val userLocation: Location?,
        val properties: List<PropertyWithLocation>,
        val poiS: List<PoiWithLocation>,
        val selectedPropertyId: String? = null,
        val isFiltered: Boolean = false,
        val activeFilters: PropertyFilters? = null
    ) : GoogleMapUiState()

    @Immutable
    data class Error(val message: String) : GoogleMapUiState()
}