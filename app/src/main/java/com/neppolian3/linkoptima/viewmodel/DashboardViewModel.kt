package com.neppolian3.linkoptima.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neppolian3.linkoptima.data.model.ProfileReview
import com.neppolian3.linkoptima.repository.ProfileReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DashboardState(
    val isLoading: Boolean = false,
    val reviews: List<ProfileReview> = emptyList(),
    val error: String? = null,
    val totalReviews: Int = 0,
    val averageScore: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val profileRepository: ProfileReviewRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    fun loadUserReviews(userId: String) {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                profileRepository.getUserReviews(userId).collectLatest { reviews ->
                    val avgScore = if (reviews.isNotEmpty()) {
                        reviews.map { it.profileScore }.average().toInt()
                    } else {
                        0
                    }
                    
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        reviews = reviews,
                        totalReviews = reviews.size,
                        averageScore = avgScore
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading reviews")
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                profileRepository.deleteReview(reviewId).fold(
                    onSuccess = { Timber.d("Review deleted") },
                    onFailure = { Timber.e(it, "Delete failed") }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception deleting review")
            }
        }
    }
}