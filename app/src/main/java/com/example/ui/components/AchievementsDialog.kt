package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DashboardScores
import kotlin.math.min

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean,
    val progressFraction: Float,
    val progressLabel: String
)

@Composable
fun AchievementsDialog(
    scores: DashboardScores,
    onDismiss: () -> Unit
) {
    val achievements = listOf(
        AchievementItem(
            id = "first_step",
            title = "First Step",
            description = "Complete your first English speaking session",
            emoji = "🌟",
            isUnlocked = scores.totalSessions >= 1,
            progressFraction = min(1f, scores.totalSessions / 1f),
            progressLabel = "${min(1, scores.totalSessions)} / 1 session"
        ),
        AchievementItem(
            id = "dedicated_speaker",
            title = "Dedicated Speaker",
            description = "Complete 5 speaking practice sessions",
            emoji = "🎯",
            isUnlocked = scores.totalSessions >= 5,
            progressFraction = min(1f, scores.totalSessions / 5f),
            progressLabel = "${min(5, scores.totalSessions)} / 5 sessions"
        ),
        AchievementItem(
            id = "thirty_min_club",
            title = "30-Minute Club",
            description = "Practice for 30+ total minutes",
            emoji = "⏱️",
            isUnlocked = scores.totalPracticeMinutes >= 30,
            progressFraction = min(1f, scores.totalPracticeMinutes / 30f),
            progressLabel = "${min(30, scores.totalPracticeMinutes)} / 30 mins"
        ),
        AchievementItem(
            id = "fluency_champion",
            title = "Fluency Champion",
            description = "Achieve 80%+ Fluency score in practice",
            emoji = "🗣️",
            isUnlocked = scores.fluencyScore >= 80,
            progressFraction = min(1f, scores.fluencyScore / 80f),
            progressLabel = "${scores.fluencyScore}% / 80%"
        ),
        AchievementItem(
            id = "vocab_virtuoso",
            title = "Vocabulary Virtuoso",
            description = "Achieve 80%+ Vocabulary score",
            emoji = "📚",
            isUnlocked = scores.vocabularyScore >= 80,
            progressFraction = min(1f, scores.vocabularyScore / 80f),
            progressLabel = "${scores.vocabularyScore}% / 80%"
        ),
        AchievementItem(
            id = "grammar_guru",
            title = "Grammar Guru",
            description = "Achieve 80%+ Grammar accuracy score",
            emoji = "✍️",
            isUnlocked = scores.grammarScore >= 80,
            progressFraction = min(1f, scores.grammarScore / 80f),
            progressLabel = "${scores.grammarScore}% / 80%"
        ),
        AchievementItem(
            id = "confidence_king",
            title = "Confidence King",
            description = "Achieve 80%+ Speaking Confidence score",
            emoji = "🎙️",
            isUnlocked = scores.confidenceScore >= 80,
            progressFraction = min(1f, scores.confidenceScore / 80f),
            progressLabel = "${scores.confidenceScore}% / 80%"
        ),
        AchievementItem(
            id = "english_master",
            title = "English Master",
            description = "Achieve 90%+ Overall Speaking score",
            emoji = "🏆",
            isUnlocked = scores.overallScore >= 90,
            progressFraction = min(1f, scores.overallScore / 90f),
            progressLabel = "${scores.overallScore}% / 90%"
        )
    )

    val unlockedCount = achievements.count { it.isUnlocked }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("achievements_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$unlockedCount of ${achievements.size} Unlocked",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(achievements) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isUnlocked)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.emoji,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Unlocked",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { item.progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (item.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.progressLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_achievements")
            ) {
                Text("Close")
            }
        }
    )
}
