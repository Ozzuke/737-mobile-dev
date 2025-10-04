package com.example.project.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.screens.HomeScreen
import com.example.project.ui.screens.ProfileScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Profile : Screen("profile")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSettingsClick = { /* TODO settings */ },
                onInfoClick = { /* TODO info/help */ },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
