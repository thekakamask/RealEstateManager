package com.dcac.realestatemanager.ui.propertyCreationPage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCreationPage(
    viewModel: PropertyCreationViewModel = hiltViewModel(),
    mode: PropertyCreationMode = PropertyCreationMode.CREATE,
    sectionToEdit: EditSection? = null,
    propertyToEdit: Property? = null,
    onExit: () -> Unit,
    onInfoClick: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val stepState = state as? PropertyCreationUiState.StepState


    val currentStep = stepState?.currentStep ?: PropertyCreationStep.Intro
    val isNextEnabled = stepState?.isNextEnabled == true
    val context = LocalContext.current
    val currency = CurrencyHelper.LocalCurrency.current
    val editingIndex by viewModel.editingPhotoIndex.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.handlePhotoPicked(context, uri)
        }
    }

    LaunchedEffect(propertyToEdit?.universalLocalId) {
        if (mode == PropertyCreationMode.EDIT_SECTION && propertyToEdit != null && sectionToEdit != null) {
            viewModel.loadDraftFromProperty(propertyToEdit, section = sectionToEdit, currency = currency)

            val step = when (sectionToEdit) {
                EditSection.TYPE -> PropertyCreationStep.PropertyType
                EditSection.ADDRESS -> PropertyCreationStep.Address
                EditSection.DESCRIPTION -> PropertyCreationStep.Description
                EditSection.PHOTOS -> PropertyCreationStep.Photos
                EditSection.POIS -> PropertyCreationStep.PoiS
            }

            viewModel.updateStep(step)
        }
    }

    LaunchedEffect(state) {
        if (state is PropertyCreationUiState.Success) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (currentStep == PropertyCreationStep.Intro) {
                            IconButton(
                                onClick = { onExit() },
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.property_creation_back_button_content_description)
                                )
                            }
                        }
                        Text(
                            text = viewModel.topBarTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Text(
                            text = stringResource(R.string.property_creation_help_button),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable { onInfoClick() }
                                .padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    if (mode == PropertyCreationMode.EDIT_SECTION) {
                        TextButton(onClick = { onExit() }) {
                            Text(
                                stringResource(R.string.property_creation_cancel_button),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.updateModelFromDraft(context, currency)
                                onFinish()
                            },
                            enabled = isNextEnabled
                        ) {
                            Text(
                                stringResource(R.string.property_creation_finish_button),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentStep != PropertyCreationStep.Intro) {
                                TextButton(onClick = { viewModel.goToPrevious() }) {
                                    Text(
                                        stringResource(R.string.property_creation_previous_button),
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier)
                            }
                            if (currentStep == PropertyCreationStep.Confirmation){
                                TextButton(
                                    onClick = {
                                        viewModel.createModelFromDraft(context, currency)
                                    },
                                    enabled = isNextEnabled
                                ) {
                                    Text(
                                        stringResource(R.string.property_creation_finish_button),
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            } else {
                                TextButton(
                                    onClick = { viewModel.goToNext() },
                                    enabled = isNextEnabled
                                ) {
                                    Text(
                                        stringResource(R.string.property_creation_next_button),
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (state !is PropertyCreationUiState.Success) {
            Box(modifier = Modifier.padding(
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                top = innerPadding.calculateTopPadding(),
                bottom = 0.dp
            )) {

                when (currentStep) {
                    is PropertyCreationStep.Intro ->
                        Step1IntroScreen()
                    is PropertyCreationStep.PropertyType ->
                        Step2TypeScreen(
                            selectedType = stepState?.draft?.type.orEmpty(),
                            onTypeSelected = { viewModel.updateType(it) },
                            bottomInset = innerPadding.calculateBottomPadding()
                        )
                    is PropertyCreationStep.Address ->
                        Step3AddressScreen(
                            street = stepState?.draft?.street.orEmpty(),
                            city = stepState?.draft?.city.orEmpty(),
                            postalCode = stepState?.draft?.postalCode.orEmpty(),
                            country = stepState?.draft?.country.orEmpty(),
                            onStreetChange = { viewModel.updateStreet(it) },
                            onPostalCodeChange = { viewModel.updatePostalCode(it) },
                            onCityChange = { viewModel.updateCity(it) },
                            onCountryChange = { viewModel.updateCountry(it) },
                            bottomInset = innerPadding.calculateBottomPadding()
                        )
                    is PropertyCreationStep.PoiS ->
                        Step4PoiScreen(
                            poiList = stepState?.draft?.poiS ?: emptyList(),
                            onTypeSelected = { index, type -> viewModel.updatePoiType(index, type) },
                            onNameChanged = { index, name -> viewModel.updatePoiName(index, name) },
                            onStreetChanged = { index, street -> viewModel.updatePoiStreet(index, street)},
                            onPostalCodeChanged = { index, postal -> viewModel.updatePoiPostalCode(index, postal)},
                            onCityChanged = { index, city -> viewModel.updatePoiCity(index, city)},
                            onCountryChanged = { index, country -> viewModel.updatePoiCountry(index, country)},
                            bottomInset = innerPadding.calculateBottomPadding()
                        )
                    is PropertyCreationStep.Description ->
                        Step5DescriptionScreen(
                            title = stepState?.draft?.title.orEmpty(),
                            price = stepState?.draft?.price ?: 0,
                            surface = stepState?.draft?.surface ?: 0,
                            rooms = stepState?.draft?.rooms ?: 0,
                            description = stepState?.draft?.description.orEmpty(),
                            onPriceChange = { viewModel.updatePrice(it) },
                            onSurfaceChange = { viewModel.updateSurface(it) },
                            onRoomsChange = { viewModel.updateRooms(it) },
                            onDescriptionChange = { viewModel.updateDescription(it) },
                            onTitleChange = {viewModel.updateTitle(it)},
                            isSold = stepState?.draft?.isSold ?: false,
                            saleDate = stepState?.draft?.saleDate,
                            onIsSoldChange = {viewModel.updateIsSold(it) },
                            onSaleDateChange = {viewModel.updateSaleDate(it) },
                            bottomInset = innerPadding.calculateBottomPadding()
                        )
                    is PropertyCreationStep.Photos ->
                        Step6PhotosScreen(
                            photos = stepState?.draft?.photos ?: emptyList(),
                            onPhotoClick = { index ->
                                viewModel.onPhotoCellClicked(index) {
                                    launcher.launch(context.getString(R.string.property_creation_launcher_image))
                                }
                            },
                            onEditPhoto = { index ->
                                viewModel.startEditingPhoto(index)
                            },
                            onDeletePhoto = { index ->
                                viewModel.removePhotoAt(index)
                            },
                            bottomInset = innerPadding.calculateBottomPadding()
                        )
                    is PropertyCreationStep.StaticMap ->
                        Step7StaticMapScreen(
                            mapBytes = stepState?.staticMapImageBytes?.toByteArray(),
                            isLoading = stepState?.isLoadingMap == true,
                            onLoadMap = { viewModel.fetchStaticMap(context) },
                            )
                    is PropertyCreationStep.Confirmation ->
                        Step8ConfirmationScreen(
                            draft = stepState?.draft ?: return@Scaffold,
                            bottomInset = innerPadding.calculateBottomPadding()
                        )

                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        editingIndex?.let { index ->
            val photo = (state as? PropertyCreationUiState.StepState)
                ?.draft
                ?.photos
                ?.getOrNull(index)

            if (photo != null && photo.uri.isNotBlank()) {
                PhotoEditDialog(
                    photo = photo,
                    onDismiss = {
                        viewModel.stopEditingPhoto()
                    },
                    onSave = { desc ->
                        viewModel.updatePhotoAt(
                            index = index,
                            uri = photo.uri.toUri(),
                            description = desc
                        )
                        viewModel.stopEditingPhoto()
                    }
                )
            }
        }

    }
}

