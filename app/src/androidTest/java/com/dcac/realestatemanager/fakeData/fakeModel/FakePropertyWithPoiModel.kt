package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.PropertyWithPoiS

object FakePropertyWithPoiModel {

    val relation1 = PropertyWithPoiS(
        property = FakePropertyModel.property1,
        poiS = listOf(
            FakePoiModel.poi1, FakePoiModel.poi2,
            FakePoiModel.poi3, FakePoiModel.poi4,
            FakePoiModel.poi5
        )
    )

    val relation2 = PropertyWithPoiS(
        property = FakePropertyModel.property2,
        poiS = listOf(
            FakePoiModel.poi6, FakePoiModel.poi7,
            FakePoiModel.poi8, FakePoiModel.poi9,
            FakePoiModel.poi10
        )
    )

    val relation3 = PropertyWithPoiS(
        property = FakePropertyModel.property3,
        poiS = listOf(
            FakePoiModel.poi11, FakePoiModel.poi12,
            FakePoiModel.poi13, FakePoiModel.poi14,
            FakePoiModel.poi15
        )
    )

    val relation4 = PropertyWithPoiS(
        property = FakePropertyModel.property4,
        poiS = listOf(
            FakePoiModel.poi16, FakePoiModel.poi17,
            FakePoiModel.poi18, FakePoiModel.poi19,
            FakePoiModel.poi20
        )
    )

    val relation5 = PropertyWithPoiS(
        property = FakePropertyModel.property5,
        poiS = listOf(
            FakePoiModel.poi16, FakePoiModel.poi17,
            FakePoiModel.poi18, FakePoiModel.poi19,
            FakePoiModel.poi20
        )
    )

    val propertyWithPoiModelList = listOf(relation1, relation2, relation3, relation4, relation5)

}