package com.dcac.realestatemanager.ui.initialLoginPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.dcac.realestatemanager.ui.initialLoginPage.LoginUiState.*
import com.dcac.realestatemanager.utils.toEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userOnlineRepository: UserOnlineRepository,
    private val userRepository: UserRepository
): ViewModel(), ILoginViewModel {

    // Backing state flow that holds the current UI state
    private val _uiState = MutableStateFlow<LoginUiState>(Idle)
    // Exposed read-only StateFlow to be observed by the UI
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Handles sign-in with email and password
    override fun signIn(email: String, password: String) {
        viewModelScope.launch {
            // Set loading state to show progress indicator
            _uiState.value = Loading

            // Call Firebase Auth
            val result = authRepository.signInWithEmail(email, password)

            result.fold(
                onSuccess = { firebaseUser ->
                    firebaseUser?.let {
                        try {
                            // Build domain model User (used in app logic)
                            val user = User(
                                // Room will auto-generate ID
                                id = 0L,
                                // Use fallback if null
                                email = it.email ?: "no-email",
                                // Default agent name
                                agentName = it.displayName ?: "Agent",
                                // Firebase UID (used for syncing)
                                firebaseUid = it.uid
                            )

                            // Convert to Room Entity and insert locally
                            val userEntity = user.toEntity()
                            userRepository.insertUser(user)

                            // Convert to Firestore entity and upload
                            val userOnline = userEntity.toOnlineEntity()
                            userOnlineRepository.uploadUser(userOnline, it.uid)

                            // Emit success state, wait a bit to let UI react, then reset
                            _uiState.value = Success(it)
                            delay(500)
                            resetState()
                        } catch (e: Exception) {
                            // Catch conversion or sync errors
                            _uiState.value = Error("User sync error: ${e.message}")
                        }
                    } ?: run {
                        // Null user returned from Firebase
                        _uiState.value = Error("Firebase returned null user")
                    }
                },
                onFailure = { e ->
                    // Handle Firebase Auth error
                    _uiState.value = Error(e.message ?: "Unknown error")
                }
            )
        }
    }
    // Handles sign-up (account creation)
    override fun signUp(email: String, password: String, agentName: String) {
        viewModelScope.launch {
            // Set UI to loading while processing
            _uiState.value = Loading

            val result = authRepository.signUpWithEmail(email, password)

            result.fold(
                onSuccess = { firebaseUser ->
                    firebaseUser?.let {
                        try {
                            // Timestamp for syncing
                            val now = System.currentTimeMillis()

                            // Build full domain model
                            val user = User(
                                id = 0L,
                                email = it.email ?: "no-email",
                                agentName = agentName,
                                firebaseUid = it.uid,
                                isSynced = false,
                                isDeleted = false,
                                updatedAt = now
                            )

                            val userEntity = user.toEntity()
                            // Prevent duplicate insertion in Room
                            val userAlreadyExists = userRepository.getUserByEmail(user.email).firstOrNull() != null
                            if (!userAlreadyExists) {
                                userRepository.insertUser(user)
                            }

                            // Upload to Firestore
                            val userOnline = userEntity.toOnlineEntity()
                            userOnlineRepository.uploadUser(userOnline, it.uid)

                            // Emit success and reset state
                            _uiState.value = Success(it)
                            delay(500)
                            resetState()
                        } catch (e: Exception) {
                            // Handle sync failure
                            _uiState.value = Error("User creation error: ${e.message}")
                        }
                    } ?: run {
                        // Handle edge case where Firebase returns null
                        _uiState.value = Error("Firebase returned null user")
                    }
                },
                onFailure = { e ->
                    // Handle Firebase sign-up failure
                    _uiState.value = Error(e.message ?: "Unknown error")
                }
            )
        }
    }
    //TODO SIGN UP FUNCTION

    // Resets the UI state to Idle (used after a success or error to clean UI)
    fun resetState() {
        _uiState.value = Idle
    }
}
