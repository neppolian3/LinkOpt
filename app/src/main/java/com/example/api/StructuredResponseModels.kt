package com.example.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StructuredOptimizationResponse(
    val headline: String,
    val aboutSection: String,
    val experienceRewrite: String,
    val skillsSuggestions: List<String>,
    val recruiterKeywords: List<String>,
    val linkedinPostIdeas: List<String>,
    
    // Scores out of 100
    val profileStrengthScore: Int,
    val keywordScore: Int,
    val leadershipBrandingScore: Int,
    val recruiterVisibilityScore: Int,
    
    // Extracted Resume Components
    val extractedExperience: String,
    val extractedSkills: String,
    val extractedEducation: String,
    val extractedAchievements: String
)

@JsonClass(generateAdapter = true)
data class StructuredAuditResponse(
    val strengths: List<String>,
    val weaknesses: List<String>,
    val missingKeywords: List<String>,
    val improvementSuggestions: List<String>,
    
    // Score from 1-100
    val auditScore: Int
)
