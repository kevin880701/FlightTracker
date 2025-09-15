package com.lhr.flighttracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lhr.flighttracker.features.main.presentation.ui.MainScreen
import com.lhr.flighttracker.features.setting.presentation.ui.QRShareScreen
import com.lhr.flighttracker.features.setting.presentation.ui.ScanQRScreen
import com.lhr.flighttracker.features.setting.presentation.ui.SearchNearbyUsersScreen
import com.lhr.flighttracker.features.setting.presentation.ui.TermsOfServiceScreen
import com.lhr.flighttracker.features.setting.presentation.ui.UserProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "Main",
    ) {
        composable("Main") { MainScreen() }
        composable("TermsOfService") { TermsOfServiceScreen(navController = navController) }
        composable("UserProfile") { UserProfileScreen(navController = navController) }
        composable("QrShareScreen") { QRShareScreen(navController = navController) }
        composable("ScanQrScreen") { ScanQRScreen(navController = navController) }
        composable("SearchNearbyUsersScreen") { SearchNearbyUsersScreen(navController = navController) }
    }
}