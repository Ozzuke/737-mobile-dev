package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.project.ui.components.DisclaimerDialog
import com.example.project.ui.navigation.AppNav
import com.example.project.ui.theme.ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CGMApplication
        setContent {
            val darkModePreference by app.preferencesRepository.getDarkModeEnabled().collectAsState(initial = null)
            val systemPrefersDark = isSystemInDarkTheme()
            ProjectTheme(darkTheme = darkModePreference ?: systemPrefersDark, dynamicColor = false) {
                var showDisclaimer by remember { mutableStateOf(true) }

                if (showDisclaimer) {
                    DisclaimerDialog(
                        onDismiss = { showDisclaimer = false },
                        showOfflineWarning = false
                    )
                }

                AppNav()
            }
        }
    }
}