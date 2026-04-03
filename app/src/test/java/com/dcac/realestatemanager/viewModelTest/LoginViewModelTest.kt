package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.fakeData.fakeOnlineEntity.FakeUserOnlineEntity
import com.dcac.realestatemanager.ui.initialLoginPage.LoginUiState
import com.dcac.realestatemanager.ui.initialLoginPage.LoginViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserModel = FakeUserModel.user1
    private val fakeOnlineUser1 = FakeUserOnlineEntity.userOnline1
    private val fakeOnlineUserDocument1 = FakeUserOnlineEntity.firestoreUserDocument1

    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val userOnlineRepository = mockk<UserOnlineRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed= true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel(
            authRepository,
            userOnlineRepository,
            userRepository,
            syncScheduler
        )
    }

    @Test
    fun signIn_success_existingLocalUser_returnsIdleAfterSuccess() = runTest {
        val firebaseUser = mockk<com.google.firebase.auth.FirebaseUser> {
            every { uid } returns fakeUserModel.firebaseUid
        }

        coEvery {
            authRepository.signInWithEmail(fakeUserModel.email, "1234")
        } returns Result.success(firebaseUser)

        coEvery {
            userRepository.getUserByFirebaseUid(fakeUserModel.firebaseUid)
        } returns flowOf(fakeUserModel)

        viewModel.signIn(fakeUserModel.email, "1234")

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun signIn_nullFirebaseUser_returnsError() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } returns Result.success(null)

        viewModel.signIn(fakeUserModel.email, "1234")

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is LoginUiState.Error)
    }

    @Test
    fun signIn_userNotLocal_butRemoteExists_returnsIdle() = runTest {
        val firebaseUser = mockk<com.google.firebase.auth.FirebaseUser> {
            every { uid } returns fakeUserModel.firebaseUid
        }

        coEvery {
            authRepository.signInWithEmail(any(), any())
        } returns Result.success(firebaseUser)

        coEvery {
            userRepository.getUserByFirebaseUid(fakeUserModel.firebaseUid)
        } returns flowOf(null)

        coEvery {
            userOnlineRepository.getUser(fakeUserModel.firebaseUid)
        } returns fakeOnlineUserDocument1

        coEvery {
            userRepository.insertUserInsertFromFirebase(any(), any())
        } returns Unit

        viewModel.signIn(fakeUserModel.email, "1234")

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun signIn_wrongPassword_returnsError() = runTest {
        coEvery {
            authRepository.signInWithEmail(any(), any())
        } returns Result.failure(RuntimeException("wrong password"))

        viewModel.signIn(fakeUserModel.email, "bad")

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is LoginUiState.Error)
    }

    @Test
    fun signUp_success_returnsIdle() = runTest {
        val firebaseUser = mockk<com.google.firebase.auth.FirebaseUser> {
            every { uid } returns fakeUserModel.firebaseUid
            every { email } returns fakeUserModel.email
        }

        coEvery {
            authRepository.signUpWithEmail(any(), any())
        } returns Result.success(firebaseUser)

        coEvery {
            userRepository.getUserByEmail(any())
        } returns flowOf(null)

        coEvery {
            userRepository.firstUserInsert(any())
        } returns "user-1"

        coEvery { userOnlineRepository.uploadUser(any(), any()) } returns fakeOnlineUser1
        viewModel.signUp(fakeUserModel.email, "1234", fakeUserModel.agentName)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }

    @Test
    fun signUp_nullFirebaseUser_returnsError() = runTest {
        coEvery {
            authRepository.signUpWithEmail(any(), any())
        } returns Result.success(null)

        viewModel.signUp(fakeUserModel.email, "1234", fakeUserModel.agentName)

        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value is LoginUiState.Error
        )
    }

    @Test
    fun resetState_setsIdle() {
        viewModel.resetState()

        assertTrue(
            viewModel.uiState.value is LoginUiState.Idle
        )
    }

    @Test
    fun isUserConnected_returnsTrue() {
        every { authRepository.currentUser } returns mockk()

        assertTrue(viewModel.isUserConnected)
    }



}