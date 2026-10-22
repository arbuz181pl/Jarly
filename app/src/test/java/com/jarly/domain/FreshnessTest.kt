package com.jarly.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FreshnessTest {

    private val today = LocalDate.of(2026, 6, 15)

    @Test
    fun noDateIsNoDate() {
        assertEquals(Freshness.NO_DATE, FreshLogic.of(null, today))
    }

    @Test
    fun yesterdayIsExpired() {
        assertEquals(Freshness.EXPIRED, FreshLogic.of(today.toEpochDay() - 1, today))
    }

    @Test
    fun todayIsToday() {
        assertEquals(Freshness.TODAY, FreshLogic.of(today.toEpochDay(), today))
    }

    @Test
    fun threeDaysIsUrgent() {
        assertEquals(Freshness.URGENT, FreshLogic.of(today.toEpochDay() + 3, today))
    }

    @Test
    fun fourDaysIsThisWeek() {
        assertEquals(Freshness.THIS_WEEK, FreshLogic.of(today.toEpochDay() + 4, today))
    }

    @Test
    fun sevenDaysIsThisWeek() {
        assertEquals(Freshness.THIS_WEEK, FreshLogic.of(today.toEpochDay() + 7, today))
    }

    @Test
    fun eightDaysIsLater() {
        assertEquals(Freshness.LATER, FreshLogic.of(today.toEpochDay() + 8, today))
    }
}
