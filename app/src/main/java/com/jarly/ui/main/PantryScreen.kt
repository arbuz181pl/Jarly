package com.jarly.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jarly.R
import com.jarly.data.PantryItem
import com.jarly.data.WasteEntry
import com.jarly.data.WasteStats
import com.jarly.domain.DateText
import com.jarly.domain.Freshness
import com.jarly.domain.FreshLogic
import com.jarly.domain.Prices
import com.jarly.domain.Zone
import java.time.LocalDate

@Composable
fun zoneColor(zone: Zone): Color = when (zone) {
    Zone.FRIDGE -> Color(0xFF6BA3E7)
    Zone.FREEZER -> Color(0xFF92B6DE)
    Zone.CUPBOARD -> Color(0xFFD9A441)
}

@Composable
fun freshColor(f: Freshness): Color = when (f) {
    Freshness.EXPIRED -> MaterialTheme.colorScheme.error
    Freshness.TODAY -> MaterialTheme.colorScheme.tertiary
    Freshness.URGENT -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
    Freshness.THIS_WEEK -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
}

@Composable
fun freshLabel(f: Freshness, expiryEpochDay: Long?): String {
    if (expiryEpochDay == null) return stringResource(R.string.pantry_no_date)
    return when (f) {
        Freshness.EXPIRED -> stringResource(R.string.fresh_expired)
        Freshness.TODAY -> stringResource(R.string.fresh_today)
        Freshness.URGENT, Freshness.THIS_WEEK ->
            (expiryEpochDay - LocalDate.now().toEpochDay()).toString() + " " + stringResource(R.string.fresh_days)
        Freshness.LATER -> DateText.short(expiryEpochDay, LocalDate.now().toEpochDay())
        Freshness.NO_DATE -> stringResource(R.string.pantry_no_date)
    }
}

object WasteStatsHelper {
    fun monthly(waste: List<WasteEntry>, todayEpochDay: Long): WasteStats {
        val todayDate = LocalDate.ofEpochDay(todayEpochDay)
        val thisMonth = waste.filter {
            val d = LocalDate.ofEpochDay(it.epochDay)
            d.year == todayDate.year && d.monthValue == todayDate.monthValue
        }
        val priced = thisMonth.mapNotNull { it.price }
        val value = if (priced.isEmpty()) null else priced.sum()
        return WasteStats(count = thisMonth.size, value = value)
    }
}

@Composable
fun PantryScreen(
    items: List<PantryItem>,
    waste: List<WasteEntry>,
    currencyCode: String,
    onEdit: (PantryItem) -> Unit,
    onRemove: (PantryItem, String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        Box(modifier = modifier.fillMaxSize()) {
            EmptyState(
                icon = { Icon(Icons.Outlined.Kitchen, contentDescription = null) },
                title = R.string.empty_pantry_title,
                line = R.string.empty_pantry_line,
            )
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_pantry_item))
            }
        }
        return
    }

    val today = LocalDate.now()
    val stats = WasteStatsHelper.monthly(waste, today.toEpochDay())
    val groups: List<Pair<String, List<PantryItem>>> = listOf(
        stringResource(R.string.fresh_expired) + ":" to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.EXPIRED },
        stringResource(R.string.fresh_today) + ":" to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.TODAY },
        "≤ 3 " + stringResource(R.string.fresh_days) + ":" to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.URGENT },
        "≤ 7 " + stringResource(R.string.fresh_days) + ":" to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.THIS_WEEK },
        stringResource(R.string.later) + ":" to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.LATER },
        stringResource(R.string.pantry_no_date) + " — " + stringResource(R.string.dialog_expiry) to items.filter { FreshLogic.of(it.expiryEpochDay, today) == Freshness.NO_DATE },
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
    ) {
        item { WasteCard(stats, currencyCode) }
        groups.forEach { (title, list) ->
            if (list.isNotEmpty()) {
                item(key = "header-" + title) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                items(list, key = { it.id }) { item ->
                    PantryRow(item = item, currencyCode = currencyCode, onEdit = onEdit, onRemove = onRemove)
                }
            }
        }
    }
}

@Composable
private fun WasteCard(stats: WasteStats, currencyCode: String) {
    if (stats.count <= 0) return
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(
                    R.string.waste_month,
                    stats.count,
                    stats.value?.let { Prices.format(it, currencyCode) } ?: "0",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PantryRow(
    item: PantryItem,
    currencyCode: String,
    onEdit: (PantryItem) -> Unit,
    onRemove: (PantryItem, String) -> Unit,
) {
    val freshness = FreshLogic.of(item.expiryEpochDay, LocalDate.now())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(item) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(freshColor(freshness), CircleShape),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.qty.toString() + " " + item.unit.suffix +
                        " · " + zoneName(item.zone) +
                        " · " + freshLabel(freshness, item.expiryEpochDay),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.price?.let { p ->
                Text(
                    text = "≈ " + Prices.format(p, currencyCode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_content_desc))
            }
            IconButton(onClick = { onRemove(item, "REMOVED") }) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_content_desc))
            }
        }
    }
}

@Composable
fun zoneName(zone: Zone): String = when (zone) {
    Zone.FRIDGE -> stringResource(R.string.zone_fridge)
    Zone.FREEZER -> stringResource(R.string.zone_freezer)
    Zone.CUPBOARD -> stringResource(R.string.zone_cupboard)
}
