package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project.ui.components.WidgetTile
import com.example.project.ui.theme.ProjectTheme

@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}

) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }

                    Text("USER", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

                    Row {
                        IconButton(onClick = onInfoClick) {
                            Icon(Icons.Outlined.Info, contentDescription = "Info")
                        }
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile")
                        }
                    }
                }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                WidgetTile(modifier = Modifier.weight(1f), height = 140.dp, label = "Example CGM")
                WidgetTile(modifier = Modifier.weight(1f), height = 140.dp, label = "Example Insulin")
            }
            WidgetTile(modifier = Modifier.fillMaxWidth(), height = 200.dp, label = "Example (wide)")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    ProjectTheme {
        HomeScreen()
    }
}
