package com.lhr.flighttracker.core.utils.loading

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class LoadingType {
    data class Circular(val color: Color = Color.White) : LoadingType()
    data class Waves(
        val color: Color = Color.White,
        val barWidth: Dp = 8.dp,
        val barRoundedCorner: Int = 48,
        val totalWidth: Dp = 48.dp,
        val maxHeight: Dp = 24.dp,
        val heightVariationFactor: Float = 0.1f,
        val duration: Int = 50
    ) : LoadingType()

    data class RingDots(
        val activeColor: Color = Color.Green, // 旋轉點顏色
        val size: Dp = 8.dp, // 控制點大小
        val spacingFactor: Float = 0.1f, // 0 ~ 1 點間距
        val duration: Int = 100 // 控制旋轉速度
    ) : LoadingType()

    data class RingBars(
        val activeColor: Color = Color.White,
        val barRoundedCorner: Int = 48,
        val barWidth: Dp = 4.dp,
        val barHeight: Dp = 12.dp,
        val totalBars: Int = 12,
        val radius: Dp = 24.dp, // 圓圈半徑
        val duration: Int = 100 // 控制旋轉速度
    ) : LoadingType()

    data class Arc(
        val activeColor: Color = Color.White,
        val inactiveColor: Color = Color.Gray,
        val strokeWidth: Dp = 6.dp,
        val radius: Dp = 36.dp,
        val sweepAngle: Float = 270f,
        val gradientStepAngle: Float = 3f,
        val duration: Int = 25,
    ) : LoadingType()
}
