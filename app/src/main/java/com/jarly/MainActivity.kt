package com.jarly

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jarly.ui.JarlyRoot
import com.jarly.ui.theme.JarlyTheme
import com.jarly.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val initial = ThemeMode.valueOf(
            prefs.getString(KEY_THEME, null) ?: ThemeMode.SYSTEM.name
        )
        super.onCreate(savedInstanceState)
        setContent {
            var mode by remember { mutableStateOf(initial) }
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
                        themeMode = mode,
                        onThemeModeChange = { newMode ->
                            prefs.edit().putString(KEY_THEME, newMode.name).apply()
                            mode = newMode
                        },
                    )
                }
            }
        }
    }

    private companion object {
        const val PREFS_NAME = "jarly_settings"
        const val KEY_THEME = "theme_mode"
    }
}
