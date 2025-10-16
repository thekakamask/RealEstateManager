package com.dcac.realestatemanager.ui.navigation

import com.dcac.realestatemanager.R

sealed class RealEstateDestination: NavDestination {

    data object Login : RealEstateDestination() {
        override val route = "login"
        override val titleRes = R.string.login
    }

    data object Home : RealEstateDestination() {
        override val route = "home"
        override val titleRes = R.string.home
    }

    data object PropertyDetails : RealEstateDestination() {
        override val route = "property_details/{propertyId}"
        override val titleRes = R.string.property_details

        fun createRoute(propertyId: Long): String = "property_details/$propertyId"
    }

    data object CreateProperty : RealEstateDestination() {
        override val route = "create_property"
        override val titleRes = R.string.create_property
    }

    data object Account : RealEstateDestination() {
        override val route = "account"
        override val titleRes = R.string.account
    }

    data object Settings : RealEstateDestination() {
        override val route = "settings"
        override val titleRes = R.string.settings
    }

    data object UserProperties : RealEstateDestination() {
        override val route = "user_properties"
        override val titleRes = R.string.user_properties
    }
}