package com.dcac.realestatemanager.ui.propertyCreationPage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyCreationPage(
    viewModel: PropertyCreationViewModel = hiltViewModel(),
    onExit: () -> Unit,
    onInfoClick: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val stepState = state as? PropertyCreationUiState.StepState


    val currentStep = stepState?.currentStep ?: PropertyCreationStep.Intro

    val isNextEnabled = stepState?.isNextEnabled == true

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.handlePhotoPicked(context, uri)
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
                            text = stringResource(R.string.property_creation_title),
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
                                viewModel.createModelFromDraft()
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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentStep) {
                is PropertyCreationStep.Intro ->
                    Step1IntroScreen()
                is PropertyCreationStep.PropertyType ->
                    Step2TypeScreen(
                    selectedType = stepState?.draft?.type.orEmpty(),
                    onTypeSelected = { viewModel.updateType(it) }
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
                    )
                is PropertyCreationStep.PoiS ->
                    Step4PoiScreen(
                        poiList = stepState?.draft?.poiS ?: emptyList(),
                        onTypeSelected = { index, type -> viewModel.updatePoiType(index, type) },
                        onNameChanged = { index, name -> viewModel.updatePoiName(index, name) },
                        onStreetChanged = { index, street -> viewModel.updatePoiStreet(index, street)},
                        onPostalCodeChanged = { index, postal -> viewModel.updatePoiPostalCode(index, postal)},
                        onCityChanged = { index, city -> viewModel.updatePoiCity(index, city)},
                        onCountryChanged = { index, country -> viewModel.updatePoiCountry(index, country)}
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
                        onTitleChange = {viewModel.updateTitle(it)}
                    )
                is PropertyCreationStep.Photos ->
                    Step6PhotosScreen(
                        photos = stepState?.draft?.photos ?: emptyList(),
                        onPhotoClick = { index ->
                            viewModel.onPhotoCellClicked(index) {
                                launcher.launch("image/*")
                            }
                        },
                        onDeletePhoto = { index ->
                            viewModel.removePhotoAt(index)
                        }
                    )
                is PropertyCreationStep.StaticMap ->
                    Step7StaticMapScreen(
                        mapBytes = stepState?.staticMapImageBytes?.toByteArray(),
                        isLoading = stepState?.isLoadingMap == true,
                        onLoadMap = { viewModel.fetchStaticMap(context) }
                    )
                is PropertyCreationStep.Confirmation ->
                    Step8ConfirmationScreen(
                        draft = stepState?.draft ?: return@Scaffold
                    )

            }
        }
    }
}

