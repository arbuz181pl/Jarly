package com.jarly

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.jarly.data.JarlyStore
import com.jarly.ui.JarlyRoot
import com.jarly.ui.theme.JarlyTheme
import com.jarly.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val app = application as JarlyApp
        val initialTheme = ThemeMode.valueOf(
            prefs.getString(KEY_THEME, null) ?: ThemeMode.SYSTEM.name
        )
        val initialCurrency = prefs.getString(KEY_CURRENCY, null) ?: "PLN"
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        setContent {
            var mode by remember { mutableStateOf(initialTheme) }
            var currency by remember { mutableStateOf(initialCurrency) }
            val systemDark = isSystemInDarkTheme()
            JarlyTheme(
                darkTheme = when (mode) {
                    ThemeMode.SYSTEM -> systemDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                },
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    JarlyRoot(
                        store = app.store,
                        currencyCode = currency,
                        themeMode = mode,
                        onThemeModeChange = { newMode ->
                            prefs.edit().putString(KEY_THEME, newMode.name).apply()
                            mode = newMode
                        },
                        onCurrencyChange = { newCurrency ->
                            prefs.edit().putString(KEY_CURRENCY, newCurrency).apply()
                            currency = newCurrency
                        },
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            registerLauncher().launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun registerLauncher() =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private companion object {
        const val PREFS_NAME = "jarly_settings"
        const val KEY_THEME = "theme_mode"
        const val KEY_CURRENCY = "currency_code"
    }
}
