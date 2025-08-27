package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.property.PropertyEntity

object FakePropertyEntity {

    val property1 = PropertyEntity(
        id = 1L,
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        isSold = false,
        entryDate = "2025-08-01",
        saleDate = null,
        userId = FakeUserEntity.user1.id,
        staticMapPath = null
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
        entryDate = "2025-08-02",
        saleDate = null,
        userId = FakeUserEntity.user1.id,
        staticMapPath = null
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
        isSold = true,
        entryDate = "2025-08-03",
        saleDate = "2025-08-20",
        userId = FakeUserEntity.user2.id,
        staticMapPath = null
    )
    val property4 = PropertyEntity(
        id = 4L,
        title = "Flat Bastille",
        type = "Apartment",
        price = 400_000,
        surface = 95,
        rooms = 4,
        description = "Modern flat 2 min from Place de la Bastille.",
        address = "14 Rue de la Roquette, 75011 Paris",
        isSold = false,
        entryDate = "2025-08-04",
        saleDate = null,
        userId = FakeUserEntity.user2.id,
        staticMapPath = null
    )
    val property5 = PropertyEntity(
        id = 5L,
        title = "Penthouse Bastille",
        type = "Apartment",
        price = 750_000,
        surface = 140,
        rooms = 5,
        description = "Penthouse with view over Place de la Bastille.",
        address = "18 Rue de la Roquette, 75011 Paris",
        isSold = false,
        entryDate = "2025-08-05",
        saleDate = null,
        userId = FakeUserEntity.user1.id,
        staticMapPath = null
    )
    val propertyEntityList = listOf(property1, property2, property3, property4, property5)
}