package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity

object FakePropertyPoiCrossOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val cross1 = PropertyPoiCrossOnlineEntity(
        propertyId = 1L,
        poiId = 1L,
        updatedAt = DEFAULT_TIMESTAMP + 1,
        roomId = 0L
    )

    val cross2 = PropertyPoiCrossOnlineEntity(
        propertyId = 1L,
        poiId = 2L,
        updatedAt = DEFAULT_TIMESTAMP + 2,
        roomId = 0L
    )

    val cross3 = PropertyPoiCrossOnlineEntity(
        propertyId = 2L,
        poiId = 2L,
        updatedAt = DEFAULT_TIMESTAMP + 3,
        roomId = 0L
    )

    val cross4 = PropertyPoiCrossOnlineEntity(
        propertyId = 2L,
        poiId = 3L,
        updatedAt = DEFAULT_TIMESTAMP + 4,
        roomId = 0L
    )

    val cross5 = PropertyPoiCrossOnlineEntity(
        propertyId = 3L,
        poiId = 1L,
        updatedAt = DEFAULT_TIMESTAMP + 5,
    )

    val cross6 = PropertyPoiCrossOnlineEntity(
        propertyId = 3L,
        poiId = 3L,
        updatedAt = DEFAULT_TIMESTAMP + 6,
    )



    val propertyPoiCrossOnlineEntityList = listOf(
        cross1, cross2, cross3, cross4, cross5, cross6
    )
}