package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirestorePoiDocument
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
object FakePoiOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poiOnline1 = PoiOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalId = "poi-1",
        name = "Franprix République",
        type = "Supermarché",
        address = "10 Place de la République, 75011 Paris",
        latitude = 48.867,
        longitude = 2.363,
        updatedAt = DEFAULT_TIMESTAMP + 1,
        isDeleted = false
    )

    val poiOnline2 = PoiOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalId = "poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        address = "5 Rue Jean Jaurès, 75011 Paris",
        latitude = 48.868,
        longitude = 2.361,
        updatedAt = DEFAULT_TIMESTAMP + 2,
        isDeleted = false
    )

    val poiOnline3 = PoiOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalId = "poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
        address = "2 Boulevard Magenta, 75011 Paris",
        latitude = 48.869,
        longitude = 2.364,
        updatedAt = DEFAULT_TIMESTAMP + 3,
        isDeleted = true
    )

    val firestorePoiDocument1 = FirestorePoiDocument(
        firebaseId = "firestore-poi-1",
        poi = poiOnline1
    )

    val firestorePoiDocument2 = FirestorePoiDocument(
        firebaseId = "firestore-poi-2",
        poi = poiOnline2
    )

    val firestorePoiDocument3 = FirestorePoiDocument(
        firebaseId = "firestore-poi-3",
        poi = poiOnline3
    )

    val poiOnlineEntityList = listOf(
        poiOnline1, poiOnline2, poiOnline3
    )

    val firestorePoiDocumentList = listOf(
        firestorePoiDocument1,
        firestorePoiDocument2,
        firestorePoiDocument3
    )


}