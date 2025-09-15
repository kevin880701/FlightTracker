package com.lhr.flighttracker.features.map.presentation.widget

import android.graphics.PointF
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationDirection
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationInstruction
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationState
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource
import kotlin.math.roundToInt

/**
 * 導航路線顯示組件 - 在地圖上繪製導航路徑
 */
@Composable
fun NavigationRouteOverlay(
    route: NavigationRoute?,
    routeScreenPositions: List<PointF>,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    if (route == null || routeScreenPositions.isEmpty()) return

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.6f,
        animationSpec = tween(300),
        label = "route_alpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "route_animation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "route_dash_animation"
    )

    Canvas(modifier = modifier) {
        drawNavigationRoute(
            screenPositions = routeScreenPositions,
            alpha = animatedAlpha,
            dashOffset = animatedOffset,
            isAccessible = route.isAccessible()
        )
    }
}

/**
 * 在Canvas上繪製導航路線
 */
private fun DrawScope.drawNavigationRoute(
    screenPositions: List<PointF>,
    alpha: Float,
    dashOffset: Float,
    isAccessible: Boolean
) {
    if (screenPositions.size < 2) return

    val routeColor = if (isAccessible) Color.Blue else Color.Green
    val strokeWidth = 8.dp.toPx()
    val dashPattern = floatArrayOf(15f, 10f)

    // 繪製路徑背景（白色描邊）
    for (i in 0 until screenPositions.size - 1) {
        val start = screenPositions[i]
        val end = screenPositions[i + 1]

        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(start.x.toFloat(), start.y.toFloat()),
            end = Offset(end.x.toFloat(), end.y.toFloat()),
            strokeWidth = strokeWidth + 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // 繪製主要路徑（虛線動畫）
    for (i in 0 until screenPositions.size - 1) {
        val start = screenPositions[i]
        val end = screenPositions[i + 1]

        drawLine(
            color = routeColor.copy(alpha = alpha),
            start = Offset(start.x.toFloat(), start.y.toFloat()),
            end = Offset(end.x.toFloat(), end.y.toFloat()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(dashPattern, dashOffset)
        )
    }

    // 繪製路徑點
    screenPositions.forEachIndexed { index, position ->
        val isStartOrEnd = index == 0 || index == screenPositions.size - 1
        val pointColor = if (isStartOrEnd) routeColor else routeColor.copy(alpha = 0.7f)
        val radius = if (isStartOrEnd) 8.dp.toPx() else 4.dp.toPx()

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius + 2.dp.toPx(),
            center = Offset(position.x.toFloat(), position.y.toFloat())
        )

        drawCircle(
            color = pointColor.copy(alpha = alpha),
            radius = radius,
            center = Offset(position.x.toFloat(), position.y.toFloat())
        )
    }
}

/**
 * 導航資訊面板
 *//**
 * 多點導航資訊面板
 */
@Composable
fun NavigationInfoPanel(
    navigationState: NavigationState.Navigating,
    onDismiss: () -> Unit,
    onShowInstructions: () -> Unit,
    onAddWaypoint: () -> Unit,
    onRemoveWaypoint: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 頂部控制列
            NavigationTopControls(
                onDismiss = onDismiss,
                onShowInstructions = onShowInstructions,
                onAddWaypoint = onAddWaypoint,
                isAddingWaypoint = navigationState.isAddingWaypoint
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 路線資訊
            NavigationRouteInfo(
                route = navigationState.route,
                currentStepIndex = navigationState.currentStepIndex
            )

            // 多點路線顯示
            if (navigationState.waypoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                MultiPointWaypointsList(
                    waypoints = navigationState.waypoints,
                    currentWaypointIndex = navigationState.currentWaypointIndex,
                    onRemoveWaypoint = onRemoveWaypoint
                )
            }

            // 添加停靠點提示
            if (navigationState.isAddingWaypoint) {
                Spacer(modifier = Modifier.height(8.dp))
                AddWaypointHint()
            }
        }
    }
}

/**
 * 頂部控制按鈕列
 */
