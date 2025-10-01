package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.PropertyPoiCross

object FakePropertyPoiCrossModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val cross1 = PropertyPoiCross(
        FakePropertyModel.property1.id,
        FakePoiModel.poi1.id,
        updatedAt =  DEFAULT_TIMESTAMP + 1
    )
    val cross2 = PropertyPoiCross(
        FakePropertyModel.property1.id, FakePoiModel.poi2.id,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )
    val cross3 = PropertyPoiCross(
        FakePropertyModel.property2.id,
        FakePoiModel.poi2.id,
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )
    val cross4 = PropertyPoiCross(
        FakePropertyModel.property2.id,
        FakePoiModel.poi3.id,
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 4
    )
    val cross5 = PropertyPoiCross(
        FakePropertyModel.property3.id, FakePoiModel.poi1.id,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 5
    )
    val cross6 = PropertyPoiCross(
        FakePropertyModel.property3.id, FakePoiModel.poi3.id,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val allCrossRefsNotDeleted = listOf(
        cross1, cross2, cross3, cross4
    )

    val allCrossRefs = listOf(
        cross1, cross2, cross3, cross4, cross5,
        cross6
    )
}
