package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

object FakePropertyEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val property1 = PropertyEntity(
        id = "property-1",
        firestoreDocumentId = "firestore-property-1",
        universalLocalUserId = FakeUserEntity.user1.id,
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
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val property2 = PropertyEntity(
        id = "property-2",
        firestoreDocumentId = "firestore-property-2",
        universalLocalUserId = FakeUserEntity.user2.id,
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
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val property3 = PropertyEntity(
        id = "property-3",
        firestoreDocumentId = "firestore-property-3",
        universalLocalUserId = FakeUserEntity.user3.id,
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
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val propertyEntityList = listOf(property1, property2, property3)

    val propertyEntityListNotDeleted = listOf(property1, property2)
}