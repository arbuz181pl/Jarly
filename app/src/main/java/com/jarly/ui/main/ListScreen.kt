package com.jarly.ui.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jarly.R
import com.jarly.data.ShoppingItem
import com.jarly.domain.Prices
import com.jarly.domain.UnitKind

@Composable
fun ListScreen(
    items: List<ShoppingItem>,
    currencyCode: String,
    onToggle: (ShoppingItem) -> Unit,
    onEdit: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onToPantry: (ShoppingItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val open = items.filterNot { it.bought }
    val bought = items.filter { it.bought }

    if (items.isEmpty()) {
        EmptyState(
            icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
            title = R.string.empty_list_title,
            line = R.string.empty_list_line,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 16.dp,
        ),
    ) {
        if (open.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.cards_count, open.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
        items(open, key = { it.id }) { item ->
            ShoppingRow(
                item = item,
                currencyCode = currencyCode,
                onToggle = onToggle,
                onEdit = onEdit,
                onDelete = onDelete,
                onToPantry = onToPantry,
            )
        }
        if (bought.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.cards_count, bought.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        items(bought, key = { it.id }) { item ->
            ShoppingRow(
                item = item,
                currencyCode = currencyCode,
                onToggle = onToggle,
                onEdit = onEdit,
                onDelete = onDelete,
                onToPantry = onToPantry,
            )
        }
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    currencyCode: String,
    onToggle: (ShoppingItem) -> Unit,
    onEdit: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onToPantry: (ShoppingItem) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.bought) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        onClick = { onEdit(item) },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.bought, onCheckedChange = { onToggle(item) })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.bought) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.qty.toString() + " " + item.unit.suffix,
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
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_content_desc))
            }
        }
        if (item.bought) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { onToPantry(item) }) {
                    Text("To pantry")
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: @Composable () -> Unit,
    title: Int,
    line: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon()
            Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(line),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
