package com.neppolian3.linkoptima.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neppolian3.linkoptima.data.model.ProfileReview
import com.neppolian3.linkoptima.repository.ProfileReviewRepository
import com.neppolian3.linkoptima.utils.PdfParser
import com.neppolian3.linkoptima.utils.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ProfileAnalysisState(
    val isLoading: Boolean = false,
    val profile: ProfileReview? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileAnalysisViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileRepository: ProfileReviewRepository
) : ViewModel() {
    
    private val _analysisState = MutableStateFlow(ProfileAnalysisState())
    val analysisState: StateFlow<ProfileAnalysisState> = _analysisState.asStateFlow()
    
    fun analyzeFromUrl(url: String) {
        if (!ValidationUtils.isValidLinkedInUrl(url)) {
            _analysisState.value = _analysisState.value.copy(
                error = "Invalid LinkedIn URL format"
            )
            return
        }
        
        _analysisState.value = _analysisState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                profileRepository.analyzeProfile(url, isUrl = true).fold(
                    onSuccess = { profile ->
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            profile = profile
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "Failed to analyze URL")
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to analyze profile"
                        )
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception analyzing URL")
            }
        }
    }
    
    fun analyzeFromPdf(uri: Uri) {
        _analysisState.value = _analysisState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                val result = PdfParser.extractTextFromPdf(context, uri)
                result.fold(
                    onSuccess = { pdfText ->
                        profileRepository.analyzeProfile(pdfText, isUrl = false).fold(
                            onSuccess = { profile ->
                                _analysisState.value = _analysisState.value.copy(
                                    isLoading = false,
                                    profile = profile
                                )
                            },
                            onFailure = { error ->
                                Timber.e(error, "Failed to analyze PDF")
                                _analysisState.value = _analysisState.value.copy(
                                    isLoading = false,
                                    error = error.message
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _analysisState.value = _analysisState.value.copy(
                            isLoading = false,
                            error = "Failed to read PDF"
                        )
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception analyzing PDF")
            }
        }
    }
    
    fun saveProfile(userId: String) {
        val profile = _analysisState.value.profile ?: return
        viewModelScope.launch {
            try {
                profileRepository.saveReview(profile.copy(userId = userId)).fold(
                    onSuccess = { Timber.d("Profile saved") },
                    onFailure = { Timber.e(it, "Save failed") }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception saving profile")
            }
        }
    }
}