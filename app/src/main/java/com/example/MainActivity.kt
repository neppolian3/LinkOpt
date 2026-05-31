package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.api.StructuredAuditResponse
import com.example.api.StructuredOptimizationResponse
import com.example.data.AppDatabase
import com.example.data.LinkedInAuditEntity
import com.example.data.OptimizationRepository
import com.example.data.ProfileOptimizationEntity
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = OptimizationRepository(database.optimizationDao())
        val viewModelFactory = LinkedInOptimizerViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    LinkedInOptimizerApp(
                        viewModelFactory = viewModelFactory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LinkedInOptimizerApp(
    viewModelFactory: LinkedInOptimizerViewModelFactory,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: LinkedInOptimizerViewModel = viewModel(factory = viewModelFactory)

    // Current screen state: 0 = Optimize, 1 = Audit, 2 = History
    var selectedTab by remember { mutableStateOf(0) }

    // Observers
    val cvText by viewModel.cvText.collectAsState()
    val currentProfileText by viewModel.currentProfileText.collectAsState()
    val targetRole by viewModel.targetRole.collectAsState()
    val targetIndustry by viewModel.targetIndustry.collectAsState()
    val pdfFileName by viewModel.pdfFileName.collectAsState()

    val auditHeadline by viewModel.auditHeadline.collectAsState()
    val auditAbout by viewModel.auditAbout.collectAsState()
    val auditExperience by viewModel.auditExperience.collectAsState()
    val auditSkills by viewModel.auditSkills.collectAsState()

    val optimizationState by viewModel.optimizationState.collectAsState()
    val auditState by viewModel.auditState.collectAsState()

    val wordHistoryList by viewModel.allOptimizations.collectAsState()
    val auditHistoryList by viewModel.allAudits.collectAsState()

    // Activity launcher for PDF selection
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = UiHelpers.readPdfBytesAndFileName(context, uri)
            if (result != null) {
                viewModel.pdfBase64.value = result.first
                viewModel.pdfFileName.value = result.second
                Toast.makeText(context, "Loaded PDF Resume: ${result.second} 📄", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Unsupported or unreadable PDF.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Block ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .border(width = 1.dp, color = Color(0xFFF1F5F9)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🤖",
                    fontSize = 24.sp
                )
                Column {
                    Text(
                        text = "Profile Optimizer",
                        color = DeepTealBlue,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Healthcare & UAE ATS Tuner",
                        color = MediumText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color(0xFFF8FAFC), CircleShape)
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔔", fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MutedBlue)
                        .border(width = 1.dp, color = DeepTealBlue, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AM",
                        color = DeepTealBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Clean Modern Tab Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Optimize ✨", "Audit 🔍", "History 📜").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (isSelected) MutedBlue else Color(0xFFF1F5F9))
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) DeepTealBlue else Color(0xFF64748B),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- Screens Layout ---
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> {
                    // OPTIMIZATION TAB
                    OptimizeScreen(
                        cvText = cvText,
                        onCvChange = { viewModel.cvText.value = it },
                        currentProfile = currentProfileText,
                        onProfileChange = { viewModel.currentProfileText.value = it },
                        role = targetRole,
                        onRoleChange = { viewModel.targetRole.value = it },
                        industry = targetIndustry,
                        onIndustryChange = { viewModel.targetIndustry.value = it },
                        pdfName = pdfFileName,
                        onClearPdf = { viewModel.clearPdf() },
                        onPickPdf = { pdfPickerLauncher.launch("application/pdf") },
                        uiState = optimizationState,
                        onOptimize = { viewModel.optimizeProfile() }
                    )
                }
                1 -> {
                    // AUDIT TAB
                    AuditScreen(
                        headline = auditHeadline,
                        onHeadlineChange = { viewModel.auditHeadline.value = it },
                        about = auditAbout,
                        onAboutChange = { viewModel.auditAbout.value = it },
                        experience = auditExperience,
                        onExperienceChange = { viewModel.auditExperience.value = it },
                        skills = auditSkills,
                        onSkillsChange = { viewModel.auditSkills.value = it },
                        uiState = auditState,
                        onAudit = { viewModel.auditProfile() }
                    )
                }
                2 -> {
                    // HISTORY TAB
                    HistoryScreen(
                        optimizationHistory = wordHistoryList,
                        auditHistory = auditHistoryList,
                        onSelectOptimization = { item ->
                            viewModel.cvText.value = item.cvText
                            viewModel.currentProfileText.value = item.currentProfileText
                            viewModel.targetRole.value = item.targetRole
                            viewModel.targetIndustry.value = item.targetIndustry
                            viewModel.clearPdf()
                            
                            val parsed = StructuredOptimizationResponse(
                                headline = item.headline,
                                aboutSection = item.aboutSection,
                                experienceRewrite = item.experienceRewrite,
                                skillsSuggestions = item.skillsSuggestions.split("\n"),
                                recruiterKeywords = item.recruiterKeywords.split(", "),
                                linkedinPostIdeas = item.linkedinPostIdeas.split("\n---\n"),
                                profileStrengthScore = item.profileStrengthScore,
                                keywordScore = item.keywordScore,
                                leadershipBrandingScore = item.leadershipBrandingScore,
                                recruiterVisibilityScore = item.recruiterVisibilityScore,
                                extractedExperience = item.extractedExperience,
                                extractedSkills = item.extractedSkills,
                                extractedEducation = item.extractedEducation,
                                extractedAchievements = item.extractedAchievements
                            )
                            viewModel.updateOptimizationState(OptimizationUiState.Success(parsed))
                            selectedTab = 0
                        },
                        onSelectAudit = { item ->
                            viewModel.auditHeadline.value = item.headline
                            viewModel.auditAbout.value = item.about
                            viewModel.auditExperience.value = item.experience
                            viewModel.auditSkills.value = item.skills
                            
                            val parsed = StructuredAuditResponse(
                                strengths = item.strengths.split("\n"),
                                weaknesses = item.weaknesses.split("\n"),
                                missingKeywords = item.missingKeywords.split(", "),
                                improvementSuggestions = item.improvementSuggestions.split("\n"),
                                auditScore = item.auditScore
                            )
                            viewModel.updateAuditState(AuditUiState.Success(parsed))
                            selectedTab = 1
                        },
                        onDeleteOptimization = { viewModel.deleteOptimization(it) },
                        onDeleteAudit = { viewModel.deleteAudit(it) }
                    )
                }
            }
        }
    }
}

// ==================== SCREEN COMPONENTS ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptimizeScreen(
    cvText: String,
    onCvChange: (String) -> Unit,
    currentProfile: String,
    onProfileChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    industry: String,
    onIndustryChange: (String) -> Unit,
    pdfName: String?,
    onClearPdf: () -> Unit,
    onPickPdf: () -> Unit,
    uiState: OptimizationUiState,
    onOptimize: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftGray),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Optimization Objective 🎯",
                        fontWeight = FontWeight.Bold,
                        color = DeepTealBlue,
                        fontSize = 16.sp
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = onRoleChange,
                        label = { Text("Target Role (e.g. Clinic Director, Operations Lead)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = industry,
                        onValueChange = onIndustryChange,
                        label = { Text("Target Industry / Field (e.g. Healthcare Operations)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftGray),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Underlay Assets 📄",
                        fontWeight = FontWeight.Bold,
                        color = DeepTealBlue,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Add your CV / Resume for automated profile elements extraction and tuning.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onPickPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepTealBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("📄 Upload PDF Resume")
                        }

                        pdfName?.let { name ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F3FF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTealBlue,
                                    modifier = Modifier.widthIn(max = 120.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "❌",
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { onClearPdf() }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cvText,
                        onValueChange = onCvChange,
                        label = { Text("Or Paste Resume / CV text here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = currentProfile,
                        onValueChange = onProfileChange,
                        label = { Text("Current LinkedIn Info (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onOptimize,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepTealBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Optimize LinkedIn Presence ✨",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // --- ASYNC STATES ---
        item {
            when (val state = uiState) {
                is OptimizationUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = DeepTealBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing with Gemini AI model...\nTuning keywords to UAE operations KPIs and healthcare regulatory compliance standard metrics.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is OptimizationUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Error: ${state.message}",
                            color = Color(0xFFC81E1E),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp
                        )
                    }
                }
                is OptimizationUiState.Success -> {
                    val r = state.response
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SCORES PANEL (BIG GRID)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Gulf Recruiter Alignment Metrics 📊",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTealBlue,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        MiniScoreMetric("🟢 Profile Strength", r.profileStrengthScore)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        MiniScoreMetric("👔 Leadership Branding", r.leadershipBrandingScore)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        MiniScoreMetric("🪙 Industry Keywords", r.keywordScore)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        MiniScoreMetric("🔍 Recruiter Visibility", r.recruiterVisibilityScore)
                                    }
                                }
                            }
                        }

                        // EXPORT BUTTONS ROW
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        val exported = buildOptimizationSummaryText(r, role, industry)
                                        UiHelpers.copyToClipboard(context, "Full Optimization", exported)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📋 Copy ALL", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val exported = buildOptimizationSummaryText(r, role, industry)
                                        ExportUtils.shareTextFile(context, "optimized_linkedin_profile.txt", exported)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("TXT 💾", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val sections = listOf(
                                            "TARGET CRITERIA" to "Role: $role\nIndustry: $industry",
                                            "LINKEDIN HEADLINE" to r.headline,
                                            "ABOUT SUMMARY" to r.aboutSection,
                                            "REWRITTEN EXPERIENCE" to r.experienceRewrite,
                                            "SKILLS SUGGESTIONS" to r.skillsSuggestions.joinToString(", "),
                                            "RECRUITER TARGETED KEYWORDS" to r.recruiterKeywords.joinToString(", "),
                                            "EXTRACTED EDUCATION" to r.extractedEducation,
                                            "EXTRACTED ACHIEVEMENTS" to r.extractedAchievements,
                                            "SAVED LINKEDIN POSTS FORMULAS" to r.linkedinPostIdeas.joinToString("\n\n---\n\n")
                                        )
                                        ExportUtils.sharePdfFile(
                                            context = context,
                                            filename = "UAELinkedInOptimization.pdf",
                                            title = "AI LinkedIn Profile Tuning Report",
                                            sections = sections
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("PDF 📄", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // AUTOMATIC EXTRACTION RESULTS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Extracted Resume Components 📂",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepTealBlue
                                )
                                Text(
                                    text = "Extracted directly from your CV text/uploads using automatic semantic indexing.",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                ExtractedItemRow(" Experience", r.extractedExperience)
                                Spacer(modifier = Modifier.height(8.dp))
                                ExtractedItemRow(" Skills Map", r.extractedSkills)
                                Spacer(modifier = Modifier.height(8.dp))
                                ExtractedItemRow(" Education", r.extractedEducation)
                                Spacer(modifier = Modifier.height(8.dp))
                                ExtractedItemRow(" Achievements", r.extractedAchievements)
                            }
                        }

                        // GENERATED HEADLINE
                        LinkedInSectionCard(
                            title = "LinkedIn Headline ✨",
                            content = r.headline,
                            onCopy = { UiHelpers.copyToClipboard(context, "Headline", r.headline) }
                        )

                        // GENERATED ABOUT SUMMARY
                        LinkedInSectionCard(
                            title = "About Section ✍️",
                            content = r.aboutSection,
                            onCopy = { UiHelpers.copyToClipboard(context, "About Summary", r.aboutSection) }
                        )

                        // REWRITTEN EXPERIENCE
                        LinkedInSectionCard(
                            title = "Rewritten Experience Points 📈",
                            content = r.experienceRewrite,
                            onCopy = { UiHelpers.copyToClipboard(context, "Experience Rewrite", r.experienceRewrite) }
                        )

                        // SKILLS CHIPS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recommended Skills Tags 🏷️", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTealBlue)
                                    Text(
                                        text = "📋 Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTealBlue,
                                        modifier = Modifier.clickable {
                                            UiHelpers.copyToClipboard(context, "Skills", r.skillsSuggestions.joinToString(", "))
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    r.skillsSuggestions.forEach { skill ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = skill, fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // CORE SEARCH KEYWORDS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recruiter Search Keywords 🎯", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "📋 Copy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTealBlue,
                                        modifier = Modifier.clickable {
                                            UiHelpers.copyToClipboard(context, "Job Keywords", r.recruiterKeywords.joinToString(", "))
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    r.recruiterKeywords.forEach { keyword ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = keyword, fontSize = 12.sp, color = DeepTealBlue, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // AUTHORITY POSTING IDEAS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("LinkedIn Post Starters 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "📋 Copy All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTealBlue,
                                        modifier = Modifier.clickable {
                                            UiHelpers.copyToClipboard(context, "LinkedIn Authority Posts", r.linkedinPostIdeas.joinToString("\n\n---\n\n"))
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                r.linkedinPostIdeas.forEachIndexed { i, post ->
                                    Column {
                                        Text(
                                            text = "Concept #${i + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = DeepTealBlue
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = post,
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 16.sp
                                        )
                                        if (i < r.linkedinPostIdeas.size - 1) {
                                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditScreen(
    headline: String,
    onHeadlineChange: (String) -> Unit,
    about: String,
    onAboutChange: (String) -> Unit,
    experience: String,
    onExperienceChange: (String) -> Unit,
    skills: String,
    onSkillsChange: (String) -> Unit,
    uiState: AuditUiState,
    onAudit: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SoftGray),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Current LinkedIn Audit Input 🔍",
                        fontWeight = FontWeight.Bold,
                        color = DeepTealBlue,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Fill in the elements you want audited below. Our AI highlights areas of improvement for Recruiter ATS maps.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = headline,
                        onValueChange = onHeadlineChange,
                        label = { Text("Profile Headline") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = about,
                        onValueChange = onAboutChange,
                        label = { Text("About Summary Section") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = experience,
                        onValueChange = onExperienceChange,
                        label = { Text("Experience Snippets") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = skills,
                        onValueChange = onSkillsChange,
                        label = { Text("Skills List (Comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = onAudit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepTealBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Audit Profile Quality 🔍",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // --- ASYNC STATES ---
        item {
            when (val state = uiState) {
                is AuditUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = DeepTealBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing profile metrics...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                is AuditUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Error: ${state.message}",
                            color = Color(0xFFC81E1E),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp
                        )
                    }
                }
                is AuditUiState.Success -> {
                    val audit = state.response
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // RADIAL DIAL SCORE
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Audit Health Rating 📊",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepTealBlue
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .background(Color(0xFFF1F5F9), CircleShape)
                                        .border(width = 4.dp, color = DeepTealBlue, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                   ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${audit.auditScore}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepTealBlue
                                        )
                                        Text(text = "/100", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = getScoreEvaluationText(audit.auditScore),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (audit.auditScore >= 80) ActiveGreen else Color(0xFFD03801)
                                )
                            }
                        }

                        // REPORT EXPORTS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        val exported = buildAuditSummaryText(audit, headline)
                                        UiHelpers.copyToClipboard(context, "Profile Audit Report", exported)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📋 Copy Report", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val exported = buildAuditSummaryText(audit, headline)
                                        ExportUtils.shareTextFile(context, "linkedin_audit_report.txt", exported)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("TXT 💾", color = Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // STRENGTHS (Greens)
                        AuditDetailsCard("✅ Core Strengths", audit.strengths, Color(0xFFE1F5FE), Color(0xFF0288D1))

                        // WEAKNESSES (Reds)
                        AuditDetailsCard("⚠️ Priority Weaknesses", audit.weaknesses, Color(0xFFFFEBEE), Color(0xFFC62828))

                        // MISSING KEYWORDS (Oranges)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SoftGray),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Missing recs keywords 📌", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepTealBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    audit.missingKeywords.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFF3E0))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = tag, fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ACTIONABLE SUGGESTIONS (Yellow/Blue)
                        AuditDetailsCard("💡 Task Suggestions", audit.improvementSuggestions, Color(0xFFF3F2EF), Color(0xFF424242))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun HistoryScreen(
    optimizationHistory: List<ProfileOptimizationEntity>,
    auditHistory: List<LinkedInAuditEntity>,
    onSelectOptimization: (ProfileOptimizationEntity) -> Unit,
    onSelectAudit: (LinkedInAuditEntity) -> Unit,
    onDeleteOptimization: (Int) -> Unit,
    onDeleteAudit: (Int) -> Unit
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Optimizations, 1 = Audits

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub-selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5F9))
                .padding(4.dp)
        ) {
            listOf("Ｔｕｎｉｎｇ 💫", "Ａｕｄｉｔｓ 🔎").forEachIndexed { index, title ->
                val isActive = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Color.White else Color.Transparent)
                        .clickable { selectedSubTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) DeepTealBlue else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (selectedSubTab == 0) {
            if (optimizationHistory.isEmpty()) {
                HistoryEmptyState("No profile optimizations found yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(optimizationHistory) { item ->
                        HistoryCardItem(
                            title = "🎯 ${item.targetRole}",
                            subtitle = "Industry: ${item.targetIndustry}",
                            score = item.profileStrengthScore,
                            onSelect = { onSelectOptimization(item) },
                            onDelete = { onDeleteOptimization(item.id) }
                        )
                    }
                }
            }
        } else {
            if (auditHistory.isEmpty()) {
                HistoryEmptyState("No profile audits logged yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(auditHistory) { item ->
                        HistoryCardItem(
                            title = "🔍 ${if (item.headline.isNotBlank()) item.headline else "Snippet Audit"}",
                            subtitle = "Strengths checklist generated",
                            score = item.auditScore,
                            onSelect = { onSelectAudit(item) },
                            onDelete = { onDeleteAudit(item.id) }
                        )
                    }
                }
            }
        }
    }
}

// ==================== SUB-COMPONENTS & LAYOUT HELPERS ====================

@Composable
fun MiniScoreMetric(label: String, score: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkText)
            Text(text = "$score/100", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepTealBlue)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = DeepTealBlue,
            trackColor = Color(0xFFF1F5F9)
        )
    }
}

@Composable
fun LinkedInSectionCard(
    title: String,
    content: String,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTealBlue)
                Text(
                    text = "💼 Copy",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTealBlue,
                    modifier = Modifier.clickable { onCopy() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = Color.DarkGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AuditDetailsCard(
    title: String,
    items: List<String>,
    backgroundColor: Color,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepTealBlue)
            Spacer(modifier = Modifier.height(10.dp))
            items.forEachIndexed { idx, value ->
                if (value.trim().isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "•", color = accentColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp))
                        Text(text = value, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExtractedItemRow(label: String, data: String) {
    Column {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepTealBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (data.isNotBlank()) data else "No info extracted.",
            fontSize = 11.sp,
            color = Color.DarkGray,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun HistoryCardItem(
    title: String,
    subtitle: String,
    score: Int,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SoftGray),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(MutedBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$score",
                    color = DeepTealBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DarkText
                )
                Text(
                    text = subtitle,
                    maxLines = 1,
                    fontSize = 11.sp,
                    color = MediumText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Text(text = "🗑️", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun HistoryEmptyState(tip: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📜", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tip, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ==================== HELPER WRITING GENERATORS ====================

private fun buildOptimizationSummaryText(r: StructuredOptimizationResponse, role: String, industry: String): String {
    return """
        # LINKEDIN OPTIMIZATION REPORT (UAE Healthcare Operations Optimized)
        
        Target Role: $role
        Target Industry: $industry
        
        --- OUTBOUND SCORE TARGETS ---
        - Profile Strength: ${r.profileStrengthScore}/100
        - Keyword Score: ${r.keywordScore}/100
        - Leadership Branding: ${r.leadershipBrandingScore}/100
        - Recruiter Visibility: ${r.recruiterVisibilityScore}/100
        
        --- COMPONENT A: LINKEDIN HEADLINE ---
        ${r.headline}
        
        --- COMPONENT B: ABOUT EXECUTIVE SUMMARY ---
        ${r.aboutSection}
        
        --- COMPONENT C: EXPERIENCES REWRITE ---
        ${r.experienceRewrite}
        
        --- COMPONENT D: ADVISORY SKILLS MAP ---
        ${r.skillsSuggestions.joinToString("\n")}
        
        --- COMPONENT E: ATS RECRUITER KEYWORDS --
        ${r.recruiterKeywords.joinToString(", ")}
        
        --- COMPONENT F: EXTRACTED COMPONENTS ---
        Experience Summary: ${r.extractedExperience}
        Skills Map: ${r.extractedSkills}
        Education: ${r.extractedEducation}
        Achievements: ${r.extractedAchievements}
        
        --- COMPONENT G: PERSONAL BRANDING LINKEDIN POSTS ---
        ${r.linkedinPostIdeas.joinToString("\n\n---\n\n")}
    """.trimIndent()
}

private fun buildAuditSummaryText(audit: StructuredAuditResponse, originalHeadline: String): String {
    return """
        # LINKEDIN AUDIT REPORT (Health ops UAE Standards verified)
        
        Current Profile Headline Audited: "$originalHeadline"
        Current Safety Rating Score: ${audit.auditScore}/100
        
        --- IDENTIFIED CORE STRENGTHS ---
        ${audit.strengths.joinToString("\n")}
        
        --- PRIORITY WEAKNESSES ---
        ${audit.weaknesses.joinToString("\n")}
        
        --- MISSING STRATEGIC KEYWORDS ---
        ${audit.missingKeywords.joinToString(", ")}
        
        --- ACTIONABLE IMPROVEMENTS SUGGESTIONS ---
        ${audit.improvementSuggestions.joinToString("\n")}
    """.trimIndent()
}

private fun getScoreEvaluationText(score: Int): String {
    return when {
        score >= 90 -> "🌟 Ultimate Executive Standing! Ready for regional UAE headhunters."
        score >= 80 -> "✅ Competitive profile. High ATS checkmate possibility."
        score >= 65 -> "⚠️ Decent, but missing critical clinic-ops metrics."
        else -> "🚨 Weak positioning. Recruiter visibility is limited."
    }
}

// Space mapping helpers bypassed via native APIs

