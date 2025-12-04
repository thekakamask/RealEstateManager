package com.dcac.realestatemanager.ui.userPropertiesPage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.ui.filter.FilterSheetContent
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.ui.filter.toUiState
import com.dcac.realestatemanager.utils.Utils.calculatePricePerSquareMeter
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPropertiesPage(
    viewModel: UserPropertiesViewModel = hiltViewModel(),
    onEditProperty: (Long) -> Unit,
    onBack: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    LaunchedEffect(Unit) {
        val userId = viewModel.getUserIdOrNull()
        userId?.let {
            viewModel.applyFilters(it, PropertyFilters())
        }
    }

    when (uiState) {
        is UserPropertiesUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UserPropertiesUiState.Success -> {
            val state = uiState as UserPropertiesUiState.Success

            val properties = state.properties
            LaunchedEffect(state.showFilterSheet) {
                if (state.showFilterSheet) bottomSheetState.show() else bottomSheetState.hide()
            }

            if (state.showFilterSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleFilterSheet(false) },
                    sheetState = bottomSheetState
                ) {

                    FilterSheetContent(
                        filterUi = state.filters.toUiState(),
                        onApply = { filters ->
                            viewModel.applyFilters(filters)
                            viewModel.toggleFilterSheet(false)
                        },
                        onReset = {
                            viewModel.resetFilters()
                            viewModel.toggleFilterSheet(false)
                        }
                    )
                }
            }
            UserPropertiesListContent(
                properties = properties,
                onEditProperty = onEditProperty,
                onBack = onBack,
                viewModel = viewModel
            )
        }

        is UserPropertiesUiState.Error -> {
            val errorMessage = (uiState as UserPropertiesUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        }

        UserPropertiesUiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.property_list_screen_no_data_to_display))
            }
        }
    }
}

@Composable
fun UserPropertiesListContent(
    properties: List<Property>,
    onEditProperty: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: UserPropertiesViewModel
) {
    Scaffold(
        topBar = {
            UserPropertiesTopBar(onBack = onBack, viewModel = viewModel)
        }
    ) { innerPadding ->
        if (properties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.user_properties_page_empty_properties_message))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(properties) { property ->
                    UserPropertyItem(property = property, onClick = { })
                }
            }
        }

    }
}

@Composable
fun UserPropertyItem(
    property: Property,
    onClick: () -> Unit
){

    val photos = property.photos.take(3)

    val pricePerSquareMeter = calculatePricePerSquareMeter(property.price, property.surface)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ){
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val uri = photos.getOrNull(index)?.uri
                    println("📸 URI #$index = $uri")
                    if (uri != null) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            onError = {
                                println("❌ Failed to load image: $uri")
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.no_photo_24px),
                                contentDescription = stringResource(R.string.property_list_screen_no_photo_content_description),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${property.price} €",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "$pricePerSquareMeter €/m²",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = getIconForPropertyType(property.type)),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium
                )

            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${property.rooms} rooms • ${property.surface} m²",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = property.address,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (property.poiS.isNotEmpty()) {
                property.poiS.take(5).forEach { poi ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = getIconForPoiType(poi.type)),
                            contentDescription = poi.type,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = poi.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPropertiesTopBar(
    onBack: () -> Unit,
    viewModel: UserPropertiesViewModel
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onBack,
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.userProperties_page_top_bar_back_button_content_description)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    painter = painterResource(id = R.drawable.home_24px),
                    contentDescription = stringResource(R.string.userProperties_page_top_bar_icon_content_description),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.userProperties_page_top_bar_app_text),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(id = R.drawable.filter_24px),
                    contentDescription = stringResource(R.string.userProperties_page_top_bar_filter_icon_content_description),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(32.dp)
                        .clickable {
                            viewModel.toggleFilterSheet(true)
                        }
                )
            }
        }
    )
}