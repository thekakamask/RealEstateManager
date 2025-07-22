package com.dcac.realestatemanager.data.userOnlineConnection

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    fun observeAuthState(): Flow<FirebaseUser?>

    suspend fun signInWithEmail(email : String, password: String) : Result<FirebaseUser?>
    suspend fun signUpWithEmail(email: String, password: String) : Result<FirebaseUser?>
    suspend fun signOut()

}