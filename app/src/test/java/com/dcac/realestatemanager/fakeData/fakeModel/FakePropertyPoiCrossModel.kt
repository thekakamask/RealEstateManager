package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.fakeData.fakeEntity.FakePoiEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.model.PropertyPoiCross

object FakePropertyPoiCrossModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val cross1 = PropertyPoiCross(
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-1",
        firestoreDocumentId = "firestore-cross-1",
        updatedAt =  DEFAULT_TIMESTAMP + 1
    )
    val cross2 = PropertyPoiCross(
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-2",
        firestoreDocumentId = "firestore-cross-2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )
    val cross3 = PropertyPoiCross(
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-2",
        firestoreDocumentId = "firestore-cross-3",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )
    val cross4 = PropertyPoiCross(
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-3",
        firestoreDocumentId = "firestore-cross-4",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 4
    )
    val cross5 = PropertyPoiCross(
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-1",
        firestoreDocumentId = "firestore-cross-5",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 5
    )
    val cross6 = PropertyPoiCross(
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-3",
        firestoreDocumentId = "firestore-cross-6",
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
