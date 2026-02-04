package com.dcac.realestatemanager.ui

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.dcac.realestatemanager.ui.navigation.RealEstateNavGraph
import androidx.navigation.compose.rememberNavController
import com.dcac.realestatemanager.ui.initialLoginPage.LoginViewModel
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RealEstateManagerApp() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()

    val isUserLoggedIn = loginViewModel.isUserConnected

    val activity = LocalActivity.current
        ?:
        return

    val windowSizeClass = calculateWindowSizeClass(activity)

    RealEstateNavGraph(
        navController = navController,
        isUserLoggedIn = isUserLoggedIn,
        windowsSizeClass = windowSizeClass
    )

}