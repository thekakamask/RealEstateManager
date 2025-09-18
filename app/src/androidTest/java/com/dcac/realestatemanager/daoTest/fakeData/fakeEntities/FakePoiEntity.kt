package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.poi.PoiEntity

object FakePoiEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poi1 = PoiEntity(
        id = 1L,
        name = "Franprix République",
        type = "Supermarché",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val poi2 = PoiEntity(
        id = 2L,
        name = "École Primaire Jean Jaurès",
        type = "École",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val poi3 = PoiEntity(3L, "Pharmacie République", "Pharmacie",
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