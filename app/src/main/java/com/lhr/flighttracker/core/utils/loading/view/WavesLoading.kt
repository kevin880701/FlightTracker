package com.lhr.flighttracker.core.utils.loading.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay

@Composable
fun WavesLoading(
    color: Color = Color.White,
    barWidth: Dp = 8.dp, // 每個 Bar 的寬度
    barRoundedCorner: Int = 48,
    totalWidth: Dp = 48.dp, // 控制整個 `Row` 的寬度
    maxHeight: Dp = 24.dp, // 控制波浪最大高度
    heightVariationFactor: Float = 0.1f, // 控制每次變化比例
    duration: Int = 50 // 控制動畫速度（ms）
) {
    val totalBars = (totalWidth.value / (barWidth.value * 1.5)).toInt().coerceAtLeast(3) // 自動計算 `Bars` 數量
    val minHeight = maxHeight * 0.2f // 最小高度
    val heightStep = maxHeight * heightVariationFactor // 每次變動的增量

    // 依照 index 來平均分配初始高度
    val barHeights = remember {
        List(totalBars) { i ->
            mutableStateOf(minHeight + (i / (totalBars - 1).toFloat()) * (maxHeight - minHeight))
        }
    }
    val directionFlags = remember { List(totalBars) { mutableStateOf(1) } }

    LaunchedEffect(Unit) {
        while (true) {
            delay(duration.toLong())

            for (i in 0 until totalBars) {
                val currentHeight = barHeights[i].value
                val direction = directionFlags[i].value
                val newHeight = currentHeight + heightStep * direction

                if (newHeight >= maxHeight) {
                    barHeights[i].value = maxHeight
                    directionFlags[i].value = -1
                } else if (newHeight <= minHeight) {
                    barHeights[i].value = minHeight
                    directionFlags[i].value = 1
                } else {
                    barHeights[i].value = newHeight
                }
            }
        }
    }

    Row(
        modifier = Modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy((totalWidth - (barWidth * totalBars)) / (totalBars - 1))
    ) {
        barHeights.forEach { heightState ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(heightState.value)
                    .align(Alignment.CenterVertically)
                    .background(color, shape = RoundedCornerShape(barRoundedCorner))
            )
        }
    }
}
