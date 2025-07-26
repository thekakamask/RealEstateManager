package com.dcac.realestatemanager.data.userConnection

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
            // ATTEMPT TO SIGN IN AND AWAIT COMPLETION (SUSPENDING)
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user) // RETURN THE AUTHENTICATED USER
        } catch (e: Exception) {
            Result.failure(e) // RETURN FAILURE WITH EXCEPTION
        }
    }

    // PERFORM ACCOUNT CREATION USING EMAIL/PASSWORD
    override suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser?> {
        return try {
            // CREATE USER ACCOUNT AND AWAIT COMPLETION
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(authResult.user) // RETURN THE NEWLY CREATED USER
        } catch (e: Exception) {
            Result.failure(e) // RETURN FAILURE WITH EXCEPTION
        }
    }

    // LOG OUT THE CURRENT USER
    override suspend fun signOut() {
        firebaseAuth.signOut() // INVALIDATE SESSION AND CLEAR LOCAL CREDENTIALS
    }
}
