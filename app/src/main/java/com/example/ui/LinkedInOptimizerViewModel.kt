package com.example.ui

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.InlineData
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.api.StructuredAuditResponse
import com.example.api.StructuredOptimizationResponse
import com.example.data.LinkedInAuditEntity
import com.example.data.OptimizationRepository
import com.example.data.ProfileOptimizationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OptimizationUiState {
    object Idle : OptimizationUiState
    object Loading : OptimizationUiState
    data class Success(val response: StructuredOptimizationResponse) : OptimizationUiState
    data class Error(val message: String) : OptimizationUiState
}

sealed interface AuditUiState {
    object Idle : AuditUiState
    object Loading : AuditUiState
    data class Success(val response: StructuredAuditResponse) : AuditUiState
    data class Error(val message: String) : AuditUiState
}

class LinkedInOptimizerViewModel(private val repository: OptimizationRepository) : ViewModel() {

    // --- Inputs for Optimize Tab ---
    val cvText = MutableStateFlow("")
    val currentProfileText = MutableStateFlow("")
    val targetRole = MutableStateFlow("")
    val targetIndustry = MutableStateFlow("")
    val pdfBase64 = MutableStateFlow<String?>(null)
    val pdfFileName = MutableStateFlow<String?>(null)

    // --- Inputs for Audit Tab ---
    val auditHeadline = MutableStateFlow("")
    val auditAbout = MutableStateFlow("")
    val auditExperience = MutableStateFlow("")
    val auditSkills = MutableStateFlow("")

    // --- Async Call States ---
    private val _optimizationState = MutableStateFlow<OptimizationUiState>(OptimizationUiState.Idle)
    val optimizationState: StateFlow<OptimizationUiState> = _optimizationState.asStateFlow()

    private val _auditState = MutableStateFlow<AuditUiState>(AuditUiState.Idle)
    val auditState: StateFlow<AuditUiState> = _auditState.asStateFlow()

    fun updateOptimizationState(state: OptimizationUiState) {
        _optimizationState.value = state
    }

    fun updateAuditState(state: AuditUiState) {
        _auditState.value = state
    }

    // --- History Lists via Room ---
    val allOptimizations: StateFlow<List<ProfileOptimizationEntity>> = repository.allOptimizations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAudits: StateFlow<List<LinkedInAuditEntity>> = repository.allAudits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearPdf() {
        pdfBase64.value = null
        pdfFileName.value = null
    }

