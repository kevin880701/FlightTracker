package com.lhr.flighttracker.features.floorplan.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource

/**
 * 顯示「使用者當前位置」的標記
 */
@Composable
internal fun CurrentLocationMarkerItem(
    marker: MapMarker,
    screenPosition: IntOffset,
    deviceAzimuth: Float,
    mapRotation: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val x = screenPosition.x - (placeable.width / 2)
                val y = screenPosition.y - (placeable.height / 2)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(x, y)
                }
            }
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