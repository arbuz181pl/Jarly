package com.jarly.data

import com.jarly.domain.UnitKind
import com.jarly.domain.Zone
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class ShoppingItem(
    val id: Long,
    val name: String,
    val qty: Int = 1,
    val unit: UnitKind = UnitKind.PIECES,
    val price: Double? = null,
    val repeatDays: Int? = null,
    val bought: Boolean = false,
    val addedEpochDay: Long = LocalDate.now().toEpochDay(),
)

data class PantryItem(
    val id: Long,
    val name: String,
    val qty: Int = 1,
    val unit: UnitKind = UnitKind.PIECES,
    val price: Double? = null,
    val zone: Zone = Zone.FRIDGE,
    val expiryEpochDay: Long? = null,
    val addedEpochDay: Long = LocalDate.now().toEpochDay(),
)

data class WasteEntry(
    val id: Long,
    val name: String,
    val price: Double?,
    val reason: String,
    val epochDay: Long,
)

data class WasteStats(val count: Int, val value: Double?)

data class Snapshot(
    val shopping: List<ShoppingItem>,
    val pantry: List<PantryItem>,
    val waste: List<WasteEntry>,
)

object JsonMapper {

    private const val SHOPPING = "shopping"
    private const val PANTRY = "pantry"
    private const val WASTE = "waste"

    fun toJson(snapshot: Snapshot): String {
        val root = JSONObject()
        root.put(SHOPPING, JSONArray().apply {
            snapshot.shopping.forEach { i ->
                put(
                    JSONObject()
                        .put("id", i.id)
                        .put("name", i.name)
                        .put("qty", i.qty)
                        .put("unit", i.unit.name)
                        .put("price", i.price?.toString() ?: JSONObject.NULL)
                        .put("repeatDays", i.repeatDays?.toString() ?: JSONObject.NULL)
                        .put("bought", i.bought)
                        .put("added", i.addedEpochDay),
                )
            }
        })
        root.put(PANTRY, JSONArray().apply {
            snapshot.pantry.forEach { i ->
                put(
                    JSONObject()
                        .put("id", i.id)
                        .put("name", i.name)
                        .put("qty", i.qty)
                        .put("unit", i.unit.name)
                        .put("price", i.price?.toString() ?: JSONObject.NULL)
                        .put("zone", i.zone.name)
                        .put("expiry", i.expiryEpochDay?.toString() ?: JSONObject.NULL)
                        .put("added", i.addedEpochDay),
                )
            }
        })
        root.put(WASTE, JSONArray().apply {
            snapshot.waste.forEach { i ->
                put(
                    JSONObject()
                        .put("id", i.id)
                        .put("name", i.name)
                        .put("price", i.price?.toString() ?: JSONObject.NULL)
                        .put("reason", i.reason)
                        .put("day", i.epochDay),
                )
            }
        })
        return root.toString(2)
    }

    fun fromJson(text: String): Snapshot {
        val root = JSONObject(text)
        val shopping = root.optJSONArray(SHOPPING) ?: JSONArray()
        val pantry = root.optJSONArray(PANTRY) ?: JSONArray()
        val waste = root.optJSONArray(WASTE) ?: JSONArray()

        val shoppingItems = (0 until shopping.length()).map { idx ->
            val o = shopping.getJSONObject(idx)
            ShoppingItem(
                id = o.getLong("id"),
                name = o.getString("name"),
                qty = o.optInt("qty", 1),
                unit = enumValueOrDefault(o.optString("unit"), UnitKind.PIECES),
                price = o.optString("price").toDoubleOrNull(),
                repeatDays = o.optString("repeatDays").toIntOrNull(),
                bought = o.optBoolean("bought", false),
                addedEpochDay = o.optLong("added", LocalDate.now().toEpochDay()),
            )
        }
        val pantryItems = (0 until pantry.length()).map { idx ->
            val o = pantry.getJSONObject(idx)
            PantryItem(
                id = o.getLong("id"),
                name = o.getString("name"),
                qty = o.optInt("qty", 1),
                unit = enumValueOrDefault(o.optString("unit"), UnitKind.PIECES),
                price = o.optString("price").toDoubleOrNull(),
                zone = enumValueOrDefault(o.optString("zone"), Zone.FRIDGE),
                expiryEpochDay = o.optString("expiry").toLongOrNull(),
                addedEpochDay = o.optLong("added", LocalDate.now().toEpochDay()),
            )
        }
        val wasteEntries = (0 until waste.length()).map { idx ->
            val o = waste.getJSONObject(idx)
            WasteEntry(
                id = o.getLong("id"),
                name = o.getString("name"),
                price = o.optString("price").toDoubleOrNull(),
                reason = o.optString("reason", "THROWN_AWAY"),
                epochDay = o.optLong("day", 0L),
            )
        }
        return Snapshot(shoppingItems, pantryItems, wasteEntries)
    }
}

inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T =
    try {
        enumValueOf<T>(name)
    } catch (_: IllegalArgumentException) {
        default
    }
