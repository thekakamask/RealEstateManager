package com.dcac.realestatemanager.ui.homePage.propertiesListScreen

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.model.Property
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.filter.PropertyFilters
import com.dcac.realestatemanager.utils.Utils.calculatePricePerSquareMeter
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper

@Composable
fun PropertiesListScreen(
    viewModel: PropertiesListViewModel = hiltViewModel(),
    onPropertyClick: (Property) -> Unit,
    filters: PropertyFilters
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(filters) {
        viewModel.applyFilters(filters)
    }

    when (uiState) {
        is PropertiesListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is PropertiesListUiState.Success -> {
            val properties = (uiState as PropertiesListUiState.Success).properties
            PropertyListContent(
                properties = properties,
                agentNames = (uiState as PropertiesListUiState.Success).agentNames,
                onClick = onPropertyClick
            )
        }

        is PropertiesListUiState.Error -> {
            val errorMessage = (uiState as PropertiesListUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.property_list_screen_ui_state_error, errorMessage), color = MaterialTheme.colorScheme.error)
            }
        }

        PropertiesListUiState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.property_list_screen_no_data_to_display))
            }
        }
    }
}

@Composable
fun PropertyListContent(
    properties: List<Property>,
    agentNames: Map<String, String>,
    onClick: (Property) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        items(properties) { property ->
            PropertyItem(
                property = property,
                agentName = agentNames[property.universalLocalUserId] ?: context.getString(R.string.property_list_screen_agent_name_message_unknown),
                onClick = { onClick(property) }
            )
        }
    }
}

@Composable
fun PropertyItem(
    property: Property,
    agentName: String,
    onClick: () -> Unit
){

    val photos = property.photos.take(3)

    val currency = CurrencyHelper.LocalCurrency.current

    val priceUnitStringRes = CurrencyHelper.getPropertyListScreenPropertyPriceText(currency)
    val priceSquareUnitText = CurrencyHelper.getPropertyListScreenPropertyPriceSquareText(currency)

    val displayPrice = if (currency == "EUR") {
        CurrencyHelper.convertDollarToEuro(property.price)
    } else {
        property.price
    }

    val formattedPrice = stringResource(id = priceUnitStringRes, displayPrice)
    val pricePerSquareMeter = calculatePricePerSquareMeter(displayPrice, property.surface)
    val formattedPricePerSquareMeter = stringResource(id = priceSquareUnitText, pricePerSquareMeter)

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user_icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = agentName,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    val uri = photos.getOrNull(index)?.uri
                    if (uri != null) {
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(
                                R.string.property_list_screen_async_image_content_description,
                                index
                            ),
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
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
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formattedPricePerSquareMeter,
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
                text = stringResource(
                    R.string.property_list_screen_property_rooms_surface_text,
                    property.rooms,
                    property.surface
                ),
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