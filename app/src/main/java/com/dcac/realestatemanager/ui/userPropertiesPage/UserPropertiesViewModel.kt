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
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.PropertySortOrder
import com.dcac.realestatemanager.ui.filter.isEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class UserPropertiesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val propertyRepository: PropertyRepository,
    private val authRepository: AuthRepository
) : ViewModel(), IUserPropertiesViewModel {

    private val _uiState = MutableStateFlow<UserPropertiesUiState>(Idle)
    override val uiState: StateFlow<UserPropertiesUiState> = _uiState.asStateFlow()

    override fun getUserIdOrNull(): String? = runBlocking {
        val firebaseUid = authRepository.currentUser?.uid ?: return@runBlocking null
        val user = userRepository.getUserByFirebaseUid(firebaseUid).firstOrNull()
        user?.universalLocalId
    }


    override fun toggleFilterSheet(show: Boolean) {
        val currentState = _uiState.value
        if (currentState is Success) {
            _uiState.value = currentState.copy(showFilterSheet = show)
        }
    }

    override fun applyFilters(filters: PropertyFilters) {
        val userId = runBlocking { getUserIdOrNull() } ?: return
        applyFilters(userId, filters)
    }

    fun applyFilters(userId: String, filters: PropertyFilters) {
        viewModelScope.launch {
            val isEmpty = filters.isEmpty()

            val flow = when {
                isEmpty && filters.sortOrder == PropertySortOrder.ALPHABETIC -> {
                    propertyRepository.getFullPropertiesByUserIdAlphabetic(userId)
                }
                isEmpty && filters.sortOrder == PropertySortOrder.DATE -> {
                    propertyRepository.getFullPropertiesByUserIdDate(userId)
                }
                else -> {
                    propertyRepository.searchUserProperties(userId, filters)
                }
            }

            flow
                .catch { e -> _uiState.value = Error("Error: ${e.message}") }
                .collectLatest { properties ->
                    _uiState.value = Success(
                        properties = properties,
                        isFiltered = !isEmpty,
                        activeFilters = if (!isEmpty) filters else null,
                        filters = filters,
                        sortOrder = filters.sortOrder
                    )
                }
        }
    }


    override fun resetFilters() {
        val userId = runBlocking { getUserIdOrNull() } ?: return
        applyFilters(userId, PropertyFilters())
    }
    override fun resetState() {
        _uiState.value = Idle
    }
}