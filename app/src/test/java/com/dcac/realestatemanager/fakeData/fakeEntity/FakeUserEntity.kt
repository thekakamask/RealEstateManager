package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity

object FakeUserEntity {

    val user1 = UserEntity(
        id = 1L,
        email = "agent1@example.com",
        agentName = "Alice Smith",
        isSynced = true,
        firebaseUid = "firebase_uid_1"
    )

    val user2 = UserEntity(
        id = 2L,
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        isSynced = false,
        firebaseUid = "firebase_uid_2"
    )

    val userEntityList = listOf(user1, user2)
}