@Composable
private fun NavigationTopControls(
    onDismiss: () -> Unit,
    onShowInstructions: () -> Unit,
    onAddWaypoint: () -> Unit,
    isAddingWaypoint: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 標題
        Text(
            text = if (isAddingWaypoint) "選擇停靠點" else "導航中",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row {
            // 添加停靠點按鈕
            if (!isAddingWaypoint) {
                IconButton(
                    onClick = onAddWaypoint,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加停靠點",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 詳細說明按鈕
            IconButton(
                onClick = onShowInstructions,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "顯示詳細說明",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 關閉按鈕
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "結束導航",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 路線基本資訊
 */
@Composable
private fun NavigationRouteInfo(
    route: NavigationRoute,
    currentStepIndex: Int
) {
    val currentInstruction = route.instructions.getOrNull(currentStepIndex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 當前指令
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentInstruction?.instruction ?: "準備導航",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (currentInstruction != null) {
                Text(
                    text = "步驟 ${currentInstruction.stepNumber}/${route.instructions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
 * 多點路線途經點列表
 */
@Composable
private fun MultiPointWaypointsList(
    waypoints: List<MapMarker>,
    currentWaypointIndex: Int,
    onRemoveWaypoint: (Int) -> Unit
) {
    Column {
        Text(
            text = "途經點 (${currentWaypointIndex + 1}/${waypoints.size})",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(waypoints) { index, waypoint ->
                WaypointItem(
                    waypoint = waypoint,
                    isCurrent = index == currentWaypointIndex,
                    isCompleted = index < currentWaypointIndex,
                    canRemove = waypoints.size > 1,
                    onRemove = { onRemoveWaypoint(index) }
                )
            }
        }
    }
}

/**
 * 單個途經點項目
 */
@Composable
private fun WaypointItem(
    waypoint: MapMarker,
    isCurrent: Boolean,
    isCompleted: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    val backgroundColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isCurrent -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(2.dp, borderColor)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(
                        when {
                            isCompleted -> R.drawable.ic_check_circle
                            isCurrent -> R.drawable.ic_navigation
                            else -> R.drawable.ic_location
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when {
                        isCompleted -> MaterialTheme.colorScheme.primary
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (canRemove && !isCurrent && !isCompleted) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除途經點",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onRemove() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = waypoint.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

/**
 * 添加停靠點提示
 */
@Composable
private fun AddWaypointHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "點擊地圖上的標記來添加停靠點",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 導航指令詳細列表
 */
@Composable
fun NavigationInstructionsList(
    route: NavigationRoute?,
    currentStepIndex: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (route == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        // 標題列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "導航指引",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "關閉"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 指令列表
        LazyColumn {
            itemsIndexed(route.instructions) { index, instruction ->
                NavigationInstructionItem(
                    instruction = instruction,
                    isCurrentStep = index == currentStepIndex,
                    isCompleted = index < currentStepIndex
                )

                if (index < route.instructions.size - 1) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }
        }
    }
}

/**
 * 單個導航指令項目
 */
@Composable
private fun NavigationInstructionItem(
    instruction: NavigationInstruction,
    isCurrentStep: Boolean,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(
                color = if (isCurrentStep) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 步驟圓點
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color.Green
                        isCurrentStep -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = instruction.stepNumber.toString(),
                    color = if (isCurrentStep) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 指令內容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = instruction.instruction,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            if (instruction.distance > 0 || instruction.estimatedTime > 0) {
                Row {
                    if (instruction.distance > 0) {
                        Text(
                            text = "${instruction.distance.roundToInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (instruction.estimatedTime > 0) {
                        if (instruction.distance > 0) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${instruction.estimatedTime}秒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 方向圖標
        Icon(
            painter = painterResource(id = getDirectionIcon(instruction.direction)),
            contentDescription = null,
            tint = if (isCurrentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 導航搜尋面板
 */
@Composable
fun NavigationSearchPanel(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<MapMarker>,
    onMarkerSelected: (MapMarker) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        // 標題和搜尋框
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "選擇目的地",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "關閉"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 搜尋框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜尋地點") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除"
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 搜尋結果
        if (searchResults.isNotEmpty()) {
            LazyColumn {
                itemsIndexed(searchResults) { _, marker ->
                    NavigationSearchResultItem(
                        marker = marker,
                        onSelected = { onMarkerSelected(marker) }
                    )
                }
            }
        } else if (searchQuery.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "找不到相關地點",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 搜尋結果項目
 */
@Composable
private fun NavigationSearchResultItem(
    marker: MapMarker,
    onSelected: () -> Unit
) {

    val painter = when (val source = marker.imageSource) {
        is ImageSource.FromResource -> {
            painterResource(id = source.resourceId)
        }
        is ImageSource.FromAsset -> {
            rememberAsyncImagePainter(model = "file:///android_asset/${source.assetName}")
        }
        is ImageSource.FromUri -> {
            rememberAsyncImagePainter(model = source.uri)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painter,
                contentDescription = marker.name,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = marker.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (marker.area.isNotEmpty()) {
                    Text(
                        text = marker.area,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (marker.description.isNotEmpty()) {
                    Text(
                        text = marker.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 根據導航方向獲取對應圖標
 */
private fun getDirectionIcon(direction: NavigationDirection): Int {
    return when (direction) {
        NavigationDirection.START -> R.drawable.ic_play_arrow
        NavigationDirection.GO_STRAIGHT -> R.drawable.ic_arrow_upward
        NavigationDirection.TURN_LEFT -> R.drawable.ic_turn_left
        NavigationDirection.TURN_RIGHT -> R.drawable.ic_turn_right
        NavigationDirection.TURN_AROUND -> R.drawable.ic_u_turn_left
        NavigationDirection.UP_STAIRS -> R.drawable.ic_stairs
        NavigationDirection.DOWN_STAIRS -> R.drawable.ic_stairs
        NavigationDirection.TAKE_ELEVATOR -> R.drawable.ic_elevator
        NavigationDirection.TAKE_ESCALATOR -> R.drawable.ic_escalator
        NavigationDirection.ARRIVE -> R.drawable.ic_flag
    }
}