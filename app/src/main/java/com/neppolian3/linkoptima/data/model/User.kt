package com.neppolian3.linkoptima.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val totalReviews: Int = 0,
    val lastReviewDate: Long? = null
)