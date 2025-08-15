package com.lhr.flighttracker.core.utils.loading.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RingBarsLoading(
    activeColor: Color = Color.White, // 高亮的條狀顏色
    barWidth: Dp = 4.dp, // 條狀寬度
    barRoundedCorner: Int = 48,
    barHeight: Dp = 16.dp, // 條狀長度
    totalBars: Int = 12, // 條狀數量
    radius: Dp = 24.dp, // 圓圈半徑
    duration: Int = 100 // 控制旋轉速度
) {
    val angleStep = 360f / totalBars // 計算每個條狀的角度間隔
    var activeIndex by remember { mutableIntStateOf(0) } // 記錄當前高亮的條狀索引

    // 啟動動畫
    LaunchedEffect(Unit) {
        while (true) {
            delay(duration.toLong()) // 控制旋轉速度
            activeIndex = (activeIndex + 1) % totalBars // 讓高亮條狀依序移動
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(radius * 2)) {
        repeat(totalBars) { index ->
            val angle = Math.toRadians((angleStep * index - 90).toDouble()) // 計算條狀位置角度
            val xOffset = cos(angle) * radius.value
            val yOffset = sin(angle) * radius.value

            val distance = (index - activeIndex + totalBars) % totalBars // 確保索引為正數
            val alphaFactor = (1f - distance.toFloat() / totalBars).coerceIn(0.3f, 1f) // 讓條狀有漸變效果

            Box(
                modifier = Modifier
                    .offset(xOffset.dp, yOffset.dp) // 讓條狀分佈在環形
                    .size(barWidth, barHeight) // 設定條狀大小
                    .graphicsLayer(
                        rotationZ = angleStep * index, // ✅ 旋轉條狀，使其對齊圓形
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f) // 以中心旋轉
                    )
                    .background(activeColor.copy(alpha = alphaFactor), shape = RoundedCornerShape(barRoundedCorner)) // 透明度漸變
            )
        }
    }
}
