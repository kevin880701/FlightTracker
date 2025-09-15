package com.lhr.flighttracker.features.floorplan.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.MarkerType

/**
 * 疊加層，負責顯示所有地圖標記
 *
 * @param markers 要顯示的標記列表
 * @param markerScreenPositions 標記在螢幕上的座標
 * @param deviceAzimuth 設備的方位角
 * @param mapRotation 地圖的旋轉角度
 * @param markerComposable 用於渲染一般標記的 Composable
 * @param currentLocationComposable 用於渲染當前位置標記的 Composable
 */
@Composable
fun MarkersOverlay(
    markers: List<MapMarker>,
    markerScreenPositions: Map<Int, IntOffset>,
    deviceAzimuth: Float,
    mapRotation: Float,
    markerComposable: @Composable (marker: MapMarker, screenPosition: IntOffset) -> Unit,
    currentLocationComposable: @Composable (marker: MapMarker, screenPosition: IntOffset, deviceAzimuth: Float, mapRotation: Float) -> Unit
) {
    markers.forEach { marker ->
        markerScreenPositions[marker.id]?.let { screenPosition ->
            if (marker.type == MarkerType.CURRENT_LOCATION) {
                currentLocationComposable(
                    marker,
                    screenPosition,
                    deviceAzimuth,
                    mapRotation
                )
            } else {
                markerComposable(marker, screenPosition)
            }
        }
    }
}