package com.dcac.realestatemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dcac.realestatemanager.ui.homePage.HomeScreen
import com.dcac.realestatemanager.ui.initialLoginPage.WelcomePage
import com.dcac.realestatemanager.ui.initialLoginPage.accountScreen.AccountCreationPage
import com.dcac.realestatemanager.ui.initialLoginPage.accountScreen.ForgotPasswordPage
import com.dcac.realestatemanager.ui.initialLoginPage.accountScreen.LoginPage
import com.dcac.realestatemanager.ui.initialLoginPage.contactScreen.ChatContactPage
import com.dcac.realestatemanager.ui.initialLoginPage.contactScreen.ContactInfoPage
import com.dcac.realestatemanager.ui.initialLoginPage.contactScreen.EmailContactPage
import com.dcac.realestatemanager.ui.propertyCreationPage.PropertyCreationPage
import com.dcac.realestatemanager.ui.propertyDetailsPage.PropertyDetailsPage

@Composable
fun RealEstateNavGraph(
    navController: NavHostController,
    isUserLoggedIn: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) RealEstateDestination.Home.route else RealEstateDestination.Welcome.route
    ){
        composable(route = RealEstateDestination.Welcome.route) {
            WelcomePage(
                onLoginClick = {
                    navController.navigate(RealEstateDestination.Login.route)
                },
                onRegisterClick = {
                    navController.navigate(RealEstateDestination.AccountCreation.route)
                },
                onInfoClick = {
                    navController.navigate(RealEstateDestination.ContactInfo.route)
                }
            )
        }

        composable(route = RealEstateDestination.ContactInfo.route) {
            ContactInfoPage(
                onBackClick = { navController.popBackStack() },
                onEmailClick = {navController.navigate(RealEstateDestination.EmailContact.route)},
                onChatClick = {navController.navigate(RealEstateDestination.ChatContact.route)}
            )
        }

        composable(route = RealEstateDestination.EmailContact.route) {
            EmailContactPage(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = RealEstateDestination.ChatContact.route) {
            ChatContactPage(
                onBackClick = { navController.popBackStack() }
            )
        }


        composable(route = RealEstateDestination.Login.route) {
            LoginPage(
                onLoginSuccess = {
                    navController.navigate(RealEstateDestination.Home.route) {
                        //Removes screens stacked above (or up to) the specified one.
                        //inclusive also removes the target screen (here Login),
                        // so it will no longer be in the backstack.
                        popUpTo(RealEstateDestination.Login.route) {
                            inclusive = true }
                    }
                },
                onInfoClick = {
                    navController.navigate(RealEstateDestination.ContactInfo.route)
                },
                onPasswordForgotClick = {
                    navController.navigate(RealEstateDestination.ForgotPassword.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = RealEstateDestination.ForgotPassword.route) {
            ForgotPasswordPage(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = RealEstateDestination.AccountCreation.route) {
            AccountCreationPage(
                onAccountCreationSuccess = {
                    navController.navigate(RealEstateDestination.Home.route) {
                        //Removes screens stacked above (or up to) the specified one.
                        //inclusive also removes the target screen (here Login),
                        // so it will no longer be in the backstack.
                        popUpTo(RealEstateDestination.AccountCreation.route) {
                            inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable( route = RealEstateDestination.Home.route) {
            HomeScreen(
                onPropertyClick = { propertyId ->
                    navController.navigate(RealEstateDestination.PropertyDetails.createRoute(propertyId))
                },
                /*onAccountClick = {
                    navController.navigate(RealEstateDestination.Account.route)
                },
                onUserPropertiesClick = {
                    navController.navigate(RealEstateDestination.UserProperties.route)
                },
                onSettingsClick = {
                    navController.navigate(RealEstateDestination.Settings.route)
                },*/
                onAddPropertyClick = {
                    navController.navigate(RealEstateDestination.CreateProperty.route)
                },
                onLogout = {
                    navController.navigate(RealEstateDestination.Welcome.route) {
                        popUpTo(0) { inclusive = true } // Clear whole backstack
                    }
                }
            )
        }
        composable(route = RealEstateDestination.CreateProperty.route) {
            PropertyCreationPage(
                onExit = { navController.popBackStack() },
                onInfoClick = {
                    navController.navigate(RealEstateDestination.ContactInfo.route)
                },
                onFinish = {
                    navController.navigate(RealEstateDestination.Home.route) {
                        popUpTo(RealEstateDestination.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = RealEstateDestination.PropertyDetails.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyDetailsPage(
                propertyId = propertyId,
                onBack = { navController.popBackStack() }
            )
        }

        /*
        composable(route = RealEstateDestination.Account.route) {
            AccountScreen()
        }

        composable(route = RealEstateDestination.Settings.route) {
            SettingsScreen()
        }

        composable(route = RealEstateDestination.UserProperties.route) {
            UserPropertiesScreen(
                onEditProperty = { propertyId ->
                    navController.navigate(RealEstateDestination.ModifyProperty.createRoute(propertyId))
                }
            )
        }

        composable(
            route = RealEstateDestination.ModifyProperty.route,
            arguments = listOf(navArgument("propertyId") { type = NavType.LongType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getLong("propertyId") ?: -1L
            ModifyPropertyScreen(propertyId = propertyId)
        }*/

    }

}