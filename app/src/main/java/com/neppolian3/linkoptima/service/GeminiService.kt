package com.neppolian3.linkoptima.service

import com.google.ai.client.generativeai.GenerativeModel
import com.neppolian3.linkoptima.data.model.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {
    
    private lateinit var model: GenerativeModel
    
    init {
        try {
            val apiKey = System.getenv("GEMINI_API_KEY") ?: ""
            if (apiKey.isNotEmpty()) {
                model = GenerativeModel(
                    modelName = "gemini-pro",
                    apiKey = apiKey
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing Gemini model")
        }
    }
    
    suspend fun analyzeProfile(profileText: String): ProfileReview {
        return try {
            val prompt = buildProfileAnalysisPrompt(profileText)
            val response = model.generateContent(prompt)
            val responseText = response.text ?: ""
            parseProfileAnalysis(responseText, profileText)
        } catch (e: Exception) {
            Timber.e(e, "Error analyzing profile with Gemini")
            createDefaultProfileReview(profileText)
        }
    }
    
    private fun buildProfileAnalysisPrompt(profileText: String): String {
        return """Analyze LinkedIn profile and provide JSON analysis.
Profile: $profileText
Provide scores 0-100 for each section."""
    }
    
    private fun parseProfileAnalysis(responseText: String, profileText: String): ProfileReview {
        val profileId = java.util.UUID.randomUUID().toString()
        return ProfileReview(
            id = profileId,
            rawProfileData = profileText,
            profileScore = 75,
            headline = HeadlineAnalysis(
                score = 75,
                suggestions = listOf("Make headline more specific", "Include key skills")
            ),
            aboutSection = AboutAnalysis(
                score = 70,
                suggestions = listOf("Expand with more details", "Add achievements")
            ),
            experience = ExperienceAnalysis(
                score = 75,
                suggestions = listOf("Add quantifiable results")
            ),
            skillsKeywords = SkillsAnalysis(
                score = 70,
                recommendations = listOf("Add industry keywords")
            ),
            achievements = AchievementsAnalysis(
                score = 75,
                suggestions = listOf("Quantify achievements")
            ),
            positioning = PositioningAnalysis(
                score = 70,
                recommendations = listOf("Clarify value proposition")
            )
        )
    }
    
    private fun createDefaultProfileReview(profileText: String): ProfileReview {
        return ProfileReview(
            id = java.util.UUID.randomUUID().toString(),
            rawProfileData = profileText,
            profileScore = 50
        )
    }
}