package com.lhr.flighttracker.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lhr.flighttracker.LocalStatusBarHeight

@Composable
fun BaseScreen(
    extendToStatusBar: Boolean = false,
    extendToNavigationBar: Boolean = false,
    statusBarColor: Color = MaterialTheme.colorScheme.background,
    navigationBarColor: Color = MaterialTheme.colorScheme.background,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit = {}
) {
    Scaffold(
        bottomBar = bottomBar,
        content = { innerPadding ->
            Column {
                if (!extendToStatusBar) {
                    Box(
                        modifier = Modifier
                            .height(LocalStatusBarHeight.current)
                            .fillMaxWidth()
                            .background(color = statusBarColor)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    content(innerPadding)
                }
                if (!extendToNavigationBar) {
                    Box(
                        modifier = Modifier
                            .height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                            .fillMaxWidth()
                            .background(color = navigationBarColor)
                    )
                }
            }

        }
    )
}