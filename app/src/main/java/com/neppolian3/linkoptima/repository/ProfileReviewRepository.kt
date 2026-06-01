package com.neppolian3.linkoptima.repository

import com.neppolian3.linkoptima.data.model.ProfileReview
import kotlinx.coroutines.flow.Flow

interface ProfileReviewRepository {
    suspend fun analyzeProfile(profileText: String, isUrl: Boolean = false): Result<ProfileReview>
    suspend fun saveReview(review: ProfileReview): Result<Unit>
    fun getUserReviews(userId: String): Flow<List<ProfileReview>>
    suspend fun getReviewById(reviewId: String): Result<ProfileReview>
    suspend fun deleteReview(reviewId: String): Result<Unit>
    suspend fun updateReview(review: ProfileReview): Result<Unit>
}

import com.google.firebase.firestore.FirebaseFirestore
import com.neppolian3.linkoptima.service.GeminiService
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ProfileReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val geminiService: GeminiService
) : ProfileReviewRepository {
    
    override suspend fun analyzeProfile(profileText: String, isUrl: Boolean): Result<ProfileReview> = try {
        val analysis = geminiService.analyzeProfile(profileText)
        Result.success(analysis)
    } catch (e: Exception) {
        Timber.e(e, "Error analyzing profile")
        Result.failure(e)
    }
    
    override suspend fun saveReview(review: ProfileReview): Result<Unit> = try {
        firestore.collection("reviews").document(review.id).set(review).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error saving review")
        Result.failure(e)
    }
    
    override fun getUserReviews(userId: String): Flow<List<ProfileReview>> = flow {
        try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            emit(snapshot.toObjects(ProfileReview::class.java))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching reviews")
            emit(emptyList())
        }
    }
    
    override suspend fun getReviewById(reviewId: String): Result<ProfileReview> = try {
        val snapshot = firestore.collection("reviews").document(reviewId).get().await()
        val review = snapshot.toObject(ProfileReview::class.java)
        if (review != null) Result.success(review) else Result.failure(Exception("Review not found"))
    } catch (e: Exception) {
        Timber.e(e, "Error fetching review")
        Result.failure(e)
    }
    
    override suspend fun deleteReview(reviewId: String): Result<Unit> = try {
        firestore.collection("reviews").document(reviewId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error deleting review")
        Result.failure(e)
    }
    
    override suspend fun updateReview(review: ProfileReview): Result<Unit> = try {
        firestore.collection("reviews").document(review.id).set(review).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating review")
        Result.failure(e)
    }
}