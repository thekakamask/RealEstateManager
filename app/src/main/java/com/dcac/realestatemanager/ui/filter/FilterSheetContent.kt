package com.dcac.realestatemanager.ui.filter

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType
import androidx.compose.foundation.lazy.LazyColumn

@SuppressLint("MutableCollectionMutableState")
@Composable
fun FilterSheetContent(
    filterUi: FilterUiState,
    onApply: (PropertyFilters) -> Unit,
    onReset: () -> Unit
) {

    var sortOrder by remember { mutableStateOf(filterUi.sortOrder) }
    var selectedType by remember { mutableStateOf(filterUi.selectedType) }
    var isSold by remember { mutableStateOf(filterUi.isSold) }
    var minSurface by remember { mutableStateOf(filterUi.minSurface) }
    var maxSurface by remember { mutableStateOf(filterUi.maxSurface) }
    var minPrice by remember { mutableStateOf(filterUi.minPrice) }
    var maxPrice by remember { mutableStateOf(filterUi.maxPrice) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp
        )
    ) {
        item {
            Text(stringResource(R.string.filter_screen_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            FilterSectionTitle(R.string.filter_screen_sort_order_title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOrderChip(
                    labelRes = R.string.filter_screen_sort_order_alphabetic_label,
                    selected = sortOrder == PropertySortOrder.ALPHABETIC
                ) { sortOrder = PropertySortOrder.ALPHABETIC }

                SortOrderChip(
                    labelRes = R.string.filter_screen_sort_order_date_added_label,
                    selected = sortOrder == PropertySortOrder.DATE
                ) { sortOrder = PropertySortOrder.DATE }
            }

            Spacer(Modifier.height(16.dp))

            FilterSectionTitle(R.string.filter_screen_property_type_label)
            Spacer(Modifier.height(8.dp))
            TypeIconSelectableRow(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            Spacer(Modifier.height(16.dp))

            FilterSectionTitle(R.string.filter_screen_status_title)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(R.string.filter_screen_status_sold_label, isSold == true) { isSold = true }
                StatusChip(R.string.filter_screen_status_available_label, isSold == false) { isSold = false }
                StatusChip(R.string.filter_screen_status_all_label, isSold == null) { isSold = null }
            }

            Spacer(Modifier.height(16.dp))

            RangeInputRow(
                labelMin = R.string.filter_screen_surface_min_label,
                minValue = minSurface,
                onMinChange = { minSurface = it },
                labelMax = R.string.filter_screen_surface_max_label,
                maxValue = maxSurface,
                onMaxChange = { maxSurface = it }
            )

            Spacer(Modifier.height(16.dp))

            RangeInputRow(
                labelMin = R.string.filter_screen_price_min_label,
                minValue = minPrice,
                onMinChange = { minPrice = it },
                labelMax = R.string.filter_screen_price_max_label,
                maxValue = maxPrice,
                onMaxChange = { maxPrice = it }
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onReset) {
                    Text(stringResource(R.string.filter_screen_reset_button_label))
                }
                Button(onClick = {
                    onApply(
                        PropertyFilters(
                            sortOrder = sortOrder,
                            selectedType = selectedType.ifBlank { null },
                            isSold = isSold,
                            minSurface = minSurface.toIntOrNull(),
                            maxSurface = maxSurface.toIntOrNull(),
                            minPrice = minPrice.toIntOrNull(),
                            maxPrice = maxPrice.toIntOrNull()
                        )
                    )
                }) {
                    Text(stringResource(R.string.filter_screen_apply_button_label))
                }
            }
        }
    }
}

@Composable
fun FilterSectionTitle(@androidx.annotation.StringRes resId: Int) {
    Text(stringResource(resId), style = MaterialTheme.typography.titleMedium)
}

@Composable
fun SortOrderChip(@androidx.annotation.StringRes labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(labelRes)) },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TypeIconSelectableRow(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {

    val allTypes = listOf(
        stringResource(R.string.home_page_type_house),
        stringResource(R.string.home_page_type_apartment),
        stringResource(R.string.home_page_type_studio),
        stringResource(R.string.home_page_type_boat),
        stringResource(R.string.home_page_type_cabin),
        stringResource(R.string.home_page_type_castle),
        stringResource(R.string.home_page_type_motor_home)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(allTypes) { type ->
            val isSelected = selectedType == type
            val iconRes = getIconForPropertyType(type)

            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
            val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

            Surface(
                onClick = { onTypeSelected(type) },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, borderColor),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier
                    .width(72.dp)
                    .height(72.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = type,
                        tint = iconTint,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(@androidx.annotation.StringRes labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(labelRes)) },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun RangeInputRow(
    @androidx.annotation.StringRes labelMin: Int,
    minValue: String,
    onMinChange: (String) -> Unit,
    @androidx.annotation.StringRes labelMax: Int,
    maxValue: String,
    onMaxChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = minValue,
            onValueChange = onMinChange,
            label = { Text(stringResource(labelMin)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        )
        OutlinedTextField(
            value = maxValue,
            onValueChange = onMaxChange,
            label = { Text(stringResource(labelMax)) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        )
    }
}