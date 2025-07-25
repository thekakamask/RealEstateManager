package com.dcac.realestatemanager.data.onlineDatabase.user

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserOnlineRepository(
    private val firestore: FirebaseFirestore
) : UserOnlineRepository {

    override suspend fun uploadUser(user: UserOnlineEntity, userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()
        } catch (e: Exception) {
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }
    }

    override suspend fun getUser(userId: String): UserOnlineEntity? {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(UserOnlineEntity::class.java)
    }
}

class FirebaseUserUploadException(message: String, cause: Throwable?) : Exception(message, cause)