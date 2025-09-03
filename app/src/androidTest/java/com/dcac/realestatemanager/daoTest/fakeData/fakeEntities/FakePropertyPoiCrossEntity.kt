package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity

object FakePropertyPoiCrossEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyPoiCross1 = PropertyPoiCrossEntity(FakePropertyEntity.property1.id, FakePoiEntity.poi1.id, true, DEFAULT_TIMESTAMP + 1)
    val propertyPoiCross2 = PropertyPoiCrossEntity(FakePropertyEntity.property1.id, FakePoiEntity.poi2.id, true, DEFAULT_TIMESTAMP + 2)
    val propertyPoiCross3 = PropertyPoiCrossEntity(FakePropertyEntity.property1.id, FakePoiEntity.poi3.id, true, DEFAULT_TIMESTAMP + 3)
    val propertyPoiCross4 = PropertyPoiCrossEntity(FakePropertyEntity.property1.id, FakePoiEntity.poi4.id, true, DEFAULT_TIMESTAMP + 4)
    val propertyPoiCross5 = PropertyPoiCrossEntity(FakePropertyEntity.property1.id, FakePoiEntity.poi5.id, true, DEFAULT_TIMESTAMP + 5)

    val propertyPoiCross6 = PropertyPoiCrossEntity(FakePropertyEntity.property2.id, FakePoiEntity.poi6.id, true, DEFAULT_TIMESTAMP + 6)
    val propertyPoiCross7 = PropertyPoiCrossEntity(FakePropertyEntity.property2.id, FakePoiEntity.poi7.id, true, DEFAULT_TIMESTAMP + 7)
    val propertyPoiCross8 = PropertyPoiCrossEntity(FakePropertyEntity.property2.id, FakePoiEntity.poi8.id, true, DEFAULT_TIMESTAMP + 8)
    val propertyPoiCross9 = PropertyPoiCrossEntity(FakePropertyEntity.property2.id, FakePoiEntity.poi9.id, true, DEFAULT_TIMESTAMP + 9)
    val propertyPoiCross10 = PropertyPoiCrossEntity(FakePropertyEntity.property2.id, FakePoiEntity.poi10.id, true, DEFAULT_TIMESTAMP + 10)

    val propertyPoiCross11 = PropertyPoiCrossEntity(FakePropertyEntity.property3.id, FakePoiEntity.poi11.id, true, DEFAULT_TIMESTAMP + 11)
    val propertyPoiCross12 = PropertyPoiCrossEntity(FakePropertyEntity.property3.id, FakePoiEntity.poi12.id, true, DEFAULT_TIMESTAMP + 12)
    val propertyPoiCross13 = PropertyPoiCrossEntity(FakePropertyEntity.property3.id, FakePoiEntity.poi13.id, true, DEFAULT_TIMESTAMP + 13)
    val propertyPoiCross14 = PropertyPoiCrossEntity(FakePropertyEntity.property3.id, FakePoiEntity.poi14.id, true, DEFAULT_TIMESTAMP + 14)
    val propertyPoiCross15 = PropertyPoiCrossEntity(FakePropertyEntity.property3.id, FakePoiEntity.poi15.id, true, DEFAULT_TIMESTAMP + 15)

    val propertyPoiCross16 = PropertyPoiCrossEntity(FakePropertyEntity.property4.id, FakePoiEntity.poi16.id, true, DEFAULT_TIMESTAMP + 16)
    val propertyPoiCross17 = PropertyPoiCrossEntity(FakePropertyEntity.property4.id, FakePoiEntity.poi17.id, true, DEFAULT_TIMESTAMP + 17)
    val propertyPoiCross18 = PropertyPoiCrossEntity(FakePropertyEntity.property4.id, FakePoiEntity.poi18.id, true, DEFAULT_TIMESTAMP + 18)
    val propertyPoiCross19 = PropertyPoiCrossEntity(FakePropertyEntity.property4.id, FakePoiEntity.poi19.id, true, DEFAULT_TIMESTAMP + 19)
    val propertyPoiCross20 = PropertyPoiCrossEntity(FakePropertyEntity.property4.id, FakePoiEntity.poi20.id, true, DEFAULT_TIMESTAMP + 20)

    val propertyPoiCross21 = PropertyPoiCrossEntity(FakePropertyEntity.property5.id, FakePoiEntity.poi16.id, true, DEFAULT_TIMESTAMP + 21)
    val propertyPoiCross22 = PropertyPoiCrossEntity(FakePropertyEntity.property5.id, FakePoiEntity.poi17.id, true, DEFAULT_TIMESTAMP + 22)
    val propertyPoiCross23 = PropertyPoiCrossEntity(FakePropertyEntity.property5.id, FakePoiEntity.poi18.id, true, DEFAULT_TIMESTAMP + 23)
    val propertyPoiCross24 = PropertyPoiCrossEntity(FakePropertyEntity.property5.id, FakePoiEntity.poi19.id, true, DEFAULT_TIMESTAMP + 24)
    val propertyPoiCross25 = PropertyPoiCrossEntity(FakePropertyEntity.property5.id, FakePoiEntity.poi20.id, true, DEFAULT_TIMESTAMP + 25)

    val propertyPoiCrossEntityList = listOf(
        propertyPoiCross1, propertyPoiCross2, propertyPoiCross3, propertyPoiCross4, propertyPoiCross5,
        propertyPoiCross6, propertyPoiCross7, propertyPoiCross8, propertyPoiCross9, propertyPoiCross10,
        propertyPoiCross11, propertyPoiCross12, propertyPoiCross13, propertyPoiCross14, propertyPoiCross15,
        propertyPoiCross16, propertyPoiCross17, propertyPoiCross18, propertyPoiCross19, propertyPoiCross20,
        propertyPoiCross21, propertyPoiCross22, propertyPoiCross23, propertyPoiCross24, propertyPoiCross25
    )
}