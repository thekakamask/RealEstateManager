package com.dcac.realestatemanager.fakeData.fakeEntity

import com.dcac.realestatemanager.data.offlineDatabase.user.UserEntity
import com.dcac.realestatemanager.utils.hashPassword

object FakeUserEntity {

    val user1 = UserEntity(
        id = 1L,
        email = "agent1@example.com",
        password = hashPassword("passwordUser1"),
        agentName = "Alice Smith",
        isSynced = true,
        firebaseUid = "firebase_uid_1"
    )

    val user2 = UserEntity(
        id = 2L,
        email = "agent2@example.com",
        password = hashPassword("passwordUser2"),
        agentName = "Bob Johnson",
        isSynced = false,
        firebaseUid = "firebase_uid_2"
    )

    val userEntityList = listOf(user1, user2)
}