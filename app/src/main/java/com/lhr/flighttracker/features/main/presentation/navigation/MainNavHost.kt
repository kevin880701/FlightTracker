package com.lhr.flighttracker.features.main.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lhr.flighttracker.features.ble.presentation.ui.BleRollCallScreen
import com.lhr.flighttracker.features.exchangeRate.presentation.ui.ExchangeRateScreen
import com.lhr.flighttracker.features.flightScheduled.presentation.ui.FlightScreen
import com.lhr.flighttracker.features.setting.presentation.ui.SettingsScreen

@Composable
fun MainNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Flight.route) {

        composable(route = Screen.Flight.route) {
            FlightScreen()
        }

        composable(route = Screen.ExchangeRate.route) {
            ExchangeRateScreen()
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }

        composable(route = Screen.BleRollCall.route) {
            BleRollCallScreen()
        }
    }
}