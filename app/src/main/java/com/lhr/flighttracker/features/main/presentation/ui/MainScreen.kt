package com.lhr.flighttracker.features.main.presentation.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.features.main.presentation.navigation.BottomNavigationBar
import com.lhr.flighttracker.features.main.presentation.navigation.MainNavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val darkTheme = isSystemInDarkTheme()

    BaseScreen(extendToNavigationBar = true,
//        isStatusBarIconsLight = darkTheme,
//        isNavigationBarIconsLight = darkTheme,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }, content = {
            MainNavHost(navController = navController)
        }
    )
}