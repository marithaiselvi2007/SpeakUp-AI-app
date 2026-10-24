package com.example.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PracticeTopic
import com.example.speech.TtsHelper
import com.example.ui.components.PracticeDaysHeaderCard
import com.example.ui.components.StreakMilestoneCelebrationDialog
import com.example.ui.components.UserProfileAvatar
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.DailyChallengeStatus
import com.example.viewmodel.SpeakUpViewModel
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    speakUpViewModel: SpeakUpViewModel,
    onStartPracticeTopic: (PracticeTopic) -> Unit,
    onNavigateToAiCoach: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToTopics: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    onOpenAchievements: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsHelper = remember { TtsHelper(context) }
    val profile by speakUpViewModel.userProfile.collectAsState()
    val hasSpokenWelcome by speakUpViewModel.hasSpokenWelcomeGreeting.collectAsState()
    val completedDays by speakUpViewModel.completedDayChallenges.collectAsState()
    val challengeStatus by speakUpViewModel.dailyChallengeStatus.collectAsState()
    val currentChallengeDay by speakUpViewModel.currentChallengeDayNumber.collectAsState()
    val streakMilestone by speakUpViewModel.streakMilestoneCelebration.collectAsState()
    val nextChallengePreparedEvent by speakUpViewModel.nextChallengePreparedEvent.collectAsState()
    val practiceDaysProgress by speakUpViewModel.practiceDaysProgress.collectAsState()
    val userLevel = profile?.englishLevel ?: com.example.data.model.EnglishLevel.BEGINNER

    val rawName = profile?.name?.trim() ?: ""
    val hasCustomName = rawName.isNotBlank() &&
        !rawName.equals("Learner", ignoreCase = true) &&
        !rawName.equals("Guest", ignoreCase = true) &&
        !rawName.equals("Guest Learner", ignoreCase = true) &&
        !rawName.equals("User", ignoreCase = true)

    val displayName = if (hasCustomName) rawName else "Learner"

    val welcomeVoiceMessage = if (hasCustomName) {
        "Welcome, $displayName, to SpeakUp AI. Let's improve your English speaking."
    } else {
        "Welcome to English Speaking with SpeakUp AI. Let's improve your English together."
    }

    // Ensure TTS stops immediately if user navigates away to Speaking Practice or AI Coach
    DisposableEffect(ttsHelper) {
        onDispose {
            ttsHelper.stop()
        }
    }

    // Automatically refresh session history and profile data upon entering the Dashboard
    LaunchedEffect(Unit) {
        speakUpViewModel.refreshData()
    }

    // Automatically speak the short welcome message once upon entering the Dashboard
    LaunchedEffect(hasSpokenWelcome, profile) {
        if (!hasSpokenWelcome) {
            speakUpViewModel.markWelcomeGreetingSpoken()
            delay(350) // Short delay to allow screen transition to complete smoothly
            ttsHelper.speak(welcomeVoiceMessage)
        }
    }

    // Streak celebration popup
    streakMilestone?.let { milestone ->
        StreakMilestoneCelebrationDialog(
            milestoneDays = milestone,
            onDismiss = { speakUpViewModel.dismissStreakCelebration() }
        )
    }

    // Next challenge prepared dialog
    nextChallengePreparedEvent?.let { msg ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { speakUpViewModel.dismissNextChallengePreparedEvent() },
            confirmButton = {
                Button(
                    onClick = { speakUpViewModel.dismissNextChallengePreparedEvent() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got it")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next Challenge Prepared! 🚀",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "💡 Only one challenge is open per practice day to ensure steady, permanent fluency gains.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // TOP HEADER: Single-line greeting & navigation
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { onOpenDrawer?.invoke() },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("dashboard_hamburger_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Navigation Menu",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Professional single-line greeting with user's actual name
                        Text(
                            text = "Welcome, $displayName 👋",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("dashboard_user_greeting")
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Voice greeting button
                        IconButton(
                            onClick = {
                                ttsHelper.speak(welcomeVoiceMessage)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("dashboard_voice_greeting_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak Greeting",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onNavigateToProfile() }
                                .testTag("dashboard_avatar_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            UserProfileAvatar(
                                photoUrl = profile?.photoUrl,
                                size = 40.dp,
                                fallbackTextSize = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // 📅 PRACTICE DAYS / DAYS DISPLAY AT TOP
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                PracticeDaysHeaderCard(
                    progress = practiceDaysProgress,
                    onEditDaysClick = { onNavigateToProfile() }
                )
            }
        }

        // DECORATIVE ENGLISH-LEARNING ILLUSTRATION BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_home_speaking),
                        contentDescription = "English Learning Practice",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 🎙️ SPEAKING PRACTICE ENTRY (User does normal Speaking Practice first)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_hero_speaking_practice"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = "15 INTERACTIVE TOPICS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Speaking Practice",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Practice English conversations with real-time speech analysis, fluency metrics, and instant grammar feedback.",
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Microphone",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Start Speaking Practice Action Button
                        Button(
                            onClick = {
                                onNavigateToTopics()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_speaking_practice_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Start Speaking Practice",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 🎯 SEPARATE DAILY CHALLENGE CARD ON DASHBOARD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_hero_daily_challenge"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (challengeStatus) {
                            DailyChallengeStatus.COMPLETED_TODAY -> SuccessGreen.copy(alpha = 0.12f)
                            DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.secondaryContainer
                            DailyChallengeStatus.PRACTICE_REQUIRED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (challengeStatus) {
                                        DailyChallengeStatus.COMPLETED_TODAY -> SuccessGreen
                                        DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.secondary
                                        DailyChallengeStatus.PRACTICE_REQUIRED -> MaterialTheme.colorScheme.outline
                                    },
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = when (challengeStatus) {
                                            DailyChallengeStatus.COMPLETED_TODAY -> "TODAY'S CHALLENGE COMPLETED ✓"
                                            DailyChallengeStatus.READY_TO_START -> "DAILY CHALLENGE UNLOCKED 🎯"
                                            DailyChallengeStatus.PRACTICE_REQUIRED -> "DAILY CHALLENGE • PRACTICE REQUIRED"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Day $currentChallengeDay Challenge",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = when (challengeStatus) {
                                        DailyChallengeStatus.COMPLETED_TODAY ->
                                            "You've completed Day $currentChallengeDay challenge today! Your required daily challenge is complete."
                                        DailyChallengeStatus.READY_TO_START ->
                                            "You've finished speaking practice today! Day $currentChallengeDay challenge is ready to begin."
                                        DailyChallengeStatus.PRACTICE_REQUIRED ->
                                            "Complete today's speaking practice first to unlock today's challenge."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Day $currentChallengeDay",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (challengeStatus == DailyChallengeStatus.COMPLETED_TODAY) SuccessGreen else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (challengeStatus == DailyChallengeStatus.COMPLETED_TODAY) "Done ✓" else userLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Button with distinct state handling
                        when (challengeStatus) {
                            DailyChallengeStatus.PRACTICE_REQUIRED -> {
                                Button(
                                    onClick = { onNavigateToTopics() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("dashboard_complete_practice_first_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Complete Speaking Practice First",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                            DailyChallengeStatus.READY_TO_START -> {
                                Button(
                                    onClick = { onNavigateToTopics() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("dashboard_start_day_challenge_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start Day $currentChallengeDay Challenge",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DailyChallengeStatus.COMPLETED_TODAY -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("dashboard_challenge_completed_badge"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Completed",
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "✓ Completed",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

