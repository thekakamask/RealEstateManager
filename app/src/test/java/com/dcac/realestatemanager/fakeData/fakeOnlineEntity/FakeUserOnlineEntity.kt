package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.user.FirestoreUserDocument
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity

object FakeUserOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val userOnline1 = UserOnlineEntity(
        email = "agent1@example.com",
        agentName = "Alice Smith",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        roomId = 1L
    )

    val userOnline2 = UserOnlineEntity(
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        updatedAt = DEFAULT_TIMESTAMP + 2,
        roomId = 2L
    )

    val userOnline3 = UserOnlineEntity(
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        updatedAt = DEFAULT_TIMESTAMP + 3,
        roomId = 3L
    )

    val firestoreUserDocument1 = FirestoreUserDocument(
        "firebase_uid_1",
        userOnline1
    )
    val firestoreUserDocument2 = FirestoreUserDocument(
        "firebase_uid_2",
        userOnline2,
    )
    val firestoreUserDocument3 = FirestoreUserDocument(
        "firebase_uid_3",
        userOnline3,
    )


    val userOnlineEntityList = listOf(userOnline1, userOnline2, userOnline3)
    val firestoreUserDocumentList = listOf(firestoreUserDocument1, firestoreUserDocument2, firestoreUserDocument3)

}