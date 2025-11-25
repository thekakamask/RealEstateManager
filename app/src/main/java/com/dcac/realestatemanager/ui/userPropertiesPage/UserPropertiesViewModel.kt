package com.dcac.realestatemanager.ui.userPropertiesPage

import androidx.lifecycle.ViewModel
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.dcac.realestatemanager.ui.userPropertiesPage.UserPropertiesUiState.*
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPropertiesViewModel @Inject constructor(
    private val propertyRepository: PropertyRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel(), IUserPropertiesViewModel {

    private val _uiState = MutableStateFlow<UserPropertiesUiState>(Idle)
    override val uiState: StateFlow<UserPropertiesUiState> = _uiState.asStateFlow()

    override fun loadUserProperties() {
        /*val firebaseUid = authRepository.currentUser?.uid

        if (firebaseUid == null) {
            _uiState.value = Error("User not logged in.")
            return
        }

        viewModelScope.launch {
            _uiState.value = Loading

            userRepository.getUserByFirebaseUid(firebaseUid)
                .catch { e ->
                    _uiState.value = Error("Failed to load user: ${e.message}")
                }
                .collectLatest { localUser ->
                    if (localUser == null) {
                        _uiState.value = Error("User not found in local DB.")
                        return@collectLatest
                    }

                    propertyRepository.getPropertiesByUserId(localUser.id)
                        .catch { e ->
                            _uiState.value = Error("Failed to load properties: ${e.message}")
                        }
                        .collectLatest { userProperties ->
                            _uiState.value = Success(userProperties)
                        }
                }
        }*/
    }

    override fun resetState() {
        _uiState.value = Idle
    }
}