package com.jarly.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarly.ui.theme.ThemeMode

@Composable
fun JarlyRoot(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
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
                onBack = { showSettings = false },
            )
        } else {
            JarlyMain(onOpenSettings = { showSettings = true })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JarlyMain(onOpenSettings: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    Icon(Icons.Rounded.Menu, contentDescription = "Settings")
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
                    0 -> JarlyEmpty(
                        icon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
                        title = "Your list is empty",
                        line = "Drop it in the Jarly today.",
                    )
                    else -> JarlyEmpty(
                        icon = { Icon(Icons.Outlined.Kitchen, contentDescription = null) },
                        title = "Nothing in the pantry",
                        line = "Add food with an expiry date to get reminders.",
                    )
                }
            }
        }

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("List") },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("Pantry") },
            )
        }

        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Just drop it in the Jarly…") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = { input = "" },
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        }
    }
}

@Composable
private fun JarlyEmpty(
    icon: @Composable () -> Unit,
    title: String,
    line: String,
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
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                line,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
