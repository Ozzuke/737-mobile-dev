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
        setContent {
            ProjectTheme(darkTheme = isSystemInDarkTheme(), dynamicColor = false) {
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