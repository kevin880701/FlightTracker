package com.lhr.flighttracker.features.map.presentation.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.dialog.DialogManager.dismissDialog
import com.lhr.flighttracker.core.dialog.DialogPosition
import com.lhr.flighttracker.core.dialog.showDialog
import com.lhr.flighttracker.core.permission.PermissionStatus
import com.lhr.flighttracker.core.permission.PermissionType
import com.lhr.flighttracker.core.permission.rememberPermissionManager
import com.lhr.flighttracker.core.permission.rememberPermissionClickHandler
import com.lhr.flighttracker.core.toast.ToastManager
import com.lhr.flighttracker.core.ui.loading.LoadingManager
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.map.presentation.viewmodel.IndoorMapViewModel
import com.lhr.flighttracker.features.floorplan.presentation.FloorPlanConfiguration
import com.lhr.flighttracker.features.floorplan.presentation.ui.FloorPlan
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.map.data.createMapDefinitionFromStaticData
import com.lhr.flighttracker.features.map.presentation.ui.components.CustomCompass
import com.lhr.flighttracker.features.map.presentation.ui.components.CustomCurrentLocationMarker
import com.lhr.flighttracker.features.map.presentation.ui.components.CustomLocation
import com.lhr.flighttracker.features.map.presentation.ui.components.CustomMarker
import com.lhr.flighttracker.features.map.presentation.widget.dialog.MarkerDetailDialogContent

/**
 * 修正後的室內地圖畫面
 *
 * 主要修正：
 * 1. 移除不存在的 FloorPlanControllerState
 * 2. 使用現有的 FloorPlanController
 * 3. 簡化 UI 邏輯，專注於核心功能
 */
@Composable
fun IndoorMapScreen(
    viewModel: IndoorMapViewModel = hiltViewModel()
) {
    // 創建 FloorPlan Controller
    val floorPlanController = FloorPlan.rememberController()
    val floorPlanState by floorPlanController.uiState.collectAsState()

    val isRequestingLocation by viewModel.isRequestingLocation.collectAsState()
    // 權限管理
    val locationPermissionManager = rememberPermissionManager(
        permission = PermissionType.LOCATION,
        rationale = "需要位置權限才能在地圖上顯示您的當前位置並提供方向指引。"
    )

    // 定位按鈕的 Handler
    val locationButtonHandler = rememberPermissionClickHandler(
        manager = locationPermissionManager,
        onGranted = {
            viewModel.requestLocationWithPermission()
        }
    )

    // 導航功能的 Handler
    val markerToNavigate = remember { mutableStateOf<MapMarker?>(null) }
    val navigationPermissionHandler = rememberPermissionClickHandler(
        manager = locationPermissionManager,
        onGranted = {
            markerToNavigate.value?.let {
                floorPlanController.navigateToMarker(it)
                markerToNavigate.value = null
            }
        },
    )

    // 自動檢查並請求位置
    LaunchedEffect(Unit) {
        if(locationPermissionManager.status == PermissionStatus.Granted){
            floorPlanController.requestLocationUpdate()
        }
    }

    val isLoading = isRequestingLocation || floorPlanState.isLoading
    LaunchedEffect(isLoading) {
        if (isLoading) {
            val message = when {
                isRequestingLocation -> "正在獲取位置..."
                floorPlanState.isLoading -> "處理中..."
                else -> "載入中..."
            }
            LoadingManager.showLoading(message)
        } else {
            LoadingManager.dismissLoading()
        }
    }

    // ✅ 確保離開畫面時，關閉 Loading
    DisposableEffect(Unit) {
        onDispose {
            LoadingManager.dismissLoading()
        }
    }

    // 地圖配置
    val floorPlanConfiguration = remember {
        FloorPlanConfiguration(
            mapDefinition = createMapDefinitionFromStaticData(),
            showNavigationRoute = true,
            zoomEnabled = true,
            panEnabled = true,
            rotationEnabled = true,
            showCompass = true,
            minScaleFactor = 0.8f,
            rotationSensitivity = 0.8f,
            markerComposable = { marker, screenPosition ->
                val fadeStartScale = 1.2f
                val fadeEndScale = 1.5f
                val textAlpha =
                    ((floorPlanState.scale - fadeStartScale) / (fadeEndScale - fadeStartScale))
                        .coerceIn(0f, 1f)

                CustomMarker(
                    marker = marker,
                    screenPosition = screenPosition,
                    textAlpha = textAlpha,
                    onMarkerClick = { clickedMarker ->
                        showDialog(
                            position = DialogPosition.BOTTOM,
                            extendToNavigationBar = true,
                            content = {
                                MarkerDetailDialogContent(
                                    marker = marker,
                                    onDismissRequest = { dismissDialog() },
                                    onNavigateToMarker = { selectedMarker ->
                                        markerToNavigate.value = selectedMarker
                                        navigationPermissionHandler()
                                    }
                                )
                            }
                        )
                    }
                )
            },
            currentLocationComposable = { marker, screenPosition, deviceAzimuth, mapRotation ->
                CustomCurrentLocationMarker(
                    marker = marker,
                    screenPosition = screenPosition,
                    deviceAzimuth = deviceAzimuth,
                    mapRotation = mapRotation,
                    onLocationClick = {
//                        locationButtonHandler
                    }
                )
            },
            compassComposable = { azimuth, mapRotation ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                ) {
                    CustomCompass(
                        azimuth = azimuth,
                        mapRotation = mapRotation,
                        onCompassClick = {
                            floorPlanController.resetRotation()
                        }
                    )
                }
            },
            locationComposable = { isLocationEnabled, hasCurrentLocation ->
                CustomLocation(
                    isLocationEnabled = isLocationEnabled,
                    hasCurrentLocation = hasCurrentLocation,
                    onLocationClick = {
                        locationButtonHandler()
                    }
                )
            }
        )
    }

    // 錯誤處理
    floorPlanState.error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Log.e("FloorPlan", errorMessage)
        }
    }

    Scaffold(
        topBar = {
            MainTitleBar(
                title = stringResource(id = R.string.map),
                testTag = "indoor_map_screen_title_bar"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 主要的 FloorPlan 視圖
            FloorPlan.View(
                configuration = floorPlanConfiguration,
                controller = floorPlanController,
                modifier = Modifier.fillMaxSize(),
            )

            // 導航資訊面板（頂部）
            floorPlanState.navigationRoute?.let { route ->
                NavigationInfoPanel(
                    route = route,
                    onStopNavigation = { floorPlanController.clearNavigation() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // 載入指示器
            if (isRequestingLocation || floorPlanState.isLoading) {
                LoadingIndicator(
                    text = when {
                        isRequestingLocation -> "正在獲取位置..."
                        floorPlanState.isLoading -> "處理中..."
                        else -> "載入中..."
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 導航資訊面板
 */
@Composable
private fun NavigationInfoPanel(
    route: com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 頂部控制列
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "導航中",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onStopNavigation) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "停止導航"
                    )
                }
            }

            // 路線資訊
            NavigationRouteInfo(route = route)
        }
    }
}

/**
 * 路線基本資訊
 */
@Composable
private fun NavigationRouteInfo(
    route: com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 當前指令
        Column(modifier = Modifier.weight(1f)) {
            val currentInstruction = route.instructions.firstOrNull()
            Text(
                text = currentInstruction?.instruction ?: "準備導航",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 距離和時間資訊
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_directions_walk),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${route.totalDistance.toInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "${route.estimatedWalkTime / 60}分鐘",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 載入指示器
 */
@Composable
private fun LoadingIndicator(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
