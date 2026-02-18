package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.FirestoreCrossDocument
import com.dcac.realestatemanager.data.firebaseDatabase.propertyPoiCross.PropertyPoiCrossOnlineEntity

object FakePropertyPoiCrossOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val crossOnline1 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-1",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val crossOnline2 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalPropertyId = "property-1",
        universalLocalPoiId = "poi-2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val crossOnline3 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-2",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val crossOnline4 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalPropertyId = "property-2",
        universalLocalPoiId = "poi-3",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 4
    )

    val crossOnline5 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-1",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 5
    )

    val crossOnline6 = PropertyPoiCrossOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalPropertyId = "property-3",
        universalLocalPoiId = "poi-3",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val propertyPoiCrossOnlineEntityList = listOf(
        crossOnline1, crossOnline2, crossOnline3, crossOnline4, crossOnline5, crossOnline6
    )

    val firestoreCrossDocument1 = FirestoreCrossDocument(
        firebaseId = "firestore-cross-1",
        cross = crossOnline1
    )
    val firestoreCrossDocument2 = FirestoreCrossDocument(
        firebaseId = "firestore-cross-2",
        cross = crossOnline2
    )
    val firestoreCrossDocument3 = FirestoreCrossDocument(
        firebaseId = "firestore-cross-3",
        cross = crossOnline3
    )

    val firestoreCrossDocument4 = FirestoreCrossDocument(
        firebaseId= "firestore-cross-4",
        cross = crossOnline4
    )

    val firestoreCrossDocument5= FirestoreCrossDocument(
        firebaseId= "firestore-cross-5",
        cross = crossOnline5
    )

    val firestoreCrossDocument6 = FirestoreCrossDocument(
        firebaseId= "firestore-cross-6",
        cross = crossOnline6
    )

    val firestoreCrossDocumentList = listOf(
        firestoreCrossDocument1,
        firestoreCrossDocument2,
        firestoreCrossDocument3,
        firestoreCrossDocument4,
        firestoreCrossDocument5,
        firestoreCrossDocument6
    )
}