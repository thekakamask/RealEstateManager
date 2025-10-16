package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListUiState.*
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class PropertiesListViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository
) : ViewModel(), IPropertiesListViewModel {

    private val _uiState = MutableStateFlow<PropertiesListUiState>(Loading)
    val uiState: StateFlow<PropertiesListUiState> = _uiState.asStateFlow()

    override fun loadProperties() {
        viewModelScope.launch {
            propertyRepository.getAllPropertiesByAlphabetic()
                .catch { e -> _uiState.value = Error("Failed to load properties: ${e.message}") }
                .collectLatest { properties ->
                    _uiState.value = Success(properties)
                }
        }
    }

    override fun searchProperties(
        minSurface: Int?,
        maxSurface: Int?,
        minPrice: Int?,
        maxPrice: Int?,
        type: String?,
        isSold: Boolean?
    ) {
        viewModelScope.launch {
            val filters = PropertyFilters(
                minSurface = minSurface,
                maxSurface = maxSurface,
                minPrice = minPrice,
                maxPrice = maxPrice,
                type = type,
                isSold = isSold
            )

            propertyRepository.searchProperties(
                minSurface = filters.minSurface,
                maxSurface = filters.maxSurface,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                type = filters.type,
                isSold = filters.isSold
            )
                .catch { e -> _uiState.value = Error("Search failed: ${e.message}") }
                .collectLatest { result ->
                    _uiState.value = Success(
                        properties = result,
                        isFiltered = true,
                        activeFilters = filters
                    )
                }
        }
    }

    override fun sortProperties(order: PropertySortOrder) {
        viewModelScope.launch {
            val flow = when (order) {
                PropertySortOrder.ALPHABETIC -> propertyRepository.getAllPropertiesByAlphabetic()
                PropertySortOrder.DATE -> propertyRepository.getAllPropertiesByDate()
            }

            flow
                .catch { e -> _uiState.value = Error("Sort failed: ${e.message}") }
                .collectLatest { result ->
                    _uiState.value = Success(
                        properties = result,
                        sortOrder = order
                    )
                }
        }
    }

    override fun clearSort() {
        viewModelScope.launch {
            // Reload properties without any specific sorting
            propertyRepository.getAllPropertiesByAlphabetic()
                .catch { e -> _uiState.value = Error("Clear sort failed: ${e.message}") }
                .collectLatest { result ->
                    _uiState.value = Success(
                        properties = result,
                        isFiltered = false,
                        activeFilters = null,
                        sortOrder = PropertySortOrder.ALPHABETIC
                    )
                }
        }
    }

    override fun resetFilters() {
        loadProperties()
    }

    override fun resetState() {
        _uiState.value = Idle
    }
}
