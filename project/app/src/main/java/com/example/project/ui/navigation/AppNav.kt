package com.example.project.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project.ui.screens.AnalysisScreen
import com.example.project.ui.screens.DatasetsScreen
import com.example.project.ui.screens.HomeScreen
import com.example.project.ui.screens.ProfileScreen
import com.example.project.ui.screens.UploadScreen
import com.example.project.ui.viewmodels.CgmApiViewModel
import com.example.project.ui.viewmodels.GlucoseViewModel
import com.example.project.ui.viewmodels.MainViewModel
import com.example.project.ui.viewmodels.MainViewModelFactory

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Upload : Screen("upload")
    data object Datasets : Screen("datasets")
    data object Analysis : Screen("analysis/{datasetId}") {
        fun createRoute(datasetId: String) = "analysis/$datasetId"
    }
    data object LLMAnalysis : Screen("llm-analysis/{datasetId}") {
        fun createRoute(datasetId: String) = "llm-analysis/$datasetId"
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

    // Create ViewModels with injected repositories
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddClick = { navController.navigate(Screen.Upload.route) },
                onSettingsClick = { /* TODO: Settings screen */ },
                onInfoClick = { navController.navigate(Screen.Datasets.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onStatusClick = { datasetId ->
                    navController.navigate(Screen.LLMAnalysis.createRoute(datasetId))
                },
                onGraphClick = { datasetId, _ ->
                    // For now, clicking graph also opens analysis
                    // TODO: Create dedicated metrics screen if needed
                    navController.navigate(Screen.Analysis.createRoute(datasetId))
                },
                onOfflineClick = {
                    showOfflineDisclaimer = true
                },
                mainViewModel = mainViewModel
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
            ProfileScreen(onBackClick = { navController.popBackStack() })
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
                    // Set active dataset and refresh home screen, then show analysis
                    coroutineScope.launch {
                        application.preferencesRepository.setActiveDatasetId(datasetId)
                        mainViewModel.fetchLatestData()
                    }
                    navController.navigate(Screen.Analysis.createRoute(datasetId))
                },
                onSetActiveDataset = { datasetId ->
                    // Set active dataset and refresh home screen
                    coroutineScope.launch {
                        application.preferencesRepository.setActiveDatasetId(datasetId)
                        mainViewModel.fetchLatestData()
                    }
                },
                viewModel = cgmApiViewModel
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
    }
}
