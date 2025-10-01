package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.Poi

object FakePoiModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = Poi(
        id = 1L,
        name = "Franprix République",
        type = "Supermarché",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )
    val poi2 = Poi(
        id = 2L,
        name = "École Primaire Jean Jaurès",
        type = "École",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )
    val poi3 = Poi(
        id = 3L,
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
