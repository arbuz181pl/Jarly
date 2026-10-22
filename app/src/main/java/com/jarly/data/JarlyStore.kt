package com.jarly.data

import android.content.Context
import com.jarly.domain.UnitKind
import com.jarly.domain.Zone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class JarlyStore(context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val file = File(context.filesDir, "jarly-data.json")

    private val _shopping = MutableStateFlow<List<ShoppingItem>>(emptyList())
    val shopping: StateFlow<List<ShoppingItem>> = _shopping

    private val _pantry = MutableStateFlow<List<PantryItem>>(emptyList())
    val pantry: StateFlow<List<PantryItem>> = _pantry

    private val _waste = MutableStateFlow<List<WasteEntry>>(emptyList())
    val waste: StateFlow<List<WasteEntry>> = _waste

    var onDataChanged: (() -> Unit)? = null

    init {
        scope.launch { load() }
    }

    private fun load() {
        runCatching {
            if (file.exists()) {
                val s = JsonMapper.fromJson(file.readText())
                _shopping.value = s.shopping
                _pantry.value = s.pantry
                _waste.value = s.waste
            }
        }
        onDataChanged?.invoke()
    }

    private fun persist() {
        scope.launch {
            runCatching {
                val tmp = File(file.parentFile, "jarly-data.tmp")
                tmp.writeText(
                    JsonMapper.toJson(Snapshot(_shopping.value, _pantry.value, _waste.value)),
                )
                tmp.renameTo(file)
            }
        }
    }

    private fun saveAndNotify() {
        persist()
        onDataChanged?.invoke()
    }

    private fun nextId(): Long = System.nanoTime() / 7

    fun addShopping(name: String, qty: Int = 1, unit: UnitKind = UnitKind.PIECES, repeatDays: Int? = null) {
        val trimmed = name.trim().take(80)
        if (trimmed.isEmpty()) return
        _shopping.value = _shopping.value + ShoppingItem(
            id = nextId(),
            name = trimmed,
            qty = qty,
            unit = unit,
            repeatDays = repeatDays,
        )
        saveAndNotify()
    }

    fun updateShopping(item: ShoppingItem) {
        _shopping.value = _shopping.value.map { if (it.id == item.id) item else it }
        saveAndNotify()
    }

    fun toggleShopping(id: Long) {
        val today = LocalDate.now().toEpochDay()
        _shopping.value = _shopping.value.map {
            if (it.id == id) it.copy(bought = !it.bought) else it
        }
        val changed = _shopping.value.firstOrNull { it.id == id }
        val becameUnbought = changed != null && !changed.bought
        if (!becameUnbought) {
            _waste.value = _waste.value.filterNot { it.id == id }
        }
        saveAndNotify()
    }

    fun deleteShopping(id: Long) {
        _shopping.value = _shopping.value.filterNot { it.id == id }
        saveAndNotify()
    }

    fun sendToPantry(item: ShoppingItem, expiryEpochDay: Long? = null, zone: Zone = Zone.FRIDGE) {
        _pantry.value = _pantry.value + PantryItem(
            id = nextId(),
            name = item.name,
            qty = item.qty,
            unit = item.unit,
            price = item.price,
            zone = zone,
            expiryEpochDay = expiryEpochDay,
        )
        val repeats: MutableList<ShoppingItem> = mutableListOf()
        _shopping.value = _shopping.value.mapNotNull { s ->
            if (s.id == item.id) {
                s.repeatDays?.let { days -> repeats.add(s.copy(bought = false, addedEpochDay = LocalDate.now().toEpochDay() + days)) }
                null
            } else {
                s
            }
        }
        if (repeats.isNotEmpty()) {
            _shopping.value = _shopping.value + repeats
        }
        saveAndNotify()
    }

    fun addPantry(name: String, qty: Int, unit: UnitKind, zone: Zone, expiryEpochDay: Long?) {
        val trimmed = name.trim().take(80)
        if (trimmed.isEmpty()) return
        _pantry.value = _pantry.value + PantryItem(
            id = nextId(),
            name = trimmed,
            qty = qty,
            unit = unit,
            zone = zone,
            expiryEpochDay = expiryEpochDay,
        )
        saveAndNotify()
    }

    fun updatePantry(item: PantryItem) {
        _pantry.value = _pantry.value.map { if (it.id == item.id) item else it }
        saveAndNotify()
    }

    fun removePantry(id: Long, reason: String) {
        val item = _pantry.value.firstOrNull { it.id == id } ?: return
        _waste.value = listOf(
            WasteEntry(id = nextId(), name = item.name, price = item.price, reason = reason, epochDay = LocalDate.now().toEpochDay()),
        ) + _waste.value
        _pantry.value = _pantry.value.filterNot { it.id == id }
        saveAndNotify()
    }

    fun snapshot(): Snapshot = Snapshot(_shopping.value, _pantry.value, _waste.value)

    fun importSnapshot(text: String) {
        val s = JsonMapper.fromJson(text)
        _shopping.value = s.shopping
        _pantry.value = s.pantry
        _waste.value = s.waste
        saveAndNotify()
    }

    fun readFileSync(context: Context): Snapshot = sync(context)

    companion object {
        const val FILE_NAME = "jarly-data.json"

        fun sync(context: Context): Snapshot {
            val f = File(context.filesDir, FILE_NAME)
            return if (f.exists()) {
                runCatching { JsonMapper.fromJson(f.readText()) }
                    .getOrDefault(Snapshot(emptyList(), emptyList(), emptyList()))
            } else Snapshot(emptyList(), emptyList(), emptyList())
        }
    }
}
