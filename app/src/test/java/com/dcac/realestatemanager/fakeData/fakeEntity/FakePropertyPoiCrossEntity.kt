package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity

object FakePropertyPoiCrossEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyPoiCross1 = PropertyPoiCrossEntity(
        FakePropertyEntity.property1.id,
        FakePoiEntity.poi1.id,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val propertyPoiCross2 = PropertyPoiCrossEntity(
        FakePropertyEntity.property1.id,
        FakePoiEntity.poi2.id,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val propertyPoiCross3 = PropertyPoiCrossEntity(
        FakePropertyEntity.property2.id,
        FakePoiEntity.poi2.id,
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )
    val propertyPoiCross4 = PropertyPoiCrossEntity(
        FakePropertyEntity.property2.id,
        FakePoiEntity.poi3.id,
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 4
    )
    val propertyPoiCross5 = PropertyPoiCrossEntity(
        FakePropertyEntity.property3.id,
        FakePoiEntity.poi1.id,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 5
    )

    val propertyPoiCross6 = PropertyPoiCrossEntity(
        FakePropertyEntity.property3.id,
        FakePoiEntity.poi3.id,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val allCrossRefsNotDeleted = listOf(
        propertyPoiCross1, propertyPoiCross2, propertyPoiCross3, propertyPoiCross4
    )

    val allCrossRefs = listOf(
        propertyPoiCross1, propertyPoiCross2, propertyPoiCross3, propertyPoiCross4, propertyPoiCross5,
        propertyPoiCross6
    )
}
