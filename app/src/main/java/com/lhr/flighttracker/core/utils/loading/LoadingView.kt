package com.lhr.flighttracker.core.utils.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.core.utils.loading.view.ArcLoading
import com.lhr.flighttracker.core.utils.loading.view.RingBarsLoading
import com.lhr.flighttracker.core.utils.loading.view.RingDotsLoading
import com.lhr.flighttracker.core.utils.loading.view.WavesLoading

@Composable
fun LoadingView(
    loadingType: LoadingType = LoadingType.Circular(),
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f)
) {
    val isLoading by LoadingManager.isLoading
    val loadingText by LoadingManager.loadingText
    val screenPadding = LocalConfiguration.current.screenHeightDp * 0.1

    if (isLoading) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(screenPadding.dp)
                        .background(Color.Black, shape = RoundedCornerShape(16.dp))
                        .padding(24.dp)
                ) {
                    val type = loadingType
                    when (type) {
                        is LoadingType.Circular -> CircularProgressIndicator(color = type.color)
                        is LoadingType.Waves -> WavesLoading(
                            color = type.color,
                            barWidth = type.barWidth,
                            barRoundedCorner = type.barRoundedCorner,
                            totalWidth = type.totalWidth,
                            maxHeight = type.maxHeight,
                            heightVariationFactor = type.heightVariationFactor,
                            duration = type.duration
                        )
                        is LoadingType.RingDots -> RingDotsLoading(
                            activeColor = type.activeColor,
                            size = type.size,
                            spacingFactor = type.spacingFactor,
                            duration = type.duration
                        )
                        is LoadingType.RingBars -> RingBarsLoading(
                            activeColor = type.activeColor,
                            barWidth = type.barWidth,
                            barHeight = type.barHeight,
                            barRoundedCorner = type.barRoundedCorner,
                            totalBars = type.totalBars,
                            radius = type.radius,
                            duration = type.duration
                        )
                        is LoadingType.Arc -> ArcLoading(
                            activeColor = type.activeColor,
                            inactiveColor = type.inactiveColor,
                            strokeWidth = type.strokeWidth,
                            radius = type.radius,
                            sweepAngle = type.sweepAngle,
                            gradientStepAngle = type.gradientStepAngle,
                            duration = type.duration
                        )
                    }
                    loadingText?.takeIf { it.isNotBlank() }?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = it, color = Color.White)
                    }
                }
            }
        }
    }
}
