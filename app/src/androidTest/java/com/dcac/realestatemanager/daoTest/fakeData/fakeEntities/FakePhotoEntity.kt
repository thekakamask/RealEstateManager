package com.dcac.realestatemanager.daoTest.fakeData.fakeEntities

import com.dcac.realestatemanager.data.offlineDatabase.photo.PhotoEntity

object FakePhotoEntity {

    val photo1 = PhotoEntity(
        id = 1L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République",
        isSynced = true
    )

    val photo2 = PhotoEntity(
        id = 2L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République"
    )

    val photo3 = PhotoEntity(
        id = 3L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre"
    )

    val photo4 = PhotoEntity(
        id = 4L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_4.jpg",
        description = "Terrace of Villa Montmartre"
    )

    val photo5 = PhotoEntity(
        id = 5L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_5.jpg",
        description = "Studio view - Latin Quarter"
    )

    val photo6 = PhotoEntity(
        id = 6L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_6.jpg",
        description = "Bathroom - Latin Quarter"
    )

    val photo7 = PhotoEntity(
        id = 7L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_7.jpg",
        description = "Living area - Flat Bastille"
    )

    val photo8 = PhotoEntity(
        id = 8L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_8.jpg",
        description = "Balcony - Flat Bastille"
    )

    val photo9 = PhotoEntity(
        id = 9L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_9.jpg",
        description = "Panoramic view - Penthouse Bastille"
    )

    val photo10 = PhotoEntity(
        id = 10L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_10.jpg",
        description = "Bedroom - Penthouse Bastille"
    )

    val photoEntityList = listOf(
        photo1, photo2, photo3, photo4, photo5,
        photo6, photo7, photo8, photo9, photo10
    )
}