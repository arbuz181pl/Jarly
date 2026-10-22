package com.jarly.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jarly.R
import com.jarly.data.PantryItem
import com.jarly.data.ShoppingItem
import com.jarly.domain.DateText
import com.jarly.domain.UnitKind
import com.jarly.domain.Zone
import com.jarly.ui.main.zoneName
import java.time.LocalDate

private val Units = listOf(UnitKind.PIECES, UnitKind.GRAMS, UnitKind.KILOGRAMS, UnitKind.MILLILITRES, UnitKind.LITRES, UnitKind.PACKS)

@Composable
fun ListEditDialog(
    initial: ShoppingItem?,
    onSave: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var qty by rememberSaveable { mutableStateOf((initial?.qty ?: 1).toString()) }
    var unit by rememberSaveable { mutableStateOf(initial?.unit ?: UnitKind.PIECES) }
    var price by rememberSaveable { mutableStateOf(initial?.price?.let { Prices.raw(it) } ?: "") }
    var repeat by rememberSaveable { mutableStateOf(initial?.repeatDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(initial?.name ?: stringResource(R.string.add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_item_name)) },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text(stringResource(R.string.dialog_quantity)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f),
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.take(10) },
                        label = { Text(stringResource(R.string.dialog_price_optional)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u.suffix) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = repeat == null,
                        onClick = { repeat = null },
                        label = { Text(stringResource(R.string.repeat_none)) },
                    )
                    FilterChip(
                        selected = repeat == 7,
                        onClick = { repeat = 7 },
                        label = { Text(stringResource(R.string.repeat_weekly)) },
                    )
                    FilterChip(
                        selected = repeat == 14,
                        onClick = { repeat = 14 },
                        label = { Text(stringResource(R.string.repeat_biweekly)) },
                    )
                    FilterChip(
                        selected = repeat == 30,
                        onClick = { repeat = 30 },
                        label = { Text(stringResource(R.string.repeat_monthly)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val created = ShoppingItem(
                        id = initial?.id ?: 0L,
                        name = name.ifBlank { return@TextButton },
                        qty = qty.toIntOrNull() ?: 1,
                        unit = unit,
                        price = price.toDoubleOrNull(),
                        repeatDays = repeat,
                        bought = initial?.bought ?: false,
                        addedEpochDay = initial?.addedEpochDay ?: LocalDate.now().toEpochDay(),
                    )
                    onSave(created)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            if (initial != null) {
                TextButton(onClick = {
                    onDelete(initial)
                    onDismiss()
                }) {
                    Text(stringResource(R.string.delete_content_desc))
                }
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryEditDialog(
    initial: PantryItem?,
    onSave: (PantryItem) -> Unit,
    onRemove: (PantryItem, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var qty by rememberSaveable { mutableStateOf((initial?.qty ?: 1).toString()) }
    var unit by rememberSaveable { mutableStateOf(initial?.unit ?: UnitKind.PIECES) }
    var zone by rememberSaveable { mutableStateOf(initial?.zone ?: Zone.FRIDGE) }
    var price by rememberSaveable { mutableStateOf(initial?.price?.let { Prices.raw(it) } ?: "") }
    var expiry by rememberSaveable { mutableStateOf(initial?.expiryEpochDay) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(initial?.name?.takeIf { it.isNotBlank() } ?: stringResource(R.string.add_pantry_item)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.dialog_item_name)) },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text(stringResource(R.string.dialog_quantity)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.5f),
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.take(10) },
                        label = { Text(stringResource(R.string.dialog_price_optional)) },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Units.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u.suffix) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_zone),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    FilterChip(selected = zone == Zone.FRIDGE, onClick = { zone = Zone.FRIDGE }, label = { Text(zoneName(Zone.FRIDGE)) })
                    FilterChip(selected = zone == Zone.FREEZER, onClick = { zone = Zone.FREEZER }, label = { Text(zoneName(Zone.FREEZER)) })
                    FilterChip(selected = zone == Zone.CUPBOARD, onClick = { zone = Zone.CUPBOARD }, label = { Text(zoneName(Zone.CUPBOARD)) })
                }
                TextButton(onClick = { showDatePicker = true }) {
                    Text(
                        text = stringResource(R.string.dialog_expiry) + ": " +
                            (expiry?.let { d -> DateText.short(d, LocalDate.now().toEpochDay()) }
                                ?: stringResource(R.string.pantry_no_date)),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) return@TextButton
                    val created = PantryItem(
                        id = initial?.id ?: 0L,
                        name = trimmedName,
                        qty = qty.toIntOrNull() ?: 1,
                        unit = unit,
                        price = price.toDoubleOrNull(),
                        zone = zone,
                        expiryEpochDay = expiry,
                        addedEpochDay = initial?.addedEpochDay ?: LocalDate.now().toEpochDay(),
                    )
                    onSave(created)
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            if (initial != null) {
                TextButton(onClick = {
                    onRemove(initial, "THROWN_AWAY")
                    onDismiss()
                }) {
                    Text(stringResource(R.string.reason_thrown))
                }
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = expiry?.let { it * 24L * 60 * 60 * 1000 },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { expiry = it / 86400000L }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

object Prices {
    fun raw(value: Double): String = value.toString()
}
