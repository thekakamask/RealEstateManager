package com.dcac.realestatemanager.fakeData.fakeOnlineEntity

import com.dcac.realestatemanager.data.firebaseDatabase.user.UserOnlineEntity

object FakeUserOnlineEntity {

    private const val DEFAULT_TIMESTAMP = 1700000000000L

    val userOnline1 = UserOnlineEntity(
        email = "agent1@example.com",
        agentName = "Alice Smith",
        firebaseUid = "firebase_uid_1",
        updatedAt = DEFAULT_TIMESTAMP + 1,
        roomId = 1L
    )

    val userOnline2 = UserOnlineEntity(
        email = "agent2@example.com",
        agentName = "Bob Johnson",
        firebaseUid = "firebase_uid_2",
        updatedAt = DEFAULT_TIMESTAMP + 2,
        roomId = 2L
    )

    val userOnline3 = UserOnlineEntity(
        email = "agent3@example.com",
        agentName = "Mounette Valco",
        firebaseUid = "firebase_uid_3",
        updatedAt = DEFAULT_TIMESTAMP + 3,
        roomId = 3L
    )

    val userOnlineEntityList = listOf(userOnline1, userOnline2, userOnline3)


}