package com.neppolian3.linkoptima.repository

import com.neppolian3.linkoptima.data.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    fun isUserAuthenticated(): Flow<Boolean>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun checkAuthStatus(): Result<User?>
}

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    
    override fun getCurrentUser(): Flow<User?> {
        return flowOf(firebaseAuth.currentUser?.let {
            User(
                id = it.uid,
                name = it.displayName ?: "",
                email = it.email ?: "",
                photoUrl = it.photoUrl?.toString()
            )
        })
    }
    
    override fun isUserAuthenticated(): Flow<Boolean> {
        return flowOf(firebaseAuth.currentUser != null)
    }
    
    override suspend fun signInWithGoogle(idToken: String): Result<User> = try {
        // In production, use GoogleAuthProvider.getCredential(idToken, null)
        // For now, return a placeholder result
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val user = User(
                id = currentUser.uid,
                name = currentUser.displayName ?: "",
                email = currentUser.email ?: "",
                photoUrl = currentUser.photoUrl?.toString()
            )
            saveUserToFirestore(user)
            Result.success(user)
        } else {
            Result.failure(Exception("Authentication failed"))
        }
    } catch (e: Exception) {
        Timber.e(e, "Error signing in with Google")
        Result.failure(e)
    }
    
    override suspend fun signOut(): Result<Unit> = try {
        firebaseAuth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error signing out")
        Result.failure(e)
    }
    
    override suspend fun checkAuthStatus(): Result<User?> = try {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val user = User(
                id = currentUser.uid,
                name = currentUser.displayName ?: "",
                email = currentUser.email ?: "",
                photoUrl = currentUser.photoUrl?.toString()
            )
            Result.success(user)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Timber.e(e, "Error checking auth status")
        Result.failure(e)
    }
    
    private suspend fun saveUserToFirestore(user: User) = try {
        firestore.collection("users").document(user.id).set(user).await()
    } catch (e: Exception) {
        Timber.e(e, "Error saving user to Firestore")
    }
}