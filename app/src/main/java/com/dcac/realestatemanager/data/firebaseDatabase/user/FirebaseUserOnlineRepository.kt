package com.dcac.realestatemanager.data.firebaseDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Repository implementation that manages Users stored in Firestore
class FirebaseUserOnlineRepository(
    private val firestore: FirebaseFirestore
) : UserOnlineRepository {

    override suspend fun uploadUser(user: UserOnlineEntity, userId: String): UserOnlineEntity {
        try {
            firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .set(user)
                .await()
            return user
        } catch (e: Exception) {
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }
    }

    override suspend fun getUser(userId: String): UserOnlineEntity? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .get()
                .await()

            snapshot.toObject(UserOnlineEntity::class.java)
        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to fetch user: ${e.message}", e)
        }
    }

    override suspend fun getAllUsers(): List<UserOnlineEntity> {
        return try {
            firestore.collection(FirestoreCollections.USERS)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(UserOnlineEntity::class.java) }
        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to fetch all users: ${e.message}", e)
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebaseUserDeleteException("Failed to delete user: ${e.message}", e)
        }
    }
}

// Custom exception to signal upload failures to Firestore
class FirebaseUserUploadException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebaseUserDeleteException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebaseUserDownloadException(message: String, cause: Throwable?) : Exception(message, cause)


