package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.propertyPoiCross.PropertyPoiCrossEntity

object FakePropertyPoiCrossEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyPoiCross1 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property1.id,
        universalLocalPoiId = FakePoiEntity.poi1.id,
        firestoreDocumentId = "firestore-cross-1",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val propertyPoiCross2 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property1.id,
        universalLocalPoiId = FakePoiEntity.poi2.id,
        firestoreDocumentId = "firestore-cross-2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val propertyPoiCross3 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property2.id,
        universalLocalPoiId = FakePoiEntity.poi2.id,
        firestoreDocumentId = "firestore-cross-3",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val propertyPoiCross4 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property2.id,
        universalLocalPoiId = FakePoiEntity.poi3.id,
        firestoreDocumentId = "firestore-cross-4",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 4
    )

    val propertyPoiCross5 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property3.id,
        universalLocalPoiId = FakePoiEntity.poi1.id,
        firestoreDocumentId = "firestore-cross-5",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 5
    )

    val propertyPoiCross6 = PropertyPoiCrossEntity(
        universalLocalPropertyId = FakePropertyEntity.property3.id,
        universalLocalPoiId = FakePoiEntity.poi3.id,
        firestoreDocumentId = "firestore-cross-6",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val allCrossRefsNotDeleted = listOf(
        propertyPoiCross1, propertyPoiCross2, propertyPoiCross3, propertyPoiCross4
    )

    val allCrossRefs = listOf(
        propertyPoiCross1, propertyPoiCross2, propertyPoiCross3,
        propertyPoiCross4, propertyPoiCross5, propertyPoiCross6
    )
}