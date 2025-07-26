package com.dcac.realestatemanager.data.onlineDatabase.user

data class UserOnlineEntity(
    val email: String = "",
    val agentName: String = "",
    val uid: String = "" // MATCH FIREBASEUID OF USERENTITY FROM ROOM
)
