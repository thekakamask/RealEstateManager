package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.accountPage.AccountUiState
import com.dcac.realestatemanager.ui.accountPage.AccountViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user1 = FakeUserModel.user1
    private val property1 = FakePropertyModel.property1

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val staticMapRepository = mockk<StaticMapRepository>(relaxed = true)
    private val crossRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    private lateinit var viewModel: AccountViewModel

    @Before
    fun setup() {
        viewModel = AccountViewModel(
            userRepository,
            authRepository,
            propertyRepository,
            photoRepository,
            staticMapRepository,
            crossRepository,
            syncScheduler
        )
    }

    @Test
    fun checkAndLoadUser_userExists_emitsSuccess() = runTest {
        val userId = user1.universalLocalId
        val userFirebaseUid = user1.firebaseUid
        val propertiesModelList = listOf(property1)

        every { authRepository.currentUser?.uid } returns userFirebaseUid
        coEvery { userRepository.getUserByFirebaseUid(userFirebaseUid) } returns flowOf(user1)
        coEvery { userRepository.getUserById(userId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(userId) } returns flowOf(propertiesModelList)

        viewModel.checkAndLoadUser()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Success)

        state as AccountUiState.Success
        assertEquals(user1, state.user)
        assertEquals(propertiesModelList, state.properties)
    }

    @Test
    fun enterEditMode_whenSuccess_updatesStateWithEditingTrue() = runTest {
        val properties = listOf(property1)

        coEvery { userRepository.getUserById(user1.universalLocalId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId) } returns flowOf(properties)

        viewModel.loadUser(user1.universalLocalId)

        advanceUntilIdle()

        viewModel.enterEditMode()

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Success)
        state as AccountUiState.Success

        assertTrue(state.isEditing)
    }

    @Test
    fun enterEditMode_whenNoSuccessState_doesNothing() = runTest {
        viewModel.enterEditMode()

        val state = viewModel.uiState.value

        assertFalse(state is AccountUiState.Success)
    }

    @Test
    fun updateUser_success_updatesUser_andReloads() = runTest {
        val properties = listOf(property1)

        coEvery { userRepository.getUserById(user1.universalLocalId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId) } returns flowOf(properties)

        viewModel.loadUser(user1.universalLocalId)

        advanceUntilIdle()

        coEvery { userRepository.updateUser(any()) } returns Unit

        viewModel.updateUser("NewName")

        advanceUntilIdle()

        coVerify {
            userRepository.updateUser(match {
                it.agentName == "NewName"
            })
        }

        coVerify {
            syncScheduler.scheduleSync()
        }

        val state = viewModel.uiState.value
        assertTrue(state is AccountUiState.Success)
    }

    @Test
    fun updateUser_noUserLoaded_returnsError() = runTest {
        viewModel.updateUser("NewName")

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Error)
        assertEquals("No user loaded", (state as AccountUiState.Error).message)
    }

    @Test
    fun updateUser_repositoryFails_returnsError() = runTest {
        val properties = listOf(property1)

        coEvery { userRepository.getUserById(user1.universalLocalId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId) } returns flowOf(properties)

        viewModel.loadUser(user1.universalLocalId)

        advanceUntilIdle()

        coEvery { userRepository.updateUser(any()) } throws RuntimeException("DB error")

        viewModel.updateUser("NewName")
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Error)
        assertTrue((state as AccountUiState.Error).message.contains("Update failed"))
    }

    @Test
    fun deleteProperty_success_callsRepositories_andReloadsUser() = runTest {
        val property = FakePropertyModel.property1
        val properties = listOf(property1)

        coEvery { userRepository.getUserById(property.universalLocalUserId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(property.universalLocalUserId) } returns flowOf(properties)

        coEvery { photoRepository.markPhotoAsDeleted(any()) } returns Unit
        coEvery { staticMapRepository.markStaticMapAsDeleted(any()) } returns Unit
        coEvery { crossRepository.markCrossRefsAsDeletedForProperty(any()) } returns Unit
        coEvery { propertyRepository.markPropertyAsDeleted(any()) } returns Unit

        viewModel.deleteProperty(property)
        advanceUntilIdle()

        println(viewModel.uiState.value)

        println(property.photos)

        property.photos.forEach { photo ->
            coVerify {
                photoRepository.markPhotoAsDeleted(photo)
            }
        }
        property.staticMap?.let {
            coVerify {
                staticMapRepository.markStaticMapAsDeleted(it)
            }
        }
        coVerify {
            crossRepository.markCrossRefsAsDeletedForProperty(property.universalLocalId)
        }
        coVerify {
            propertyRepository.markPropertyAsDeleted(property)
        }
        coVerify {
            syncScheduler.scheduleSync()
        }

        val state = viewModel.uiState.value
        assertTrue(state is AccountUiState.Success)
    }

    @Test
    fun deleteProperty_repositoryFails_returnsError() = runTest {
        val property = FakePropertyModel.property1

        coEvery { propertyRepository.markPropertyAsDeleted(any()) } throws RuntimeException("DB error")

        viewModel.deleteProperty(property)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Error)
        assertTrue((state as AccountUiState.Error).message.contains("Failed to delete property"))
    }

    @Test
    fun setError_updatesUiState() {
        viewModel.setError("Test error")

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Error)
        assertEquals("Test error", (state as AccountUiState.Error).message)
    }

    @Test
    fun resetState_callsCheckAndLoadUser_andUpdatesState() = runTest {
        val properties = listOf(property1)

        every { authRepository.currentUser?.uid } returns user1.firebaseUid
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)
        coEvery { userRepository.getUserById(user1.universalLocalId) } returns flowOf(user1)
        coEvery { propertyRepository.getFullPropertiesByUserIdAlphabetic(user1.universalLocalId) } returns flowOf(properties)

        viewModel.resetState()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is AccountUiState.Success)
    }



}