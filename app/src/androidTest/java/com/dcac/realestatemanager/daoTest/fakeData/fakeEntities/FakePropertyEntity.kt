package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

object FakePropertyEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val property1 = PropertyEntity(
        id = 1L,
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        isSold = true,
        entryDate = "01/08/2025",
        saleDate = null,
        userId = FakeUserEntity.user1.id,
        staticMapPath = null,
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val property2 = PropertyEntity(
        id = 2L,
        title = "Villa Montmartre",
        type = "House",
        price = 550_000,
        surface = 200,
        rooms = 6,
        description = "Charming villa with terrace in Montmartre.",
        address = "27 Rue Lepic, 75018 Paris",
        isSold = false,
        entryDate = "02/08/2025",
        saleDate = null,
        userId = FakeUserEntity.user2.id,
        staticMapPath = null,
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val property3 = PropertyEntity(
        id = 3L,
        title = "Studio Latin Quarter",
        type = "Studio",
        price = 180_000,
        surface = 40,
        rooms = 1,
        description = "Bright studio in the heart of the Latin Quarter.",
        address = "5 Rue des Écoles, 75005 Paris",
        isSold = false,
        entryDate = "03/08/2025",
        saleDate = "20/08/2025",
        userId = FakeUserEntity.user3.id,
        staticMapPath = null,
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val propertyEntityList = listOf(property1, property2, property3)

    val propertyEntityListNotDeleted = listOf(property1, property2)
}