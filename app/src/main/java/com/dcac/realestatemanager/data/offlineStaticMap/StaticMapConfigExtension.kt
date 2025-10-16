package com.dcac.realestatemanager.data.offlineStaticMap

import com.dcac.realestatemanager.model.Property

fun StaticMapConfig.Companion.createFromProperty(property: Property): StaticMapConfig {
    val location = property.address.replace(" ", "+") // Convert address for URL
    val marker = "color:blue|label:P|${location}"

    return StaticMapConfig(
        center = location,
        markers = listOf(marker)
    )
}