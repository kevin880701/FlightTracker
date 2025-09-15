package com.lhr.flighttracker.features.map.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * 指北針組件，用於在UI上顯示當前的方向。
 * 它可以根據裝置的方位角和地圖的旋轉角度，動態地指向地理北方。
 *
 * @param azimuth 當前裝置的方位角 (0-360度)，0度代表正北方。
 * @param mapRotation 當前地圖的旋轉角度 (0-360度)。
 * @param modifier Compose 的 Modifier，用於調整佈局、大小等。
 * @param size 指北針組件的尺寸。
 * @param onCompassClick 點擊指北針時觸發的回調函式。可用於將地圖方向重設為正北。
 */
@Composable
fun CustomCompass(
    azimuth: Float,
    mapRotation: Float = 0f,
    onCompassClick: (() -> Unit)? = null
) {
    // 根據裝置方位角和地圖旋轉角度，計算出指北針最終需要旋轉的角度
    val compassRotation = calculateCompassRotation(azimuth, mapRotation)

    // 使用 animateFloatAsState 讓旋轉角度的變化具有平滑的動畫效果
    val animatedRotation by animateFloatAsState(
        targetValue = compassRotation,
        animationSpec = tween(durationMillis = 300),
        label = "compass_rotation"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DirectionInfoWidget(
            azimuth = azimuth,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), // 設定半透明背景
                shape = CircleShape
            )
            .then(
                // 如果提供了點擊回調，則啟用點擊效果
                if (onCompassClick != null) {
                    Modifier.clickable { onCompassClick() }
                } else Modifier
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 使用 Canvas 繪製指北針的圖形
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCompass(
                rotation = animatedRotation, // 傳入動畫化的旋轉角度
                primaryColor = Color.Red, // 北方指針顏色
                secondaryColor = Color.White, // 南方指針顏色
                backgroundColor = Color.Black.copy(alpha = 0.1f) // 內部背景色
            )
        }

        // 在頂部中央放置一個固定的 "N" 文字，代表北方
        Text(
            text = "N",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 4.dp) // 稍微向下偏移，避免貼邊
        )
    } }


}

/**
 * 負責在 DrawScope 中繪製指北針的具體圖形。
 *
 * @receiver DrawScope 繪圖的上下文環境。
 * @param rotation 指北針需要旋轉的角度。
 * @param primaryColor 北方指針的顏色。
 * @param secondaryColor 南方指針的顏色。
 * @param backgroundColor 內部圓盤的背景色。
 */
private fun DrawScope.drawCompass(
    rotation: Float,
    primaryColor: Color,
    secondaryColor: Color,
    backgroundColor: Color
) {
    val center = this.center // 畫布中心點
    val radius = size.minDimension / 2f * 0.8f // 計算半徑，留出邊距

    // 繪製內部圓盤背景
    drawCircle(
        color = backgroundColor,
        radius = radius * 1.1f,
        center = center
    )

    // 將整個繪圖區域以中心為軸進行旋轉
    rotate(rotation, pivot = center) {
        // 繪製北方（紅色）箭頭
        val northArrowPath = Path().apply {
            moveTo(center.x, center.y - radius * 0.7f) // 箭頭頂點
            lineTo(center.x - radius * 0.15f, center.y - radius * 0.2f) // 左下點
            lineTo(center.x, center.y - radius * 0.3f) // 中間點
            lineTo(center.x + radius * 0.15f, center.y - radius * 0.2f) // 右下點
            close()
        }
        drawPath(
            path = northArrowPath,
            color = primaryColor
        )

        // 繪製南方（白色）箭頭
        val southArrowPath = Path().apply {
            moveTo(center.x, center.y + radius * 0.7f) // 箭頭頂點
            lineTo(center.x - radius * 0.15f, center.y + radius * 0.2f) // 左上點
            lineTo(center.x, center.y + radius * 0.3f) // 中間點
            lineTo(center.x + radius * 0.15f, center.y + radius * 0.2f) // 右上點
            close()
        }
        drawPath(
            path = southArrowPath,
            color = secondaryColor
        )

        // 繪製中心軸點
        drawCircle(
            color = Color.Black,
            radius = radius * 0.08f,
            center = center
        )
    }

    // 繪製外部的刻度線
    for (i in 0 until 360 step 30) { // 每隔30度繪製一條線
        val angle = Math.toRadians(i.toDouble())
        val startRadius = radius * 0.9f
        val endRadius = radius * 0.95f

        // 計算刻度線的起點和終點座標
        val startX = center.x + startRadius * sin(angle).toFloat()
        val startY = center.y - startRadius * cos(angle).toFloat()
        val endX = center.x + endRadius * sin(angle).toFloat()
        val endY = center.y - endRadius * cos(angle).toFloat()

        // 繪製線條
        drawLine(
            color = Color.Gray,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            // 東西南北四個主要方向的刻度線加粗
            strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.dp.toPx()
        )
    }
}

/**
 * 用於以文字形式顯示當前方向和角度的組件。
 *
 * @param azimuth 當前裝置的方位角 (0-360度)。
 * @param modifier Compose 的 Modifier。
 */
@Composable
fun DirectionInfoWidget(
    azimuth: Float,
    modifier: Modifier = Modifier
) {
    // 根據角度值判斷對應的方向文字
    val direction = when {
        azimuth < 22.5f || azimuth >= 337.5f -> "北"
        azimuth < 67.5f -> "東北"
        azimuth < 112.5f -> "東"
        azimuth < 157.5f -> "東南"
        azimuth < 202.5f -> "南"
        azimuth < 247.5f -> "西南"
        azimuth < 292.5f -> "西"
        azimuth < 337.5f -> "西北"
        else -> "北" // 備用情況
    }

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = CircleShape
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顯示方向文字，如 "東北"
            Text(
                text = direction,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            // 顯示精確的角度值，如 "45°"
            Text(
                text = "${azimuth.roundToInt()}°",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 根據方位角計算指北針箭頭的旋轉角度
 */
fun calculateCompassRotation(azimuth: Float, mapRotation: Float): Float {
    // 計算相對於地圖北方的角度
    return (360f - azimuth + mapRotation) % 360f
}
