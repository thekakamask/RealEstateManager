package com.dcac.realestatemanager.ui.accountPage

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.accountPage.AccountUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val propertyRepository: PropertyRepository,
    private val photoRepository: PhotoRepository,
    private val staticMapRepository: StaticMapRepository,
    private val crossRefRepository: PropertyPoiCrossRepository,
    private val syncScheduler: SyncScheduler
) : ViewModel(), IAccountViewModel {

    private val _uiState = MutableStateFlow<AccountUiState>(Idle)
    override val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private var currentSuccessState: Success? = null


    override fun checkAndLoadUser() {
        val userId = getUserIdOrNull()
        if (userId != null) {
            loadUser(userId)
        } else {
            setError("No user connected.")
        }
    }

    override fun getUserIdOrNull(): String? = runBlocking {
        val firebaseUid = authRepository.currentUser?.uid ?: return@runBlocking null
        val user = userRepository.getUserByFirebaseUid(firebaseUid).firstOrNull()
        user?.universalLocalId
    }

    override fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = Loading
            try {
                val user = userRepository.getUserById(userId).firstOrNull()
                if (user != null) {
                    val properties = propertyRepository.getFullPropertiesByUserIdAlphabetic(userId).firstOrNull().orEmpty()
                    val newState = Success(user = user, properties = properties)
                    currentSuccessState = newState
                    _uiState.value = newState
                } else {
                    _uiState.value = Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = Error("Failed to load user: ${e.message}")
            }
        }
    }


    override fun enterEditMode() {
        val currentState = currentSuccessState
        if (currentState != null) {
            _uiState.value = currentState.copy(isEditing = true)
        }
    }

    override fun updateUser(newName: String) {
        val user = (currentSuccessState?.user) ?: run {
            _uiState.value = Error("No user loaded")
            return
        }

        viewModelScope.launch {
            try {
                userRepository.updateUser(user.copy(agentName = newName))
                syncScheduler.scheduleSync()
                loadUser(user.universalLocalId)
            } catch (e: Exception) {
                _uiState.value = Error("Update failed: ${e.message}")
            }
        }
    }

    override fun deleteProperty(property: Property) {
        viewModelScope.launch {
            try {
                val propertyId = property.universalLocalId

                property.photos.forEach { photo ->
                    photo.uri.toUri().path?.let { path ->
                        runCatching {
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                    }
                    photoRepository.markPhotoAsDeleted(photo)
                }

                property.staticMap?.let { map ->
                    map.uri.toUri().path?.let { path ->
                        runCatching {
                            val file = File(path)
                            if (file.exists()) file.delete()
                        }
                    }
                    staticMapRepository.markStaticMapAsDeleted(map)
                }

                crossRefRepository.markCrossRefsAsDeletedForProperty(propertyId)
                propertyRepository.markPropertyAsDeleted(property)

                syncScheduler.scheduleSync()

                loadUser(property.universalLocalUserId)

            } catch (e: Exception) {
                _uiState.value = Error("Failed to delete property: ${e.message}")
            }
        }
    }

    override fun setError(message: String) {
        _uiState.value = Error(message)
    }

    override fun resetState() {
        checkAndLoadUser()
    }
}
