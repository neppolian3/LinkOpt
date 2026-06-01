package com.neppolian3.linkoptima.utils

object Constants {
    const val FIREBASE_REVIEWS_COLLECTION = "reviews"
    const val FIREBASE_USERS_COLLECTION = "users"
    
    const val PDF_READ_TIMEOUT = 30L
    const val API_TIMEOUT = 60L
    
    const val PROFILE_SCORE_WEIGHT_HEADLINE = 0.2f
    const val PROFILE_SCORE_WEIGHT_ABOUT = 0.2f
    const val PROFILE_SCORE_WEIGHT_EXPERIENCE = 0.2f
    const val PROFILE_SCORE_WEIGHT_SKILLS = 0.2f
    const val PROFILE_SCORE_WEIGHT_ACHIEVEMENTS = 0.2f
    
    const val MIN_HEADLINE_LENGTH = 10
    const val MAX_HEADLINE_LENGTH = 220
    
    const val MIN_ABOUT_LENGTH = 50
    const val MAX_ABOUT_LENGTH = 2600
    
    const val MIN_SKILLS_COUNT = 3
    const val RECOMMENDED_SKILLS_COUNT = 5
}