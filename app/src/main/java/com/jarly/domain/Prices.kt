package com.jarly.domain

import java.time.LocalDate

object Prices {

    fun format(value: Double, currencyCode: String): String {
        var whole = value.toLong()
        var fracRounded = Math.round((value - whole) * 100.0).toInt()
        if (fracRounded >= 100) {
            whole += 1
            fracRounded = 0
        }
        val fracText = if (fracRounded in 1..9) "0$fracRounded" else "$fracRounded"
        return if (fracRounded > 0) "$whole.$fracText ${Currency.symbol(currencyCode)}"
        else "$whole ${Currency.symbol(currencyCode)}"
    }
}

object DateText {

    /** Locale-neutral short date: "14.11" or "14.11.2027" when the year differs. */
    fun short(epochDay: Long, todayEpochDay: Long): String {
        val date = LocalDate.ofEpochDay(epochDay)
        val today = LocalDate.ofEpochDay(todayEpochDay)
        return if (date.year == today.year) "%02d.%02d".format(
            date.monthValue,
            date.dayOfMonth,
        ) else "%02d.%02d.%04d".format(date.monthValue, date.dayOfMonth, date.year)
    }
}
