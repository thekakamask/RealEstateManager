package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.FirestoreStaticMapDocument
import com.dcac.realestatemanager.data.firebaseDatabase.staticMap.StaticMapOnlineEntity

object FakeStaticMapOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val staticMapOnline1 = StaticMapOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalId = "static-map-1",
        universalLocalPropertyId = "property-1",
        storageUrl = "https://firebase.storage.com/static_map_1.png",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        isDeleted = false
    )

    val staticMapOnline2 = StaticMapOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalId = "static-map-2",
        universalLocalPropertyId = "property-2",
        storageUrl = "https://firebase.storage.com/static_map_2.png",
        updatedAt = DEFAULT_TIMESTAMP + 2,
        isDeleted = false
    )

    val staticMapOnline3 = StaticMapOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalId = "static-map-3",
        universalLocalPropertyId = "property-3",
        storageUrl = "https://firebase.storage.com/static_map_3.png",
        updatedAt = DEFAULT_TIMESTAMP + 3,
        isDeleted = true
    )

    val firestoreStaticMapDocument1 = FirestoreStaticMapDocument(
        firebaseId = "firestore-static-map-1",
        staticMap = staticMapOnline1
    )

    val firestoreStaticMapDocument2 = FirestoreStaticMapDocument(
        firebaseId = "firestore-static-map-2",
        staticMap = staticMapOnline2
    )

    val firestoreStaticMapDocument3 = FirestoreStaticMapDocument(
        firebaseId = "firestore-static-map-3",
        staticMap = staticMapOnline3
    )

    val staticMapOnlineEntityList = listOf(
        staticMapOnline1,
        staticMapOnline2,
        staticMapOnline3
    )

    val firestoreStaticMapDocumentList = listOf(
        firestoreStaticMapDocument1,
        firestoreStaticMapDocument2,
        firestoreStaticMapDocument3
    )
}