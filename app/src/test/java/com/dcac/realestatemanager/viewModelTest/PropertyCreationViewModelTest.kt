package com.dcac.realestatemanager.viewModelTest

import android.content.Context
import android.net.Uri
import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoRepository
import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiRepository
import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyRepository
import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossRepository
import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapRepository
import com.dcac.realestatemanager.data.offlineDatabase.user.UserRepository
import com.dcac.realestatemanager.data.sync.SyncScheduler
import com.dcac.realestatemanager.data.userConnection.AuthRepository
import com.dcac.realestatemanager.fakeData.fakeModel.FakePropertyModel
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel
import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationStep
import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationUiState
import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationViewModel
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.*
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
import org.threeten.bp.LocalDate
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.propertyCreationPage.geocodeAddress
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyCreationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeUserModel1 = FakeUserModel.user1
    private val fakePropertyModel1 = FakePropertyModel.property1

    private val type = "House"
    private val country = "France"
    private val city = "Paris"
    private val postalCode = "75001"
    private val address = "10 rue Victor Hugo"
    private val title = "Beautiful apartment"
    private val price = 300000
    private val surface = 80
    private val rooms = 3
    private val description = "Very nice apartment"
    private val poiType = "School"
    private val poiType2 = "Food"
    private val poiName = "Primary School"
    private val poiName2 = "Bakery"
    private val poiName3 = "Restaurant"
    private val poiStreet = "5 avenue Niel"
    private val poiPostalCode = "75008"
    private val photoDescription = "Living room"
    private val photoDescription2 = "Kitchen"

    private val propertyRepository = mockk<PropertyRepository>(relaxed = true)
    private val poiRepository = mockk<PoiRepository>(relaxed = true)
    private val photoRepository = mockk<PhotoRepository>(relaxed = true)
    private val crossRefRepository = mockk<PropertyPoiCrossRepository>(relaxed = true)
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val staticMapRepository = mockk<StaticMapRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    val uri = mockk<Uri>()
    val firebaseUser = mockk<FirebaseUser>()

    private lateinit var viewModel: PropertyCreationViewModel

    @Before
    fun setup() {
        viewModel = PropertyCreationViewModel(
            context,
            propertyRepository,
            poiRepository,
            photoRepository,
            crossRefRepository,
            authRepository,
            staticMapRepository,
            userRepository,
            syncScheduler
        )
    }

    @Test
    fun updateStep_shouldUpdateCurrentStep() = runTest {
        viewModel.updateStep(PropertyCreationStep.Address)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(PropertyCreationStep.Address, state.currentStep)
    }

    @Test
    fun isNextEnabled_introStep_shouldBeTrue() = runTest {
        assertTrue(viewModel.isNextEnabled)
    }

    @Test
    fun updateStep_propertyTypeWithoutValue_shouldDisableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.PropertyType)

        assertFalse(viewModel.isNextEnabled)
    }

    @Test
    fun updateType_propertyTypeWithValue_shouldEnableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.PropertyType)

        viewModel.updateType(type)

        assertTrue(viewModel.isNextEnabled)
    }

    @Test
    fun updateAddress_withCompleteFields_shouldEnableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.Address)

        viewModel.updateStreet(address)
        viewModel.updateCity(city)
        viewModel.updatePostalCode(postalCode)
        viewModel.updateCountry(country)

        assertTrue(viewModel.isNextEnabled)
    }

    @Test
    fun updateAddress_withMissingField_shouldDisableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.Address)

        viewModel.updateStreet(address)
        viewModel.updateCity(city)
        viewModel.updatePostalCode(postalCode)

        assertFalse(viewModel.isNextEnabled)
    }

    @Test
    fun descriptionStep_withValidDraft_shouldEnableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.Description)

        viewModel.updateTitle(title)
        viewModel.updatePrice(price)
        viewModel.updateSurface(surface)
        viewModel.updateRooms(rooms)
        viewModel.updateDescription(description)

        assertTrue(viewModel.isNextEnabled)
    }

    @Test
    fun descriptionStep_withInvalidPrice_shouldDisableNext() = runTest {
        viewModel.updateStep(PropertyCreationStep.Description)

        viewModel.updateTitle(title)
        viewModel.updatePrice(0)
        viewModel.updateSurface(surface)
        viewModel.updateRooms(rooms)
        viewModel.updateDescription(description)

        assertFalse(viewModel.isNextEnabled)
    }

    @Test
    fun goToNext_shouldNavigateCorrectly() = runTest {
        viewModel.goToNext()

        var state = viewModel.uiState.value as PropertyCreationUiState.StepState
        assertEquals(PropertyCreationStep.PropertyType, state.currentStep)

        viewModel.goToNext()

        state = viewModel.uiState.value as PropertyCreationUiState.StepState
        assertEquals(PropertyCreationStep.Address, state.currentStep)
    }

    @Test
    fun goToPrevious_shouldNavigateBackCorrectly() = runTest {
        viewModel.updateStep(PropertyCreationStep.Description)

        viewModel.goToPrevious()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(PropertyCreationStep.PoiS, state.currentStep)
    }

    @Test
    fun goToPrevious_fromIntro_shouldStayOnIntro() = runTest {
        viewModel.goToPrevious()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(PropertyCreationStep.Intro, state.currentStep)
    }

    @Test
    fun goToNext_fromConfirmation_shouldStayOnConfirmation() = runTest {
        viewModel.updateStep(PropertyCreationStep.Confirmation)

        viewModel.goToNext()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(PropertyCreationStep.Confirmation, state.currentStep)
    }

    @Test
    fun updateType_shouldUpdateDraftType() = runTest {
        viewModel.updateType(type)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(type, state.draft.type)
    }

    @Test
    fun updateStreet_shouldUpdateDraftStreet() = runTest {
        viewModel.updateStreet(address)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(address, state.draft.street)
    }

    @Test
    fun updateCity_shouldUpdateDraftCity() = runTest {
        viewModel.updateCity(city)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(city, state.draft.city)
    }

    @Test
    fun updatePostalCode_shouldUpdateDraftPostalCode() = runTest {
        viewModel.updatePostalCode(postalCode)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(postalCode, state.draft.postalCode)
    }

    @Test
    fun updateCountry_shouldUpdateDraftCountry() = runTest {
        viewModel.updateCountry(city)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(city, state.draft.country)
    }

    @Test
    fun updateTitle_shouldUpdateDraftTitle() = runTest {
        viewModel.updateTitle(title)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(title, state.draft.title)
    }

    @Test
    fun updatePrice_shouldUpdateDraftPrice() = runTest {
        viewModel.updatePrice(price)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(price, state.draft.price)
    }

    @Test
    fun updateSurface_shouldUpdateDraftSurface() = runTest {
        viewModel.updateSurface(surface)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(surface, state.draft.surface)
    }

    @Test
    fun updateRooms_shouldUpdateDraftRooms() = runTest {
        viewModel.updateRooms(rooms)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(rooms, state.draft.rooms)
    }

    @Test
    fun updateDescription_shouldUpdateDraftDescription() = runTest {
        viewModel.updateDescription(description)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(description, state.draft.description)
    }

    @Test
    fun updateIsSold_true_shouldUpdateFlag() = runTest {
        viewModel.updateIsSold(true)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertTrue(state.draft.isSold)
    }

    @Test
    fun updateIsSold_false_shouldClearSaleDate() = runTest {
        val date = LocalDate.of(2024, 1, 15)

        viewModel.updateSaleDate(date)
        viewModel.updateIsSold(false)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertFalse(state.draft.isSold)
        assertEquals(null, state.draft.saleDate)
    }

    @Test
    fun updateSaleDate_shouldUpdateDraftSaleDate() = runTest {
        val date = LocalDate.of(2024, 1, 15)

        viewModel.updateSaleDate(date)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(date, state.draft.saleDate)
    }

    @Test
    fun updatePoiType_shouldCreatePoiAndUpdateType() = runTest {
        viewModel.updatePoiType(0, poiType)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(poiType, state.draft.poiS[0].type)
    }

    @Test
    fun updatePoiName_shouldUpdateName() = runTest {
        viewModel.updatePoiName(0, poiName)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(poiName, state.draft.poiS[0].name)
    }

    @Test
    fun updatePoiAtIndex2_shouldExpandList() = runTest {
        viewModel.updatePoiName(2, poiName2)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(3, state.draft.poiS.size)
        assertEquals(poiName2, state.draft.poiS[2].name)
    }

    @Test
    fun updatePoiAddressFields_shouldUpdateCorrectly() = runTest {
        viewModel.updatePoiStreet(0, poiStreet)
        viewModel.updatePoiCity(0, city)
        viewModel.updatePoiPostalCode(0, poiPostalCode)
        viewModel.updatePoiCountry(0, country)

        val poi = (viewModel.uiState.value as PropertyCreationUiState.StepState)
            .draft.poiS[0]

        assertEquals(poiStreet, poi.street)
        assertEquals(city, poi.city)
        assertEquals(poiPostalCode, poi.postalCode)
        assertEquals(country, poi.country)
    }

    @Test
    fun updatePoiType_shouldUpdatePoiType() = runTest {
        viewModel.updatePoiType(0, poiType)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(poiType, state.draft.poiS[0].type)
    }

    @Test
    fun updatePoiName_shouldUpdatePoiName() = runTest {
        viewModel.updatePoiName(0, poiName)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(poiName, state.draft.poiS[0].name)
    }

    @Test
    fun updatePoiStreet_shouldUpdatePoiStreet() = runTest {
        viewModel.updatePoiStreet(0, poiStreet)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(poiStreet, state.draft.poiS[0].street)
    }

    @Test
    fun updatePoiCity_shouldUpdatePoiCity() = runTest {
        viewModel.updatePoiCity(0, city)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(city, state.draft.poiS[0].city)
    }

    @Test
    fun updatePoiPostalCode_shouldUpdatePoiPostalCode() = runTest {
        viewModel.updatePoiPostalCode(0, poiPostalCode)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(poiPostalCode, state.draft.poiS[0].postalCode)
    }

    @Test
    fun updatePoiCountry_shouldUpdatePoiCountry() = runTest {
        viewModel.updatePoiCountry(0, country)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.poiS.size)
        assertEquals(country, state.draft.poiS[0].country)
    }

    @Test
    fun updatePoiFields_shouldKeepPreviousValues() = runTest {
        viewModel.updatePoiName(0, poiName2)
        viewModel.updatePoiType(0, poiType2)
        viewModel.updatePoiCity(0, city)

        val poi = (viewModel.uiState.value as PropertyCreationUiState.StepState)
            .draft.poiS[0]

        assertEquals(poiName2, poi.name)
        assertEquals(poiType2, poi.type)
        assertEquals(city, poi.city)
    }

    @Test
    fun updatePoiAtHigherIndex_shouldExpandList() = runTest {
        viewModel.updatePoiName(2, poiName3)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(3, state.draft.poiS.size)
        assertEquals(poiName3, state.draft.poiS[2].name)
    }

    @Test
    fun startEditingPhoto_shouldUpdateEditingIndex() = runTest {
        viewModel.startEditingPhoto(2)

        assertEquals(2, viewModel.editingPhotoIndex.value)
    }

    @Test
    fun stopEditingPhoto_shouldResetEditingIndex() = runTest {
        viewModel.startEditingPhoto(2)

        viewModel.stopEditingPhoto()

        assertEquals(null, viewModel.editingPhotoIndex.value)
    }

    @Test
    fun updatePhotoAt_shouldInsertPhotoAtIndex0() = runTest {
        every { uri.toString() } returns "file://photo1.jpg"

        viewModel.updatePhotoAt(
            index = 0,
            uri = uri,
            description = photoDescription
        )

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(1, state.draft.photos.size)
        assertEquals("file://photo1.jpg", state.draft.photos[0].uri)
        assertEquals(photoDescription, state.draft.photos[0].description)
    }

    @Test
    fun updatePhotoAt_shouldExpandListForHigherIndex() = runTest {
        every { uri.toString() } returns "file://photo3.jpg"

        viewModel.updatePhotoAt(
            index = 2,
            uri = uri,
            description = photoDescription2
        )

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(3, state.draft.photos.size)
        assertEquals("file://photo3.jpg", state.draft.photos[2].uri)
        assertEquals(photoDescription2, state.draft.photos[2].description)
    }

    @Test
    fun removePhotoAt_shouldClearPhotoUri() = runTest {
        every { uri.toString() } returns "file://photo1.jpg"

        viewModel.updatePhotoAt(0, uri, photoDescription)

        viewModel.removePhotoAt(0)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals("", state.draft.photos[0].uri)
    }

    @Test
    fun removePhotoAt_whenEditingSameIndex_shouldResetEditingIndex() = runTest {
        every { uri.toString() } returns "file://photo1.jpg"

        viewModel.updatePhotoAt(0, uri, photoDescription)
        viewModel.startEditingPhoto(0)

        viewModel.removePhotoAt(0)

        assertEquals(null, viewModel.editingPhotoIndex.value)
    }

    @Test
    fun removePhotoAt_invalidIndex_shouldDoNothing() = runTest {
        viewModel.removePhotoAt(5)

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertTrue(state.draft.photos.isEmpty())
    }

    @Test
    fun onPhotoCellClicked_shouldLaunchPicker() = runTest {
        var pickerLaunched = false

        viewModel.onPhotoCellClicked(1) {
            pickerLaunched = true
        }

        assertTrue(pickerLaunched)
    }

    @Test
    fun handlePhotoPicked_withoutPendingIndex_shouldDoNothing() = runTest {
        val before = viewModel.uiState.value as PropertyCreationUiState.StepState

        viewModel.handlePhotoPicked(context, uri)

        val after = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(before.draft.photos, after.draft.photos)
    }

    @Test
    fun fetchStaticMap_withBlankAddress_shouldStopLoading() = runTest {
        viewModel.fetchStaticMap(context)

        advanceUntilIdle()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertFalse(state.isLoadingMap)
        assertEquals(null, state.staticMapImageBytes)
    }

    @Test
    fun fetchStaticMap_success_shouldUpdateDraftAndUiState() = runTest {
        val bytes = byteArrayOf(1, 2, 3)
        val savedPath = "file://static_map.png"

        viewModel.updateStreet(address)
        viewModel.updateCity(city)
        viewModel.updatePostalCode(postalCode)
        viewModel.updateCountry(country)

        coEvery {
            staticMapRepository.getStaticMapImage(any())
        } returns bytes

        coEvery {
            staticMapRepository.saveStaticMapToLocal(any(), any(), bytes)
        } returns savedPath

        viewModel.fetchStaticMap(context)

        advanceUntilIdle()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertFalse(state.isLoadingMap)
        assertEquals(bytes.toList(), state.staticMapImageBytes)
        assertEquals(savedPath, state.draft.staticMap?.uri)
    }

    @Test
    fun createModelFromDraft_success_shouldUpdateUiStateToSuccess() = runTest {
        val fixedDate = LocalDate.of(2024, 1, 15)

        mockkStatic(::geocodeAddress)
        mockkStatic(LocalDate::class)

        try {
            coEvery { geocodeAddress(any(), any()) } returns null
            every { LocalDate.now() } returns fixedDate

            every { authRepository.currentUser } returns firebaseUser
            every { firebaseUser.uid } returns fakeUserModel1.firebaseUid

            coEvery {
                userRepository.getUserByFirebaseUid(fakeUserModel1.firebaseUid)
            } returns flowOf(fakeUserModel1)

            coEvery { poiRepository.insertPoiSInsertFromUi(any()) } returns emptyList()
            coEvery { propertyRepository.insertPropertyFromUI(any()) } just Runs
            coEvery { photoRepository.insertPhotosInsertFromUI(any()) } just Runs
            coEvery { syncScheduler.scheduleSync() } just Runs

            viewModel.updateStreet(address)
            viewModel.updateCity(city)
            viewModel.updatePostalCode(postalCode)
            viewModel.updateCountry(country)
            viewModel.updateType(type)
            viewModel.updateTitle(title)
            viewModel.updatePrice(300000)
            viewModel.updateSurface(120)
            viewModel.updateRooms(4)
            viewModel.updateDescription(description)

            viewModel.createModelFromDraft(context, "USD")

            advanceUntilIdle()

            println("UI STATE = ${viewModel.uiState.value}")

            coVerify(exactly = 1) { propertyRepository.insertPropertyFromUI(any()) }
            coVerify(exactly = 1) { photoRepository.insertPhotosInsertFromUI(any()) }
            coVerify(exactly = 1) { syncScheduler.scheduleSync() }

            assertTrue(viewModel.uiState.value is PropertyCreationUiState.Success)

        } finally {
            unmockkStatic(::geocodeAddress)
            unmockkStatic(LocalDate::class)
        }
    }

    @Test
    fun createModelFromDraft_noLocalUser_shouldReturnError() = runTest {
        every { authRepository.currentUser?.uid } returns fakeUserModel1.firebaseUid

        coEvery {
            userRepository.getUserByFirebaseUid(fakeUserModel1.firebaseUid)
        } returns flowOf(null)

        viewModel.createModelFromDraft(context, "USD")

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is PropertyCreationUiState.Error)
    }

    @Test
    fun loadDraftFromProperty_shouldPopulateDraft() = runTest {
        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.TYPE,
            "USD"
        )

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(fakePropertyModel1.title, state.draft.title)
        assertEquals(fakePropertyModel1.type, state.draft.type)
        assertEquals(fakePropertyModel1.price, state.draft.price)
    }

    @Test
    fun updateModelFromDraft_typeSection_shouldUpdateProperty() = runTest {
        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.TYPE,
            "USD"
        )

        viewModel.updateType(type)

        viewModel.updateModelFromDraft(context, "USD")

        advanceUntilIdle()

        coVerify {
            propertyRepository.updatePropertyFromUI(
                match { it.type == type }
            )
        }

        assertTrue(
            viewModel.uiState.value is PropertyCreationUiState.Success
        )
    }

    @Test
    fun updateModelFromDraft_descriptionSection_shouldUpdateFields() = runTest {
        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.DESCRIPTION,
            "USD"
        )

        viewModel.updateTitle("New Title")
        viewModel.updatePrice(500000)

        viewModel.updateModelFromDraft(context, "USD")

        advanceUntilIdle()

        coVerify {
            propertyRepository.updatePropertyFromUI(
                match {
                    it.title == "New Title" &&
                            it.price == 500000
                }
            )
        }
    }

    @Test
    fun resetState_shouldRestoreInitialStepState() = runTest {
        viewModel.updateType(type)
        viewModel.updateStreet(address)
        viewModel.updateStep(PropertyCreationStep.Description)

        viewModel.resetState()

        val state = viewModel.uiState.value as PropertyCreationUiState.StepState

        assertEquals(PropertyCreationStep.Intro, state.currentStep)
        assertEquals("", state.draft.type)
        assertEquals("", state.draft.street)
        assertTrue(state.isNextEnabled)
    }

    @Test
    fun topBarTitle_withoutPropertyToEdit_shouldReturnCreationTitle() = runTest {
        every {
            context.getString(R.string.property_creation_title)
        } returns "Create Property"

        val result = viewModel.topBarTitle

        assertEquals("Create Property", result)
    }

    @Test
    fun topBarTitle_withTypeSection_shouldReturnTypeModificationTitle() = runTest {
        every {
            context.getString(R.string.property_creation_type_modification_title)
        } returns "Edit Type"

        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.TYPE,
            "USD"
        )

        val result = viewModel.topBarTitle

        assertEquals("Edit Type", result)
    }

    @Test
    fun topBarTitle_withAddressSection_shouldReturnAddressTitle() = runTest {
        every {
            context.getString(R.string.property_creation_address_modification_title)
        } returns "Edit Address"

        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.ADDRESS,
            "USD"
        )

        val result = viewModel.topBarTitle

        assertEquals("Edit Address", result)
    }

    @Test
    fun topBarTitle_withDescriptionSection_shouldReturnDescriptionTitle() = runTest {
        every {
            context.getString(R.string.property_creation_description_modification_title)
        } returns "Edit Description"

        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.DESCRIPTION,
            "USD"
        )

        val result = viewModel.topBarTitle

        assertEquals("Edit Description", result)
    }

    @Test
    fun topBarTitle_withPhotosSection_shouldReturnPhotosTitle() = runTest {
        every {
            context.getString(R.string.property_creation_photo_modification_title)
        } returns "Edit Photos"

        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.PHOTOS,
            "USD"
        )

        val result = viewModel.topBarTitle

        assertEquals("Edit Photos", result)
    }

    @Test
    fun topBarTitle_withPoiSSection_shouldReturnPoiSTitle() = runTest {
        every {
            context.getString(R.string.property_creation_poi_modification_title)
        } returns "Edit POIs"

        viewModel.loadDraftFromProperty(
            fakePropertyModel1,
            EditSection.POIS,
            "USD"
        )

        val result = viewModel.topBarTitle

        assertEquals("Edit POIs", result)
    }

}