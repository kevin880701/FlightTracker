package com.lhr.flighttracker.features.main.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.features.main.presentation.navigation.BottomNavigationBar
import com.lhr.flighttracker.features.main.presentation.navigation.MainNavHost

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val darkTheme = isSystemInDarkTheme()

    BaseScreen(extendToNavigationBar = true,
//        isStatusBarIconsLight = darkTheme,
//        isNavigationBarIconsLight = darkTheme,
        content = {
            Column {
                Box(modifier = Modifier.weight(1F)){
                    MainNavHost(navController = navController)
                }
                BottomNavigationBar(navController = navController)
            }
        }
    )
}