package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.User

object FakeUserModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val user1 = User(
        universalLocalId = "user-1",
        firebaseUid = "firebase_uid_1",
        email = "agent1@example.com",
        agentName = "Alice Smith",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val user2 = User(
        universalLocalId = "user-2",
        firebaseUid = "firebase_uid_2",
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        isSynced = true,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val user3 = User(
        universalLocalId = "user-3",
        firebaseUid = "firebase_uid_3",
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val userModelList = listOf(user1, user2, user3)
    val userModelListNotDeleted = listOf(user1,user2)

}
