package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.property.FirestorePropertyDocument
import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user1
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user2
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user3

object FakePropertyOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyOnline1 = PropertyOnlineEntity(
        universalLocalId = "property-1",
        universalLocalUserId = user1.id,
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        isSold = true,
        entryDate = "2025-08-01",
        saleDate = "2025-08-20",
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val propertyOnline2 = PropertyOnlineEntity(
        universalLocalId = "property-2",
        universalLocalUserId = user2.id,
        title = "Villa Montmartre",
        type = "House",
        price = 550_000,
        surface = 200,
        rooms = 6,
        description = "Charming villa with terrace in Montmartre.",
        address = "27 Rue Lepic, 75018 Paris",
        isSold = false,
        entryDate = "2025-08-02",
        saleDate = null,
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val propertyOnline3 = PropertyOnlineEntity(
        universalLocalId = "property-3",
        universalLocalUserId = user3.id,
        title = "Studio Latin Quarter",
        type = "Studio",
        price = 180_000,
        surface = 40,
        rooms = 1,
        description = "Bright studio in the heart of the Latin Quarter.",
        address = "5 Rue des Écoles, 75005 Paris",
        isSold = false,
        entryDate = "2025-08-03",
        saleDate = null,
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val firestorePropertyDocument1 = FirestorePropertyDocument(
        id = "firestore-property-1",
        property = propertyOnline1
    )

    val firestorePropertyDocument2 = FirestorePropertyDocument(
        id = "firestore-property-2",
        property = propertyOnline2
    )

    val firestorePropertyDocument3 = FirestorePropertyDocument(
        id = "firestore-property-3",
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