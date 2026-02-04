package com.dcac.realestatemanager.ui.propertyDetailsPage.propertyDetailsResponsive

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Property
import com.dcac.realestatemanager.utils.Utils.calculatePricePerSquareMeter
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper

@Composable
fun PropertyDetailsSuccessContent(
    property: Property,
    userName: String
) {
    val currency = CurrencyHelper.LocalCurrency.current

    val displayPrice = if (currency == "EUR") {
        CurrencyHelper.convertDollarToEuro(property.price)
    } else {
        property.price
    }

    val formattedPrice = stringResource(
        id = CurrencyHelper.getPropertyDetailsPagePropertyPriceText(currency),
        displayPrice
    )

    val pricePerSquareMeter =
        calculatePricePerSquareMeter(displayPrice, property.surface)

    val formattedPricePerSquareMeter = stringResource(
        id = CurrencyHelper.getPropertyDetailsPagePropertyPriceSquareText(currency),
        pricePerSquareMeter
    )

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