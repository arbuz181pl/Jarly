package com.jarly.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JsonRoundtripTest {

    @Test
    fun roundtripKeepsEverything() {
        val snapshot = Snapshot(
            shopping = listOf(
                ShoppingItem(id = 1, name = "Milk", qty = 2, unit = com.jarly.domain.UnitKind.LITRES, price = 3.5),
                ShoppingItem(id = 2, name = "Bread", repeatDays = 7),
            ),
            pantry = listOf(
                PantryItem(
                    id = 5,
                    name = "Yogurt",
                    zone = com.jarly.domain.Zone.FRIDGE,
                    expiryEpochDay = LocalDate.of(2026, 7, 1).toEpochDay(),
                ),
                PantryItem(id = 6, name = "Rice", zone = com.jarly.domain.Zone.CUPBOARD),
            ),
            waste = listOf(
                WasteEntry(id = 9, name = "Old egg", price = 0.2, reason = "EXPIRED", epochDay = 20600),
            ),
        )

        val restored = JsonMapper.fromJson(JsonMapper.toJson(snapshot))

        assertEquals(
            snapshot.shopping.map { it.copy() },
            restored.shopping,
        )
        assertEquals(snapshot.pantry, restored.pantry)
        assertEquals(snapshot.waste, restored.waste)
        assertTrue(restored.shopping.first().repeatDays == null)
    }
}
