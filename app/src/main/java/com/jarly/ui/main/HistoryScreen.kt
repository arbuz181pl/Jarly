package com.jarly.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jarly.R
import com.jarly.data.PantryItem
import com.jarly.data.WasteEntry
import com.jarly.domain.DateText
import com.jarly.domain.Prices
import java.time.LocalDate

private fun reasonLabel(reason: String): Int =
    if (reason.contains("THROWN", ignoreCase = true)) R.string.reason_thrown else R.string.reason_removed

@Composable
fun HistoryScreen(
    waste: List<WasteEntry>,
    currencyCode: String,
    onBuyAgain: (WasteEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (waste.isEmpty()) {
        EmptyState(
            icon = { Icon(Icons.Rounded.History, contentDescription = null) },
            title = R.string.empty_history_title,
            line = R.string.empty_history_line,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
    ) {
        items(waste, key = { it.id }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = DateText.short(entry.epochDay, LocalDate.now().toEpochDay()) +
                                " · " + stringResource(reasonLabel(entry.reason)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    entry.price?.let {
                        Text(
                            text = "≈ " + Prices.format(it, currencyCode),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onBuyAgain(entry) }) {
                        Text(stringResource(R.string.buy_again))
                    }
                }
            }
        }
    }
}
