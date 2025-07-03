package com.dcac.realestatemanager.data.offlineStaticMap

//Represents the configuration for a static map request.
// Includes the map center, zoom, size, map type, and marker parameters.
data class StaticMapConfig(
    val center: String,                 // "latitude,longitude" or full address
    val zoom: Int = 15,                 // Suggested: 15 for street-level view
    val size: String = "600x400",       // Width x Height in pixels
    val mapType: String = "roadmap",    // roadmap | satellite | hybrid | terrain
    val markers: List<String>           // Example: ["color:red|label:P|lat,lng", ...]
)
