package com.dcac.realestatemanager.ui.accountPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.ui.accountPage.AccountUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel(), IAccountViewModel {

    private val _uiState = MutableStateFlow<AccountUiState>(Idle)
    override val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    override fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = Loading
            userRepository.getUserById(userId)
                .catch { e ->
                    _uiState.value = Error("Failed to load user: ${e.message}")
                }
                .collectLatest { user ->
                    if (user != null) {
                        currentUser = user
                        _uiState.value = Viewing(user)
                    } else {
                        _uiState.value = Error("User not found")
                    }
                }
        }
    }

    override fun enterEditMode() {
        val user = currentUser
        if (user != null) {
            _uiState.value = Editing(user)
        }
    }

    override fun updateUser(newName: String, newEmail: String) {
        val user = currentUser
        if (user == null) {
            _uiState.value = Error("No user loaded")
            return
        }

        viewModelScope.launch {
            try {
                val updated = user.copy(agentName = newName, email = newEmail)
                userRepository.updateUser(updated)
                currentUser = updated
                _uiState.value = Success(updatedUser = updated)
            } catch (e: Exception) {
                _uiState.value = Error("Update failed: ${e.message}")
            }
        }
    }

    override fun resetState() {
        _uiState.value = Idle
    }
}
