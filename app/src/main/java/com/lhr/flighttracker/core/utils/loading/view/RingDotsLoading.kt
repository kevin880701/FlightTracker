package com.lhr.flighttracker.core.utils.loading.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RingDotsLoading(
    activeColor: Color = Color.Green,
    size: Dp = 8.dp,
    spacingFactor: Float = 0.1f,
    duration: Int = 100
) {
    val totalDots = (12 * (1 - spacingFactor)).toInt().coerceAtLeast(3) // ✅ 根據 `spacingFactor` 計算點數
    val angleStep = 360f / totalDots // ✅ 計算每個點的角度
    val radius = 24.dp // ✅ 圓圈半徑
    var activeIndex by remember { mutableStateOf(0) } // ✅ 記錄當前高亮的點

    LaunchedEffect(Unit) {
        while (true) {
            delay(duration.toLong()) // ✅ 控制旋轉速度
            activeIndex = (activeIndex + 1) % totalDots // ✅ 讓高亮點順時針移動
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(radius * 2)) {
        repeat(totalDots) { index ->
            val angle = Math.toRadians((angleStep * index).toDouble()) // ✅ 計算角度
            val xOffset = cos(angle) * radius.value
            val yOffset = sin(angle) * radius.value

            val distance = (index - activeIndex + totalDots) % totalDots // 確保索引為正
            val alphaFactor = abs(0f - (distance.toFloat() / totalDots.toFloat())).coerceIn(0.3f, 1.0f) // ✅ 讓點逐漸變暗

            Box(
                modifier = Modifier
                    .offset(xOffset.dp, yOffset.dp) // ✅ 點分佈在環形上
                    .size(size)
                    .background(activeColor.copy(alpha = alphaFactor.toFloat()), shape = CircleShape) // ✅ 漸變顏色
            )
        }
    }
}
