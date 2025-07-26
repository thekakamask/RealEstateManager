package com.dcac.realestatemanager.data.onlineDatabase.user

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// IMPLEMENTATION OF UserOnlineRepository THAT USES FIREBASE FIRESTORE
class FirebaseUserOnlineRepository(
    private val firestore: FirebaseFirestore // FIRESTORE INSTANCE INJECTED VIA CONSTRUCTOR
) : UserOnlineRepository {

    // UPLOAD A USER OBJECT TO FIRESTORE UNDER THE "users" COLLECTION
    override suspend fun uploadUser(user: UserOnlineEntity, userId: String) {
        try {
            firestore.collection("users")           // SELECT THE "users" COLLECTION
                .document(userId)                   // CREATE OR OVERWRITE DOCUMENT WITH userId AS ID
                .set(user)                          // UPLOAD THE USER OBJECT TO FIRESTORE
                .await()                            // AWAIT FOR COMPLETION (COROUTINE FRIENDLY)
        } catch (e: Exception) {
            // THROW A CUSTOM EXCEPTION IF UPLOAD FAILS (INCLUDES ORIGINAL ERROR)
            throw FirebaseUserUploadException("Failed to upload user: ${e.message}", e)
        }
    }

    // FETCH A USER OBJECT FROM FIRESTORE GIVEN ITS userId
    override suspend fun getUser(userId: String): UserOnlineEntity? {
        return firestore.collection("users")                         // ACCESS THE COLLECTION
            .document(userId)                                        // TARGET THE SPECIFIC DOCUMENT
            .get()                                                   // RETRIEVE IT
            .await()                                                 // WAIT FOR COMPLETION
            .toObject(UserOnlineEntity::class.java)                 // CONVERT TO Kotlin DATA CLASS
    }
}

// CUSTOM EXCEPTION USED TO SIGNAL A FIREBASE UPLOAD ERROR
class FirebaseUserUploadException(message: String, cause: Throwable?) : Exception(message, cause)
