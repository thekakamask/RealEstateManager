package com.dcac.realestatemanager.data.userConnection

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// THIS CLASS HANDLES AUTHENTICATION USING FIREBASE AUTH
// IT IMPLEMENTS AuthRepository TO ABSTRACT AWAY THE AUTH BACKEND
class OnlineAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance() // DEFAULT TO SINGLETON INSTANCE
) : AuthRepository {

    // PROPERTY TO ACCESS THE CURRENTLY LOGGED-IN USER (IF ANY)
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // EXPOSES A FLOW TO OBSERVE LOGIN/LOGOUT CHANGES IN REAL-TIME
    override fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        // CREATE A LISTENER THAT PUSHES THE CURRENT USER TO THE FLOW EVERY TIME AUTH STATE CHANGES
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser) // SEND THE CURRENT USER TO THE FLOW
        }

        // REGISTER THE LISTENER
        firebaseAuth.addAuthStateListener(listener)

        // ENSURE CLEANUP WHEN FLOW IS CLOSED (AVOID MEMORY LEAKS)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    // PERFORM LOGIN USING EMAIL/PASSWORD
    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Log.d("AUTH", "Login success for ${authResult.user?.email}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("AUTH", "Login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            Log.d("AUTH", "Attempting to sign up user with email: $email")
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Log.d("AUTH", "User sign-up success. UID: ${authResult.user?.uid}, Email: ${authResult.user?.email}")
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("AUTH", "User sign-up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}
