package com.example.project.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R
import com.example.project.ui.theme.ProjectTheme
import com.example.project.ui.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val scroll = rememberScrollState()
    val c = MaterialTheme.colorScheme
    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description), tint = c.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.primary,
                    titleContentColor = c.onPrimary,
                    navigationIconContentColor = c.onPrimary,
                    actionIconContentColor = c.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            userProfile?.let {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = c.surfaceVariant,
                        contentColor = c.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = stringResource(id = R.string.user_avatar_description),
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            color = c.onSurface,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = it.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.onSurfaceVariant
                        )
                    }
                }
            }

            SectionCard(title = stringResource(id = R.string.personal_details_title)) {
                KeyValueRow(stringResource(id = R.string.dob), stringResource(id = R.string.dob_value))
                KeyValueRow(stringResource(id = R.string.phone), stringResource(id = R.string.phone_value))
            }

            SectionCard(title = stringResource(id = R.string.cgm_device_title)) {
                KeyValueRow(stringResource(id = R.string.cgm_sensor), stringResource(id = R.string.cgm_sensor_value))
            }

            SectionCard(title = stringResource(id = R.string.app_preferences_title)) {
                KeyValueRow(stringResource(id = R.string.notifications), stringResource(id = R.string.notifications_value))
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.app_version),
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = c.primary,
                modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium), vertical = dimensionResource(id = R.dimen.padding_small))
            )
            Divider()
            Column(content = content)
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = {
            Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    )
    Divider()
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreviewLight() {
    ProjectTheme { ProfileScreen() }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreviewDark() {
    ProjectTheme(darkTheme = true) { ProfileScreen() }
}