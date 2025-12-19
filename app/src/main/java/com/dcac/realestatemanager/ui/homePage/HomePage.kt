package com.dcac.realestatemanager.ui.homePage

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
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.dcac.realestatemanager.R
import com.dcac.realestatemanager.ui.filter.FilterSheetContent
import com.dcac.realestatemanager.ui.filter.toUiState
import com.dcac.realestatemanager.ui.homePage.googleMapScreen.GoogleMapScreen
import com.dcac.realestatemanager.ui.homePage.propertiesListScreen.PropertiesListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit,
    onPropertyClick: (String) -> Unit,
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
                ModalDrawerSheet {
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
                    Spacer(modifier = Modifier.height(16.dp))

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
            Text(stringResource(R.string.home_page_ui_state_error, error), color = MaterialTheme.colorScheme.error)
        }
    }
}
