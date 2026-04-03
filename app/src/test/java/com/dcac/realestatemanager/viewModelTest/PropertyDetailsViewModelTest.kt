package com.dcac.realestatemanager.viewModelTest

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyPoiCrossModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeStaticMapModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsUiState
import kotlinx.coroutines.test.runTest
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsViewModel
import com.google.firebase.auth.FirebaseUser
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import io.mockk.just
import io.mockk.Runs
import android.net.Uri
import androidx.core.net.toUri
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val user1 = FakeUserModel.user1
    private val property1 = FakePropertyModel.property1
    private val photo1 = FakePhotoModel.photo1
    private val poi1 = FakePoiModel.poi1
    private val poi2 = FakePoiModel.poi2
    private val staticMap1 = FakeStaticMapModel.staticMap1
    private val crossRef1 = FakePropertyPoiCrossModel.cross1
    private val crossRef2 = FakePropertyPoiCrossModel.cross2

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val poiRepository = mockk<PoiRepository>(relaxed = true)
    private val staticMapRepository = mockk<StaticMapRepository>(relaxed = true)
    private val crossRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    private lateinit var viewModel: PropertyDetailsViewModel

    @Before
    fun setup() {
        viewModel = PropertyDetailsViewModel(
            authRepository,
            userRepository,
            propertyRepository,
            photoRepository,
            poiRepository,
            crossRepository,
            staticMapRepository,
            syncScheduler
        )
    }

    @Test
    fun loadPropertyDetails_success_shouldEmitSuccess() = runTest {
        val propertyId = property1.universalLocalId

        val firebaseUser = mockk<FirebaseUser>()
        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid

        coEvery { propertyRepository.getPropertyById(propertyId) } returns flowOf(property1)
        coEvery { userRepository.getUserById(property1.universalLocalUserId) } returns flowOf(user1)
        coEvery { photoRepository.getPhotosByPropertyId(propertyId) } returns flowOf(listOf(photo1))
        coEvery { crossRepository.getAllCrossRefs() } returns flowOf(listOf(crossRef1, crossRef2))
        coEvery { poiRepository.getAllPoiS() } returns flowOf(listOf(poi1, poi2))
        coEvery { staticMapRepository.getStaticMapByPropertyId(propertyId) } returns flowOf(staticMap1)
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)

        viewModel.loadPropertyDetails(propertyId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PropertyDetailsUiState.Success)

        state as PropertyDetailsUiState.Success
        assertEquals(propertyId, state.property.universalLocalId)
        assertEquals(1, state.property.photos.size)
        assertEquals(2, state.property.poiS.size)
        assertEquals(poi1.universalLocalId, state.property.poiS.first().universalLocalId)
        assertEquals(staticMap1, state.property.staticMap)
        assertEquals(user1.agentName, state.userName)
        assertTrue(state.isOwnedByCurrentUser)
    }

    @Test
    fun loadPropertyDetails_propertyNotFound_shouldEmitError() = runTest {
        val propertyId = property1.universalLocalId

        coEvery { propertyRepository.getPropertyById(propertyId) } returns flowOf(null)

        viewModel.loadPropertyDetails(propertyId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PropertyDetailsUiState.Error)
        assertEquals("Property not found", (state as PropertyDetailsUiState.Error).message)
    }

    @Test
    fun loadPropertyDetails_whenUserMissing_shouldUseUnknown() = runTest {
        val propertyId = property1.universalLocalId
        val firebaseUser = mockk<FirebaseUser>()

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns user1.firebaseUid

        coEvery { propertyRepository.getPropertyById(propertyId) } returns flowOf(property1)
        coEvery { userRepository.getUserById(property1.universalLocalUserId) } returns flowOf(null)
        coEvery { photoRepository.getPhotosByPropertyId(propertyId) } returns flowOf(emptyList())
        coEvery { crossRepository.getAllCrossRefs() } returns flowOf(emptyList())
        coEvery { poiRepository.getAllPoiS() } returns flowOf(emptyList())
        coEvery { staticMapRepository.getStaticMapByPropertyId(propertyId) } returns flowOf(null)
        coEvery { userRepository.getUserByFirebaseUid(user1.firebaseUid) } returns flowOf(user1)

        viewModel.loadPropertyDetails(propertyId)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PropertyDetailsUiState.Success
        assertEquals("Unknown", state.userName)
    }

    @Test
    fun loadPropertyDetails_shouldSetOwnedByCurrentUserToFalse() = runTest {
        val propertyId = property1.universalLocalId
        val firebaseUser = mockk<FirebaseUser>()
        val otherUser = FakeUserModel.user2

        every { authRepository.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns otherUser.firebaseUid

        coEvery { propertyRepository.getPropertyById(propertyId) } returns flowOf(property1)
        coEvery { userRepository.getUserById(property1.universalLocalUserId) } returns flowOf(user1)
        coEvery { photoRepository.getPhotosByPropertyId(propertyId) } returns flowOf(emptyList())
        coEvery { crossRepository.getAllCrossRefs() } returns flowOf(emptyList())
        coEvery { poiRepository.getAllPoiS() } returns flowOf(emptyList())
        coEvery { staticMapRepository.getStaticMapByPropertyId(propertyId) } returns flowOf(null)
        coEvery { userRepository.getUserByFirebaseUid(otherUser.firebaseUid) } returns flowOf(otherUser)

        viewModel.loadPropertyDetails(propertyId)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PropertyDetailsUiState.Success
        assertFalse(state.isOwnedByCurrentUser)
    }

    @Test
    fun loadPropertyDetails_repositoryThrows_shouldEmitError() = runTest {
        val propertyId = property1.universalLocalId

        coEvery { propertyRepository.getPropertyById(propertyId) } throws RuntimeException("boom")

        viewModel.loadPropertyDetails(propertyId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PropertyDetailsUiState.Error)
        assertTrue((state as PropertyDetailsUiState.Error).message.contains("boom"))
    }

    @Test
    fun deleteProperty_shouldMarkEverythingDeletedAndCallOnDeleted() = runTest {
        mockkStatic(Uri::class)

        try {
            val fakePhotoUri = mockk<Uri>(relaxed = true)
            val fakeMapUri = mockk<Uri>(relaxed = true)

            every { Uri.parse(photo1.uri) } returns fakePhotoUri
            every { Uri.parse(staticMap1.uri) } returns fakeMapUri
            every { fakePhotoUri.path } returns null
            every { fakeMapUri.path } returns null

            val property = property1.copy(
                photos = listOf(photo1),
                staticMap = staticMap1
            )

            var deletedCalled = false

            coEvery { photoRepository.markPhotoAsDeleted(photo1) } just Runs
            coEvery { staticMapRepository.markStaticMapAsDeleted(staticMap1) } just Runs
            coEvery { crossRepository.markCrossRefsAsDeletedForProperty(property.universalLocalId) } just Runs
            coEvery { propertyRepository.markPropertyAsDeleted(property) } just Runs
            coEvery { syncScheduler.scheduleSync() } just Runs

            viewModel.deleteProperty(property) {
                deletedCalled = true
            }

            advanceUntilIdle()

            coVerify(exactly = 1) { photoRepository.markPhotoAsDeleted(photo1) }
            coVerify(exactly = 1) { staticMapRepository.markStaticMapAsDeleted(staticMap1) }
            coVerify(exactly = 1) { crossRepository.markCrossRefsAsDeletedForProperty(property.universalLocalId) }
            coVerify(exactly = 1) { propertyRepository.markPropertyAsDeleted(property) }
            coVerify(exactly = 1) { syncScheduler.scheduleSync() }

            assertTrue(deletedCalled)
        } finally {
            unmockkStatic(Uri::class)
        }
    }

    @Test
    fun deleteProperty_withoutStaticMap_shouldStillDeleteProperty() = runTest {
        mockkStatic(Uri::class)

        try {
            val fakeUri = mockk<Uri>(relaxed = true)
            every { Uri.parse(photo1.uri) } returns fakeUri
            every { fakeUri.path } returns null

            val property = property1.copy(
                photos = listOf(photo1),
                staticMap = null
            )

            var deletedCalled = false

            viewModel.deleteProperty(property) {
                deletedCalled = true
            }

            advanceUntilIdle()

            coVerify { photoRepository.markPhotoAsDeleted(photo1) }
            coVerify(exactly = 0) { staticMapRepository.markStaticMapAsDeleted(any()) }
            coVerify { crossRepository.markCrossRefsAsDeletedForProperty(property.universalLocalId) }
            coVerify { propertyRepository.markPropertyAsDeleted(property) }
            coVerify { syncScheduler.scheduleSync() }

            assertTrue(deletedCalled)
        } finally {
            unmockkStatic(Uri::class)
        }
    }

    @Test
    fun deleteProperty_whenException_shouldNotCallOnDeleted() = runTest {
        val property = property1.copy(photos = listOf(photo1))

        var deletedCalled = false

        coEvery { photoRepository.markPhotoAsDeleted(photo1) } throws RuntimeException("delete failed")

        viewModel.deleteProperty(property) {
            deletedCalled = true
        }

        advanceUntilIdle()

        assertFalse(deletedCalled)
    }

}