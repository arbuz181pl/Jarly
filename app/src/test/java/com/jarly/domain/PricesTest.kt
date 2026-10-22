package com.jarly.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PricesTest {

    @Test
    fun wholeNumber() {
        assertEquals("14 zł", Prices.format(14.0, "PLN"))
    }

    @Test
    fun fraction() {
        assertEquals("14.50 zł", Prices.format(14.5, "PLN"))
    }

    @Test
    fun smallFraction() {
        assertEquals("3.05 zł", Prices.format(3.05, "PLN"))
    }

    @Test
    fun euroSymbol() {
        assertEquals("2 €", Prices.format(2.0, "EUR"))
    }
}
