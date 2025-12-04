package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.googleMap.GoogleMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleMapViewModel @Inject constructor(
    private val mapRepository : GoogleMapRepository,
    private val propertyRepository: PropertyRepository
): ViewModel(), IGoogleMapViewModel {

    private val _uiState = MutableStateFlow<GoogleMapUiState>(GoogleMapUiState.Loading)
    override val uiState: StateFlow<GoogleMapUiState> = _uiState.asStateFlow()

    override fun loadMapData() {
        viewModelScope.launch {
            _uiState.value = GoogleMapUiState.Loading

            try {
                val location = mapRepository.getUserLocation()

                _uiState.value = GoogleMapUiState.Partial(location)

                val propertiesDeferred = async { mapRepository.getAllProperties().first() }
                val poiDeferred = async { mapRepository.getAllPoiS().first() }

                val properties = propertiesDeferred.await()
                val poiS = poiDeferred.await()

                val propertiesWithLocation = properties.mapNotNull { property ->
                    val lat = property.latitude
                    val lng = property.longitude
                    if (lat != null && lng != null) {
                        PropertyWithLocation(property, LatLng(lat, lng))
                    } else null
                }

                val poiWithLocation = poiS.mapNotNull { poi ->
                    val lat = poi.latitude
                    val lng = poi.longitude
                    if (lat != null && lng != null) {
                        PoiWithLocation(poi, LatLng(lat, lng))
                    } else null
                }

                _uiState.value = GoogleMapUiState.Success(
                    userLocation = location,
                    properties = propertiesWithLocation,
                    poiS = poiWithLocation
                )

            } catch (e: Exception) {
                _uiState.value = GoogleMapUiState.Error("Map load failed: ${e.message}")
            }
        }
    }

    override fun applyFilters(filters: PropertyFilters) {
        viewModelScope.launch {
            try {
                val location = mapRepository.getUserLocation()
                _uiState.value = GoogleMapUiState.Partial(location)

                val properties = propertyRepository.searchProperties(
                    minSurface = filters.minSurface,
                    maxSurface = filters.maxSurface,
                    minPrice = filters.minPrice,
                    maxPrice = filters.maxPrice,
                    type = filters.selectedType,
                    isSold = filters.isSold,
                    sortOrder = filters.sortOrder
                ).first()

                val poiS = mapRepository.getAllPoiS().first()

                val propertiesWithLocation = properties.mapNotNull { property ->
                    property.latitude?.let { lat ->
                        property.longitude?.let { lng ->
                            PropertyWithLocation(property, LatLng(lat, lng))
                        }
                    }
                }

                val poiWithLocation = poiS.mapNotNull { poi ->
                    poi.latitude?.let { lat ->
                        poi.longitude?.let { lng ->
                            PoiWithLocation(poi, LatLng(lat, lng))
                        }
                    }
                }

                _uiState.value = GoogleMapUiState.Success(
                    userLocation = location,
                    properties = propertiesWithLocation,
                    poiS = poiWithLocation,
                    isFiltered = true,
                    activeFilters = filters
                )

            } catch (e: Exception) {
                _uiState.value = GoogleMapUiState.Error("Filtered map load failed: ${e.message}")
            }
        }
    }


    override fun selectProperty(propertyId: String) {
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

    override fun resetFilters() {
        loadMapData()
    }

    override fun resetState() {
        _uiState.value = GoogleMapUiState.Idle
    }
}
