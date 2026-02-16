package com.dcac.realestatemanager.ui.homePage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.filter.FilterSheetContent
import com.dcac.realestatemanager.ui.filter.toUiState
import com.dcac.realestatemanager.ui.homePage.googleMapScreen.GoogleMapScreen
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListScreen
import com.dcac.realestatemanager.ui.propertyDetailsPage.propertyDetailsResponsive.PropertyDetailsTablet
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsViewModel
import androidx.compose.material3.VerticalDivider
import com.dcac.realestatemanager.ui.propertyDetailsPage.EditSection

@Composable
fun HomeScreenAdaptive(
    windowSizeClass: WindowSizeClass,
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onEditProperty: (EditSection, String) -> Unit,
    onUserPropertiesClick: () -> Unit,
    onUserAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            //Smartphone
            HomeScreen(
                onLogout = onLogout,
                onAddPropertyClick = onAddPropertyClick,
                onNavigateToDetails = onNavigateToDetails,
                onUserPropertiesClick = onUserPropertiesClick,
                onUserAccountClick = onUserAccountClick,
                onSettingsClick = onSettingsClick
            )
        }
        else -> {
            //Tablet
            HomeScreenTablet(
                onLogout = onLogout,
                onAddPropertyClick = onAddPropertyClick,
                onEditProperty = onEditProperty,
                onUserPropertiesClick = onUserPropertiesClick,
                onUserAccountClick = onUserAccountClick,
                onSettingsClick = onSettingsClick
            )
        }
    }
}

@Composable
fun HomeMainContent(
    state: HomeUiState.Success,
    selectedPropertyId: String?,
    onPropertySelected: (String) -> Unit
) {
    when (state.currentScreen) {
        is HomeDestination.PropertyList -> {
            PropertiesListScreen(
                filters = state.filters,
                selectedPropertyId = selectedPropertyId,
                onPropertyClick = { property ->
                    onPropertySelected(property.universalLocalId)
                }
            )
        }

        is HomeDestination.GoogleMap -> {
            GoogleMapScreen(
                filters = state.filters,
                onPropertyClick = { property ->
                    onPropertySelected(property.property.universalLocalId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTablet(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit,
    onEditProperty: (EditSection, String) -> Unit,
    onUserPropertiesClick: () -> Unit,
    onUserAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val detailsViewModel: PropertyDetailsViewModel = hiltViewModel()
    val detailsUiState by detailsViewModel.uiState.collectAsState()

    var selectedPropertyId by rememberSaveable { mutableStateOf<String?>(null) }

    val filterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (uiState !is HomeUiState.Success) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val state = uiState as HomeUiState.Success

    LaunchedEffect(selectedPropertyId) {
        selectedPropertyId?.let {
            detailsViewModel.loadPropertyDetails(it)
        }
    }
    LaunchedEffect(state.showFilterSheet) {
        if (state.showFilterSheet) {
            filterSheetState.show()
        } else {
            filterSheetState.hide()
        }
    }

    if (state.showFilterSheet) {
        ModalBottomSheet(
            sheetState = filterSheetState,
            onDismissRequest = {
                viewModel.toggleFilterSheet(false)
            }
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.user_icon),
                        contentDescription = stringResource(R.string.home_page_user_icon_content_description),
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                onUserAccountClick()
                            }
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
                                text = state.totalProperties.toString(),
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
                                text = state.soldProperties.toString(),
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
                        onUserPropertiesClick()
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
                        onSettingsClick()
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
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(R.drawable.user_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open()
                                        else drawerState.close()
                                    }
                                },
                            tint = Color.Unspecified
                        )
                    },
                    actions = {
                        Icon(
                            painter = painterResource(R.drawable.filter_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    viewModel.toggleFilterSheet(true)
                                }
                        )
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(R.drawable.list_24px),
                                contentDescription = null
                            )
                        },
                        selected = state.isOnPropertyList,
                        onClick = {
                            viewModel.navigateTo(HomeDestination.PropertyList)
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(R.drawable.map_24px),
                                contentDescription = null
                            )
                        },
                        selected = state.isOnMap,
                        onClick = {
                            viewModel.navigateTo(HomeDestination.GoogleMap)
                        }
                    )
                }
            }
        ) { padding ->
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                // Home content at left
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                ) {

                    HomeMainContent(
                        state = state,
                        selectedPropertyId = selectedPropertyId,
                        onPropertySelected = { selectedPropertyId = it }
                    )

                    if (state.currentScreen is HomeDestination.PropertyList) {
                        FloatingActionButton(
                            onClick = onAddPropertyClick,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_home_24px),
                                contentDescription = stringResource(R.string.add_property),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }


                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Details at right
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp)
                ) {
                    if (selectedPropertyId != null) {
                        PropertyDetailsTablet(
                            uiState = detailsUiState,
                            onEditSectionSelected = { section, propertyId ->
                                onEditProperty(section, propertyId)
                            },
                            onDeleteConfirmed = { property ->
                                detailsViewModel.deleteProperty(
                                    property = property,
                                    onDeleted = {
                                        selectedPropertyId = null
                                    }
                                )
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.home_page_tablet_property_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onUserPropertiesClick: () -> Unit,
    onUserAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
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
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent ={
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_icon),
                            contentDescription = stringResource(R.string.home_page_user_icon_content_description),
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(72.dp)
                                .clickable {
                                    onUserAccountClick()
                                }
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
                                    text = state.totalProperties.toString(),
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
                                    text = state.soldProperties.toString(),
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
                            onUserPropertiesClick()
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
                            onSettingsClick()
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
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))

                    /*Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                            },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.sync_24px),
                                contentDescription = stringResource(
                                    R.string.home_page_navigation_drawer_sync_button_content_description
                                ),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.home_page_navigation_drawer_sync_button_text)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
*/

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
                    if (state.currentScreen is HomeDestination.PropertyList) {
                        FloatingActionButton(
                            onClick = onAddPropertyClick,
                            modifier = Modifier
                                .padding(end = 16.dp, bottom = 16.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.add_home_24px),
                                contentDescription = stringResource(R.string.add_property),
                                modifier = Modifier.size(32.dp)
                            )
                        }
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
                    HomeMainContent(
                        state = state,
                        selectedPropertyId = null,
                        onPropertySelected = onNavigateToDetails
                    )
                    /*when (state.currentScreen) {
                        is HomeDestination.PropertyList -> {
                            PropertiesListScreen(
                                filters = state.filters,
                                onPropertyClick = { property ->
                                    onPropertyClick(property.universalLocalId)
                                }
                            )
                        }
                        is HomeDestination.GoogleMap -> {
                            GoogleMapScreen(
                                filters = state.filters,
                                onPropertyClick = { property ->
                                    onPropertyClick(property.property.universalLocalId)
                                }
                            )
                        }
                    }*/
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
            Text(stringResource(R.string.home_page_ui_state_error, error), color = MaterialTheme.colorScheme.error)
        }
    }
}
