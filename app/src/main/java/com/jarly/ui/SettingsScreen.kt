package com.jarly.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.StatFs
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.jarly.JarlyApp
import com.jarly.R
import com.jarly.domain.Currencies
import com.jarly.domain.CurrencyEntry
import com.jarly.ui.theme.ThemeMode
import java.io.File
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currencyCode: String,
    onCurrencyChange: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = (context.applicationContext as JarlyApp).store
    val prefs = context.getSharedPreferences("jarly_settings", Context.MODE_PRIVATE)

    var showCurrencySearch by rememberSaveable { mutableStateOf(false) }
    var allowInternet by rememberSaveable {
        mutableStateOf(prefs.getBoolean("allow_internet", false))
    }
    var showInternetDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        runCatching {
            context.contentResolver.openOutputStream(uri!!)?.use { out ->
                val json = com.jarly.data.JsonMapper.toJson(store.snapshot())
                out.write(json.toByteArray())
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        runCatching {
            context.contentResolver.openInputStream(uri!!)?.use { input ->
                val text = input.readBytes().decodeToString()
                store.importSnapshot(text)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPaddingCompat(),
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back_content_desc))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = stringResource(R.string.section_appearance)) {
                ThemeOptionRow(
                    title = stringResource(R.string.theme_system),
                    subtitle = stringResource(R.string.theme_system_sub),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                )
                ThemeOptionRow(
                    title = stringResource(R.string.theme_light),
                    subtitle = stringResource(R.string.theme_light_sub),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                )
                ThemeOptionRow(
                    title = stringResource(R.string.theme_dark),
                    subtitle = stringResource(R.string.theme_dark_sub),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                )
            }

            SectionCard(title = stringResource(R.string.section_currency)) {
                Text(
                    text = stringResource(R.string.currency_row_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Currencies.POPULAR.take(6).forEach { entry ->
                        FilterChip(
                            selected = currencyCode == entry.code,
                            onClick = { onCurrencyChange(entry.code) },
                            label = { Text(entry.symbol) },
                        )
                    }
                }
                TextButton(
                    onClick = { showCurrencySearch = true },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                ) {
                    Icon(Icons.Rounded.CurrencyExchange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.currency_search_open))
                }
            }

            SectionCard(title = stringResource(R.string.section_internet)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.internet_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = if (allowInternet) {
                                stringResource(R.string.internet_on_sub)
                            } else {
                                stringResource(R.string.internet_off_sub)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = allowInternet,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showInternetDialog = true
                            } else {
                                allowInternet = false
                                prefs.edit().putBoolean("allow_internet", false).apply()
                            }
                        },
                    )
                }
            }

            SectionCard(title = stringResource(R.string.section_ai)) {
                Text(
                    text = stringResource(R.string.ai_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                val ram = hardwareRam(context)
                val free = freeSpaceGb(context)
                val androidText = if (Build.VERSION.SDK_INT >= 33) {
                    Build.VERSION.RELEASE
                } else {
                    Build.VERSION.SDK_INT.toString()
                }
                Text(
                    text = stringResource(R.string.ai_device_card) +
                        " · " + if (ram >= 4.0f && free >= 1.0f) {
                        stringResource(R.string.ai_all_ok)
                    } else {
                        ""
                    },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = stringResource(R.string.ai_ram) + ": " + ramGb(ram) + " GB · " +
                        stringResource(R.string.ai_free_space) + ": " + free + " GB · " +
                        stringResource(R.string.ai_android_version) + ": " + androidText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(6.dp))
                val apkMb = runCatching {
                    File(context.packageCodePath).length() / (1024.0 * 1024.0)
                }.getOrDefault(0.0)
                val apkMbText = if (apkMb < 10.0) {
                    String.format(Locale.US, "%.1f", apkMb)
                } else {
                    String.format(Locale.US, "%.0f", apkMb)
                }
                Text(
                    text = stringResource(R.string.ai_size_now_label) + ": " + apkMbText + " MB \u00B7 " +
                        stringResource(R.string.ai_total_eye_label) + ": ~2\u20133 GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.ai_brick_text) + " — " + stringResource(R.string.ai_brick_size_text) + " (soon)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = stringResource(R.string.ai_brick_read) + " — " + stringResource(R.string.ai_brick_size_read) + " (soon)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Text(
                    text = stringResource(R.string.ai_brick_eye) + " — " + stringResource(R.string.ai_brick_size_eye),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.ai_keep_control),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            SectionCard(title = stringResource(R.string.section_notifications)) {
                Text(
                    text = stringResource(R.string.notif_helper_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            runCatching {
                                context.startActivity(
                                    Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(stringResource(R.string.notif_exact) + " · " + stringResource(R.string.notif_exact_sub))
                }
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            runCatching {
                                context.startActivity(
                                    Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(stringResource(R.string.notif_allow))
                }
            }

            SectionCard(title = stringResource(R.string.section_data)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TextButton(onClick = {
                        exportLauncher.launch("jarly-backup-" + LocalDate.now() + ".json")
                    }) {
                        Text(stringResource(R.string.backup_export))
                    }
                    TextButton(onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }) {
                        Text(stringResource(R.string.backup_import))
                    }
                }
                Text(
                    text = stringResource(R.string.privacy_line),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            SectionCard(title = stringResource(R.string.about_title)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Jarly", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "v0.2.0 · just drop it in the Jarly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.machine_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showCurrencySearch) {
        CurrencySearchDialog(
            current = currencyCode,
            onPick = { code ->
                onCurrencyChange(code)
                showCurrencySearch = false
            },
            onDismiss = { showCurrencySearch = false },
        )
    }

    if (showInternetDialog) {
        AlertDialog(
            onDismissRequest = { showInternetDialog = false },
            title = { Text(stringResource(R.string.internet_dialog_title)) },
            text = { Text(stringResource(R.string.internet_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        allowInternet = true
                        prefs.edit().putBoolean("allow_internet", true).apply()
                        showInternetDialog = false
                    },
                ) { Text(stringResource(R.string.internet_allow)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showInternetDialog = false
                        runCatching {
                            context.startActivity(
                                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(android.net.Uri.fromParts("package", context.packageName, null))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                ) { Text(stringResource(R.string.internet_system_settings)) }
            },
        )
    }
}

@Composable
private fun CurrencySearchDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = Currencies.search(query)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.currency_search_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.currency_search_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(results.size, key = { results[it].code }) { index ->
                        val entry = results[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(entry.code) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = entry.symbol,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(end = 12.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = entry.country,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = entry.code,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (entry.code == current) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                    item {
                        if (results.isEmpty()) {
                            Text(
                                text = stringResource(R.string.currency_search_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}

private fun hardwareRam(context: Context): Float {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(mi)
    return mi.totalMem / (1024f * 1024f * 1024f)
}

private fun freeSpaceGb(context: Context): Long {
    val stat = StatFs(context.filesDir.path)
    return stat.availableBytes / (1024L * 1024L * 1024L)
}

private fun ramGb(ram: Float): String = if (ram < 10f) {
    String.format(Locale.US, "%.1f", ram)
} else {
    String.format(Locale.US, "%.0f", ram)
}

private fun Modifier.systemBarsPaddingCompat(): Modifier = this
