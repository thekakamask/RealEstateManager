package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity

object FakePhotoEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photo1 = PhotoEntity(
        id = 1L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val photo2 = PhotoEntity(
        id = 2L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val photo3 = PhotoEntity(
        id = 3L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre",
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
