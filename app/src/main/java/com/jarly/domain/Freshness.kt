package com.jarly.domain

import java.time.LocalDate

enum class Freshness { EXPIRED, TODAY, URGENT, THIS_WEEK, LATER, NO_DATE }

object FreshLogic {

    fun of(expiryEpochDay: Long?, today: LocalDate): Freshness {
        if (expiryEpochDay == null) return Freshness.NO_DATE
        val days = expiryEpochDay - today.toEpochDay()
        return when {
            days < 0 -> Freshness.EXPIRED
            days == 0L -> Freshness.TODAY
            days <= 3 -> Freshness.URGENT
            days <= 7 -> Freshness.THIS_WEEK
            else -> Freshness.LATER
        }
    }
}
