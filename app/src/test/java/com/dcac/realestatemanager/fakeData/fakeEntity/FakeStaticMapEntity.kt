package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.staticMap.StaticMapEntity

object FakeStaticMapEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val staticMap1 = StaticMapEntity(
        id = "static-map-1",
        firestoreDocumentId = "firestore-static-map-1",
        universalLocalPropertyId = "property-1",
        uri = "file://static_map_1.png",
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val staticMap2 = StaticMapEntity(
        id = "static-map-2",
        firestoreDocumentId = "firestore-static-map-2",
        universalLocalPropertyId = "property-2",
        uri = "file://static_map_2.png",
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val staticMap3 = StaticMapEntity(
        id = "static-map-3",
        firestoreDocumentId = "firestore-static-map-3",
        universalLocalPropertyId = "property-3",
        uri = "file://static_map_3.png",
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val staticMapEntityList = listOf(staticMap1, staticMap2, staticMap3)

    val staticMapEntityListNotDeleted = staticMapEntityList.filter { !it.isDeleted }
}