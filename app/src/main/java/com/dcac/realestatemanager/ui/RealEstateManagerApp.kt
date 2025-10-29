package com.dcac.realestatemanager.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.ui.navigation.RealEstateNavGraph
import androidx.navigation.compose.rememberNavController
import com.dcac.realestatemanager.ui.initialLoginPage.LoginViewModel

@Composable
fun RealEstateManagerApp() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()

    val isUserLoggedIn = loginViewModel.isUserConnected

    RealEstateNavGraph(
        navController = navController,
        isUserLoggedIn = isUserLoggedIn
    )

}