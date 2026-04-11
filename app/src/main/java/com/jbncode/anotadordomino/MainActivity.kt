package com.jbncode.anotadordomino

import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.ui.DominoComposeApp
import com.jbncode.anotadordomino.ui.theme.AnotadorDominoTheme
import com.jbncode.anotadordomino.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observar isDarkMode desde SettingsViewModel
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
            val appLanguage by settingsViewModel.appLanguage.collectAsStateWithLifecycle()

            val context = LocalContext.current

            val (configuration, contextWrapper) = remember(appLanguage, context) {
                val locale = Locale(appLanguage)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)

                val newContext = context.createConfigurationContext(config)

                // LA SOLUCIÓN: Un ContextWrapper que Hilt puede "desenvolver" para
                // encontrar la Actividad, pero que le da a Compose los recursos traducidos.
                val wrapper = object : ContextWrapper(context) {
                    override fun getResources() = newContext.resources
                }

                Pair(config, wrapper)
            }

            CompositionLocalProvider(
                LocalConfiguration provides configuration,
                LocalContext provides contextWrapper
            ) {
                AnotadorDominoTheme(darkTheme = isDarkMode) {
                    DominoComposeApp()
                }
            }
        }
    }
}

