package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity

object FakePhotoOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photoEntity1 = PhotoOnlineEntity(
        description = "Living room of Loft République",
        propertyId = 1L,
        updatedAt = DEFAULT_TIMESTAMP + 1,
        storageUrl = "https://firebase.storage.com/photo_1.jpg",
        roomId = 1L
    )

    val photoEntity2 = PhotoOnlineEntity(
        description = "Kitchen of Loft République",
        propertyId = 2L,
        updatedAt = DEFAULT_TIMESTAMP + 2,
        storageUrl = "https://firebase.storage.com/photo_2.jpg",
        roomId = 2L
    )

    val photoEntity3 = PhotoOnlineEntity(
        description = "Garden view of Villa Montmartre",
        propertyId = 3L,
        updatedAt = DEFAULT_TIMESTAMP + 3,
        storageUrl = "https://firebase.storage.com/photo_3.jpg",
        roomId = 3L
    )

    val photoOnlineEntityList = listOf(
        photoEntity1, photoEntity2, photoEntity3
    )

}
