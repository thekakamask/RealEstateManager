package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.Poi

object FakePoiModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = Poi(
        universalLocalId = "poi-1",
        firestoreDocumentId = "firestore-poi-1",
        name = "Franprix République",
        type = "Supermarché",
        address = "10 Place de la République, 75011 Paris",
        latitude = 48.867,
        longitude = 2.363,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val poi2 = Poi(
        universalLocalId = "poi-2",
        firestoreDocumentId = "firestore-poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        address = "5 Rue Jean Jaurès, 75011 Paris",
        latitude = 48.868,
        longitude = 2.361,
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )
    val poi3 = Poi(
        universalLocalId = "poi-3",
        firestoreDocumentId = "firestore-poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
        address = "2 Boulevard Magenta, 75011 Paris",
        latitude = 48.869,
        longitude = 2.364,
        isDeleted = true,
        isSynced = false,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val poiModelList = listOf(
        poi1, poi2, poi3
    )

    val poiModelListNotDeleted = listOf(poi1, poi2)

}
