package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.fakeData.fakeModel.FakePhotoModel.photoModelList
import com.dcac.realestatemanager.fakeData.fakeModel.FakePoiModel.poiModelList
import com.dcac.realestatemanager.model.Property
import org.threeten.bp.LocalDate

object FakePropertyModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val property1 = Property(
        universalLocalId = "property-1",
        firestoreDocumentId = "firestore-property-1",
        universalLocalUserId = "user-1",
        title = "Loft République",
        type = "Loft",
        price = 300_000,
        surface = 85,
        rooms = 3,
        description = "Spacious loft near Place de la République.",
        address = "12 Rue du Faubourg du Temple, 75011 Paris",
        isSold = true,
        entryDate = LocalDate.parse("2025-08-01"),
        saleDate = LocalDate.parse("2025-08-20"),
        staticMapPath = null,
        photos = listOf(photoModelList[0]),
        poiS = listOf(poiModelList[0], poiModelList[1]),
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val property2 = Property(
        universalLocalId = "property-2",
        firestoreDocumentId = "firestore-property-2",
        universalLocalUserId = "user-2",
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
        staticMapPath = null,
        photos = listOf(photoModelList[1]),
        poiS = listOf(poiModelList[1]),
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val property3 = Property(
        universalLocalId = "property-3",
        firestoreDocumentId = "firestore-property-3",
        universalLocalUserId = "user-3",
        title = "Studio Latin Quarter",
        type = "Studio",
        price = 180_000,
        surface = 40,
        rooms = 1,
        description = "Bright studio in the heart of the Latin Quarter.",
        address = "5 Rue des Écoles, 75005 Paris",
        isSold = false,
        entryDate = LocalDate.parse("2025-08-03"),
        saleDate = LocalDate.parse("2025-08-20"),
        staticMapPath = null,
        photos = listOf(photoModelList[2]),
        poiS = emptyList(),
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val propertyModelList = listOf(property1, property2, property3)
    val propertyModelListNotDeleted = listOf(property1, property2)
}
