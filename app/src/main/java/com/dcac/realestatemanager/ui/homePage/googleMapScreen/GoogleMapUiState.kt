package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import android.location.Location
import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property

sealed interface GoogleMapUiState {

    data object Loading : GoogleMapUiState

    @Immutable
    data class Success(
        val userLocation: Location?,               // null if loc fail
        val properties: List<Property>,
        val poiS: List<Poi>,
        val selectedPropertyId: Long? = null       // for display selected pin
    ) : GoogleMapUiState

    @Immutable
    data class Error(val message: String) : GoogleMapUiState
}