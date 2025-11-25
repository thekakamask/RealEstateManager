package com.dcac.realestatemanager.ui.homePage

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.ui.Alignment
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListScreen
import com.dcac.realestatemanager.utils.Utils.getIconForPropertyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState is HomeUiState.Success) {
        val state = uiState as HomeUiState.Success

        LaunchedEffect(drawerState.isOpen) {
            viewModel.toggleDrawer(drawerState.isOpen)
        }

        LaunchedEffect(state.showFilterSheet) {
            if (state.showFilterSheet) bottomSheetState.show() else bottomSheetState.hide()
        }

        if (state.showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleFilterSheet(false) },
                sheetState = bottomSheetState
            ) {
                FilterSheetContent(
                    state = state,
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
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent ={
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_icon),
                            contentDescription = stringResource(R.string.home_page_user_icon_content_description),
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(72.dp)
                        )

                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = state.userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.home_page_user_apartment_handle),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.apartment_24px),
                                    contentDescription = stringResource(R.string.home_page_user_apartment_handle_content_description)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.home_page_user_apartment_sold),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.money_24px),
                                    contentDescription = stringResource(R.string.home_page_user_apartment_sold_content_description)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                    }

                    NavigationDrawerItem(
                        icon = {Icon (painterResource(id = R.drawable.apartment_24px),
                            contentDescription = stringResource(R.string.home_page_navigation_drawer_user_properties_icon_content_description))} ,
                        label = {Text(stringResource(R.string.home_page_navigation_drawer_user_properties_label))},
                        selected = false,
                        onClick = {
                            viewModel.navigateTo(HomeDestination.PropertyList)
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = {Icon (painterResource(id = R.drawable.settings_24px),
                            contentDescription = stringResource(R.string.home_page_navigation_drawer_settings_icon_content_description)
                        )},
                        label= {Text(stringResource(R.string.home_page_navigation_drawer_settings_label))},
                        selected = false,
                        onClick = {
                            scope.launch {drawerState.close()}
                        }
                    )
                    NavigationDrawerItem(
                        icon = {Icon(painterResource(id = R.drawable.log_out_24px),
                            contentDescription =  stringResource(R.string.home_page_navigation_drawer_log_out_icon_content_description)
                        )},
                        label = { Text(stringResource(R.string.home_page_navigation_drawer_log_out_label))},
                        selected = false,
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.user_icon),
                                    contentDescription = stringResource(R.string.home_page_top_bar_user_icon_content_description),
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(32.dp)
                                        .clickable {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                            }
                                        },
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(id = R.drawable.real_estate_manager_logo),
                                    contentDescription = stringResource(R.string.home_page_top_bar_app_icon_content_description),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.weight(1f))

                                Icon(
                                    painter = painterResource(id = R.drawable.filter_24px),
                                    contentDescription = stringResource(R.string.home_page_top_bar_filter_icon_content_description),
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
                },

                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onAddPropertyClick,
                        modifier = Modifier
                            .padding(end = 16.dp, bottom = 32.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_home_24px),
                            contentDescription = stringResource(R.string.add_property),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(painterResource(id= R.drawable.list_24px),
                                contentDescription = stringResource(R.string.home_page_bottom_bar_list_icon_content_description)
                            ) },
                            selected = state.isOnPropertyList,
                            onClick = { viewModel.navigateTo(HomeDestination.PropertyList) }
                        )
                        NavigationBarItem(
                            icon = { Icon(painterResource(id = R.drawable.map_24px),
                                contentDescription = stringResource(R.string.home_page_bottom_bar_map_icon_content_description)) },
                            selected = state.isOnMap,
                            onClick = { viewModel.navigateTo(HomeDestination.GoogleMap) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (state.currentScreen) {
                        is HomeDestination.PropertyList -> {
                            PropertiesListScreen(
                                filters = state.filterUiState.toFilters(),
                                onPropertyClick = { /* ... */ }
                            )
                        }
                        is HomeDestination.GoogleMap -> {

                        }
                    }
                }
            }
        }
    } else if (uiState is HomeUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState is HomeUiState.Error) {
        val error = (uiState as HomeUiState.Error).message
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $error", color = MaterialTheme.colorScheme.error)
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun FilterSheetContent(
    state: HomeUiState.Success,
    onApply: (PropertyFilters) -> Unit,
    onReset: () -> Unit
) {
    val filterUi = state.filterUiState

    var sortOrder by remember { mutableStateOf(filterUi.sortOrder) }
    var selectedType by remember { mutableStateOf(filterUi.selectedType) }
    var isSold by remember { mutableStateOf(filterUi.isSold) }
    var minSurface by remember { mutableStateOf(filterUi.minSurface) }
    var maxSurface by remember { mutableStateOf(filterUi.maxSurface) }
    var minPrice by remember { mutableStateOf(filterUi.minPrice) }
    var maxPrice by remember { mutableStateOf(filterUi.maxPrice) }

    Column(modifier = Modifier.padding(24.dp)) {
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
    val allTypes = listOf("House", "Apartment", "Studio", "Boat", "Cabin", "Castle", "Motor home")

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        allTypes.forEach { type ->
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
