package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.Poi

object FakePoiModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = Poi(
        universalLocalId = "poi-1",
        firestoreDocumentId = "firestore-poi-1",
        name = "Franprix République",
        type = "Supermarché",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )
    val poi2 = Poi(
        universalLocalId = "poi-2",
        firestoreDocumentId = "firestore-poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )
    val poi3 = Poi(
        universalLocalId = "poi-3",
        firestoreDocumentId = "firestore-poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
        isSynced = false,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val poiModelList = listOf(
        poi1, poi2, poi3
    )

    val poiModelListNotDeleted = listOf(poi1, poi2)

}
