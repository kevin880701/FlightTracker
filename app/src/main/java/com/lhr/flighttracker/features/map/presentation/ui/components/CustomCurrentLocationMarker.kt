package com.lhr.flighttracker.features.map.presentation.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource

/**
 * 自定義的當前位置標記 Composable
 *
 * @param marker 標記數據 (應為 CURRENT_LOCATION 類型)
 * @param screenPosition 標記在螢幕上的位置
 * @param deviceAzimuth 設備的方位角 (0-360, 0 為正北方)
 * @param mapRotation 地圖的當前旋轉角度
 * @param onLocationClick 當前位置標記點擊事件的回調
 */
@Composable
fun CustomCurrentLocationMarker(
    marker: MapMarker,
    screenPosition: IntOffset,
    deviceAzimuth: Float,
    mapRotation: Float,
    onLocationClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val x = screenPosition.x - (placeable.width / 2)
                val y = screenPosition.y - (placeable.height / 2)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(x, y)
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick ={
                    onLocationClick()
                }
            )
    ) {
        when (val imageSource = marker.imageSource) {
            is ImageSource.FromResource -> {
                Icon(
                    painter = painterResource(id = imageSource.resourceId),
                    contentDescription = marker.name,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(deviceAzimuth - mapRotation)
                )
            }

            is ImageSource.FromAsset -> {
                AsyncImage(
                    model = "file:///android_asset/${imageSource.assetName}",
                    contentDescription = marker.name,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(deviceAzimuth - mapRotation)
                )
            }

            is ImageSource.FromUri -> {
                AsyncImage(
                    model = imageSource.uri,
                    contentDescription = marker.name,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(deviceAzimuth - mapRotation)
                )
            }
        }
    }
}