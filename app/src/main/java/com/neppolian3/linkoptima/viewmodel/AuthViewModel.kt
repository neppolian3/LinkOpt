package com.neppolian3.linkoptima.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neppolian3.linkoptima.data.model.AuthState
import com.neppolian3.linkoptima.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                authRepository.checkAuthStatus().fold(
                    onSuccess = { user ->
                        _authState.value = AuthState(
                            isAuthenticated = user != null,
                            user = user
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "Auth check failed")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception checking auth")
            }
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            try {
                authRepository.signInWithGoogle(idToken).fold(
                    onSuccess = { user ->
                        _authState.value = AuthState(
                            isAuthenticated = true,
                            isLoading = false,
                            user = user
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "Google sign-in failed")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception during sign-in")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut().fold(
                    onSuccess = {
                        _authState.value = AuthState(
                            isAuthenticated = false,
                            user = null
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "Sign-out failed")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Exception during sign-out")
            }
        }
    }
}