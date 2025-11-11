package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

object FakeUserEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val user1 = UserEntity(
        id = "user-1",
        email = "agent1@example.com",
        agentName = "Alice Smith",
        firebaseUid = "firebase_uid_1",
        isSynced = false,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val user2 = UserEntity(
        id = "user-2",
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        firebaseUid = "firebase_uid_2",
        isSynced = true,
        isDeleted = false,
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val user3 = UserEntity(
        id = "user-3",
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        firebaseUid = "firebase_uid_3",
        isSynced = false,
        isDeleted = true,
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val userEntityList = listOf(user1, user2, user3)
    val userEntityListNotDeleted = listOf(user1, user2)
}
