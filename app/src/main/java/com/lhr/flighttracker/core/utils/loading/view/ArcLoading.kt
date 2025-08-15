package com.lhr.flighttracker.core.utils.loading.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun ArcLoading(
    activeColor: Color = Color.White, // 高亮區域顏色
    inactiveColor: Color = Color.Gray, // 背景圓環顏色
    strokeWidth: Dp = 6.dp, // 圓環的厚度
    radius: Dp = 36.dp, // 圓的半徑
    sweepAngle: Float = 270f, // 高亮部分的角度範圍
    gradientStepAngle: Float = 3f, // 每層弧度間隔
    duration: Int = 25 // 旋轉速度
) {
    var startAngle by remember { mutableFloatStateOf(0f) } // 當前旋轉角度

    LaunchedEffect(Unit) {
        while (true) {
            delay(duration.toLong())
            startAngle = (startAngle + 5) % 360 // 讓高亮部分順時針移動
        }
    }

    Canvas(modifier = Modifier.size(radius * 2)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        // 繪製背景圓環
        drawArc(
            color = inactiveColor.copy(alpha = 0.3f), // 淡色背景圓環
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )

        // 計算分層數量
        val segmentCount = (sweepAngle / gradientStepAngle).toInt().coerceAtLeast(1)

        // 分層繪製高亮弧線，讓末尾有透明度變化
        for (i in 0 until segmentCount) {
            val alphaFactor = abs(0f - i.toFloat() / segmentCount).coerceIn(0.2f, 1f) // 讓透明度遞減
            drawArc(
                color = activeColor.copy(alpha = alphaFactor),
                startAngle = startAngle + (i * gradientStepAngle), // 控制每層起始角度
                sweepAngle = gradientStepAngle, // 固定每層弧度大小
                useCenter = false,
                style = stroke
            )
        }
    }
}
