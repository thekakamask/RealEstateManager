package com.dcac.realestatemanager.ui.propertyDetailsPage

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
import com.dcac.realestatemanager.utils.Utils.calculatePricePerSquareMeter
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper

@Composable
fun PropertyDetailsPage(
    propertyId: String,
    viewModel: PropertyDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditSectionSelected: (EditSection, String) -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val showEditSheet = remember { mutableStateOf(false) }
    val propertyLocalId = (uiState as? PropertyDetailsUiState.Success)?.property?.universalLocalId
    val currency = CurrencyHelper.LocalCurrency.current

    LaunchedEffect(propertyId) {
        viewModel.loadPropertyDetails(propertyId)
    }

    val isOwnedByCurrentUser = (uiState as? PropertyDetailsUiState.Success)?.isOwnedByCurrentUser ?: false
    if (showEditSheet.value) {
        EditPropertyBottomSheet(
            onDismiss = { showEditSheet.value = false },
            onOptionSelected = { section ->
                showEditSheet.value = false
                if (propertyLocalId != null) {
                    onEditSectionSelected(section, propertyLocalId)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            PropertyDetailsTopBar(onBack = onBack)

        },
        floatingActionButton = {
            if (isOwnedByCurrentUser && propertyLocalId != null) {
                FloatingActionButton(
                    onClick = { showEditSheet.value = true },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 32.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.modify_24px),
                        contentDescription = stringResource(R.string.details_page_modify_button_description),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PropertyDetailsUiState.Loading -> {
                    Text(
                        text = stringResource(R.string.details_page_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is PropertyDetailsUiState.Error -> {
                    Text(
                        text = stringResource(
                            R.string.property_details_page_ui_state_error,
                            state.message
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is PropertyDetailsUiState.Success -> {
                    val property = state.property
                    val userName= state.userName

                    val displayPrice = if (currency == "EUR") {
                        CurrencyHelper.convertDollarToEuro(property.price)
                    } else {
                        property.price
                    }

                    val formattedPrice = stringResource(
                        id = CurrencyHelper.getPropertyDetailsPagePropertyPriceText(currency),
                        displayPrice
                    )

                    val pricePerSquareMeter = calculatePricePerSquareMeter(displayPrice, property.surface)

                    val formattedPricePerSquareMeter = stringResource(
                        id = CurrencyHelper.getPropertyDetailsPagePropertyPriceSquareText(currency),
                        pricePerSquareMeter
                    )


                    property.photos.forEach {
                        println(
                            stringResource(
                                R.string.property_details_page_debug_photo_uri,
                                it.uri
                            ))
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                PhotoSlider(photos = property.photos)
                            }
                        }
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-22).dp),
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 8.dp,
                                tonalElevation = 3.dp,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = stringResource(
                                            R.string.property_details_page_property_entry_date_text,
                                            property.entryDate
                                        ),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = property.saleDate?.let {
                                                stringResource(
                                                    R.string.property_details_page_property_sale_date_text,
                                                    it
                                                ) } ?: stringResource(
                                                R.string.details_page_text_available
                                            ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Icon(
                                        painter = painterResource(id = getIconForPropertyType(property.type)),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = property.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text(text = formattedPrice,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(text = formattedPricePerSquareMeter,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Text(text = stringResource(
                                            R.string.property_details_page_property_surface_text,
                                            property.surface
                                        ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(text = stringResource(
                                            R.string.property_details_page_property_rooms_text,
                                            property.rooms
                                        ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = property.address,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.user_icon),
                                            contentDescription = null,
                                            tint = Color.Unspecified
                                            // tint = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = userName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )

                                    if (property.poiS.isNotEmpty()) {
                                        property.poiS.take(5).forEach { poi ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = getIconForPoiType(poi.type)),
                                                    contentDescription = poi.type,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = poi.name,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )

                                    Text(
                                        text = property.description,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 16.dp),
                                        thickness = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )

                                    val staticMapUriStr = property.staticMap?.uri

                                    if (!staticMapUriStr.isNullOrBlank()) {
                                        val bitmap = remember(staticMapUriStr) {
                                            val uri = staticMapUriStr.toUri()
                                            when (uri.scheme) {
                                                "file" -> BitmapFactory.decodeFile(uri.path)?.asImageBitmap()
                                                null, "" -> BitmapFactory.decodeFile(staticMapUriStr)?.asImageBitmap() // si c'est déjà un path brut
                                                else -> null // http/https -> pas géré par decodeFile
                                            }
                                        }

                                        bitmap?.let {
                                            Image(
                                                bitmap = it,
                                                contentDescription = stringResource(R.string.details_page_static_map_content_description),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(500.dp)
                                            )
                                        } ?: Text(
                                            text = stringResource(R.string.details_page_text_static_map_not_available),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                }
                            }
                        }
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
                contentDescription = photo.description ?: stringResource(R.string.property_details_page_image_content_description_empty),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPropertyBottomSheet(
    onDismiss: () -> Unit,
    onOptionSelected: (EditSection) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
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
        }
    }
}