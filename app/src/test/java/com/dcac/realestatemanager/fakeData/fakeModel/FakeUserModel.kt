package com.dcac.realestatemanager.fakeData.fakeModel

import com.dcac.realestatemanager.model.User

object FakeUserModel {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val user1 = User(
        id = 1L,
        email = "agent1@example.com",
        agentName = "Alice Smith",
        isSynced = true,
        firebaseUid = "firebase_uid_1",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val user2 = User(
        id = 2L,
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        isSynced = false,
        firebaseUid = "firebase_uid_2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val userModelList = listOf(user1, user2)
}