    // --- Core Action: Optimize Profile ---
    fun optimizeProfile() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _optimizationState.value = OptimizationUiState.Error("API Key is missing! Please configure GEMINI_API_KEY in the AI Studio Secrets panel.")
            return
        }

        val cv = cvText.value
        val profile = currentProfileText.value
        val role = targetRole.value
        val industry = targetIndustry.value
        val pdfData = pdfBase64.value

        if (cv.isBlank() && pdfData == null) {
            _optimizationState.value = OptimizationUiState.Error("Please enter CV text or upload a Resume PDF file.")
            return
        }
        if (role.isBlank()) {
            _optimizationState.value = OptimizationUiState.Error("Please clarify your Target Role.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _optimizationState.value = OptimizationUiState.Loading
            try {
                // Build prompt
                val promptText = """
                    You are an expert executive recruiter and ATS optimizer, specializing in the UAE job market, particularly healthcare operations, clinic administration, patient experience, and clinic leadership.
                    Your task is to optimize the user's career assets and profile to command presence for the Target Role: "$role" in the Target Industry: "$industry".
                    
                    Optimize output elements strictly for:
                    - Leadership positioning (e.g. Director, Manager, Lead, Senior Clinic Administrator)
                    - Clinic operational KPI excellence (billing lifecycle, slot utilization, clinical audits compliance)
                    - Patient experience journey metrics (patient NPS, turn-around-time limits, clinic patient flow improvements)
                    - Regional Gulf/UAE recruiter visibility (e.g., DHA licensure, Abu Dhabi DOH licensing, MOH compliance, Dubai Healthcare City)
                    - Applicant Tracking Systems (ATS) keyword relevance

                    Inputs provided:
                    ${if (pdfData != null) "- A PDF Resume was attached to this request and provided in base64." else ""}
                    - Paste CV Segment: "$cv"
                    - Current LinkedIn representation: "$profile"

                    Generate:
                    1. Headline: Strong, striking, 3-part layout (Roles | Areas of impact | Key UAE health metrics) ensuring maximum ATS indexability. Under 220 chars. Keep professional and neat copy-paste ready.
                    2. About: 1st person narrative focusing on healthcare management philosophy, UAE experience, achievements, and licensing.
                    3. Experience Rewrite: 3 highly detailed bullet points with quantified healthcare achievements (e.g. 'Optimized billing lifecycle by 22%', 'Boosted patient NPS satisfaction to 96%').
                    4. Skills suggestions: 8-10 high-value healthcare/clinic operational skills.
                    5. Recruiter search keywords: 8-12 major recruiter search keywords for UAE healthcare.
                    6. 3 creative LinkedIn post ideas to build professional authority in healthcare and clinic admin operations.
                    7. Extraction components (Experience, Skills, Education, Achievements) extracted directly from their input CV/Resume.
                    8. Calculated Scores out of 100 representing Profile Strength, Keyword Score, Leadership Branding, and Recruiter Visibility based on target alignment.

                    You MUST return your answer as a raw JSON string adhering to this schema:
                    {
                      "headline": "...",
                      "aboutSection": "...",
                      "experienceRewrite": "...",
                      "skillsSuggestions": ["...", "..."],
                      "recruiterKeywords": ["...", "..."],
                      "linkedinPostIdeas": ["...", "..."],
                      "profileStrengthScore": 85,
                      "keywordScore": 90,
                      "leadershipBrandingScore": 80,
                      "recruiterVisibilityScore": 95,
                      "extractedExperience": "...",
                      "extractedSkills": "...",
                      "extractedEducation": "...",
                      "extractedAchievements": "..."
                    }

                    Do NOT wrap the JSON in markdown formatting (like ```json), just return the raw JSON object representation. Ensure all outputs are 100% professional, copy-paste ready, realistic, and contain no brackets or templated generic answers. Use UAE English style.
                """.trimIndent()

                // Formulate request
                val parts = mutableListOf<Part>()
                if (pdfData != null) {
                    parts.add(Part(inlineData = InlineData(mimeType = "application/pdf", data = pdfData)))
                }
                parts.add(Part(text = promptText))

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = parts)),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from model.")

                val cleanedJson = cleanJsonString(rawText)
                val adapter = RetrofitClient.moshiInstance.adapter(StructuredOptimizationResponse::class.java)
                val parsedResponse = adapter.fromJson(cleanedJson)
                    ?: throw Exception("Failed to deserialize optimization details.")

                // Save optimization into Room database
                val dbEntity = ProfileOptimizationEntity(
                    cvText = cv,
                    currentProfileText = profile,
                    targetRole = role,
                    targetIndustry = industry,
                    headline = parsedResponse.headline,
                    aboutSection = parsedResponse.aboutSection,
                    experienceRewrite = parsedResponse.experienceRewrite,
                    skillsSuggestions = parsedResponse.skillsSuggestions.joinToString("\n"),
                    recruiterKeywords = parsedResponse.recruiterKeywords.joinToString(", "),
                    linkedinPostIdeas = parsedResponse.linkedinPostIdeas.joinToString("\n---\n"),
                    profileStrengthScore = parsedResponse.profileStrengthScore,
                    keywordScore = parsedResponse.keywordScore,
                    leadershipBrandingScore = parsedResponse.leadershipBrandingScore,
                    recruiterVisibilityScore = parsedResponse.recruiterVisibilityScore,
                    extractedExperience = parsedResponse.extractedExperience,
                    extractedSkills = parsedResponse.extractedSkills,
                    extractedEducation = parsedResponse.extractedEducation,
                    extractedAchievements = parsedResponse.extractedAchievements
                )
                repository.saveOptimization(dbEntity)

                _optimizationState.value = OptimizationUiState.Success(parsedResponse)
            } catch (e: Exception) {
                Log.e("ViewModel", "Optimization failure", e)
                _optimizationState.value = OptimizationUiState.Error(e.localizedMessage ?: "Network or parsing error has occurred.")
            }
        }
    }

    // --- Core Action: Audit Existing Profile ---
    fun auditProfile() {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            _auditState.value = AuditUiState.Error("API Key is missing! Please configure GEMINI_API_KEY in the AI Studio Secrets panel.")
            return
        }

        val headline = auditHeadline.value
        val about = auditAbout.value
        val exp = auditExperience.value
        val skills = auditSkills.value

        if (headline.isBlank() && about.isBlank() && exp.isBlank() && skills.isBlank()) {
            _auditState.value = AuditUiState.Error("Please enter at least one profile section to audit.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _auditState.value = AuditUiState.Loading
            try {
                // Build prompt
                val promptText = """
                    You are a premium healthcare executive recruiter auditing a LinkedIn profile for compliance with UAE high-end clinic and patient experience standards.
                    Auditing Inputs:
                    - Headline: "$headline"
                    - About Section: "$about"
                    - Experience Section: "$exp"
                    - Skills Section: "$skills"

                    Analyze these segments rigorously and return:
                    1. Strengths: 3-4 professional strengths you identified.
                    2. Weaknesses: 3-4 critical weaknesses holding this profile back.
                    3. Missing Keywords: 5-8 major keywords missing for high-ranking visibility (specifically in UAE healthcare, operations, DHA compliance, clinic admin, patient satisfaction).
                    4. Improvement Suggestions: 3-4 actionable items to fix.
                    5. Audit Score (1-100): Overriding quality score based on structure, impact verbs, licensing references, and metrics.

                    Return your answer strictly as a raw JSON string adhering to this schema:
                    {
                      "strengths": ["...", "..."],
                      "weaknesses": ["...", "..."],
                      "missingKeywords": ["...", "..."],
                      "improvementSuggestions": ["...", "..."],
                      "auditScore": 75
                    }

                    Do NOT wrap inside markdown. Just return the raw JSON object string. Keep answers practical, constructive, copy-paste ready, and customized for UAE healthcare recruiting.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = promptText)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.2f
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI model.")

                val cleanedJson = cleanJsonString(rawText)
                val adapter = RetrofitClient.moshiInstance.adapter(StructuredAuditResponse::class.java)
                val parsedResponse = adapter.fromJson(cleanedJson)
                    ?: throw Exception("Failed to deserialize audit results.")

                // Save Audit into Room database
                val dbEntity = LinkedInAuditEntity(
                    headline = headline,
                    about = about,
                    experience = exp,
                    skills = skills,
                    strengths = parsedResponse.strengths.joinToString("\n"),
                    weaknesses = parsedResponse.weaknesses.joinToString("\n"),
                    missingKeywords = parsedResponse.missingKeywords.joinToString(", "),
                    improvementSuggestions = parsedResponse.improvementSuggestions.joinToString("\n"),
                    auditScore = parsedResponse.auditScore
                )
                repository.saveAudit(dbEntity)

                _auditState.value = AuditUiState.Success(parsedResponse)
            } catch (e: Exception) {
                Log.e("ViewModel", "Audit failure", e)
                _auditState.value = AuditUiState.Error(e.localizedMessage ?: "Network or parsing error.")
            }
        }
    }

    // --- Actions: Delete History Elements ---
    fun deleteOptimization(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOptimization(id)
        }
    }

    fun deleteAudit(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAudit(id)
        }
    }

    // --- Helpers ---
    private fun cleanJsonString(input: String): String {
        var cleaned = input.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }
}

class LinkedInOptimizerViewModelFactory(private val repository: OptimizationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LinkedInOptimizerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LinkedInOptimizerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
