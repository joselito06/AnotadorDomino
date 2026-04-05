package com.jbncode.anotadordomino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jbncode.anotadordomino.ui.DominoComposeApp
import com.jbncode.anotadordomino.ui.theme.AnotadorDominoTheme
import com.jbncode.anotadordomino.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observar isDarkMode desde SettingsViewModel
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

            AnotadorDominoTheme(darkTheme = isDarkMode) {
                DominoComposeApp()
            }
        }
    }
}

