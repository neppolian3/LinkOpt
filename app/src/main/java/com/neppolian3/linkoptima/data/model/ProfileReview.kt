package com.neppolian3.linkoptima.data.model

import java.util.Date

data class ProfileReview(
    val id: String = "",
    val userId: String = "",
    val profileName: String = "",
    val profileUrl: String? = null,
    val rawProfileData: String = "",
    val profileScore: Int = 0,
    val headline: HeadlineAnalysis = HeadlineAnalysis(),
    val aboutSection: AboutAnalysis = AboutAnalysis(),
    val experience: ExperienceAnalysis = ExperienceAnalysis(),
    val skillsKeywords: SkillsAnalysis = SkillsAnalysis(),
    val achievements: AchievementsAnalysis = AchievementsAnalysis(),
    val positioning: PositioningAnalysis = PositioningAnalysis(),
    val optimizedContent: OptimizedContent = OptimizedContent(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class HeadlineAnalysis(
    val current: String = "",
    val score: Int = 0,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val optimized: String = ""
)

data class AboutAnalysis(
    val current: String = "",
    val score: Int = 0,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val optimized: String = ""
)

data class ExperienceAnalysis(
    val current: List<String> = emptyList(),
    val score: Int = 0,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val optimized: List<String> = emptyList()
)

data class SkillsAnalysis(
    val current: List<String> = emptyList(),
    val score: Int = 0,
    val issues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList()
)

data class AchievementsAnalysis(
    val current: List<String> = emptyList(),
    val score: Int = 0,
    val issues: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val quantifiedExamples: List<String> = emptyList()
)

data class PositioningAnalysis(
    val score: Int = 0,
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val brandStatement: String = ""
)

data class OptimizedContent(
    val headline: String = "",
    val about: String = "",
    val experience: List<String> = emptyList(),
    val recommendedSkills: List<String> = emptyList(),
    val featuredSuggestions: List<String> = emptyList()
)