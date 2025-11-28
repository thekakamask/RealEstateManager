package com.dcac.realestatemanager.ui.navigation

import com.dcac.realestatemanager.R

sealed interface NavDestination {

    //Unique name to define the path for a composable
    val route: String

    //String resource id to that contains title to be displayed for the screen.
    val titleRes: Int
}

sealed class RealEstateDestination: NavDestination {

    data object Welcome : RealEstateDestination() {
        override val route = "welcome"
        override val titleRes = R.string.welcome
    }

    data object ContactInfo : RealEstateDestination() {
        override val route = "contact_info"
        override val titleRes = R.string.contact_info
    }

    data object EmailContact : RealEstateDestination() {
        override val route = "email_contact"
        override val titleRes = R.string.email_contact
    }

    data object ChatContact : RealEstateDestination() {
        override val route = "chat_contact"
        override val titleRes = R.string.email_contact
    }

    data object Login : RealEstateDestination() {
        override val route = "login"
        override val titleRes = R.string.login
    }

    data object ForgotPassword : RealEstateDestination() {
        override val route = "forgot_password"
        override val titleRes = R.string.forgot_password
    }

    data object AccountCreation  : RealEstateDestination() {
        override val route = "account_creation"
        override val titleRes = R.string.account_creation
    }

    data object Home : RealEstateDestination() {
        override val route = "home"
        override val titleRes = R.string.home
    }

    data object PropertyDetails : RealEstateDestination() {
        override val route = "property_details/{propertyId}"
        override val titleRes = R.string.property_details

        fun createRoute(propertyId: String): String = "property_details/$propertyId"
    }

    data object CreateProperty : RealEstateDestination() {
        override val route = "create_property"
        override val titleRes = R.string.create_property
    }

    data object ModifyProperty : RealEstateDestination() {
        override val route = "modify_property/{propertyId}"
        override val titleRes = R.string.modify_property

        fun createRoute(propertyId: Long): String = "modify_property/$propertyId"
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