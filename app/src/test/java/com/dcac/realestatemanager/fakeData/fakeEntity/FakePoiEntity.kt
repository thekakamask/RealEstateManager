package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity


object FakePoiEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = PoiEntity(
        id = "poi-1",
        firestoreDocumentId = "firestore-poi-1",
        name = "Franprix République",
        type = "Supermarché",
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val poi2 = PoiEntity(
        id = "poi-2",
        firestoreDocumentId = "firestore-poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val poi3 = PoiEntity(
        id = "poi-3",
        firestoreDocumentId = "firestore-poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
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
