package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity

object FakePhotoEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photo1 = PhotoEntity(
        id = "photo-1",
        firestoreDocumentId = "firestore-photo-1",
        universalLocalPropertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République",
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val photo2 = PhotoEntity(
        id = "photo-2",
        firestoreDocumentId = "firestore-photo-2",
        universalLocalPropertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République",
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val photo3 = PhotoEntity(
        id = "photo-3",
        firestoreDocumentId = "firestore-photo-3",
        universalLocalPropertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre",
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val photoEntityList = listOf(
        photo1, photo2, photo3
    )

    val photoEntityListNotDeleted = listOf(
        photo1, photo2
    )
}