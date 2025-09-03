package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.PropertyPoiCross

object FakePropertyPoiCrossModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val cross1 = PropertyPoiCross(1L, 1L, true, DEFAULT_TIMESTAMP + 1)
    val cross2 = PropertyPoiCross(1L, 2L, true, DEFAULT_TIMESTAMP + 2)
    val cross3 = PropertyPoiCross(1L, 3L, true, DEFAULT_TIMESTAMP + 3)
    val cross4 = PropertyPoiCross(1L, 4L, true, DEFAULT_TIMESTAMP + 4)
    val cross5 = PropertyPoiCross(1L, 5L, true, DEFAULT_TIMESTAMP + 5)

    val cross6 = PropertyPoiCross(2L, 6L, true, DEFAULT_TIMESTAMP + 6)
    val cross7 = PropertyPoiCross(2L, 7L, true, DEFAULT_TIMESTAMP + 7)
    val cross8 = PropertyPoiCross(2L, 8L, true, DEFAULT_TIMESTAMP + 8)
    val cross9 = PropertyPoiCross(2L, 9L, true, DEFAULT_TIMESTAMP + 9)
    val cross10 = PropertyPoiCross(2L, 10L, true, DEFAULT_TIMESTAMP + 10)

    val cross11 = PropertyPoiCross(3L, 11L, true, DEFAULT_TIMESTAMP + 11)
    val cross12 = PropertyPoiCross(3L, 12L, true, DEFAULT_TIMESTAMP + 12)
    val cross13 = PropertyPoiCross(3L, 13L, true, DEFAULT_TIMESTAMP + 13)
    val cross14 = PropertyPoiCross(3L, 14L, true, DEFAULT_TIMESTAMP + 14)
    val cross15 = PropertyPoiCross(3L, 15L, true, DEFAULT_TIMESTAMP + 15)

    val cross16 = PropertyPoiCross(4L, 16L, true, DEFAULT_TIMESTAMP + 16)
    val cross17 = PropertyPoiCross(4L, 17L, true, DEFAULT_TIMESTAMP + 17)
    val cross18 = PropertyPoiCross(4L, 18L, true, DEFAULT_TIMESTAMP + 18)
    val cross19 = PropertyPoiCross(4L, 19L, true, DEFAULT_TIMESTAMP + 19)
    val cross20 = PropertyPoiCross(4L, 20L, true, DEFAULT_TIMESTAMP + 20)

    val cross21 = PropertyPoiCross(5L, 16L, true, DEFAULT_TIMESTAMP + 21)
    val cross22 = PropertyPoiCross(5L, 17L, true, DEFAULT_TIMESTAMP + 22)
    val cross23 = PropertyPoiCross(5L, 18L, true, DEFAULT_TIMESTAMP + 23)
    val cross24 = PropertyPoiCross(5L, 19L, true, DEFAULT_TIMESTAMP + 24)
    val cross25 = PropertyPoiCross(5L, 20L, true, DEFAULT_TIMESTAMP + 25)

    val propertyPoiCrossModelList = listOf(
        cross1, cross2, cross3, cross4, cross5,
        cross6, cross7, cross8, cross9, cross10,
        cross11, cross12, cross13, cross14, cross15,
        cross16, cross17, cross18, cross19, cross20,
        cross21, cross22, cross23, cross24, cross25
    )
}
