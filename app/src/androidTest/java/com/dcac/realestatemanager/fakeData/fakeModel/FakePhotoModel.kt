package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.fakeData.fakeEntity.FakePropertyEntity
import com.dcac.realestatemanager.model.Photo

object FakePhotoModel {

    val photo1 = Photo(
        id = 1L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_1.jpg",
        description = "Living room of Loft République"
    )

    val photo2 = Photo(
        id = 2L,
        propertyId = FakePropertyEntity.property1.id,
        uri = "file://photo_2.jpg",
        description = "Kitchen of Loft République"
    )

    val photo3 = Photo(
        id = 3L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_3.jpg",
        description = "Garden view of Villa Montmartre"
    )

    val photo4 = Photo(
        id = 4L,
        propertyId = FakePropertyEntity.property2.id,
        uri = "file://photo_4.jpg",
        description = "Terrace of Villa Montmartre"
    )

    val photo5 = Photo(
        id = 5L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_5.jpg",
        description = "Studio view - Latin Quarter"
    )

    val photo6 = Photo(
        id = 6L,
        propertyId = FakePropertyEntity.property3.id,
        uri = "file://photo_6.jpg",
        description = "Bathroom - Latin Quarter"
    )

    val photo7 = Photo(
        id = 7L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_7.jpg",
        description = "Living area - Flat Bastille"
    )

    val photo8 = Photo(
        id = 8L,
        propertyId = FakePropertyEntity.property4.id,
        uri = "file://photo_8.jpg",
        description = "Balcony - Flat Bastille"
    )

    val photo9 = Photo(
        id = 9L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_9.jpg",
        description = "Panoramic view - Penthouse Bastille"
    )

    val photo10 = Photo(
        id = 10L,
        propertyId = FakePropertyEntity.property5.id,
        uri = "file://photo_10.jpg",
        description = "Bedroom - Penthouse Bastille"
    )

    val photoModelList = listOf(
        photo1, photo2, photo3, photo4, photo5,
        photo6, photo7, photo8, photo9, photo10
    )
}