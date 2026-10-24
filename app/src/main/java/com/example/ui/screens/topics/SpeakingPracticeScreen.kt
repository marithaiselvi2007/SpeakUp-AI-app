package com.example.ui.screens.topics

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.PracticeSession
import com.example.data.remote.SpeakingPracticeReportResponse
import com.example.speech.SpeechState
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GrammarCorrectionCard
import com.example.ui.components.ScoreCard
import com.example.ui.components.ScoreMetric
import com.example.ui.components.VocabularySuggestionChips
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.SpeakingPracticeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakingPracticeScreen(
    practiceViewModel: SpeakingPracticeViewModel,
    dailyGoalMinutes: Int = 30,
    onNavigateBack: () -> Unit,
    onSessionFinished: () -> Unit,
    onPrepareNextDayChallenge: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val topic by practiceViewModel.currentTopic.collectAsState()
    val isRecording by practiceViewModel.isRecording.collectAsState()
    val isPaused by practiceViewModel.isPaused.collectAsState()
    val actualSeconds by practiceViewModel.actualSpeakingSeconds.collectAsState()
    val transcript by practiceViewModel.transcript.collectAsState()
    val partialTranscript by practiceViewModel.partialTranscript.collectAsState()
    val speechState by practiceViewModel.speechState.collectAsState()
    val speechRmsDb by practiceViewModel.speechRmsDb.collectAsState()
    val targetDurationMinutes by practiceViewModel.targetDurationMinutes.collectAsState()
    val isAnalyzing by practiceViewModel.isAnalyzing.collectAsState()
    val showConfirmDialog by practiceViewModel.showFinishConfirmDialog.collectAsState()
    val showReportDialog by practiceViewModel.showReportDialog.collectAsState()
    val showIncompleteDurationNotice by practiceViewModel.showIncompleteDurationNotice.collectAsState()
    val showOffTopicNotice by practiceViewModel.showOffTopicNotice.collectAsState()
    val offTopicReport by practiceViewModel.offTopicReport.collectAsState()
    val completedReport by practiceViewModel.completedReport.collectAsState()
    val isTimerFinished by practiceViewModel.isTimerFinished.collectAsState()
    val dayNumber by practiceViewModel.dayNumber.collectAsState()
    val currentQuestionIndex by practiceViewModel.currentQuestionIndex.collectAsState()

    val tamilTtsHelper = remember { com.example.speech.TamilTtsHelper.getInstance(context) }
    val currentlySpeakingTamil by tamilTtsHelper.currentlySpeakingText.collectAsState()

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            tamilTtsHelper.stop()
        }
    }

    // Play completion bell sound when session duration completes
    androidx.compose.runtime.LaunchedEffect(isTimerFinished) {
        if (isTimerFinished) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            } catch (e: Exception) {
                // Ignore tone error gracefully
            }
        }
    }

    var showTextInputDialog by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }
    var showExitWarningDialog by remember { mutableStateOf(false) }
    var showTamilMeaningForQuestion by remember { mutableStateOf(false) }

    // Reset Tamil meaning visibility when switching questions
    androidx.compose.runtime.LaunchedEffect(currentQuestionIndex) {
        showTamilMeaningForQuestion = false
    }

    // Intercept hardware and gesture back navigation when class is active
    val isSessionIncomplete = !isTimerFinished && !showReportDialog && (actualSeconds < targetDurationMinutes * 60L)
    BackHandler(enabled = true) {
        if (isSessionIncomplete) {
            showExitWarningDialog = true
        } else {
            practiceViewModel.pauseSpeaking()
            onNavigateBack()
        }
    }

    // Microphone permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            practiceViewModel.startSpeaking()
        }
    }

    fun handleMicAction() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (!isRecording) {
                practiceViewModel.startSpeaking()
            } else if (isPaused) {
                practiceViewModel.resumeSpeaking()
            } else {
                practiceViewModel.pauseSpeaking()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Session Incomplete Warning Dialog (Exit Protection & State Preservation)
    if (showExitWarningDialog) {
        AlertDialog(
            onDismissRequest = { showExitWarningDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Session Incomplete",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your session is not completed yet. Do you want to leave?",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "If you leave, your spoken progress (${formatDuration(actualSeconds)}) will be saved so you can resume exactly where you left off.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitWarningDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("continue_practice_button")
                ) {
                    Text("Continue Practice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            showExitWarningDialog = false
                            practiceViewModel.discardIncompleteSession()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("discard_session_button")
                    ) {
                        Text("Discard")
                    }
                    Button(
                        onClick = {
                            showExitWarningDialog = false
                            practiceViewModel.saveIncompleteSession()
                            practiceViewModel.pauseSpeaking()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("save_and_leave_button")
                    ) {
                        Text("Leave & Save")
                    }
                }
            }
        )
    }

    // Practice Time Complete Dialog (when selected duration ends)
    if (isTimerFinished) {
        AlertDialog(
            onDismissRequest = { /* Require user to click View Result */ },
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
                        text = "Practice Complete",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your practice time is complete.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Great job! Your actual practice time of ${formatDuration(actualSeconds)} has been recorded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        practiceViewModel.dismissTimerFinishedDialog()
                        practiceViewModel.confirmFinishSession()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("view_result_button")
                ) {
                    Text("View Result", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Stop confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { practiceViewModel.cancelFinishConfirmation() },
            title = {
                Text(
                    text = "Finish Speaking Session?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to finish this practice session and generate your speaking report?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Actual speaking duration: ${formatDuration(actualSeconds)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { practiceViewModel.confirmFinishSession() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("confirm_finish_button")
                ) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { practiceViewModel.cancelFinishConfirmation() },
                    modifier = Modifier.testTag("cancel_finish_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Incomplete Duration Warning Dialog
    if (showIncompleteDurationNotice) {
        AlertDialog(
            onDismissRequest = { practiceViewModel.dismissIncompleteDurationNotice() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Practice Incomplete",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Please complete the full practice time before viewing your result.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Selected Goal: $targetDurationMinutes min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Spoken: ${formatDuration(actualSeconds)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        practiceViewModel.dismissIncompleteDurationNotice()
                        practiceViewModel.resumeSpeaking()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("continue_practice_duration_button")
                ) {
                    Text("Continue Practice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        practiceViewModel.dismissIncompleteDurationNotice()
                    },
                    modifier = Modifier.testTag("dismiss_duration_notice_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Off-Topic Warning Dialog
    if (showOffTopicNotice) {
        val offTopic = offTopicReport
        AlertDialog(
            onDismissRequest = { practiceViewModel.dismissOffTopicNotice() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Topic Relevance Check",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Please speak only about the selected topic.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selected Topic: \"${topic.title}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Do not give a score just because English grammar is correct. You must speak directly about the selected topic to generate your final result and score.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (offTopic?.grammarCorrections?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Grammar feedback on your speech:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (corr in offTopic.grammarCorrections.take(2)) {
                                    Text(
                                        text = "• Original: \"${corr.originalSentence}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "  Correction: \"${corr.correctedSentence}\"",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        practiceViewModel.dismissOffTopicNotice()
                        practiceViewModel.resumeSpeaking()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("continue_topic_practice_button")
                ) {
                    Text("Speak on Topic", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        practiceViewModel.dismissOffTopicNotice()
                    },
                    modifier = Modifier.testTag("dismiss_off_topic_button")
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Full Session Report Dialog
    if (showReportDialog && completedReport != null) {
        SpeakingSessionReportDialog(
            session = completedReport!!,
            dailyGoalMinutes = dailyGoalMinutes,
            onContinueDayChallenge = onPrepareNextDayChallenge,
            onDismiss = {
                practiceViewModel.dismissReportDialog()
                onSessionFinished()
            }
        )
    }

    // Manual input fallback dialog
    if (showTextInputDialog) {
        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text(
                    text = "Add Spoken Sentence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Type text to add into your transcript:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Hello, my name is Suja.") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        practiceViewModel.appendManualText(manualInputText)
                        manualInputText = ""
                        showTextInputDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("speaking_practice_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (dayNumber != null) "Day $dayNumber Challenge: ${topic.title}" else "Topic: ${topic.title}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = if (dayNumber != null) "Daily Progressive Speaking Challenge" else "Structured Speaking Practice",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isSessionIncomplete) {
                                showExitWarningDialog = true
                            } else {
                                practiceViewModel.pauseSpeaking()
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (dayNumber != null) "Day $dayNumber" else topic.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // 1. Topic Guide & Talking Points / Day-wise Questions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (dayNumber != null) "Day $dayNumber Speaking Tasks" else "Speaking Prompts & Questions",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (topic.sampleQuestions.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${currentQuestionIndex + 1}/${topic.sampleQuestions.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = topic.starterPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (topic.sampleQuestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Active focus question highlight card
                            val activeQuestion = topic.sampleQuestions.getOrNull(currentQuestionIndex) ?: topic.sampleQuestions.first()
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Current Question (${currentQuestionIndex + 1} of ${topic.sampleQuestions.size}):",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        // "தமிழில் அர்த்தம்" / "Tamil Meaning" Toggle Button
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (showTamilMeaningForQuestion) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    showTamilMeaningForQuestion = !showTamilMeaningForQuestion
                                                }
                                                .testTag("toggle_tamil_meaning_button")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Info,
                                                    contentDescription = "Tamil Meaning",
                                                    tint = if (showTamilMeaningForQuestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (showTamilMeaningForQuestion) "தமிழில் அர்த்தம் ✓" else "தமிழில் அர்த்தம்",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = if (showTamilMeaningForQuestion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"$activeQuestion\"",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    // Tamil Meaning container (appears only when user requests it)
                                    if (showTamilMeaningForQuestion) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val tamilMeaning = com.example.util.TamilQuestionTranslator.getTamilMeaning(activeQuestion)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("tamil_meaning_display")
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "தமிழில் அர்த்தம் (Tamil Meaning):",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )

                                                    val isSpeakingThis = currentlySpeakingTamil == tamilMeaning
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isSpeakingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            if (isSpeakingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                                        ),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                tamilTtsHelper.speakTamil(tamilMeaning)
                                                            }
                                                            .testTag("read_aloud_tamil_button")
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                                contentDescription = "Read Tamil Aloud",
                                                                tint = if (isSpeakingThis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = if (isSpeakingThis) "🔊 படிக்கும்..." else "🔊 கேட்க",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                ),
                                                                color = if (isSpeakingThis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tamilMeaning,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        lineHeight = 18.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "💡 Please answer and speak in English.",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (topic.sampleQuestions.size > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { practiceViewModel.previousQuestion() },
                                        enabled = currentQuestionIndex > 0
                                    ) {
                                        Text("← Previous Task")
                                    }
                                    TextButton(
                                        onClick = { practiceViewModel.nextQuestion() },
                                        enabled = currentQuestionIndex < topic.sampleQuestions.size - 1
                                    ) {
                                        Text("Next Task →")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Large Microphone & Speaking Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Target Duration Selector (2 min minimum/default, up to 60 min)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Duration:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                softWrap = false,
                                maxLines = 1,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(2, 5, 10, 15, 30, 45, 60).forEach { mins ->
                                    val isSelected = targetDurationMinutes == mins
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(enabled = !isRecording || isPaused) {
                                                practiceViewModel.setTargetDurationMinutes(mins)
                                            }
                                            .testTag("duration_option_${mins}m")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (mins == 60) "60m" else "${mins}m",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                softWrap = false,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Live Speaking Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${formatTimerDisplay(actualSeconds)} / ${formatTimerDisplay(targetDurationMinutes * 60L)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("speaking_timer_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Animated Microphone Button
                        SpeakingMicButton(
                            isRecording = isRecording,
                            isPaused = isPaused,
                            speechState = speechState,
                            speechRmsDb = speechRmsDb,
                            onClick = { handleMicAction() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status text
                        val statusText = when {
                            isAnalyzing -> "Analyzing speech session..."
                            isRecording && !isPaused -> "Listening... Speak continuously"
                            isRecording && isPaused -> "Session Paused. Tap Resume to continue"
                            else -> "Tap Microphone to Start Speaking"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = when {
                                isRecording && !isPaused -> SuccessGreen
                                isRecording && isPaused -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center
                        )

                        if (isRecording && !isPaused) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AudioWaveform(
                                isListening = true,
                                rmsDb = speechRmsDb,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Pause / Resume / Finish Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRecording) {
                                if (!isPaused) {
                                    OutlinedButton(
                                        onClick = { practiceViewModel.pauseSpeaking() },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("pause_speaking_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Pause,
                                            contentDescription = "Pause",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pause")
                                    }
                                } else {
                                    Button(
                                        onClick = { practiceViewModel.resumeSpeaking() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SuccessGreen
                                        ),
                                        modifier = Modifier.testTag("resume_speaking_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = "Resume",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Resume")
                                    }
                                }

                                Button(
                                    onClick = { practiceViewModel.requestFinishConfirmation() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("finish_session_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Stop,
                                        contentDescription = "Finish",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Finish Session")
                                }
                            } else {
                                if (transcript.isNotBlank()) {
                                    Button(
                                        onClick = { practiceViewModel.requestFinishConfirmation() },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("finish_session_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Finish",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Finish & View Result")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Accurate Verbatim Live Speech Transcript
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transcript_container_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Column {
                                Text(
                                    text = "Original Speech Transcript",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Exact words captured from your microphone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showTextInputDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Add Text Manually",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(16.dp)
                        ) {
                            if (transcript.isNotBlank() || partialTranscript.isNotBlank()) {
                                Column {
                                    if (transcript.isNotBlank()) {
                                        Text(
                                            text = transcript,
                                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("verbatim_transcript_text")
                                        )
                                    }
                                    if (partialTranscript.isNotBlank()) {
                                        Text(
                                            text = "$partialTranscript...",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Tap the microphone above and speak clearly about ${topic.title}. Your speech will appear here exactly as heard.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Loading Analysis Indicator
            if (isAnalyzing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Evaluating scores, grammar & vocabulary...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakingMicButton(
    isRecording: Boolean,
    isPaused: Boolean,
    speechState: SpeechState,
    speechRmsDb: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording && !isPaused) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            isRecording && !isPaused -> ErrorRed
            isRecording && isPaused -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "mic_color"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        // Outer pulsing ring
        if (isRecording && !isPaused) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.25f))
            )
        }

        Surface(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .testTag("speaking_practice_mic_button"),
            color = buttonColor,
            shape = CircleShape,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when {
                        isRecording && !isPaused -> Icons.Filled.Mic
                        isRecording && isPaused -> Icons.Filled.PlayArrow
                        else -> Icons.Filled.Mic
                    },
                    contentDescription = if (isRecording) "Pause/Resume" else "Start Speaking",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun SpeakingSessionReportDialog(
    session: PracticeSession,
    dailyGoalMinutes: Int,
    onDismiss: () -> Unit,
    onContinueDayChallenge: (() -> Unit)? = null
) {
    val practicedMinutes = ((session.durationSeconds + 59) / 60).coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("report_done_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Speaking Result",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Overall ${session.overallScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time & Daily Goal Progress Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Actual Speaking Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${session.durationSeconds} seconds ($practicedMinutes min)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Daily Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$practicedMinutes / $dailyGoalMinutes min",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Topic Relevance Alert Card
                if (!session.isTopicRelevant || !session.topicRelevanceNotice.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("topic_relevance_alert_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Topic Relevance Notice",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = session.topicRelevanceNotice ?: "Your answer was not closely related to \"${session.topicTitle}\". Please practice speaking about the selected topic to earn full credit and score.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // 5 Real Metric Scores
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreCard(
                                metric = ScoreMetric.GRAMMAR,
                                score = session.grammarScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreCard(
                                metric = ScoreMetric.VOCABULARY,
                                score = session.vocabularyScore,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreCard(
                                metric = ScoreMetric.FLUENCY,
                                score = session.fluencyScore,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreCard(
                                metric = ScoreMetric.PRONUNCIATION,
                                score = session.pronunciationScore,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        ScoreCard(
                            metric = ScoreMetric.CONFIDENCE,
                            score = session.confidenceScore,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Grammar Corrections
                if (session.grammarCorrections.isNotEmpty()) {
                    item {
                        Text(
                            text = "Grammar Corrections",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(session.grammarCorrections) { correction ->
                        GrammarCorrectionCard(correction = correction)
                    }
                }

                // Vocabulary Suggestions
                if (session.vocabularySuggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Vocabulary Suggestions",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        VocabularySuggestionChips(suggestions = session.vocabularySuggestions)
                    }
                }

                // Speaking Feedback
                if (session.feedbackSummary.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Speaking Feedback",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = session.feedbackSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (session.improvementTip.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tip: ${session.improvementTip}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

private fun formatTimerDisplay(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hrs > 0 && mins > 0 -> "$hrs hr $mins min $secs sec"
        hrs > 0 -> "$hrs hr $secs sec"
        mins > 0 -> "$mins min $secs sec"
        else -> "$secs seconds"
    }
}
