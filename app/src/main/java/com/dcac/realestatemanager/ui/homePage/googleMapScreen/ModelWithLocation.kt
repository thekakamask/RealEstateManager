package com.dcac.realestatemanager.ui.homePage.googleMapScreen

import com.dcac.realestatemanager.model.Poi
import com.dcac.realestatemanager.model.Property
import com.google.android.gms.maps.model.LatLng

data class PropertyWithLocation(
    val property: Property,
    val latLng: LatLng
)

data class PoiWithLocation(
    val poi: Poi,
    val latLng: LatLng
)