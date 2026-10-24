package com.example.ui.screens.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayChallenge
import com.example.data.model.PracticeTopic
import com.example.data.repository.DayChallengeData
import com.example.data.repository.TopicsData
import com.example.ui.components.TopicCard
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.SpeakUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsScreen(
    onSelectTopic: (PracticeTopic) -> Unit,
    speakUpViewModel: SpeakUpViewModel? = null,
    onStartDayChallenge: ((Int) -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedScreenTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val completedDays = speakUpViewModel?.completedDayChallenges?.collectAsState()?.value ?: emptySet()

    val categories = listOf(
        "All", "Basics", "Education", "Social", "Personal",
        "Everyday", "Interests", "Lifestyle", "Entertainment",
        "Modern", "Professional", "Open"
    )

    val filteredTopics = TopicsData.allTopics.filter { topic ->
        val matchesCategory = selectedCategory == "All" || topic.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                topic.title.contains(searchQuery, ignoreCase = true) ||
                topic.subtitle.contains(searchQuery, ignoreCase = true) ||
                topic.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("topics_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (selectedScreenTab == 0) "Daily Speaking Challenges" else "Practice Topics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedScreenTab == 0) "One AI-adaptive challenge per practice day" else "Choose from structured practice topics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onOpenDrawer?.invoke() },
                        modifier = Modifier.testTag("topics_hamburger_menu")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Navigation Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mode Selector Tabs: Daily Challenge vs Topics
            TabRow(
                selectedTabIndex = selectedScreenTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("topics_screen_tab_row")
            ) {
                Tab(
                    selected = selectedScreenTab == 0,
                    onClick = { selectedScreenTab = 0 },
                    text = {
                        Text(
                            text = "🎯 Daily Challenges",
                            fontWeight = if (selectedScreenTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_daily_challenges")
                )
                Tab(
                    selected = selectedScreenTab == 1,
                    onClick = { selectedScreenTab = 1 },
                    text = {
                        Text(
                            text = "📚 Practice Topics",
                            fontWeight = if (selectedScreenTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("tab_practice_topics")
                )
            }

            if (selectedScreenTab == 0) {
                // TAB 0: DAILY SPEAKING CHALLENGES PROGRESSION (Infinite single challenge per day)
                DailyChallengesView(
                    speakUpViewModel = speakUpViewModel,
                    completedDays = completedDays,
                    onSwitchToPracticeTopics = { selectedScreenTab = 1 },
                    onStartChallenge = { dayNumber ->
                        if (onStartDayChallenge != null) {
                            onStartDayChallenge(dayNumber)
                        } else {
                            val userLevel = speakUpViewModel?.userProfile?.value?.englishLevel ?: com.example.data.model.EnglishLevel.BEGINNER
                            val challenge = DayChallengeData.getChallengeForDay(dayNumber, userLevel)
                            val topic = PracticeTopic(
                                id = "day_challenge_$dayNumber",
                                title = challenge.title,
                                subtitle = challenge.subtitle,
                                category = challenge.category,
                                iconName = challenge.iconName,
                                starterPrompt = challenge.starterPrompt,
                                sampleQuestions = challenge.questions
                            )
                            onSelectTopic(topic)
                        }
                    }
                )
            } else {
                // TAB 1: PRACTICE TOPICS (Standard + Custom)
                PracticeTopicsListView(
                    speakUpViewModel = speakUpViewModel,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    filteredTopics = filteredTopics,
                    onSelectTopic = onSelectTopic
                )
            }
        }
    }
}

@Composable
private fun DailyChallengesView(
    speakUpViewModel: SpeakUpViewModel?,
    completedDays: Set<Int>,
    onSwitchToPracticeTopics: () -> Unit,
    onStartChallenge: (Int) -> Unit
) {
    val completedCount = completedDays.size
    val challengeStatus = speakUpViewModel?.dailyChallengeStatus?.collectAsState()?.value
        ?: com.example.viewmodel.DailyChallengeStatus.READY_TO_START
    val currentDayNumber = speakUpViewModel?.currentChallengeDayNumber?.collectAsState()?.value ?: 1
    val practiceDaysProgress = speakUpViewModel?.practiceDaysProgress?.collectAsState()?.value
        ?: speakUpViewModel?.getPracticeDaysProgress()
    val todayChallenge = speakUpViewModel?.todayDailyChallenge?.collectAsState()?.value
    val userProfile = speakUpViewModel?.userProfile?.collectAsState()?.value
    val userLevel = userProfile?.englishLevel ?: com.example.data.model.EnglishLevel.BEGINNER
    val nextChallengePreparedEvent = speakUpViewModel?.nextChallengePreparedEvent?.collectAsState()?.value

    val context = androidx.compose.ui.platform.LocalContext.current
    val tamilTtsHelper = remember { com.example.speech.TamilTtsHelper.getInstance(context) }
    val currentlySpeakingTamil by tamilTtsHelper.currentlySpeakingText.collectAsState()

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            tamilTtsHelper.stop()
        }
    }

    var showPracticeFirstDialog by remember { mutableStateOf(false) }
    var expandedTamilMeaningIndex by remember { mutableStateOf<Int?>(null) }
    var viewingCompletedHistoryDay by remember { mutableStateOf<Int?>(null) }

    // Dialog showing completed challenge details and history without restarting practice
    if (viewingCompletedHistoryDay != null) {
        val dayNum = viewingCompletedHistoryDay!!
        val challenge = DayChallengeData.getChallengeForDay(dayNum, userLevel)
        val bestScore = speakUpViewModel?.getDayChallengeBestScore(dayNum)
        var dialogTamilIndex by remember { mutableStateOf<Int?>(null) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewingCompletedHistoryDay = null },
            confirmButton = {
                Button(
                    onClick = { viewingCompletedHistoryDay = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("completed_history_close_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "COMPLETED ✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Day $dayNum Challenge History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (bestScore != null && bestScore > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Score: $bestScore%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = challenge.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Speaking Questions & Translations:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    challenge.questions.forEachIndexed { idx, q ->
                        val isTamilOpen = dialogTamilIndex == idx
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${idx + 1}. $q",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isTamilOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                dialogTamilIndex = if (isTamilOpen) null else idx
                                            }
                                    ) {
                                        Text(
                                            text = if (isTamilOpen) "தமிழில் ✓" else "தமிழில் அர்த்தம்",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isTamilOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isTamilOpen) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val tamil = com.example.util.TamilQuestionTranslator.getTamilMeaning(q)
                                    val isSpeaking = currentlySpeakingTamil == tamil
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tamil,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        tamilTtsHelper.speakTamil(tamil)
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Read Tamil Aloud",
                                                        tint = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isSpeaking) "🔊..." else "🔊 கேட்க",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "This challenge is completed. Each Daily Challenge can be completed once per eligible calendar day.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Next challenge ready confirmation dialog
    if (nextChallengePreparedEvent != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { speakUpViewModel?.dismissNextChallengePreparedEvent() },
            confirmButton = {
                Button(
                    onClick = { speakUpViewModel?.dismissNextChallengePreparedEvent() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Great! Keep Practicing")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next Challenge Prepared!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = nextChallengePreparedEvent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your next challenge is tailored to your real practice performance and ${userLevel.displayName} English level. 1 challenge is unlocked per practice day to build lifelong fluency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog when today's speaking practice is required first
    if (showPracticeFirstDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPracticeFirstDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showPracticeFirstDialog = false
                        onSwitchToPracticeTopics()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Speaking Practice 🎙️")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showPracticeFirstDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Speaking Practice First 🎙️",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Complete today's speaking practice session first to unlock today's Day $currentDayNumber challenge. Choose any practice topic to get started!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    val activeChallenge = todayChallenge ?: DayChallengeData.getChallengeForDay(currentDayNumber, userLevel)
    val isCompletedToday = challengeStatus == com.example.viewmodel.DailyChallengeStatus.COMPLETED_TODAY || completedDays.contains(currentDayNumber)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_challenges_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Practice Schedule Days Header
        if (practiceDaysProgress != null) {
            item {
                com.example.ui.components.PracticeDaysHeaderCard(
                    progress = practiceDaysProgress
                )
            }
        }

        // Progression Journey Status Header Card (Infinite - no fixed limit)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_challenge_progress_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Speaking Challenge",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active Day: Challenge $currentDayNumber",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "$completedCount Days Completed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1 adaptive AI challenge per day. Complete today's challenge and tap CONTINUE to generate your next practice day's challenge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section Title: TODAY'S ACTIVE CHALLENGE
        item {
            Text(
                text = "TODAY'S ACTIVE CHALLENGE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Active Challenge Card (Day $currentDayNumber)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("day_challenge_card_${activeChallenge.dayNumber}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCompletedToday -> MaterialTheme.colorScheme.surface
                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header: Day Tag and Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isCompletedToday -> SuccessGreen.copy(alpha = 0.15f)
                                challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            }
                        ) {
                            Text(
                                text = "DAY ${activeChallenge.dayNumber} CHALLENGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = when {
                                    isCompletedToday -> SuccessGreen
                                    challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Unlock/Lock/Completed Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isCompletedToday -> SuccessGreen.copy(alpha = 0.15f)
                                challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isCompletedToday -> Icons.Filled.CheckCircle
                                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> Icons.Filled.LockOpen
                                        else -> Icons.Filled.Lock
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isCompletedToday -> SuccessGreen
                                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when {
                                        isCompletedToday -> "Completed"
                                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> "Available"
                                        else -> "Practice First"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when {
                                        isCompletedToday -> SuccessGreen
                                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Day ${activeChallenge.dayNumber}: ${activeChallenge.title.replace("Day ${activeChallenge.dayNumber}: ", "")}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = activeChallenge.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${userLevel.displayName} Level",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activeChallenge.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Questions / Speaking Tasks Preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Speaking Tasks (${activeChallenge.questions.size} questions):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        activeChallenge.questions.forEachIndexed { index, q ->
                            val isTamilExpanded = (expandedTamilMeaningIndex == index)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTamilExpanded) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                                    .padding(if (isTamilExpanded) 8.dp else 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = q,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Small "தமிழில் அர்த்தம்" button beside question
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isTamilExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                expandedTamilMeaningIndex = if (isTamilExpanded) null else index
                                            }
                                    ) {
                                        Text(
                                            text = if (isTamilExpanded) "தமிழில் ✓" else "தமிழில் அர்த்தம்",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isTamilExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (isTamilExpanded) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val tamilTranslation = com.example.util.TamilQuestionTranslator.getTamilMeaning(q)
                                    val isSpeaking = currentlySpeakingTamil == tamilTranslation
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = tamilTranslation,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        lineHeight = 16.sp,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            tamilTtsHelper.speakTamil(tamilTranslation)
                                                        }
                                                        .testTag("speak_tamil_question_$index")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                            contentDescription = "Read Tamil Aloud",
                                                            tint = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = if (isSpeaking) "🔊..." else "🔊 கேட்க",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = if (isSpeaking) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "💡 Please answer in English.",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button Flow
                    when {
                        isCompletedToday -> {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewingCompletedHistoryDay = activeChallenge.dayNumber }
                                    .testTag("topics_challenge_completed_badge"),
                                shape = RoundedCornerShape(12.dp),
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
                                        modifier = Modifier.size(20.dp)
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

                        challengeStatus == com.example.viewmodel.DailyChallengeStatus.READY_TO_START -> {
                            Button(
                                onClick = { onStartChallenge(activeChallenge.dayNumber) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_day_challenge_button_${activeChallenge.dayNumber}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start Day ${activeChallenge.dayNumber} Challenge",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        else -> {
                            Button(
                                onClick = { showPracticeFirstDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_day_challenge_button_${activeChallenge.dayNumber}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete Speaking Practice First",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: NEXT PRACTICE DAY PREVIEW (Locked for future day)
        val nextDayNum = if (isCompletedToday) currentDayNumber + 1 else currentDayNumber + 1
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_day_locked_card_$nextDayNum"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Day $nextDayNum: Next Practice Day Challenge 🔒",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AI adapts the next challenge using your real performance and ${userLevel.displayName} English level. Unlocks on your next practice day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section: PAST COMPLETED CHALLENGES (if any)
        val pastCompletedList = completedDays
            .filter { if (isCompletedToday) it != activeChallenge.dayNumber else true }
            .sortedDescending()
        if (pastCompletedList.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "COMPLETED CHALLENGES (${pastCompletedList.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(pastCompletedList) { dayNum ->
                val pastChallenge = DayChallengeData.getChallengeForDay(dayNum, userLevel)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("completed_day_card_$dayNum")
                        .clickable { viewingCompletedHistoryDay = dayNum },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "D$dayNum",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Day $dayNum: ${pastChallenge.title.replace("Day $dayNum: ", "")}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = pastChallenge.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Completed",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PracticeTopicsListView(
    speakUpViewModel: SpeakUpViewModel? = null,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    filteredTopics: List<PracticeTopic>,
    onSelectTopic: (PracticeTopic) -> Unit
) {
    val practiceDaysProgress = speakUpViewModel?.practiceDaysProgress?.collectAsState()?.value
        ?: speakUpViewModel?.getPracticeDaysProgress()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("topics_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Practice Schedule Days Header
        if (practiceDaysProgress != null) {
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    com.example.ui.components.PracticeDaysHeaderCard(
                        progress = practiceDaysProgress
                    )
                }
            }
        }

        // Search Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search topics, keywords...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        }

        // Categories Filter Chips
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // "Talk About Anything" Custom Topic Builder Card
        item {
            var customTopicText by remember { mutableStateOf("") }
            val sampleCustomSuggestions = listOf(
                "My favourite person",
                "My village",
                "My project",
                "My college",
                "My dream",
                "Something that happened today"
            )

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("talk_about_anything_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✨", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Talk About Anything",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Speak on your own topic or idea",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customTopicText,
                            onValueChange = { customTopicText = it },
                            placeholder = { Text("e.g. My village, My dream, today's event...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_topic_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick suggestion chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(sampleCustomSuggestions) { suggestion ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { customTopicText = suggestion },
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "+ $suggestion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val finalTopicTitle = if (customTopicText.isNotBlank()) {
                                    customTopicText.trim()
                                } else {
                                    "Free Conversation & Spontaneous Speaking"
                                }
                                val customTopic = PracticeTopic(
                                    id = "custom_${System.currentTimeMillis()}",
                                    title = finalTopicTitle,
                                    subtitle = "Custom Speaking Practice",
                                    category = "Custom",
                                    iconName = "Chat",
                                    starterPrompt = "I'd love to chat about $finalTopicTitle! What would you like to share about it?",
                                    sampleQuestions = listOf(
                                        "Tell me more about $finalTopicTitle.",
                                        "What makes this important to you?",
                                        "How has this impacted your life or daily routine?"
                                    )
                                )
                                onSelectTopic(customTopic)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_custom_topic_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (customTopicText.isNotBlank()) "Start Speaking: \"$customTopicText\"" else "Start Free Speaking Practice",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Topics Count Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTopics.size} Topics Available",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "English & Tanglish Supported",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Topic List (All 15 Topics)
        if (filteredTopics.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No topics found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredTopics) { topic ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    TopicCard(
                        topic = topic,
                        onClick = { onSelectTopic(topic) }
                    )
                }
            }
        }

        // =========================================================================
        // OWN TOPIC (Positioned strictly at the bottom as requested)
        // "Want to speak about something else? Choose your own topic."
        // =========================================================================
        item {
            var showOwnTopicDialog by remember { mutableStateOf(false) }
            var ownTopicInput by remember { mutableStateOf("") }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Card(
                    onClick = { showOwnTopicDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("own_topic_bottom_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OWN TOPIC",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Want to speak about something else? Choose your own topic.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Choose your own topic",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (showOwnTopicDialog) {
                AlertDialog(
                    onDismissRequest = { showOwnTopicDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Choose Your Own Topic",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    },
                    text = {
                        Column {
                            Text(
                                text = "Want to speak about something else? Choose your own topic.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = ownTopicInput,
                                onValueChange = { ownTopicInput = it },
                                placeholder = { Text("e.g. My road trip, My favorite hobby...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("own_topic_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Quick suggestions:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(listOf("My favorite memory", "My hometown journey", "My weekend project", "My life goal")) { suggestion ->
                                    SuggestionChip(
                                        onClick = { ownTopicInput = suggestion },
                                        label = { Text(suggestion, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val trimmed = ownTopicInput.trim()
                                if (trimmed.isNotBlank()) {
                                    val customTopic = PracticeTopic(
                                        id = "custom_${System.currentTimeMillis()}",
                                        title = trimmed,
                                        subtitle = "Custom Speaking Practice. Speak freely about $trimmed.",
                                        category = "Personal Choice",
                                        iconName = "RecordVoiceOver",
                                        starterPrompt = "Today I would like to speak about $trimmed because...",
                                        sampleQuestions = listOf(
                                            "What inspired you to talk about $trimmed today?",
                                            "Can you share a detailed experience or story about $trimmed?",
                                            "What is the most important thought you want to share about $trimmed?"
                                        )
                                    )
                                    showOwnTopicDialog = false
                                    onSelectTopic(customTopic)
                                }
                            },
                            enabled = ownTopicInput.trim().isNotBlank(),
                            modifier = Modifier.testTag("own_topic_start_button")
                        ) {
                            Text("Start Speaking 🎙️")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOwnTopicDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
