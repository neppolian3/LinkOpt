package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_optimizations")
data class ProfileOptimizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cvText: String,
    val currentProfileText: String,
    val targetRole: String,
    val targetIndustry: String,
    
    // Generated Results
    val headline: String,
    val aboutSection: String,
    val experienceRewrite: String,
    val skillsSuggestions: String,
    val recruiterKeywords: String,
    val linkedinPostIdeas: String,
    
    // Scores
    val profileStrengthScore: Int,
    val keywordScore: Int,
    val leadershipBrandingScore: Int,
    val recruiterVisibilityScore: Int,
    
    // Extracted Data
    val extractedExperience: String,
    val extractedSkills: String,
    val extractedEducation: String,
    val extractedAchievements: String
)

@Entity(tableName = "linkedin_audits")
data class LinkedInAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val headline: String,
    val about: String,
    val experience: String,
    val skills: String,
    
    // Audit Results
    val strengths: String,
    val weaknesses: String,
    val missingKeywords: String,
    val improvementSuggestions: String,
    val auditScore: Int
)
