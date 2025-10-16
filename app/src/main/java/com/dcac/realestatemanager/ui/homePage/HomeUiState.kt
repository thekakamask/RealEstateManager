package com.dcac.realestatemanager.ui.homePage

import androidx.compose.runtime.Immutable
import com.dcac.realestatemanager.data.sync.SyncStatus

sealed interface HomeUiState {

    data object Idle : HomeUiState
    data object Loading : HomeUiState

    @Immutable
    data class Success(
        val userEmail: String,
        val userName: String,
        val snackBarMessage: String? = null,
        val isDrawerOpen: Boolean,
        val isSyncing: Boolean = false,
        val lastSyncStatus: List<SyncStatus> = emptyList(),
        val currentScreen: HomeDestination = HomeDestination.PropertyList
    ) : HomeUiState

    @Immutable
    data class Error(val message: String) : HomeUiState
}