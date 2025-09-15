package com.lhr.flighttracker.features.floorplan.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanCallbacks
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanConfiguration
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanController
import com.lhr.flighttracker.features.floorplan.presentation.ui.components.CompassWidget
import com.lhr.flighttracker.features.floorplan.presentation.ui.components.MapMarkerItem
import com.lhr.flighttracker.features.floorplan.presentation.ui.components.MapViewComponent
import com.lhr.flighttracker.features.floorplan.presentation.ui.components.MarkersOverlay
import com.lhr.flighttracker.features.floorplan.presentation.ui.components.CurrentLocationMarkerItem
import com.lhr.flighttracker.features.map.presentation.widget.NavigationRouteOverlay

/**
 * 平面圖核心視圖實現
 *
 * 這是 FloorPlan 模組的內部實現，負責將 ViewModel 的狀態渲染到 UI 上。
 *
 * @param configuration 靜態的平面圖配置參數
 * @param modifier Compose 修飾符
 * @param callbacks 用戶交互事件回調
 * @param controller 外部控制器，是 ViewModel 的唯一入口
 */
@Composable
internal fun FloorPlanView(
    configuration: FloorPlanConfiguration,
    modifier: Modifier,
    callbacks: FloorPlanCallbacks,
    controller: FloorPlanController
) {
    val viewModel = controller.viewModel
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(configuration.mapDefinition) {
        configuration.mapDefinition?.let {
            viewModel.setMapDefinition(it)
        }
    }

    // 合併所有標記，來源是靜態的 configuration 和動態的 uiState
    val allMarkers = remember(configuration.markers, uiState.currentLocationMarker) {
        configuration.markers + listOfNotNull(uiState.currentLocationMarker)
    }

    LaunchedEffect(allMarkers) {
        viewModel.setCurrentMarkers(allMarkers)
    }

    Box(modifier = modifier) {
        MapViewComponent(
            configuration = configuration,
            viewModel = viewModel,
            callbacks = callbacks
        )

        // 導航路線覆蓋層
        if (configuration.showNavigationRoute && uiState.navigationRoute != null) {
            NavigationRouteOverlay(
                route = uiState.navigationRoute,
                routeScreenPositions = uiState.navigationRouteScreenPositions,
                modifier = Modifier.fillMaxSize(),
                isActive = uiState.navigationRoute != null
            )
        }

        // 第三層：標記覆蓋層
        MarkersOverlay(
            markers = allMarkers,
            markerScreenPositions = uiState.markerScreenPositions,
            deviceAzimuth = uiState.deviceAzimuth,
            mapRotation = uiState.rotation,
            markerComposable = configuration.markerComposable ?: { marker, screenPosition ->
                val fadeStartScale = 1.2f
                val fadeEndScale = 1.5f
                val textAlpha = ((uiState.scale - fadeStartScale) / (fadeEndScale - fadeStartScale))
                    .coerceIn(0f, 1f)
                MapMarkerItem(
                    marker = marker,
                    screenPosition = screenPosition,
                    textAlpha = textAlpha,
                )
            },
            currentLocationComposable = configuration.currentLocationComposable ?: { marker, screenPosition, deviceAzimuth, mapRotation ->
                CurrentLocationMarkerItem(
                    marker = marker,
                    screenPosition = screenPosition,
                    deviceAzimuth = deviceAzimuth,
                    mapRotation = mapRotation,
                )
            }
        )

        // 第四層：指北針控制
        if (configuration.showCompass) {
            Box(
                modifier = Modifier
                    .align(configuration.compassAlignment)
            ) {
                if (configuration.compassComposable != null) {
                    // 使用自訂的指北針組件
                    configuration.compassComposable.invoke(
                        uiState.deviceAzimuth,
                        uiState.rotation
                    )
                } else {
                    // 使用預設的指北針組件
                    CompassWidget(
                        azimuth = uiState.deviceAzimuth,
                        mapRotation = uiState.rotation,
                        onCompassClick = {
                            viewModel.resetRotation()
                        },
                        modifier = Modifier.padding(bottom = 64.dp)
                    )
                }
            }
        }

        // 第五層：位置按鈕控制
        Box(
            modifier = Modifier
                .align(configuration.locationAlignment)
        ) {
            if (configuration.locationComposable != null) {
                // 使用自訂的位置按鈕組件
                configuration.locationComposable.invoke(
                    uiState.isLocationEnabled,
                    uiState.currentLocationMarker != null
                )
            } else {
                // 使用預設的位置按鈕組件
                LocationButton(
                    isLocationEnabled = uiState.isLocationEnabled,
                    hasCurrentLocation = uiState.currentLocationMarker != null,
                    onLocationClick = {
                        if (!viewModel.onLocationButtonClick()) {
                            viewModel.requestLocationUpdate()
                        }
                    }
                )
            }
        }
    }
}