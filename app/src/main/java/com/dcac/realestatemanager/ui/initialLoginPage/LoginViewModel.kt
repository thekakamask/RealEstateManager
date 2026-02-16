package com.dcac.realestatemanager.ui.initialLoginPage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.dcac.realestatemanager.ui.initialLoginPage.LoginUiState.*
import com.dcac.realestatemanager.utils.toEntity
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userOnlineRepository: UserOnlineRepository,
    private val userRepository: UserRepository,
    private val syncScheduler: SyncScheduler
): ViewModel(), ILoginViewModel {

    private val _uiState = MutableStateFlow<LoginUiState>(Idle)
    override val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    override fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = Loading

            val result = authRepository.signInWithEmail(email, password)

            result.fold(
                onSuccess = { firebaseUser ->
                    if (firebaseUser == null) {
                        _uiState.value = Error(R.string.error_null_user)
                        return@fold
                    }

                    val uid = firebaseUser.uid

                    try {
                        val localUser = userRepository.getUserByFirebaseUid(uid).firstOrNull()

                        if (localUser == null) {
                            val remoteUser = userOnlineRepository.getUser(uid)

                            if (remoteUser != null) {
                                userRepository.insertUserInsertFromFirebase(remoteUser.user, remoteUser.firebaseId)
                            } else {
                                _uiState.value = Error(R.string.error_no_account_found)
                                return@fold
                            }
                        }

                        syncScheduler.scheduleSync()

                        _uiState.value = Success(firebaseUser)
                        delay(500)
                        resetState()
                    } catch (e: Exception) {
                        Log.e("LoginError", "Failed to sync user on login", e)
                        _uiState.value = Error(R.string.error_user_sync)
                    }
                },
                onFailure = { e ->
                    Log.e("AuthError", "Sign in failed: ${e.message}", e)

                    val messageResId = when {
                        e.message?.contains("no user record", true) == true ||
                                (e as? FirebaseAuthException)?.errorCode == "ERROR_USER_NOT_FOUND" ->
                            R.string.error_no_account_found

                        e.message?.contains("password", true) == true ||
                                (e as? FirebaseAuthException)?.errorCode == "ERROR_WRONG_PASSWORD" ->
                            R.string.error_invalid_credentials

                        else -> R.string.error_unknown
                    }

                    _uiState.value = Error(messageResId)
                }
            )
        }
    }

    override fun signUp(email: String, password: String, agentName: String) {
        viewModelScope.launch {
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
                                email = it.email ?: "no-email",
                                agentName = agentName,
                                firebaseUid = it.uid,
                                isSynced = true,
                                isDeleted = false,
                                updatedAt = now
                            )

                            val userAlreadyExists =
                                userRepository.getUserByEmail(user.email).firstOrNull() != null

                            if (!userAlreadyExists) {
                                val generatedId = userRepository.firstUserInsert(user)

                                val userWithId = user.copy(universalLocalId = generatedId)
                                val userOnline = userWithId.toEntity().toOnlineEntity()

                                userOnlineRepository.uploadUser(userOnline, it.uid)
                                syncScheduler.scheduleSync()

                            }

                            // Emit success and reset state
                            _uiState.value = Success(it)
                            delay(500)
                            resetState()
                        } catch (e: Exception) {
                            // Handle sync failure
                            Log.e("SignUpError", "Error during local insertion or sync", e)
                            _uiState.value = Error(R.string.error_user_creation)
                        }
                    } ?: run {
                        // Handle edge case where Firebase returns null
                        _uiState.value = Error(R.string.error_null_user)
                    }
                },
                onFailure = {
                    // Handle Firebase sign-up failure
                    _uiState.value = Error(R.string.error_unknown)
                }
            )
        }
    }

    val isUserConnected: Boolean
        get() = authRepository.currentUser != null

    // Resets the UI state to Idle (used after a success or error to clean UI)
    fun resetState() {
        _uiState.value = Idle
    }
}
