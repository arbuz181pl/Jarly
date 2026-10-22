package com.jarly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jarly.R
import com.jarly.data.JarlyStore
import com.jarly.data.PantryItem
import com.jarly.data.ShoppingItem
import com.jarly.ui.dialogs.ListEditDialog
import com.jarly.ui.dialogs.PantryEditDialog
import com.jarly.ui.main.HistoryScreen
import com.jarly.ui.main.ListScreen
import com.jarly.ui.main.PantryScreen
import com.jarly.ui.theme.ThemeMode

@Composable
fun JarlyRoot(
    store: JarlyStore,
    currencyCode: String,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = showSettings,
        label = "screen",
        transitionSpec = {
            if (targetState) {
                (slideInVertically(tween(320)) { it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutVertically(tween(280)) { it / 3 } + fadeOut(tween(180)))
            } else {
                (slideInVertically(tween(320)) { -it / 3 } + fadeIn(tween(220))) togetherWith
                    (slideOutVertically(tween(280)) { -it / 3 } + fadeOut(tween(180)))
            }
        },
    ) { settings ->
        if (settings) {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                currencyCode = currencyCode,
                onCurrencyChange = onCurrencyChange,
                onBack = { showSettings = false },
            )
        } else {
            MainScreen(store = store, currencyCode = currencyCode, onOpenSettings = { showSettings = true })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(store: JarlyStore, currencyCode: String, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var editList by remember { mutableStateOf<ShoppingItem?>(null) }
    var editPantry by remember { mutableStateOf<PantryItem?>(null) }
    var draftPantry by remember { mutableStateOf<PantryItem?>(null) }

    val shopping by store.shopping.collectAsState()
    val pantry by store.pantry.collectAsState()
    val waste by store.waste.collectAsState()

    fun typedToItem() {
        val text = input.trim()
        if (text.isNotEmpty()) {
            store.addShopping(text)
            input = ""
            tab = 0
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Jarly",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Rounded.Menu, contentDescription = stringResource(R.string.settings_content_desc))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = tab,
                label = "tab",
                transitionSpec = {
                    (fadeIn(tween(220)) + slideInHorizontally(tween(320)) { it / 6 }) togetherWith
                        (fadeOut(tween(160)) + slideOutHorizontally(tween(280)) { -it / 6 })
                },
            ) { current ->
                when (current) {
                    0 -> ListScreen(
                        items = shopping,
                        currencyCode = currencyCode,
                        onToggle = { store.toggleShopping(it.id) },
                        onEdit = { editList = it },
                        onDelete = { store.deleteShopping(it.id) },
                        onToPantry = { s ->
                            draftPantry = PantryItem(
                                id = s.id,
                                name = s.name,
                                qty = s.qty,
                                unit = s.unit,
                                price = s.price,
                            )
                        },
                    )
                    1 -> PantryScreen(
                        items = pantry,
                        waste = waste,
                        currencyCode = currencyCode,
                        onEdit = { editPantry = it },
                        onRemove = { item, reason -> store.removePantry(item.id, reason) },
                        onAdd = {
                            draftPantry = PantryItem(
                                id = 0,
                                name = "",
                                qty = 1,
                            )
                        },
                    )
                    else -> HistoryScreen(
                        waste = waste,
                        currencyCode = currencyCode,
                        onBuyAgain = { store.addShopping(it.name) },
                    )
                }
            }
        }

        PrimaryTabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.tab_list)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.tab_pantry)) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.tab_history)) })
        }

        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val glow = rememberInfiniteTransition(label = "glow")
                val spin by glow.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        tween(9000, easing = LinearEasing),
                    ),
                    label = "spin",
                )
                val breathe by glow.animateFloat(
                    initialValue = 0.55f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        tween(1500),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "breathe",
                )
                val rainbow = listOf(
                    Color(0xFFF25767),
                    Color(0xFFF5893B),
                    Color(0xFFF3C64F),
                    Color(0xFF46C467),
                    Color(0xFF3CC2B9),
                    Color(0xFF4D8BF0),
                    Color(0xFF9C6BEF),
                )
                fun ringStops(phase: Float): List<Pair<Float, Color>> {
                    val n = rainbow.size
                    val seg = n * ((phase / 360f).mod(1f))
                    val s0 = seg.toInt().mod(n)
                    val t = seg - seg.toInt()
                    val out = mutableListOf(0f to rainbow[s0])
                    for (k in 1 until n) {
                        val f = (k - t) / n
                        if (f > 0f && f < 1f) out += f to rainbow[(s0 + k).mod(n)]
                    }
                    out += 1f to rainbow[s0]
                    return out
                }
                Box(modifier = Modifier.weight(1f).padding(10.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                val insetX = 4.dp.toPx()
                                val insetTop = 3.dp.toPx()
                                val topLeft = Offset(insetX, insetTop)
                                val ringSize = Size(size.width - 2 * insetX, size.height - insetTop)
                                val radius = CornerRadius(30.dp.toPx())
                                val brush = Brush.sweepGradient(
                                    colorStops = ringStops(spin).toTypedArray(),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                )
                                val wGlow = (4.dp + 5.dp * breathe).toPx()
                                drawRoundRect(
                                    brush = brush,
                                    topLeft = topLeft,
                                    size = ringSize,
                                    cornerRadius = radius,
                                    style = Stroke(width = wGlow),
                                    alpha = 0.28f * breathe,
                                )
                                drawRoundRect(
                                    brush = brush,
                                    topLeft = topLeft,
                                    size = ringSize,
                                    cornerRadius = radius,
                                    style = Stroke(width = wGlow * 0.55f),
                                    alpha = 0.45f * breathe,
                                )
                                drawRoundRect(
                                    brush = brush,
                                    topLeft = topLeft,
                                    size = ringSize,
                                    cornerRadius = radius,
                                    style = Stroke(width = 2.5.dp.toPx()),
                                    alpha = 0.9f,
                                )
                            },
                    )
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(stringResource(R.string.input_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { typedToItem() }),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                FloatingActionButton(
                    onClick = { typedToItem() },
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_content_desc))
                }
            }
        }
    }

    if (editList != null) {
        ListEditDialog(
            initial = editList,
            onSave = { item ->
                if (item.id != 0L && shopping.any { it.id == item.id }) {
                    store.updateShopping(item)
                } else {
                    store.addShopping(item.name, item.qty, item.unit, item.repeatDays)
                }
            },
            onDelete = { store.deleteShopping(it.id) },
            onDismiss = {
                editList = null
            },
        )
    }

    (editPantry ?: draftPantry)?.let { target ->
        val isNew = target.id == 0L || pantry.none { it.id == target.id }
        PantryEditDialog(
            initial = target,
            onSave = { item ->
                if (isNew) {
                    store.addPantry(item.name, item.qty, item.unit, item.zone, item.expiryEpochDay)
                    if (draftPantry != null) {
                        store.deleteShopping(draftPantry!!.id)
                    }
                    draftPantry = null
                } else {
                    store.updatePantry(item)
                }
                editPantry = null
                draftPantry = null
            },
            onRemove = { item, reason ->
                store.removePantry(item.id, reason)
                editPantry = null
                draftPantry = null
            },
            onDismiss = {
                editPantry = null
                draftPantry = null
            },
        )
    }
}
