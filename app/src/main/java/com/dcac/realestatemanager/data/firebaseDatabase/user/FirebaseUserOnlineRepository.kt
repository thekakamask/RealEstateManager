package com.dcac.realestatemanager.data.firebaseDatabase.user

import com.dcac.realestatemanager.data.firebaseDatabase.FirestoreCollections.USERS
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseUserOnlineRepository(
    private val firestore: FirebaseFirestore
) : UserOnlineRepository {

    override suspend fun uploadUser(user: UserOnlineEntity, firebaseUserId: String): UserOnlineEntity {
        try {
            // Check if email already exists in Firestore
            val emailExists = firestore.collection(USERS)
                .whereEqualTo("email", user.email)
                .get()
                .await()
                .documents
                .any { it.id != firebaseUserId } // ignore if it is an update

            if (emailExists) {
                throw FirebaseUserUploadException("Email already in use", null)
            }

            firestore.collection(USERS)
                .document(firebaseUserId)
                .set(user)
                .await()

            return user

        } catch (e: Exception) {
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }
    }

    override suspend fun getUser(firebaseUserId: String): FirestoreUserDocument? {
        return try {
            val snapshot = firestore.collection(USERS)
                .document(firebaseUserId)
                .get()
                .await()

            val user = snapshot.toObject(UserOnlineEntity::class.java)
            user?.let { FirestoreUserDocument(firebaseId = snapshot.id, user = it) }
        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to fetch user: ${e.message}", e)
        }
    }

    override suspend fun getAllUsers(): List<FirestoreUserDocument> {
        return try {
            firestore.collection(USERS)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    val user = doc.toObject(UserOnlineEntity::class.java)
                    user?.let { FirestoreUserDocument(firebaseId = doc.id, user = it) }
                }
        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to fetch all users: ${e.message}", e)
        }
    }

    /*override suspend fun deleteUser(firebaseUserId: String) {
        try {
            firestore.collection(FirestoreCollections.USERS)
                .document(firebaseUserId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw FirebaseUserDeleteException("Failed to delete user: ${e.message}", e)
        }
    }*/

    override suspend fun markUserAsDeleted(firebaseUserId: String, updatedAt: Long) {
        firestore.collection(USERS)
            .document(firebaseUserId)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to updatedAt
                )
            )
            .await()
    }
}

// Custom exception to signal upload failures to Firestore
class FirebaseUserUploadException(message: String, cause: Throwable?) : Exception(message, cause)
//class FirebaseUserDeleteException(message: String, cause: Throwable?) : Exception(message, cause)
class FirebaseUserDownloadException(message: String, cause: Throwable?) : Exception(message, cause)

data class FirestoreUserDocument(
    val firebaseId: String,                      // => Firebase UID (document ID)
    val user: UserOnlineEntity           // => User data
)

