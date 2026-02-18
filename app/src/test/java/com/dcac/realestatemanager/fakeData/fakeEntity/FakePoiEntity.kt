package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity


object FakePoiEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = PoiEntity(
        id = "poi-1",
        firestoreDocumentId = "firestore-poi-1",
        name = "Franprix République",
        type = "Supermarché",
        address = "10 Place de la République, 75011 Paris",
        latitude = 48.867,
        longitude = 2.363,
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val poi2 = PoiEntity(
        id = "poi-2",
        firestoreDocumentId = "firestore-poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        address = "5 Rue Jean Jaurès, 75011 Paris",
        latitude = 48.868,
        longitude = 2.361,
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val poi3 = PoiEntity(
        id = "poi-3",
        firestoreDocumentId = "firestore-poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
        address = "2 Boulevard Magenta, 75011 Paris",
        latitude = 48.869,
        longitude = 2.364,
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val poiEntityList = listOf(
        poi1, poi2, poi3
    )

    val poiEntityListNotDeleted = listOf(
        poi1, poi2
    )
}
