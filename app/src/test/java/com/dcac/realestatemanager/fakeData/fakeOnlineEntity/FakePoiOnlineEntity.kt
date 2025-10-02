package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity

object FakePoiOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poiEntity1 = PoiOnlineEntity(
        name = "Franprix République",
        type = "Supermarché",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        roomId = 1L
    )
    val poiEntity2 = PoiOnlineEntity(
        name = "École Primaire Jean Jaurès",
        type = "École",
        updatedAt = DEFAULT_TIMESTAMP + 2,
        roomId = 2L
    )
    val poiEntity3 = PoiOnlineEntity(
        name = "Pharmacie République",
        type = "Pharmacie",
        updatedAt = DEFAULT_TIMESTAMP + 3,
        roomId = 3L
    )

    val poiOnlineEntityList = listOf(
        poiEntity1, poiEntity2, poiEntity3
    )
}