package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel.user1
import com.dcac.realestatemanager.fakeData.fakeModel.FakeUserModel.user2
import com.dcac.realestatemanager.model.Property
import org.threeten.bp.LocalDate

object FakePropertyModel {

    val property1 = Property(
        id = 1L,
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        isSold = false,
        entryDate = LocalDate.parse("2025-08-01"),
        saleDate = null,
        user = user1,
        staticMapPath = null
    )

    val property2 = Property(
        id = 2L,
        title = "Villa Montmartre",
        type = "House",
        price = 550_000,
        surface = 200,
        rooms = 6,
        description = "Charming villa with terrace in Montmartre.",
        address = "27 Rue Lepic, 75018 Paris",
        isSold = false,
        entryDate = LocalDate.parse("2025-08-02"),
        saleDate = null,
        user = user1,
        staticMapPath = null
    )

    val property3 = Property(
        id = 3L,
        title = "Studio Latin Quarter",
        type = "Studio",
        price = 180_000,
        surface = 40,
        rooms = 1,
        description = "Bright studio in the heart of the Latin Quarter.",
        address = "5 Rue des Écoles, 75005 Paris",
        isSold = true,
        entryDate = LocalDate.parse("2025-08-03"),
        saleDate = LocalDate.parse("2025-08-20"),
        user = user2,
        staticMapPath = null
    )

    val property4 = Property(
        id = 4L,
        title = "Flat Bastille",
        type = "Apartment",
        price = 400_000,
        surface = 95,
        rooms = 4,
        description = "Modern flat 2 min from Place de la Bastille.",
        address = "14 Rue de la Roquette, 75011 Paris",
        isSold = false,
        entryDate = LocalDate.parse("2025-08-04"),
        saleDate = null,
        user = user2,
        staticMapPath = null
    )

    val property5 = Property(
        id = 5L,
        title = "Penthouse Bastille",
        type = "Apartment",
        price = 750_000,
        surface = 140,
        rooms = 5,
        description = "Penthouse with view over Place de la Bastille.",
        address = "18 Rue de la Roquette, 75011 Paris",
        isSold = false,
        entryDate = LocalDate.parse("2025-08-05"),
        saleDate = null,
        user = user1,
        staticMapPath = null
    )

    val propertyModelList = listOf(property1, property2, property3, property4, property5)
}