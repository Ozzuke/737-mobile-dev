package com.example.project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Upload : Screen("upload")
    data object Datasets : Screen("datasets")
    data object Analysis : Screen("analysis/{datasetId}") {
        fun createRoute(datasetId: String) = "analysis/$datasetId"
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    // Get application instance for dependency injection
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as com.example.project.CGMApplication

    // Create ViewModels with injected repositories
    val glucoseViewModel: GlucoseViewModel = viewModel(
        factory = com.example.project.ui.viewmodels.GlucoseViewModelFactory(
            application = application,
            glucoseRepository = application.glucoseRepository,
            csvRepository = application.csvRepository
        )
    )

    val cgmApiViewModel: CgmApiViewModel = viewModel(
        factory = com.example.project.ui.viewmodels.CgmApiViewModelFactory(
            repository = application.cgmApiRepository
        )
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddClick = { navController.navigate(Screen.Upload.route) },
                onSettingsClick = { /* TODO settings */ },
                onInfoClick = { navController.navigate(Screen.Datasets.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                viewModel = glucoseViewModel
            )
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
                    navController.navigate(Screen.Analysis.createRoute(datasetId))
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
    }
}
