package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.Photo

object FakePhotoModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photo1 = Photo(
        universalLocalId = "photo-1",
        firestoreDocumentId = "firestore-photo-1",
        universalLocalPropertyId = "property-1",
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val photo2 = Photo(
        universalLocalId = "photo-2",
        firestoreDocumentId = "firestore-photo-2",
        universalLocalPropertyId = "property-2",
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val photo3 = Photo(
        universalLocalId = "photo-3",
        firestoreDocumentId = "firestore-photo-3",
        universalLocalPropertyId = "property-3",
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val photoModelList = listOf(
        photo1, photo2, photo3
    )
    val photoModelListNotDeleted = listOf(photo1, photo2)
}
