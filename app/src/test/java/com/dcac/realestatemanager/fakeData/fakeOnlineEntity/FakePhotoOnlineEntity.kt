package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.photo.FirestorePhotoDocument
import com.dcac.realestatemanager.data.firebaseDatabase.photo.PhotoOnlineEntity
object FakePhotoOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val photoOnline1 = PhotoOnlineEntity(
        ownerUid = "firebase_uid_1",
        universalLocalId = "photo-1",
        universalLocalPropertyId = "property-1",
        description = "Living room of Loft République",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        storageUrl = "https://firebase.storage.com/photo_1.jpg",
        isDeleted = false
    )

    val photoOnline2 = PhotoOnlineEntity(
        ownerUid = "firebase_uid_2",
        universalLocalId = "photo-2",
        universalLocalPropertyId = "property-2",
        description = "Kitchen of Loft République",
        updatedAt = DEFAULT_TIMESTAMP + 2,
        storageUrl = "https://firebase.storage.com/photo_2.jpg",
        isDeleted = false
    )

    val photoOnline3 = PhotoOnlineEntity(
        ownerUid = "firebase_uid_3",
        universalLocalId = "photo-3",
        universalLocalPropertyId = "property-3",
        description = "Garden view of Villa Montmartre",
        updatedAt = DEFAULT_TIMESTAMP + 3,
        storageUrl = "https://firebase.storage.com/photo_3.jpg",
        isDeleted = true
    )

    val firestorePhotoDocument1 = FirestorePhotoDocument(
        firebaseId = "firestore-photo-1",
        photo = photoOnline1
    )

    val firestorePhotoDocument2 = FirestorePhotoDocument(
        firebaseId = "firestore-photo-2",
        photo = photoOnline2
    )

    val firestorePhotoDocument3 = FirestorePhotoDocument(
        firebaseId = "firestore-photo-3",
        photo = photoOnline3
    )

    val photoOnlineEntityList = listOf(
        photoOnline1, photoOnline2, photoOnline3
    )

    val firestorePhotoDocumentList = listOf(
        firestorePhotoDocument1,
        firestorePhotoDocument2,
        firestorePhotoDocument3
    )
}
