package com.dcac.realestatemanager.ui.homePage

sealed class HomeDestination {
    data object PropertyList : HomeDestination()
    data object GoogleMap : HomeDestination()
}