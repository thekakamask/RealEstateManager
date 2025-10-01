package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

object FakeUserEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val user1 = UserEntity(
        id = 1L,
        email = "agent1@example.com",
        agentName = "Alice Smith",
        firebaseUid = "firebase_uid_1",
        updatedAt = DEFAULT_TIMESTAMP + 1
    )

    val user2 = UserEntity(
        id = 2L,
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        isSynced = true,
        firebaseUid = "firebase_uid_2",
        updatedAt = DEFAULT_TIMESTAMP + 2
    )

    val user3 = UserEntity(
        id = 3L,
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        isDeleted = true,
        firebaseUid = "firebase_uid_3",
        updatedAt = DEFAULT_TIMESTAMP + 3
    )

    val userEntityList = listOf(user1, user2, user3)
    val userEntityListNotDeleted = listOf(user1, user2)
}
