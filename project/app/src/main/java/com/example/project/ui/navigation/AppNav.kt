package com.example.project.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project.ui.screens.*
import com.example.project.ui.viewmodels.*

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Upload : Screen("upload")
    data object Datasets : Screen("datasets")
    data object Connections : Screen("connections")
    data object Analysis : Screen("analysis/{datasetId}") {
        fun createRoute(datasetId: String) = "analysis/$datasetId"
    }
    data object LLMAnalysis : Screen("llm-analysis/{datasetId}") {
        fun createRoute(datasetId: String) = "llm-analysis/$datasetId"
    }
    data object Settings : Screen("settings")
    data object FullGraph : Screen("full_graph/{preset}") {
        fun createRoute(preset: String) = "full_graph/$preset"
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    var showOfflineDisclaimer by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Get application instance for dependency injection
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as com.example.project.CGMApplication
    val darkModeFlow = remember { application.preferencesRepository.getDarkModeEnabled() }

    // Create ViewModels with injected repositories
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = application.authRepository
        )
    )

    val glucoseViewModel: GlucoseViewModel = viewModel(
        factory = com.example.project.ui.viewmodels.GlucoseViewModelFactory(
            application = application,
            glucoseRepository = application.glucoseRepository,
            csvRepository = application.csvRepository,
            cgmApiRepository = application.cgmApiRepository
        )
    )

    val cgmApiViewModel: CgmApiViewModel = viewModel(
        factory = com.example.project.ui.viewmodels.CgmApiViewModelFactory(
            repository = application.cgmApiRepository
        )
    )

    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            repository = application.cgmApiRepository,
            preferencesRepository = application.preferencesRepository,
            context = context.applicationContext
        )
    )
    val homeState by mainViewModel.homeState.collectAsState()

    // Observe authentication state
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    // Determine start destination based on authentication
    val startDestination = if (isAuthenticated) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication screens
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Main app screens (require authentication)
        composable(Screen.Home.route) {
            val connectionViewModel: ConnectionViewModel = viewModel(
                factory = ConnectionViewModelFactory(
                    authRepository = application.authRepository
                )
            )

            HomeScreen(
                onAddClick = { navController.navigate(Screen.Upload.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onInfoClick = { navController.navigate(Screen.Datasets.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onConnectionsClick = { navController.navigate(Screen.Connections.route) },
                onStatusClick = { datasetId ->
                    navController.navigate(Screen.LLMAnalysis.createRoute(datasetId))
                },
                onGraphClick = { _, preset ->
                    navController.navigate(Screen.FullGraph.createRoute(preset))
                },
                onOfflineClick = {
                    showOfflineDisclaimer = true
                },
                mainViewModel = mainViewModel,
                authViewModel = authViewModel,
                connectionViewModel = connectionViewModel
            )

            // Show offline disclaimer dialog
            if (showOfflineDisclaimer) {
                com.example.project.ui.components.DisclaimerDialog(
                    onDismiss = { showOfflineDisclaimer = false },
                    showOfflineWarning = true
                )
            }
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Connections.route) {
            val connectionViewModel: ConnectionViewModel = viewModel(
                factory = ConnectionViewModelFactory(
                    authRepository = application.authRepository
                )
            )
            ConnectionsScreen(
                authViewModel = authViewModel,
                connectionViewModel = connectionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Upload.route) {
            UploadScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = glucoseViewModel
            )
        }
        composable(Screen.Datasets.route) {
            DatasetsScreen(
                onBackClick = { navController.popBackStack() },
                onDatasetClick = { datasetId ->
                    coroutineScope.launch {
                        application.preferencesRepository.setActiveDatasetId(datasetId)
                        mainViewModel.fetchLatestData()
                    }
                    navController.navigate(Screen.Analysis.createRoute(datasetId))
                },
                onSetActiveDataset = { datasetId ->
                    coroutineScope.launch {
                        application.preferencesRepository.setActiveDatasetId(datasetId)
                        mainViewModel.fetchLatestData()
                    }
                },
                onDeleteDataset = { datasetId ->
                    cgmApiViewModel.deleteDataset(datasetId, homeState.selectedPatientId) {
                        coroutineScope.launch {
                            application.preferencesRepository.clearActiveDatasetId()
                            mainViewModel.clearAllDatasetState()
                        }
                    }
                },
                viewModel = cgmApiViewModel,
                patientId = homeState.selectedPatientId,
                isClinician = homeState.isClinician
            )
        }
        composable(
            route = Screen.Analysis.route,
            arguments = listOf(
                navArgument("datasetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val datasetId = backStackEntry.arguments?.getString("datasetId") ?: ""
            AnalysisScreen(
                datasetId = datasetId,
                onBackClick = { navController.popBackStack() },
                viewModel = cgmApiViewModel
            )
        }
        composable(
            route = Screen.LLMAnalysis.route,
            arguments = listOf(
                navArgument("datasetId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val datasetId = backStackEntry.arguments?.getString("datasetId") ?: ""
            com.example.project.ui.screens.LLMAnalysisScreen(
                datasetId = datasetId,
                onBackClick = { navController.popBackStack() },
                viewModel = cgmApiViewModel
            )
        }
        composable(Screen.Settings.route) {
            val scope = rememberCoroutineScope()
            val darkMode by darkModeFlow.collectAsState(initial = null)
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                getPreferredUnit = { application.preferencesRepository.getPreferredUnit() },
                setPreferredUnit = { unit ->
                    scope.launch { application.preferencesRepository.setPreferredUnit(unit) }
                },
                darkModeEnabled = darkMode ?: false,
                onDarkModeChanged = { enabled ->
                    scope.launch { application.preferencesRepository.setDarkModeEnabled(enabled) }
                }
            )
        }
        composable(
            route = Screen.FullGraph.route,
            arguments = listOf(navArgument("preset") { type = NavType.StringType })
        ) { backStackEntry ->
            val preset = backStackEntry.arguments?.getString("preset") ?: "24h"
            FullscreenGraphScreen(
                preset = preset,
                homeViewModel = mainViewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
