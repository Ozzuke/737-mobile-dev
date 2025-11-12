package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.AuthViewModel
import java.time.Year

/**
 * Registration screen with role selection and appropriate fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Patient-specific fields
    var nickname by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var diagnosisMonth by remember { mutableStateOf("") }
    var diagnosisYear by remember { mutableStateOf("") }

    // Clinician-specific fields
    var fullName by remember { mutableStateOf("") }

    // Handle successful registration
    LaunchedEffect(authState) {
        if (authState is UiState.Success) {
            onRegisterSuccess()
            viewModel.resetAuthState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role selection
            if (selectedRole == null) {
                Text(
                    text = "Select your role",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { selectedRole = UserRole.PATIENT },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("Register as Patient")
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { selectedRole = UserRole.CLINICIAN },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text("Register as Clinician")
                }
            } else {
                // Show selected role
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Role: ${if (selectedRole == UserRole.PATIENT) "Patient" else "Clinician"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = { selectedRole = null }) {
                            Text("Change")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Common fields
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    supportingText = { Text("Min 3 characters") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    enabled = authState !is UiState.Loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    supportingText = { Text("Min 8 characters") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show")
                        }
                    },
                    enabled = authState !is UiState.Loading
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    enabled = authState !is UiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role-specific fields
                when (selectedRole) {
                    UserRole.PATIENT -> {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Display Name/Nickname") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            enabled = authState !is UiState.Loading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = birthYear,
                            onValueChange = { birthYear = it.filter { c -> c.isDigit() } },
                            label = { Text("Birth Year") },
                            placeholder = { Text("e.g., 1990") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            enabled = authState !is UiState.Loading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = diagnosisMonth,
                            onValueChange = { diagnosisMonth = it.filter { c -> c.isDigit() } },
                            label = { Text("Diabetes Diagnosis Month") },
                            placeholder = { Text("1-12") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            enabled = authState !is UiState.Loading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = diagnosisYear,
                            onValueChange = { diagnosisYear = it.filter { c -> c.isDigit() } },
                            label = { Text("Diabetes Diagnosis Year") },
                            placeholder = { Text("e.g., 2020") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            enabled = authState !is UiState.Loading
                        )
                    }
                    UserRole.CLINICIAN -> {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            enabled = authState !is UiState.Loading
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message
                if (authState is UiState.Error) {
                    Text(
                        text = (authState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Register button
                val isFormValid = when (selectedRole) {
                    UserRole.PATIENT -> {
                        username.length >= 3 &&
                                password.length >= 8 &&
                                password == confirmPassword &&
                                nickname.isNotBlank() &&
                                birthYear.toIntOrNull() != null &&
                                diagnosisMonth.toIntOrNull() in 1..12 &&
                                diagnosisYear.toIntOrNull() != null
                    }
                    UserRole.CLINICIAN -> {
                        username.length >= 3 &&
                                password.length >= 8 &&
                                password == confirmPassword &&
                                fullName.isNotBlank()
                    }
                    else -> false
                }

                Button(
                    onClick = {
                        when (selectedRole) {
                            UserRole.PATIENT -> {
                                viewModel.registerPatient(
                                    username = username,
                                    password = password,
                                    nickname = nickname,
                                    birthYear = birthYear.toInt(),
                                    diabetesDiagnosisMonth = diagnosisMonth.toInt(),
                                    diabetesDiagnosisYear = diagnosisYear.toInt()
                                )
                            }
                            UserRole.CLINICIAN -> {
                                viewModel.registerClinician(
                                    username = username,
                                    password = password,
                                    fullName = fullName
                                )
                            }
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = authState !is UiState.Loading && isFormValid
                ) {
                    if (authState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Register")
                    }
                }
            }
        }
    }
}

private enum class UserRole {
    PATIENT,
    CLINICIAN
}
