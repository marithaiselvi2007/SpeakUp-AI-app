package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.PracticeDaysProgress
import java.util.Calendar
import java.util.Locale

@Composable
fun PracticeDaysHeaderCard(
    progress: PracticeDaysProgress,
    onEditDaysClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val total = progress.totalSelectedDays
    val completed = progress.completedCount.coerceIn(0, total)
    val progressFraction = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
    val isAllCompleted = total > 0 && completed >= total

    val todayCalendar = Calendar.getInstance(Locale.ENGLISH)
    val todayName = when (todayCalendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        Calendar.SUNDAY -> "Sunday"
        else -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_days_header_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title and Counter Badge (e.g., "1/3 Days")
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
                            .background(
                                if (isAllCompleted) SuccessGreen.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAllCompleted) Icons.Default.EmojiEvents else Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (isAllCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Practice Schedule",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                isAllCompleted -> "Weekly goal achieved! 🎉"
                                total == 0 -> "No practice days selected"
                                else -> "$completed of $total days completed this week"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isAllCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Real completion counter: 0/3 → 1/3 → 2/3 → 3/3 (or 0/7 → 7/7, 0/0)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAllCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("practice_days_counter_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (total == 0) "0/0 Days" else "$completed/$total Days",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllCompleted) SuccessGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Weekly Achievement Banner when all selected days are completed
            if (isAllCompleted) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SuccessGreen.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_goal_completed_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "1 Week Completed! 🎉",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )
                            Text(
                                text = "All required practice days completed for this week!",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar row showing Mon - Sun:
            // Selected days display with ✓, Completed days display with completion ✓ in SuccessGreen, Unselected days without ✓
            val allWeekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(allWeekDays) { dayName ->
                    val shortDayLabel = when (dayName) {
                        "Monday" -> "Mon"
                        "Tuesday" -> "Tue"
                        "Wednesday" -> "Wed"
                        "Thursday" -> "Thu"
                        "Friday" -> "Fri"
                        "Saturday" -> "Sat"
                        else -> "Sun"
                    }
                    val isDaySelected = progress.selectedDaysList.any {
                        it.equals(dayName, ignoreCase = true) || it.take(3).equals(shortDayLabel, ignoreCase = true)
                    }
                    val isDayDone = isDaySelected && progress.completedDaysList.any {
                        it.equals(dayName, ignoreCase = true) || it.take(3).equals(shortDayLabel, ignoreCase = true)
                    }
                    val isToday = dayName.equals(todayName, ignoreCase = true)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isDayDone -> SuccessGreen.copy(alpha = 0.15f)
                            isDaySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isDayDone || isDaySelected || isToday) 1.5.dp else 1.dp,
                            color = when {
                                isDayDone -> SuccessGreen
                                isDaySelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.outline
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.testTag("day_chip_${shortDayLabel.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDayDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            } else if (isDaySelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Scheduled",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            } else if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Column {
                                Text(
                                    text = if (isDayDone || isDaySelected) "$shortDayLabel ✓" else shortDayLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isDayDone || isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = when {
                                        isDayDone -> SuccessGreen
                                        isDaySelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    }
                                )
                                if (isToday) {
                                    Text(
                                        text = if (isDayDone) "Done" else "Today",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isDayDone) SuccessGreen else MaterialTheme.colorScheme.primary
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
