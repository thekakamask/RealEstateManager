package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity

object FakePhotoEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photo1 = PhotoEntity(
        id = 1L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val photo2 = PhotoEntity(
        id = 2L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val photo3 = PhotoEntity(
        id = 3L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val photo4 = PhotoEntity(
        id = 4L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_4.jpg",
        description = "Terrace of Villa Montmartre",
        updatedAt = DEFAULT_TIMESTAMP + 4
    )

    val photo5 = PhotoEntity(
        id = 5L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_5.jpg",
        description = "Studio view - Latin Quarter",
        updatedAt = DEFAULT_TIMESTAMP + 5
    )

    val photo6 = PhotoEntity(
        id = 6L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_6.jpg",
        description = "Bathroom - Latin Quarter",
        updatedAt = DEFAULT_TIMESTAMP + 6
    )

    val photo7 = PhotoEntity(
        id = 7L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_7.jpg",
        description = "Living area - Flat Bastille",
        updatedAt = DEFAULT_TIMESTAMP + 7
    )

    val photo8 = PhotoEntity(
        id = 8L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_8.jpg",
        description = "Balcony - Flat Bastille",
        updatedAt = DEFAULT_TIMESTAMP + 8
    )

    val photo9 = PhotoEntity(
        id = 9L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_9.jpg",
        description = "Panoramic view - Penthouse Bastille",
        updatedAt = DEFAULT_TIMESTAMP + 9
    )

    val photo10 = PhotoEntity(
        id = 10L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_10.jpg",
        description = "Bedroom - Penthouse Bastille",
        updatedAt = DEFAULT_TIMESTAMP + 10
    )

    val photoEntityList = listOf(
        photo1, photo2, photo3, photo4, photo5,
        photo6, photo7, photo8, photo9, photo10
    )
}