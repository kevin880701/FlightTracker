package com.lhr.flighttracker.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lhr.flighttracker.features.main.presentation.ui.MainScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "Main",
    ) {
        composable("Main") { MainScreen() }
    }
}