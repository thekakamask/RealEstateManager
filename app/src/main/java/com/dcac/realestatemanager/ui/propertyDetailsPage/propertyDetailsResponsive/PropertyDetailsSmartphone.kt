package com.dcac.realestatemanager.ui.propertyDetailsPage.propertyDetailsResponsive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.dcac.realestatemanager.R
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dcac.realestatemanager.model.Photo
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsViewModel


@Composable
fun PropertyDetailsSmartphone(
    propertyId: String,
    viewModel: PropertyDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditSectionSelected: (EditSection, String) -> Unit,
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(propertyId) {
        viewModel.loadPropertyDetails(propertyId)
    }

    Scaffold(
        topBar = {
            PropertyDetailsTopBar(onBack = onBack)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {

            PropertyDetailsStateHost(
                uiState = uiState,
                onEditSectionSelected = onEditSectionSelected,
                onDeleteConfirmed = { property ->
                    viewModel.deleteProperty(
                        property = property,
                        onDeleted = onBack
                    )
                }
            ) { property, userName, isOwnedByCurrentUser, onEditClick ->

                PropertyDetailsSuccessContent(
                    property = property,
                    userName = userName
                )

                if (isOwnedByCurrentUser) {
                    FloatingActionButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.modify_24px),
                            contentDescription = stringResource(
                                R.string.details_page_modify_button_description
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailsTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.details_page_top_bar_back_button_content_description)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.real_estate_manager_logo),
                        contentDescription = stringResource(R.string.details_page_top_bar_app_icon_content_description),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    )
}

@Composable
fun PhotoSlider(photos: List<Photo>) {

    val pagerState = rememberPagerState(initialPage = 0) {
        photos.size
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val photo = photos[page]

            Image(
                painter = rememberAsyncImagePainter(model = photo.uri),
                contentDescription = photo.description
                    ?: stringResource(R.string.property_details_page_image_content_description_empty),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = stringResource(
                R.string.property_details_page_photo_pager_text,
                pagerState.currentPage + 1,
                photos.size
            ),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(vertical = 28.dp, horizontal = 16.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        val currentPhoto = photos.getOrNull(pagerState.currentPage)
        val description = currentPhoto?.description

        if (!description.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(vertical = 28.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = description,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (EditSection) -> Unit,
    onDeleteProperty: () -> Unit
) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.edit_section_title_text), style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            EditSection.entries.forEach { section ->
                Text(
                    text = stringResource(section.labelRes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOptionSelected(section)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        onDeleteProperty()
                        onDismiss()
                    },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.error
                        )
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_24px),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.delete_section_button_text))
                }
            }
        }
    }
}