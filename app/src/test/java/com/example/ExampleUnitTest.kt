package com.example

import com.example.notification.ScheduleNotificationManager
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testParseTimeToHourMinute_7PMParsing() {
        val (hour, minute) = ScheduleNotificationManager.parseTimeToHourMinute("07:00 PM")
        assertEquals(19, hour)
        assertEquals(0, minute)
    }

    @Test
    fun testParseTimeToHourMinute_12AMParsing() {
        val (hour, minute) = ScheduleNotificationManager.parseTimeToHourMinute("12:00 AM")
        assertEquals(0, hour)
        assertEquals(0, minute)
    }

    @Test
    fun testParseTimeToHourMinute_12PMParsing() {
        val (hour, minute) = ScheduleNotificationManager.parseTimeToHourMinute("12:30 PM")
        assertEquals(12, hour)
        assertEquals(30, minute)
    }

    @Test
    fun testParseTimeToHourMinute_830AMParsing() {
        val (hour, minute) = ScheduleNotificationManager.parseTimeToHourMinute("08:30 AM")
        assertEquals(8, hour)
        assertEquals(30, minute)
    }

    @Test
    fun testUniqueDayCounting_DoesNotExceedMaxDenominator() {
        val scheduledDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val completedDayNames = setOf("Monday", "Tuesday") // multiple sessions on Monday/Tuesday still produce 2 items in set
        val denominator = scheduledDays.size.coerceIn(1, 7)
        val count = scheduledDays.count { scheduledDay ->
            completedDayNames.any { it.equals(scheduledDay, ignoreCase = true) }
        }.coerceIn(0, denominator)

        assertEquals(7, denominator)
        assertEquals(2, count)
    }
}
