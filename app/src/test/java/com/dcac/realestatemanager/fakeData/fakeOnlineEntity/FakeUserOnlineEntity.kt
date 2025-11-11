package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.user.FirestoreUserDocument
import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity

object FakeUserOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val userOnline1 = UserOnlineEntity(
        universalLocalId = "user-1",
        email = "agent1@example.com",
        agentName = "Alice Smith",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val userOnline2 = UserOnlineEntity(
        universalLocalId = "user-2",
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val userOnline3 = UserOnlineEntity(
        universalLocalId = "user-3",
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val firestoreUserDocument1 = FirestoreUserDocument(
        id = "firebase_uid_1",
        user = userOnline1
    )

    val firestoreUserDocument2 = FirestoreUserDocument(
        id = "firebase_uid_2",
        user = userOnline2
    )

    val firestoreUserDocument3 = FirestoreUserDocument(
        id = "firebase_uid_3",
        user = userOnline3
    )

    val userOnlineEntityList = listOf(userOnline1, userOnline2, userOnline3)

    val firestoreUserDocumentList = listOf(
        firestoreUserDocument1,
        firestoreUserDocument2,
        firestoreUserDocument3
    )
}