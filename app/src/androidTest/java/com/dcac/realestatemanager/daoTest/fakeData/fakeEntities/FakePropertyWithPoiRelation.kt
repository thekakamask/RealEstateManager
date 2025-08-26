package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyWithPoiSRelation

object FakePropertyWithPoiRelation {

    val relation1 = PropertyWithPoiSRelation(
        property = FakePropertyEntity.property1,
        poiS = listOf(
            FakePoiEntity.poi1,
            FakePoiEntity.poi2,
            FakePoiEntity.poi3,
            FakePoiEntity.poi4,
            FakePoiEntity.poi5
        )
    )

    val relation2 = PropertyWithPoiSRelation(
        property = FakePropertyEntity.property2,
        poiS = listOf(
            FakePoiEntity.poi6,
            FakePoiEntity.poi7,
            FakePoiEntity.poi8,
            FakePoiEntity.poi9,
            FakePoiEntity.poi10
        )
    )

    val relation3 = PropertyWithPoiSRelation(
        property = FakePropertyEntity.property3,
        poiS = listOf(
            FakePoiEntity.poi11,
            FakePoiEntity.poi12,
            FakePoiEntity.poi13,
            FakePoiEntity.poi14,
            FakePoiEntity.poi15
        )
    )

    val relation4 = PropertyWithPoiSRelation(
        property = FakePropertyEntity.property4,
        poiS = listOf(
            FakePoiEntity.poi16,
            FakePoiEntity.poi17,
            FakePoiEntity.poi18,
            FakePoiEntity.poi19,
            FakePoiEntity.poi20
        )
    )

    val relation5 = PropertyWithPoiSRelation(
        property = FakePropertyEntity.property5,
        poiS = listOf(
            FakePoiEntity.poi16,
            FakePoiEntity.poi17,
            FakePoiEntity.poi18,
            FakePoiEntity.poi19,
            FakePoiEntity.poi20
        )
    )

    val fakePropertyWithPoiRelationList = listOf(relation1, relation2, relation3, relation4, relation5)
}