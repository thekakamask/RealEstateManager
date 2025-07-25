package com.dcac.realestatemanager.utils

import com.dcac.realestatemanager.data.onlineDatabase.user.UserOnlineEntity
import com.dcac.realestatemanager.model.User

fun User.toOnlineEntity(): UserOnlineEntity {
    return UserOnlineEntity(
        email = this.email,
        agentName = this.agentName,
        uid = this.firebaseUid
    )
}

fun UserOnlineEntity.toUser(userId: Long = 0L): User {
    return User(
        id = userId,
        email = this.email,
        password = "",
        agentName = this.agentName,
        isSynced = true,
        firebaseUid = this.uid
    )
}