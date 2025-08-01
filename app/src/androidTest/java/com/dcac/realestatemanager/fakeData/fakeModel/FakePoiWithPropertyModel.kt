package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.PoiWithProperties

object FakePoiWithPropertyModel {

    val relation1 = PoiWithProperties(FakePoiModel.poi1, listOf(FakePropertyModel.property1))
    val relation2 = PoiWithProperties(FakePoiModel.poi2, listOf(FakePropertyModel.property1))
    val relation3 = PoiWithProperties(FakePoiModel.poi3, listOf(FakePropertyModel.property1))
    val relation4 = PoiWithProperties(FakePoiModel.poi4, listOf(FakePropertyModel.property1))
    val relation5 = PoiWithProperties(FakePoiModel.poi5, listOf(FakePropertyModel.property1))

    val relation6 = PoiWithProperties(FakePoiModel.poi6, listOf(FakePropertyModel.property2))
    val relation7 = PoiWithProperties(FakePoiModel.poi7, listOf(FakePropertyModel.property2))
    val relation8 = PoiWithProperties(FakePoiModel.poi8, listOf(FakePropertyModel.property2))
    val relation9 = PoiWithProperties(FakePoiModel.poi9, listOf(FakePropertyModel.property2))
    val relation10 = PoiWithProperties(FakePoiModel.poi10, listOf(FakePropertyModel.property2))

    val relation11 = PoiWithProperties(FakePoiModel.poi11, listOf(FakePropertyModel.property3))
    val relation12 = PoiWithProperties(FakePoiModel.poi12, listOf(FakePropertyModel.property3))
    val relation13 = PoiWithProperties(FakePoiModel.poi13, listOf(FakePropertyModel.property3))
    val relation14 = PoiWithProperties(FakePoiModel.poi14, listOf(FakePropertyModel.property3))
    val relation15 = PoiWithProperties(FakePoiModel.poi15, listOf(FakePropertyModel.property3))

    val relation16 = PoiWithProperties(FakePoiModel.poi16, listOf(FakePropertyModel.property4, FakePropertyModel.property5))
    val relation17 = PoiWithProperties(FakePoiModel.poi17, listOf(FakePropertyModel.property4, FakePropertyModel.property5))
    val relation18 = PoiWithProperties(FakePoiModel.poi18, listOf(FakePropertyModel.property4, FakePropertyModel.property5))
    val relation19 = PoiWithProperties(FakePoiModel.poi19, listOf(FakePropertyModel.property4, FakePropertyModel.property5))
    val relation20 = PoiWithProperties(FakePoiModel.poi20, listOf(FakePropertyModel.property4, FakePropertyModel.property5))

    val poiWithPropertyList = listOf(
        relation1, relation2, relation3, relation4, relation5,
        relation6, relation7, relation8, relation9, relation10,
        relation11, relation12, relation13, relation14, relation15,
        relation16, relation17, relation18, relation19, relation20
    )

}