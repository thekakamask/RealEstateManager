package com.dcac.realestatemanager.data.offlineDatabase.staticMap

//Represents the configuration for a static map request.
// Includes the map center, zoom, size, map type, and marker parameters.
data class StaticMapConfig(
    val center: String,
    val zoom: Int = 17,
    val size: String = "600x600",
    val mapType: String = "roadmap",
    val markers: List<String>,
    val styles: List<String> = emptyList()
) {
    companion object
}
