package com.dcac.realestatemanager.ui.homePage

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.dcac.realestatemanager.R

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onAddPropertyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    if (uiState is HomeUiState.Success) {
        val state = uiState as HomeUiState.Success

        LaunchedEffect(state.isDrawerOpen) {
            if (state.isDrawerOpen) drawerState.open() else drawerState.close()
        }
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent ={
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.user_icon),
                            contentDescription = "user icon",
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
                                Text(text = "0", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.apartment_24px),
                                    contentDescription = "apartment handle"
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "0", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.money_24px),
                                    contentDescription = "apartment sell"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                    }

                    NavigationDrawerItem(
                        icon = {Icon (painterResource(id = R.drawable.apartment_24px),
                            contentDescription = "my properties")} ,
                        label = {Text("My properties")},
                        selected = false,
                        onClick = {
                            viewModel.navigateTo(HomeDestination.PropertyList)
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        icon = {Icon (painterResource(id = R.drawable.settings_24px),
                            contentDescription = "settings")},
                        label= {Text("Settings")},
                        selected = false,
                        onClick = {
                            scope.launch {drawerState.close()}
                        }
                    )
                    NavigationDrawerItem(
                        icon = {Icon(painterResource(id = R.drawable.log_out_24px),
                            contentDescription =  "log out")},
                        label = { Text("Log out")},
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
                                contentDescription = "List") },
                            selected = state.currentScreen is HomeDestination.PropertyList,
                            onClick = { viewModel.navigateTo(HomeDestination.PropertyList) }
                        )
                        NavigationBarItem(
                            icon = { Icon(painterResource(id = R.drawable.map_24px),
                                contentDescription = "Map") },
                            selected = state.currentScreen is HomeDestination.GoogleMap,
                            onClick = { viewModel.navigateTo(HomeDestination.GoogleMap) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (state.currentScreen) {
                        is HomeDestination.PropertyList -> {
                            Text("Properties list (global)") // Placeholder
                        }

                        is HomeDestination.GoogleMap -> {
                            Text("Google Map") // Placeholder
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