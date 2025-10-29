package com.dcac.realestatemanager.ui.homePage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.HorizontalDivider

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onLogout: () -> Unit,
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
                    Text(
                        text = "${state.userName} (${state.userEmail})",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = {Text("My properties")},
                        selected = state.currentScreen == HomeDestination.PropertyList,
                        onClick = {
                            viewModel.navigateTo(HomeDestination.PropertyList)
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label= {Text("Parameters")},
                        selected = false,
                        onClick = {
                            scope.launch {drawerState.close()}
                        }
                    )
                    NavigationDrawerItem(
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
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                            selected = state.currentScreen is HomeDestination.GoogleMap,
                            onClick = { viewModel.navigateTo(HomeDestination.GoogleMap) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.List, contentDescription = "List") },
                            selected = state.currentScreen is HomeDestination.PropertyList,
                            onClick = { viewModel.navigateTo(HomeDestination.PropertyList) }
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