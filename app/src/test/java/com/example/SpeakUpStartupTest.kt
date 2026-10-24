package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EnglishLevel
import com.example.data.repository.TopicsData
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CoachViewModel
import com.example.viewmodel.SpeakUpViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = SpeakUpApplication::class, sdk = [34])
class SpeakUpStartupTest {

    @Test
    fun testViewModelsInitializeSafelyWithoutCrashing() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        assertNotNull(app)

        val authViewModel = AuthViewModel(app)
        assertNotNull(authViewModel)

        val speakUpViewModel = SpeakUpViewModel(app)
        assertNotNull(speakUpViewModel)

        val coachViewModel = CoachViewModel(app)
        assertNotNull(coachViewModel)

        val practiceViewModel = com.example.viewmodel.SpeakingPracticeViewModel(app)
        assertNotNull(practiceViewModel)
    }

    @Test
    fun testGuestAuthenticationFlow() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val authViewModel = AuthViewModel(app)

        authViewModel.continueAsGuest()
        assertNotNull(authViewModel.uiState.value)
    }

    @Test
    fun testTopicsListPopulated() {
        val topics = TopicsData.allTopics
        assertTrue("Topics list should not be empty", topics.isNotEmpty())
        assertTrue("Topics list should contain at least 15 topics", topics.size >= 15)
    }

    @Test
    fun testRepeatedColdStartLifecycle() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        assertNotNull(app)

        // Simulate multiple cold starts
        for (i in 1..5) {
            val authVM = AuthViewModel(app)
            val speakUpVM = SpeakUpViewModel(app)
            val coachVM = CoachViewModel(app)
            val practiceVM = com.example.viewmodel.SpeakingPracticeViewModel(app)

            assertNotNull(authVM.uiState.value)
            assertNotNull(speakUpVM.themeColor.value)
            assertNotNull(coachVM.messages.value)
            assertNotNull(practiceVM.currentTopic.value)
        }
    }

    @Test
    fun testCompleteProfileFlow() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val authViewModel = AuthViewModel(app)

        authViewModel.completeProfile(
            name = "Test User",
            englishLevel = EnglishLevel.INTERMEDIATE,
            dailyGoalMinutes = 20,
            practiceDays = listOf("Mon", "Wed", "Fri"),
            preferredPracticeTime = "08:00 AM",
            sessionDurationMinutes = 16,
            preferredTopics = listOf("Daily Routine", "Travel Experiences"),
            photoUrl = ""
        )

        assertNotNull(authViewModel.uiState.value)
    }

    @Test
    fun testCoachSessionStart() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val coachViewModel = CoachViewModel(app)

        coachViewModel.startFreeConversation(EnglishLevel.INTERMEDIATE)
        assertEquals(EnglishLevel.INTERMEDIATE, coachViewModel.userEnglishLevel.value)
        assertEquals(1, coachViewModel.messages.value.size)
    }

    @Test
    fun testPracticeDaysCounterLogic() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val speakUpViewModel = SpeakUpViewModel(app)

        // TEST 1: Select all 7 days -> Initial 0/7
        val all7Days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        speakUpViewModel.updatePracticeDays(all7Days)
        var progress = speakUpViewModel.getPracticeDaysProgress()
        assertEquals(0, progress.completedCount)
        assertEquals(7, progress.totalSelectedDays)
        assertEquals(7, progress.selectedDaysList.size)

        // TEST 6: Select only 3 days -> Initial 0/3
        val threeDays = listOf("Monday", "Wednesday", "Friday")
        speakUpViewModel.updatePracticeDays(threeDays)
        progress = speakUpViewModel.getPracticeDaysProgress()
        assertEquals(0, progress.completedCount)
        assertEquals(3, progress.totalSelectedDays)
        assertEquals(3, progress.selectedDaysList.size)

        // TEST 9: Select only 2 days -> Initial 0/2
        val twoDays = listOf("Monday", "Friday")
        speakUpViewModel.updatePracticeDays(twoDays)
        progress = speakUpViewModel.getPracticeDaysProgress()
        assertEquals(0, progress.completedCount)
        assertEquals(2, progress.totalSelectedDays)

        // TEST 10: Select only 1 day -> Initial 0/1
        val oneDay = listOf("Monday")
        speakUpViewModel.updatePracticeDays(oneDay)
        progress = speakUpViewModel.getPracticeDaysProgress()
        assertEquals(0, progress.completedCount)
        assertEquals(1, progress.totalSelectedDays)

        // TEST 11: Select zero days -> 0/0
        val noDays = emptyList<String>()
        speakUpViewModel.updatePracticeDays(noDays)
        progress = speakUpViewModel.getPracticeDaysProgress()
        assertEquals(0, progress.completedCount)
        assertEquals(0, progress.totalSelectedDays)
    }

    @Test
    fun testPracticeDaysWithSessionsAndMultipleSessionsPerDay() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val speakUpViewModel = SpeakUpViewModel(app)

        // Calculate timestamps for current week Monday, Wednesday, Friday
        val cal = java.util.Calendar.getInstance(java.util.Locale.ENGLISH).apply {
            firstDayOfWeek = java.util.Calendar.MONDAY
            set(java.util.Calendar.HOUR_OF_DAY, 10)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            val dayOfWeek = get(java.util.Calendar.DAY_OF_WEEK)
            val daysSinceMonday = if (dayOfWeek == java.util.Calendar.SUNDAY) 6 else (dayOfWeek - java.util.Calendar.MONDAY)
            add(java.util.Calendar.DAY_OF_MONTH, -daysSinceMonday)
        }
        val mondayTime = cal.timeInMillis

        cal.add(java.util.Calendar.DAY_OF_MONTH, 2) // Wednesday
        val wednesdayTime = cal.timeInMillis

        cal.add(java.util.Calendar.DAY_OF_MONTH, 2) // Friday
        val fridayTime = cal.timeInMillis

        // TEST 2 & 3: 3 sessions on Monday with Mon, Wed, Fri selected -> 1/3
        val selectedThreeDays = listOf("Monday", "Wednesday", "Friday")
        val mondaySessions = listOf(
            com.example.data.model.PracticeSession(
                userId = "test_user",
                topicTitle = "Session 1",
                startTime = mondayTime,
                endTime = mondayTime + 600000
            ),
            com.example.data.model.PracticeSession(
                userId = "test_user",
                topicTitle = "Session 2",
                startTime = mondayTime + 1000000,
                endTime = mondayTime + 1600000
            ),
            com.example.data.model.PracticeSession(
                userId = "test_user",
                topicTitle = "Session 3",
                startTime = mondayTime + 2000000,
                endTime = mondayTime + 2600000
            )
        )
        val countMon = speakUpViewModel.getCompletedDaysInCurrentWeek(selectedThreeDays, mondaySessions)
        assertEquals(1, countMon) // Monday counted only once!

        // TEST 7: Monday + Wednesday completed -> 2/3
        val wedSessions = mondaySessions + com.example.data.model.PracticeSession(
            userId = "test_user",
            topicTitle = "Wed Session",
            startTime = wednesdayTime,
            endTime = wednesdayTime + 600000
        )
        val countMonWed = speakUpViewModel.getCompletedDaysInCurrentWeek(selectedThreeDays, wedSessions)
        assertEquals(2, countMonWed)

        // TEST 8: Monday + Wednesday + Friday completed -> 3/3
        val allSessions = wedSessions + com.example.data.model.PracticeSession(
            userId = "test_user",
            topicTitle = "Fri Session",
            startTime = fridayTime,
            endTime = fridayTime + 600000
        )
        val countAll = speakUpViewModel.getCompletedDaysInCurrentWeek(selectedThreeDays, allSessions)
        assertEquals(3, countAll)

        // Schedule change: if user changes to 7 days, progress is 3/7
        val all7Days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val count7 = speakUpViewModel.getCompletedDaysInCurrentWeek(all7Days, allSessions)
        assertEquals(3, count7)

        // TEST: Previous week session exclusion (never 14/7 across weeks)
        val twoWeeksAgoTime = mondayTime - (14 * 24 * 60 * 60 * 1000L)
        val lastWeekTime = mondayTime - (7 * 24 * 60 * 60 * 1000L)
        val multiWeekSessions = allSessions + listOf(
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 1", startTime = twoWeeksAgoTime),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 2", startTime = twoWeeksAgoTime + 86400000),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 3", startTime = lastWeekTime),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 4", startTime = lastWeekTime + 86400000),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 5", startTime = lastWeekTime + 2 * 86400000),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 6", startTime = lastWeekTime + 3 * 86400000),
            com.example.data.model.PracticeSession(userId = "test_user", topicTitle = "Old 7", startTime = lastWeekTime + 4 * 86400000)
        )
        // With 12 total sessions in database across 3 weeks, current week progress must strictly remain 3/3 or 3/7
        val countMultiWeekFor3Days = speakUpViewModel.getCompletedDaysInCurrentWeek(selectedThreeDays, multiWeekSessions)
        assertEquals(3, countMultiWeekFor3Days)

        val countMultiWeekFor7Days = speakUpViewModel.getCompletedDaysInCurrentWeek(all7Days, multiWeekSessions)
        assertEquals(3, countMultiWeekFor7Days) // Only Mon, Wed, Fri of current week count!
    }

    @Test
    fun testTamilQuestionTranslator() {
        val q1 = "What is one thing you learned recently?"
        val tamil1 = com.example.util.TamilQuestionTranslator.getTamilMeaning(q1)
        assertTrue(tamil1.isNotEmpty())
        assertTrue(tamil1.contains("நீங்கள்") || tamil1.contains("கற்றுக்கொண்ட"))

        val q2 = "Introduce yourself clearly by stating your full name, where you grew up, and your family background."
        val tamil2 = com.example.util.TamilQuestionTranslator.getTamilMeaning(q2)
        assertTrue(tamil2.contains("முழுப் பெயர்") || tamil2.contains("அறிமுகப்படுத்துங்கள்"))

        val q3 = "What is your dream job or ideal career role, and what inspired you to pursue this path?"
        val tamil3 = com.example.util.TamilQuestionTranslator.getTamilMeaning(q3)
        assertTrue(tamil3.contains("கனவு வேலை") || tamil3.contains("இலட்சியப் பணி"))
    }

    @Test
    fun testScheduleNotificationManagerFollowUpAndDoneActions() {
        val app: SpeakUpApplication = ApplicationProvider.getApplicationContext()
        val prefs = app.getSharedPreferences(com.example.notification.ScheduleNotificationManager.PREFS_NAME, Context.MODE_PRIVATE)

        // Ensure clean state
        prefs.edit().clear().apply()

        // 1. User dismisses reminder when challenge incomplete -> Follow-up is scheduled
        com.example.notification.ScheduleNotificationManager.handleAlarmOrNotificationDismissed(app)
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Calendar.getInstance().time)
        val scheduledDate = prefs.getString(com.example.notification.ScheduleNotificationManager.KEY_FOLLOW_UP_SCHEDULED_DATE, "")
        assertEquals(todayStr, scheduledDate)

        // 2. User completes practice -> Follow up reminder is cancelled and practice is marked attended
        com.example.notification.ScheduleNotificationManager.markPracticeSessionAttended(app)
        val hasAttended = com.example.notification.ScheduleNotificationManager.hasUserAttendedPracticeToday(app)
        assertTrue(hasAttended)
    }
}
