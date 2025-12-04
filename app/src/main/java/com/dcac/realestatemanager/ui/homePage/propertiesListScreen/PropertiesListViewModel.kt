package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListUiState.*
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import com.dcac.realestatemanager.ui.filter.isEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class PropertiesListViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository
) : ViewModel(), IPropertiesListViewModel {

    private val _uiState = MutableStateFlow<PropertiesListUiState>(Loading)
    override val uiState: StateFlow<PropertiesListUiState> = _uiState.asStateFlow()

    override fun applyFilters(filters: PropertyFilters) {
        viewModelScope.launch {
            val isEmpty = filters.isEmpty()

            val flow = when {
                isEmpty && filters.sortOrder == PropertySortOrder.ALPHABETIC -> {
                    propertyRepository.getAllPropertiesByAlphabetic()
                }
                isEmpty && filters.sortOrder == PropertySortOrder.DATE -> {
                    propertyRepository.getAllPropertiesByDate()
                }
                else -> {
                    propertyRepository.searchProperties(
                        minSurface = filters.minSurface,
                        maxSurface = filters.maxSurface,
                        minPrice = filters.minPrice,
                        maxPrice = filters.maxPrice,
                        type = filters.selectedType,
                        isSold = filters.isSold,
                        sortOrder = filters.sortOrder
                    )
                }
            }

            flow
                .catch { e ->
                    _uiState.value = Error("Search failed: ${e.message}")
                }
                .collectLatest { properties ->
                    val userIds = properties.map { it.universalLocalUserId }.distinct()

                    val agentNames = userIds.associateWith { id ->
                        userRepository.getUserById(id).firstOrNull()?.agentName ?: "Unknown"
                    }

                    _uiState.value = Success(
                        properties = properties,
                        isFiltered = !isEmpty,
                        activeFilters = if (!isEmpty) filters else null,
                        sortOrder = filters.sortOrder,
                        agentNames = agentNames
                    )
                }
        }
    }

    override fun resetFilters() {
        applyFilters(PropertyFilters())
    }

    override fun resetState() {
        _uiState.value = Idle
    }
}
