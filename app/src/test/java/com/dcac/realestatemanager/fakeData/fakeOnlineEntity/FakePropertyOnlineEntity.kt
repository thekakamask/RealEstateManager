package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.property.FirestorePropertyDocument
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity

object FakePropertyOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyOnline1 = PropertyOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalId = "property-1",
        universalLocalUserId = "user-1",
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        latitude = 48.867,
        longitude = 2.363,
        isSold = true,
        entryDate = "2025-08-01",
        saleDate = "2025-08-20",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        isDeleted = false
    )

    val propertyOnline2 = PropertyOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalId = "property-2",
        universalLocalUserId = "user-2",
        title = "Villa Montmartre",
        type = "House",
        price = 550_000,
        surface = 200,
        rooms = 6,
        description = "Charming villa with terrace in Montmartre.",
        address = "27 Rue Lepic, 75018 Paris",
        latitude = 48.886,
        longitude = 2.343,
        isSold = false,
        entryDate = "2025-08-02",
        saleDate = null,
        updatedAt = DEFAULT_TIMESTAMP + 2,
        isDeleted = false
    )

    val propertyOnline3 = PropertyOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalId = "property-3",
        universalLocalUserId = "user-3",
        title = "Studio Latin Quarter",
        type = "Studio",
        price = 180_000,
        surface = 40,
        rooms = 1,
        description = "Bright studio in the heart of the Latin Quarter.",
        address = "5 Rue des Écoles, 75005 Paris",
        latitude = 48.849,
        longitude = 2.345,
        isSold = false,
        entryDate = "2025-08-03",
        saleDate = null,
        updatedAt = DEFAULT_TIMESTAMP + 3,
        isDeleted = true
    )

    val firestorePropertyDocument1 = FirestorePropertyDocument(
        firebaseId = "firestore-property-1",
        property = propertyOnline1
    )

    val firestorePropertyDocument2 = FirestorePropertyDocument(
        firebaseId = "firestore-property-2",
        property = propertyOnline2
    )

    val firestorePropertyDocument3 = FirestorePropertyDocument(
        firebaseId = "firestore-property-3",
        property = propertyOnline3
    )

    val propertyOnlineEntityList = listOf(
        propertyOnline1, propertyOnline2, propertyOnline3
    )

    val firestorePropertyDocumentList = listOf(
        firestorePropertyDocument1,
        firestorePropertyDocument2,
        firestorePropertyDocument3
    )
}