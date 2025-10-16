package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleMapViewModel @Inject constructor(
    private val mapRepository : GoogleMapRepository
): ViewModel(), IGoogleMapViewModel {

    private val _uiState = MutableStateFlow<GoogleMapUiState>(GoogleMapUiState.Loading)
    val uiState: StateFlow<GoogleMapUiState> = _uiState.asStateFlow()

    override fun loadMapData() {
        viewModelScope.launch {
            _uiState.value = GoogleMapUiState.Loading

            try {
                coroutineScope {
                    // Parallel execution
                    val locationDeferred = async { mapRepository.getUserLocation() }
                    val propertiesDeferred = async { mapRepository.getAllProperties().first() } // Or collectLatest
                    val poiDeferred = async { mapRepository.getAllPoiS().first() }

                    val location = runCatching { locationDeferred.await() }.getOrNull() // safe fallback
                    val properties = propertiesDeferred.await()
                    val poiS = poiDeferred.await()

                    _uiState.value = GoogleMapUiState.Success(
                        userLocation = location,
                        properties = properties,
                        poiS = poiS
                    )
                }

            } catch (e: Exception) {
                _uiState.value = GoogleMapUiState.Error("Map load failed: ${e.message}")
            }
        }
    }

    override fun selectProperty(propertyId: Long) {
        val current = _uiState.value
        if (current is GoogleMapUiState.Success) {
            _uiState.value = current.copy(selectedPropertyId = propertyId)
        }
    }

    override fun clearSelectedProperty() {
        val current = _uiState.value
        if (current is GoogleMapUiState.Success) {
            _uiState.value = current.copy(selectedPropertyId = null)
        }
    }
}
