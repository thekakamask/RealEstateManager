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
            // Check if email already exists in Firestore
            val emailExists = firestore.collection(FirestoreCollections.USERS)
                .whereEqualTo("email", user.email)
                .get()
                .await()
                .documents
                .any { it.id != userId } // ignore if it is an update

            if (emailExists) {
                throw FirebaseUserUploadException("Email already in use", null)
            }

            // Check if roomId already exist
            val roomIdExists = firestore.collection(FirestoreCollections.USERS)
                .whereEqualTo("roomId", user.roomId)
                .get()
                .await()
                .documents
                .any { it.id != userId }

            if (roomIdExists) {
                throw FirebaseUserUploadException("Room ID already in use", null)
            }

            firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .set(user)
                .await()

            return user

        } catch (e: Exception) {
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }
    }


    override suspend fun getUser(userId: String): FirestoreUserDocument? {
        return try {
            val snapshot = firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .get()
                .await()

            val user = snapshot.toObject(UserOnlineEntity::class.java)
            user?.let { FirestoreUserDocument(id = snapshot.id, user = it) }
        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to fetch user: ${e.message}", e)
        }
    }

    override suspend fun getAllUsers(): List<FirestoreUserDocument> {
        return try {
            firestore.collection(FirestoreCollections.USERS)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val user = doc.toObject(UserOnlineEntity::class.java)
                    user?.let { FirestoreUserDocument(id = doc.id, user = it) }
                }
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

data class FirestoreUserDocument(
    val id: String,                      // => Firebase UID (document ID)
    val user: UserOnlineEntity           // => User data
)

