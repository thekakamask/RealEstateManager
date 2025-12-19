package com.dcac.realestatemanager.ui.homePage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.ui.filter.PropertyFilters

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
        val showFilterSheet: Boolean = false,
        val totalProperties: Int = 0,
        val soldProperties: Int = 0
    ) : HomeUiState() {

        val isOnPropertyList: Boolean
            get() = currentScreen == HomeDestination.PropertyList

        val isOnMap: Boolean
            get() = currentScreen == HomeDestination.GoogleMap
    }

    @Immutable
    data class Error(val message: String) : HomeUiState()
}