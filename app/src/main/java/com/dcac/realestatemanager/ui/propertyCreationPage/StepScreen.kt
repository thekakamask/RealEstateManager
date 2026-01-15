package com.dcac.realestatemanager.ui.propertyCreationPage

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.model.Photo
import androidx.core.net.toUri
import com.dcac.realestatemanager.utils.Utils.getIconForPoiType
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import com.dcac.realestatemanager.utils.settingsUtils.CurrencyHelper
import org.threeten.bp.LocalDate
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.Dp
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
@Composable
fun Step1IntroScreen(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(R.string.property_creation_step_1_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_1_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_1_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.appartement_creation_image),
                contentDescription = stringResource(R.string.property_creation_step_1_apartment_image_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

    }
}

@Composable
fun Step2TypeScreen(
    selectedType: String?,
    onTypeSelected: (String) -> Unit,
    bottomInset: Dp
) {

    val propertyTypes = listOf(
        stringResource(R.string.property_creation_step_2_type_house),
        stringResource(R.string.property_creation_step_2_type_apartment),
        stringResource(R.string.property_creation_step_2_type_studio),
        stringResource(R.string.property_creation_step_2_type_boat),
        stringResource(R.string.property_creation_step_2_type_cabin),
        stringResource(R.string.property_creation_step_2_type_castle),
        stringResource(R.string.property_creation_step_2_type_motor_home)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(R.string.property_creation_step_2_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_2_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_2_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = bottomInset),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(propertyTypes.size) { index ->
                    val label = propertyTypes[index]
                    val iconRes = getIconForPropertyType(label)
                    val isSelected = selectedType == label

                    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

                    Surface(
                        onClick = { onTypeSelected(label) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, borderColor),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(vertical = 24.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = label,
                                tint = iconTint,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Step3AddressScreen(
    street: String,
    city: String,
    postalCode: String,
    country: String,
    onStreetChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    bottomInset: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(R.string.property_creation_step_3_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_3_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_3_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))


            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomInset),
                )
            {
                item {
                    AddressTextField(
                        stringResource(R.string.property_creation_step_3_number_street_label),
                        stringResource(R.string.property_creation_step_3_number_street_content),
                        street,
                        onStreetChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AddressTextField(
                        stringResource(R.string.property_creation_step_3_postal_code_label),
                        stringResource(R.string.property_creation_step_3_postal_code_content),
                        city,
                        onCityChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AddressTextField(
                        stringResource(R.string.property_creation_step_3_city_label),
                        stringResource(R.string.property_creation_step_3_city_content),
                        postalCode,
                        onPostalCodeChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AddressTextField(
                        stringResource(R.string.property_creation_step_3_country_label),
                        stringResource(R.string.property_creation_step_3_country_content),
                        country,
                        onCountryChange
                    )
                }
            }
        }
    }
}

@Composable
fun AddressTextField(
    explanation: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = explanation,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = minLines == 1,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            minLines = minLines
        )
    }
}

@Composable
fun Step4PoiScreen(
    poiList: List<PoiDraft>,
    onTypeSelected: (Int, String) -> Unit,
    onNameChanged: (Int, String) -> Unit,
    onStreetChanged: (Int, String) -> Unit,
    onCityChanged: (Int, String) -> Unit,
    onPostalCodeChanged: (Int, String) -> Unit,
    onCountryChanged: (Int, String) -> Unit,
    bottomInset: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.property_creation_step_4_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_4_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_4_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = bottomInset)
                ) {
                items(5) { index ->
                    val poi = poiList.getOrNull(index)
                    PoiInputBlock(
                        index = index,
                        selectedType = poi?.type,
                        name = poi?.name.orEmpty(),
                        street = poi?.street.orEmpty(),
                        postalCode = poi?.postalCode.orEmpty(),
                        city = poi?.city.orEmpty(),
                        country = poi?.country.orEmpty(),
                        onTypeSelected = { type -> onTypeSelected(index, type) },
                        onNameChanged = { name -> onNameChanged(index, name) },
                        onStreetChanged = { street -> onStreetChanged(index, street) },
                        onCityChanged = { city -> onCityChanged(index, city) },
                        onPostalCodeChanged = { postal -> onPostalCodeChanged(index, postal) },
                        onCountryChanged = { country -> onCountryChanged(index, country) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

        }
    }
}

@Composable
fun PoiInputBlock(
    index: Int,
    selectedType: String?,
    name: String,
    street: String,
    postalCode: String,
    city: String,
    country: String,
    onTypeSelected: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onStreetChanged: (String) -> Unit,
    onPostalCodeChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit
) {
    val poiTypes = listOf(
        stringResource(R.string.property_creation_step_4_poi_type_school),
        stringResource(R.string.property_creation_step_4_poi_type_grocery),
        stringResource(R.string.property_creation_step_4_poi_type_bakery),
        stringResource(R.string.property_creation_step_4_poi_type_butcher),
        stringResource(R.string.property_creation_step_4_poi_type_restaurant)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.property_creation_step_4_poi_title, index + 1),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            poiTypes.forEach { label ->
                val icon = getIconForPoiType(label)
                val isSelected = selectedType == label

                PoiIcon(icon, label, isSelected) {
                    onTypeSelected(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { onNameChanged(it) },
            label = { Text(stringResource(R.string.property_creation_step_4_poi_outlined_name_text_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = street,
            onValueChange = { onStreetChanged(it) },
            label = { Text(stringResource(R.string.property_creation_step_4_poi_outlined_street_text_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = postalCode,
            onValueChange = { onPostalCodeChanged(it) },
            label = { Text(stringResource(R.string.property_creation_step_4_poi_outlined_postal_code_text_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = city,
            onValueChange = { onCityChanged(it) },
            label = { Text(stringResource(R.string.property_creation_step_4_poi_outlined_city_text_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = country,
            onValueChange = { onCountryChanged(it) },
            label = { Text(stringResource(R.string.property_creation_step_4_poi_outlined_country_text_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun PoiIcon(
    @DrawableRes iconRes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier.size(60.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step5DescriptionScreen(
    title: String,
    price: Int,
    surface: Int,
    rooms: Int,
    description: String,
    onPriceChange: (Int) -> Unit,
    onSurfaceChange: (Int) -> Unit,
    onRoomsChange: (Int) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    isSold: Boolean,
    saleDate: LocalDate?,
    onIsSoldChange: (Boolean) -> Unit,
    onSaleDateChange: (LocalDate) -> Unit,
    bottomInset: Dp
){

    val currency = CurrencyHelper.LocalCurrency.current
    val priceUnitString = stringResource(id = CurrencyHelper.getPropertyCreationStep5Unit(currency))
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.property_creation_step_5_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_5_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_5_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomInset)
            )
            {

                item {
                    AddressTextField(
                        stringResource(R.string.property_creation_step_5_title_label),
                        stringResource(R.string.property_creation_step_5_title_content),
                        title,
                        onTitleChange,
                    )


                    Spacer(modifier = Modifier.height(16.dp))


                    NumberTextField(
                        stringResource(R.string.property_creation_step_5_price_label),
                        stringResource(R.string.property_creation_step_5_price_content),
                        price,
                        onPriceChange,
                        unit = priceUnitString
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    NumberTextField(
                        stringResource(R.string.property_creation_step_5_surface_label),
                        stringResource(R.string.property_creation_step_5_surface_content),
                        surface,
                        onSurfaceChange,
                        unit = stringResource(R.string.property_creation_step_5_unit_square_meter)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    NumberTextField(
                        stringResource(R.string.property_creation_step_5_rooms_label),
                        stringResource(R.string.property_creation_step_5_rooms_content),
                        rooms,
                        onRoomsChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.property_creation_step_5_is_sold_label),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isSold,
                            onCheckedChange = onIsSoldChange
                        )
                    }

                    if (isSold) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.property_creation_step_5_sale_date_label),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                saleDate?.toString()
                                    ?: stringResource(R.string.property_creation_step_5_select_date)
                            )
                        }
                    }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        onSaleDateChange(date)
                                    }
                                    showDatePicker = false
                                }) {
                                    Text(stringResource(R.string.property_creation_step_5_select_date_confirm))
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AddressTextField(
                        stringResource(R.string.property_creation_step_5_description_label),
                        stringResource(R.string.property_creation_step_5_description_content),
                        description,
                        onDescriptionChange,
                        minLines = 20
                    )
                }
            }
        }
    }
}

@Composable
fun NumberTextField(
    explanation: String,
    placeholder: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    unit: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text(
            text = explanation,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        OutlinedTextField(
            value = if (value == 0) stringResource(R.string.property_creation_step_5_number_text_field_empty) else value.toString(),
            onValueChange = { text ->
                val number = text.toIntOrNull()
                if (number != null) {
                    onValueChange(number)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.tertiary
                )
            },
            trailingIcon = {
                unit?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Composable
fun Step6PhotosScreen(
    photos: List<Photo>,
    onPhotoClick: (Int) -> Unit,
    onDeletePhoto: (Int) -> Unit,
    bottomInset: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(R.string.property_creation_step_6_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_6_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_6_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomInset)
            )
            {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            repeat(4) { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    repeat(3) { col ->
                                        val index = row * 3 + col
                                        val photo = photos.getOrNull(index)
                                        AddPhotoCell(
                                            modifier = Modifier.weight(1f),
                                            imageUri = photo?.uri?.takeIf { it.isNotBlank() }?.toUri(),
                                            onClick = { onPhotoClick(index) },
                                            onDeleteClick = if (photo != null) {
                                                { onDeletePhoto(index) }
                                            } else null
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

@Composable
fun AddPhotoCell(
    modifier: Modifier = Modifier,
    imageUri: Uri? = null,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Icon(
                    painter = painterResource(id = R.drawable.delete_24px),
                    contentDescription = stringResource(R.string.property_creation_step_6_delete_photo_icon_content_description),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(40.dp)
                        .clickable {
                            onDeleteClick?.invoke()
                        }
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.add_24px),
                    contentDescription = stringResource(R.string.property_creation_step_6_add_photo_icon_content_description),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun Step7StaticMapScreen(
    mapBytes: ByteArray?,
    isLoading: Boolean,
    onLoadMap: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onLoadMap()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = stringResource(R.string.property_creation_step_7_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_7_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_7_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            when {
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                mapBytes != null -> {
                    val bitmap = remember(mapBytes) {
                        BitmapFactory.decodeByteArray(mapBytes, 0, mapBytes.size).asImageBitmap()
                    }

                    Image(
                        bitmap = bitmap,
                        contentDescription = stringResource(R.string.property_creation_step_7_static_map_content_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                else -> Text(
                    stringResource(R.string.property_creation_step_7_static_map_unavailable),
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun Step8ConfirmationScreen(
    draft: PropertyDraft,
    bottomInset: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        val currency = CurrencyHelper.LocalCurrency.current
        val priceUnitStringRes = CurrencyHelper.getPropertyCreationStep8PriceText(currency)
        val formattedPrice = stringResource(id = priceUnitStringRes, draft.price)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.property_creation_step_8_title_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.property_creation_step_8_subtitle_text),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.property_creation_step_8_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = bottomInset)
            ) {

                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = getIconForPropertyType(draft.type)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = draft.type,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.property_creation_step_8_address_title))
                    BorderedBox {
                        InfoRow(label = stringResource(R.string.property_creation_step_8_street_title), value = draft.street)
                        InfoRow(label = stringResource(R.string.property_creation_step_8_city_title), value = draft.city)
                        InfoRow(label = stringResource(R.string.property_creation_step_8_postal_code_title), value = draft.postalCode)
                        InfoRow(label =  stringResource(R.string.property_creation_step_8_country_title), value = draft.country)
                    }
                }

                if (draft.poiS.any { it.name.isNotBlank() || it.type.isNotBlank() }) {
                    item {
                        SectionTitle(stringResource(R.string.property_creation_step_8_poi_title))
                        BorderedBox {
                            draft.poiS
                                .filter { it.name.isNotBlank() || it.type.isNotBlank() }
                                .forEach { poi ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = getIconForPoiType(poi.type)),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = stringResource(
                                                R.string.property_creation_step_8_poi_text,
                                                poi.name,
                                                poi.type
                                            ),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.property_creation_step_8_description_title))
                    BorderedBox {
                        InfoRow(
                            label = stringResource(R.string.property_creation_step_8_price_title),
                            value = formattedPrice
                        )
                        InfoRow(label = stringResource(R.string.property_creation_step_8_surface_title), value = stringResource(
                            R.string.property_creation_step_8_surface_text, draft.surface
                        )
                        )
                        InfoRow(label = stringResource(R.string.property_creation_step_8_rooms_title), value = stringResource(
                            R.string.property_creation_step_8_rooms_text, draft.rooms
                        )
                        )
                        InfoRow(label = stringResource(R.string.property_creation_step_8_description_title), value = "")
                        Text(
                            text = draft.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (draft.photos.any { it.uri.isNotBlank() }) {
                    item {
                        SectionTitle(stringResource(R.string.property_creation_step_8_photos_title))
                        BorderedBox {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                draft.photos
                                    .filter { it.uri.isNotBlank() }
                                    .forEach { photo ->
                                        AsyncImage(
                                            model = photo.uri.toUri(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                            }
                        }
                    }
                }

                if (draft.staticMap != null && draft.staticMap.uri.isNotBlank()) {
                    item {
                        SectionTitle(stringResource(R.string.property_creation_step_8_map_title))
                        BorderedBox {
                            val bitmap = remember(draft.staticMap.uri) {
                                val uri = draft.staticMap.uri.toUri()
                                when (uri.scheme) {
                                    "file" -> BitmapFactory.decodeFile(uri.path)?.asImageBitmap()
                                    else -> null
                                }
                            }

                            bitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = stringResource(R.string.property_creation_step_8_map_content_description),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } ?: Text(
                                text = stringResource(R.string.property_creation_step_8_map_unavailable),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BorderedBox(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


