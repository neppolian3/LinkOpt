package com.neppolian3.linkoptima.data.model

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)