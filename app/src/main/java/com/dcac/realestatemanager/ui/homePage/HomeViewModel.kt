package com.dcac.realestatemanager.ui.homePage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.sync.SyncStatus
import com.dcac.realestatemanager.data.sync.globalManager.DownloadInterfaceManager
import com.dcac.realestatemanager.data.sync.globalManager.UploadInterfaceManager
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.dcac.realestatemanager.ui.homePage.HomeUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadManager: UploadInterfaceManager,
    private val downloadManager: DownloadInterfaceManager,
) : ViewModel(), IHomeViewModel {

    // Internal mutable state
    private val _uiState = MutableStateFlow<HomeUiState>(Loading)
    // UI exposed read-only state
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    // Load user info from Firebase auth and update UI state
    override fun loadUserInfo() {
        val user = authRepository.currentUser
        if (user != null) {
            _uiState.value = Success(
                userEmail = user.email ?: "No email",
                userName = user.displayName ?: "Agent",
                isDrawerOpen = false
            )
        } else {
            _uiState.value = Error("User not logged in")
        }
    }

    // Launch global sync (upload + download)
    override fun syncAll() {
        val currentState = _uiState.value
        if (currentState is Success) {
            _uiState.value = currentState.copy(isSyncing = true, snackBarMessage = null)

            viewModelScope.launch {
                try {
                    val uploadResults = uploadManager.syncAll()
                    val downloadResults = downloadManager.downloadAll()
                    val allResults = uploadResults + downloadResults

                    _uiState.value = currentState.copy(
                        isSyncing = false,
                        lastSyncStatus = allResults,
                        snackBarMessage = "Sync completed with ${allResults.count { it is SyncStatus.Failure }} error(s)"
                    )
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isSyncing = false,
                        snackBarMessage = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }

    // Clear snackbar message after displaying it
    override fun resetSnackBarMessage() {
        val currentState = _uiState.value
        if (currentState is Success) {
            _uiState.value = currentState.copy(snackBarMessage = null)
        }
    }

    // Optional: toggle drawer open/close from UI
    override fun toggleDrawer(isOpen: Boolean) {
        val currentState = _uiState.value
        if (currentState is Success) {
            _uiState.value = currentState.copy(isDrawerOpen = isOpen)
        }
    }

    override fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.value = Idle
                // You can trigger navigation to login screen via a NavigationEffect or similar
            } catch (e: Exception) {
                _uiState.value = Error("Logout failed: ${e.message}")
            }
        }
    }

    override fun navigateTo(screen: HomeDestination) {
        val currentState = _uiState.value
        if (currentState is Success) {
            _uiState.value = currentState.copy(currentScreen = screen)
        }
    }

    override fun resetState() {
        _uiState.value = Idle
    }

}
