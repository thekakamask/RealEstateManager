package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.StaticMap

object FakeStaticMapModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val staticMap1 = StaticMap(
        universalLocalId = "static-map-1",
        firestoreDocumentId = "firestore-static-map-1",
        universalLocalPropertyId = "property-1",
        uri = "file://static_map_1.png",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val staticMap2 = StaticMap(
        universalLocalId = "static-map-2",
        firestoreDocumentId = "firestore-static-map-2",
        universalLocalPropertyId = "property-2",
        uri = "file://static_map_2.png",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val staticMap3 = StaticMap(
        universalLocalId = "static-map-3",
        firestoreDocumentId = "firestore-static-map-3",
        universalLocalPropertyId = "property-3",
        uri = "file://static_map_3.png",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val staticMapList = listOf(staticMap1, staticMap2, staticMap3)
    val staticMapListNotDeleted = listOf(staticMap1, staticMap2)
}