package com.lhr.flighttracker.core.utils.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun ToastView(
    message: String?,
    onToastFinished: () -> Unit,
    duration: Long = 2000,
    position: ToastPosition = ToastPosition.BOTTOM,
    backgroundColor: Color = Color.Black.copy(alpha = 0.8f),
    textColor: Color = Color.White
) {
    val toastPadding = LocalConfiguration.current.screenHeightDp * 0.1
    val imeHeightDp = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val bottomPadding = if (imeHeightDp > 0.dp) imeHeightDp else 0.dp

    if (message != null) {
        var isVisible by remember { mutableStateOf(true) }

        LaunchedEffect(message) {
            delay(duration)
            isVisible = false
            delay(500)
            onToastFinished()
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                contentAlignment = when (position) {
                    ToastPosition.TOP -> Alignment.TopCenter
                    ToastPosition.CENTER -> Alignment.Center
                    ToastPosition.BOTTOM -> Alignment.BottomCenter
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.0f))
                    .padding(
                        top = if (position == ToastPosition.TOP) toastPadding.dp else 0.dp,
                        bottom = if (position == ToastPosition.BOTTOM) bottomPadding + toastPadding.dp else 0.dp
                    )
                    .clearAndSetSemantics { }
            ) {
                Box(
                    modifier = Modifier
                        .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(text = message, color = textColor)
                }
            }
        }
    }
}