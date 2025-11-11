package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirestoreCrossDocument
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity

object FakePropertyPoiCrossOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val crossOnline1 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-1",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val crossOnline2 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val crossOnline3 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-2",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val crossOnline4 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-3",
        updatedAt = DEFAULT_TIMESTAMP + 4
    )

    val crossOnline5 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-1",
        updatedAt = DEFAULT_TIMESTAMP + 5
    )

    val crossOnline6 = PropertyPoiCrossOnlineEntity(
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-3",
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val propertyPoiCrossOnlineEntityList = listOf(
        crossOnline1, crossOnline2, crossOnline3, crossOnline4, crossOnline5, crossOnline6
    )

    val firestoreCrossDocument1 = FirestoreCrossDocument(
        id = "firestore-cross-1",
        cross = crossOnline1
    )
    val firestoreCrossDocument2 = FirestoreCrossDocument(
        id = "firestore-cross-2",
        cross = crossOnline2
    )
    val firestoreCrossDocument3 = FirestoreCrossDocument(
        id = "firestore-cross-3",
        cross = crossOnline3
    )

    val firestoreCrossDocumentList = listOf(
        firestoreCrossDocument1,
        firestoreCrossDocument2,
        firestoreCrossDocument3
    )
}