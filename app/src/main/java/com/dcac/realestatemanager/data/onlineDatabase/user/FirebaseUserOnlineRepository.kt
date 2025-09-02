package com.dcac.realestatemanager.data.onlineDatabase.user

import com.dcac.realestatemanager.data.onlineDatabase.FirestoreCollections
import com.dcac.realestatemanager.model.User
import com.dcac.realestatemanager.utils.toModel
import com.dcac.realestatemanager.utils.toOnlineEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Repository implementation that manages Users stored in Firestore
class FirebaseUserOnlineRepository(
    private val firestore: FirebaseFirestore           // Injected Firestore instance
) : UserOnlineRepository {

    // Uploads a user to Firestore under collection "users" with the given userId
    override suspend fun uploadUser(user: User, userId: String): User {
        val onlineEntity = user.toOnlineEntity()       // Convert domain User -> Firestore DTO
        try {
            firestore.collection(FirestoreCollections.USERS)              // Access "users" collection in Firestore
                .document(userId)                      // Select document with given userId (firebaseUid)
                .set(onlineEntity)                     // Write (create/overwrite) the Firestore DTO
                .await()                               // Suspend until operation completes
        } catch (e: Exception) {
            // If Firestore fails, wrap error in a custom exception
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }

        // Return updated domain User: mark it as synced and set firebaseUid
        return user.copy(isSynced = true, firebaseUid = userId)
    }

    // Fetches a user from Firestore by its userId (firebaseUid)
    override suspend fun getUser(userId: String): User? {
        val entity = firestore.collection(FirestoreCollections.USERS)     // Access "users" collection
            .document(userId)                          // Select document with given userId
            .get()                                     // Fetch document snapshot
            .await()                                   // Suspend until operation completes
            .toObject(UserOnlineEntity::class.java)    // Deserialize into Firestore DTO

        return entity?.toModel(firebaseUid = userId)    // Map DTO -> domain User (null if not found)
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshots = firestore.collection(FirestoreCollections.USERS)
                .get()
                .await()

            snapshots.documents.mapNotNull { doc ->
                val entity = doc.toObject(UserOnlineEntity::class.java)
                val firebaseUid = doc.id

                entity?.toModel(firebaseUid = firebaseUid)
            }

        } catch (e: Exception) {
            throw FirebaseUserDownloadException("Failed to get users: ${e.message}", e)
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


