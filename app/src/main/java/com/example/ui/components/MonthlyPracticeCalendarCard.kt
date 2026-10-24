package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Monthly Practice Calendar Component
 * Displays the current (or browsed) month with:
 * - Current month name, year, and weekday headers (Mon, Tue, Wed, Thu, Fri, Sat, Sun).
 * - Real completed Speaking Practice sessions marked with a prominent '✓'.
 * - Selected practice days indicated.
 * - Previous/Next month navigation to review completion history.
 * - Based ONLY on real completed practice data for the current Firebase UID.
 */
@Composable
fun MonthlyPracticeCalendarCard(
    completedPracticeDates: Set<String>,
    selectedPracticeDays: List<String>,
    modifier: Modifier = Modifier
) {
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var currentYear by remember { mutableIntStateOf(todayYear) }
    var currentMonth by remember { mutableIntStateOf(todayMonth) }

    val monthCalendar = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthName = remember(currentYear, currentMonth) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        sdf.format(monthCalendar.time)
    }

    val daysInMonth = remember(currentYear, currentMonth) {
        monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    // Day of week of the 1st day (Java Calendar: Sunday = 1, Monday = 2, ... Saturday = 7)
    // Convert to Monday = 0, Tuesday = 1, ... Sunday = 6
    val firstDayOfWeekOffset = remember(currentYear, currentMonth) {
        val dayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
        // Convert Sunday (1) -> 6, Monday (2) -> 0, Tuesday (3) -> 1, etc.
        (dayOfWeek + 5) % 7
    }

    val normalizedSelectedDays = remember(selectedPracticeDays) {
        selectedPracticeDays.map { normalizeDayName(it) }.toSet()
    }

    // Count how many days in this month have real completed practice sessions
    val completedThisMonthCount = remember(currentYear, currentMonth, completedPracticeDates) {
        var count = 0
        for (day in 1..daysInMonth) {
            val dateStr = String.format(Locale.ENGLISH, "%04d-%02d-%02d", currentYear, currentMonth + 1, day)
            if (completedPracticeDates.contains(dateStr)) {
                count++
            }
        }
        count
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_practice_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Month Title, Navigation Arrows, Today Shortcut
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (completedThisMonthCount > 0) "$completedThisMonthCount practice session${if (completedThisMonthCount > 1) "s" else ""} completed"
                            else "Track completed practice days",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (completedThisMonthCount > 0) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Previous month
                    IconButton(
                        onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_prev_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Return to Today button (if browsing another month)
                    if (currentYear != todayYear || currentMonth != todayMonth) {
                        IconButton(
                            onClick = {
                                currentYear = todayYear
                                currentMonth = todayMonth
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("calendar_today_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Current Month",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Next month
                    IconButton(
                        onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("calendar_next_month_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Headers: Mon, Tue, Wed, Thu, Fri, Sat, Sun
            val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val totalCells = firstDayOfWeekOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIndex in 0..6) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - firstDayOfWeekOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val dateStr = String.format(
                                    Locale.ENGLISH,
                                    "%04d-%02d-%02d",
                                    currentYear,
                                    currentMonth + 1,
                                    dayNumber
                                )
                                val isCompleted = completedPracticeDates.contains(dateStr)

                                // Day of week name for this specific date
                                val dayCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear)
                                    set(Calendar.MONTH, currentMonth)
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val dayOfWeekName = when (dayCal.get(Calendar.DAY_OF_WEEK)) {
                                    Calendar.MONDAY -> "Monday"
                                    Calendar.TUESDAY -> "Tuesday"
                                    Calendar.WEDNESDAY -> "Wednesday"
                                    Calendar.THURSDAY -> "Thursday"
                                    Calendar.FRIDAY -> "Friday"
                                    Calendar.SATURDAY -> "Saturday"
                                    Calendar.SUNDAY -> "Sunday"
                                    else -> ""
                                }
                                val isSelectedPracticeDay = normalizedSelectedDays.contains(dayOfWeekName)
                                val isToday = (currentYear == todayYear && currentMonth == todayMonth && dayNumber == todayDay)

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    isCompleted = isCompleted,
                                    isSelectedPracticeDay = isSelectedPracticeDay,
                                    isToday = isToday,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Empty cell for padding offset
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Completed session with ✓
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Completed ✓",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Scheduled practice day
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Practice Day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Today
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isCompleted: Boolean,
    isSelectedPracticeDay: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .testTag("calendar_day_$dayNumber"),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 1. REAL COMPLETED PRACTICE SESSION (Shows prominent ✓ badge)
            isCompleted -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed practice on day $dayNumber",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$dayNumber",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 10.sp
                        )
                    }
                }
            }

            // 2. TODAY (Highlight ring)
            isToday -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$dayNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isSelectedPracticeDay) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }

            // 3. SELECTED SCHEDULED PRACTICE DAY (Not completed yet)
            isSelectedPracticeDay -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$dayNumber",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                    }
                }
            }

            // 4. ORDINARY CALENDAR DAY
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNumber",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

private fun normalizeDayName(day: String): String {
    return when (day.trim().lowercase(Locale.ENGLISH)) {
        "mon", "monday" -> "Monday"
        "tue", "tues", "tuesday" -> "Tuesday"
        "wed", "wednesday" -> "Wednesday"
        "thu", "thur", "thurs", "thursday" -> "Thursday"
        "fri", "friday" -> "Friday"
        "sat", "saturday" -> "Saturday"
        "sun", "sunday" -> "Sunday"
        else -> day.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
    }
}
