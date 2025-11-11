package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.poi.FirestorePoiDocument
import com.dcac.realestatemanager.data.firebaseDatabase.poi.PoiOnlineEntity
object FakePoiOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val poiOnline1 = PoiOnlineEntity(
        universalLocalId = "poi-1",
        name = "Franprix République",
        type = "Supermarché",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val poiOnline2 = PoiOnlineEntity(
        universalLocalId = "poi-2",
        name = "École Primaire Jean Jaurès",
        type = "École",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val poiOnline3 = PoiOnlineEntity(
        universalLocalId = "poi-3",
        name = "Pharmacie République",
        type = "Pharmacie",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val firestorePoiDocument1 = FirestorePoiDocument(
        id = "firestore-poi-1",
        poi = poiOnline1
    )

    val firestorePoiDocument2 = FirestorePoiDocument(
        id = "firestore-poi-2",
        poi = poiOnline2
    )

    val firestorePoiDocument3 = FirestorePoiDocument(
        id = "firestore-poi-3",
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