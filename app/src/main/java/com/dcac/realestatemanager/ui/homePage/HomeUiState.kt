package com.dcac.realestatemanager.ui.homePage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.data.sync.SyncStatus

sealed class HomeUiState {

    data object Idle : HomeUiState()
    data object Loading : HomeUiState()

    @Immutable
    data class Success(
        val userEmail: String,
        val userName: String,
        val snackBarMessage: String? = null,
        val isDrawerOpen: Boolean,
        val isSyncing: Boolean = false,
        val lastSyncStatus: List<SyncStatus> = emptyList(),
        val currentScreen: HomeDestination = HomeDestination.PropertyList,
        val filters: PropertyFilters = PropertyFilters(),
        val showFilterSheet: Boolean = false
    ) : HomeUiState() {

        val filterUiState: FilterUiState
            get() = FilterUiState(
                sortOrder = filters.sortOrder,
                selectedType = filters.selectedType.orEmpty(),
                isSold = filters.isSold,
                minSurface = filters.minSurface?.toString().orEmpty(),
                maxSurface = filters.maxSurface?.toString().orEmpty(),
                minPrice = filters.minPrice?.toString().orEmpty(),
                maxPrice = filters.maxPrice?.toString().orEmpty()
            )

        //val isDrawerClosed: Boolean
         //   get() = !isDrawerOpen

        val isOnPropertyList: Boolean
            get() = currentScreen == HomeDestination.PropertyList

        val isOnMap: Boolean
            get() = currentScreen == HomeDestination.GoogleMap
    }

    @Immutable
    data class Error(val message: String) : HomeUiState()
}

@Immutable
data class FilterUiState(
    val sortOrder: PropertySortOrder,
    val selectedType: String,
    val isSold: Boolean?,
    val minSurface: String,
    val maxSurface: String,
    val minPrice: String,
    val maxPrice: String
)

fun FilterUiState.toFilters(): PropertyFilters = PropertyFilters(
    sortOrder = this.sortOrder,
    selectedType = this.selectedType.ifBlank { null },
    isSold = this.isSold,
    minSurface = this.minSurface.toIntOrNull(),
    maxSurface = this.maxSurface.toIntOrNull(),
    minPrice = this.minPrice.toIntOrNull(),
    maxPrice = this.maxPrice.toIntOrNull()
)