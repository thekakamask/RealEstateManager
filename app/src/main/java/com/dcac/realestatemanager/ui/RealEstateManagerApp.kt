package com.dcac.realestatemanager.ui

import androidx.compose.runtime.Composable
import com.dcac.realestatemanager.ui.navigation.RealEstateNavGraph
import androidx.navigation.compose.rememberNavController

@Composable
fun RealEstateManagerApp() {
    val navController = rememberNavController()

    // TODO: Replace with real auth check (from ViewModel or DataStore)
    val isUserLoggedIn = false // For now, fake value

    RealEstateNavGraph(
        navController = navController,
        isUserLoggedIn = isUserLoggedIn
    )

}