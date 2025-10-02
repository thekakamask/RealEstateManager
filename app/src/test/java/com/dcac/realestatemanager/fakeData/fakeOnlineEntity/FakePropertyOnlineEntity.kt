package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.property.PropertyOnlineEntity
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user1
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user2
import com.dcac.realestatemanager.fakeData.fakeEntity.FakeUserEntity.user3

object FakePropertyOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val propertyEntity1 = PropertyOnlineEntity(
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
        userId = user1.id,
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 1,
        roomId = 1L
    )

    // Correspond exactement à FakePropertyEntity.property2
    val propertyEntity2 = PropertyOnlineEntity(
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
        userId = user2.id,
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 2,
        roomId = 2L
    )

    // Correspond exactement à FakePropertyEntity.property3
    val propertyEntity3 = PropertyOnlineEntity(
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
        userId = user3.id,
        staticMapPath = null,
        updatedAt = DEFAULT_TIMESTAMP + 3,
        roomId = 3L
    )

    val propertyOnlineEntityList = listOf(propertyEntity1, propertyEntity2, propertyEntity3)
}