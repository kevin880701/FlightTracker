package com.lhr.flighttracker.features.map.presentation.widget

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource
import kotlin.math.roundToInt

/**
 * 可拖曳排序的途經點列表組件（垂直佈局，適用於規劃階段）
 * 使用長按後拖曳的方式，避免與滾動手勢衝突
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableMapMarkersList(
    waypoints: List<MapMarker>,
    isReordering: Boolean,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showRemoveButtons: Boolean = true,
) {
    var draggedItemIndex by remember { mutableIntStateOf(-1) }
    var targetIndex by remember { mutableIntStateOf(-1) }
    var currentDragOffset by remember { mutableFloatStateOf(0f) }
    val hapticFeedback = LocalHapticFeedback.current
    val itemHeight = 72.dp // 項目高度
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    Column(modifier = modifier) {
        // 標題和提示
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isReordering) "長按並拖曳調整順序" else "途經點列表",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            if (isReordering) {
                Text(
                    text = "拖曳模式",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isReordering) "長按並拖曳調整順序" else "途經點列表",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

            if (isReordering) {
                Text(
                    text = "拖曳模式",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 途經點列表
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            waypoints.forEachIndexed { index, waypoint ->
                val isDragging = draggedItemIndex == index

                val offsetY by animateDpAsState(
                    targetValue = when {
                        draggedItemIndex == -1 || targetIndex == -1 -> 0.dp

                        draggedItemIndex < targetIndex && index > draggedItemIndex && index <= targetIndex -> -itemHeight
                        draggedItemIndex > targetIndex && index < draggedItemIndex && index >= targetIndex -> itemHeight

                        else -> 0.dp
                    },
                    animationSpec = tween(300),
                    label = "item_offset_$index"
                )

                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 16.dp else 2.dp,
                    animationSpec = tween(200),
                    label = "elevation_$index"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = if (isDragging) {
                                currentDragOffset
                            } else {
                                offsetY.toPx()
                            }
                            scaleX = if (isDragging) 1.05f else 1f
                            scaleY = if (isDragging) 1.05f else 1f
                        }
                        .zIndex(if (isDragging) 10f else 0f)
                ) {
                    ReorderableWaypointItem(
                        waypoint = waypoint,
                        index = index,
                        isReordering = isReordering,
                        isDragging = isDragging,
                        elevation = elevation,
                        onRemove = if (showRemoveButtons && waypoints.size > 1 && !isReordering) {
                            { onRemove(index) }
                        } else null,
                        onStartDrag = {
                            if (isReordering) {
                                Log.d("ReorderableList", "Start dragging item at index: $index")
                                draggedItemIndex = index
                                targetIndex = index
                                currentDragOffset = 0f
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onDrag = { dragDelta ->
                            currentDragOffset += dragDelta

                            val draggedSteps = (currentDragOffset / itemHeightPx).roundToInt()
                            val newTargetIndex = (draggedItemIndex + draggedSteps).coerceIn(0, waypoints.size - 1)

                            if (newTargetIndex != targetIndex) {
                                targetIndex = newTargetIndex
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onEndDrag = {
                            Log.d("ReorderableList", "End drag - from: $draggedItemIndex to: $targetIndex")
                            if (draggedItemIndex != -1 && targetIndex != -1 && draggedItemIndex != targetIndex) {
                                Log.d("ReorderableList", "Calling onReorder($draggedItemIndex, $targetIndex)")
                                onReorder(draggedItemIndex, targetIndex)
                            }
                            // 重置所有狀態
                            draggedItemIndex = -1
                            targetIndex = -1
                            currentDragOffset = 0f
                        }
                    )
                }
            }
        }
    }
}

/**
 * 單個可拖曳的途經點項目 - 使用長按拖曳
 */
@Composable
private fun ReorderableWaypointItem(
    waypoint: MapMarker,
    index: Int,
    isReordering: Boolean,
    isDragging: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    onRemove: (() -> Unit)?,
    onStartDrag: () -> Unit,
    onDrag: (Float) -> Unit,
    onEndDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter = when (val source = waypoint.imageSource) {
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
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(12.dp))
            .then(
                if (isReordering) {
                    Modifier
                        .border(
                            width = if (isDragging) 2.dp else 0.dp,
                            color = if (isDragging) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    Log.d("ReorderableItem", "Drag started for item: ${waypoint.name}")
                                    onStartDrag()
                                },
                                onDragEnd = {
                                    Log.d("ReorderableItem", "Drag ended for item: ${waypoint.name}")
                                    onEndDrag()
                                },
                                onDragCancel = {
                                    Log.d("ReorderableItem", "Drag cancelled for item: ${waypoint.name}")
                                    onEndDrag()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.y)
                                }
                            )
                        }
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> MaterialTheme.colorScheme.primaryContainer
                isReordering -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReordering) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "長按拖曳調整順序",
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(if (isDragging) 1f else 0.7f),
                    tint = if (isDragging)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = waypoint.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (waypoint.area.isNotEmpty()) {
                    Text(
                        text = waypoint.area,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onRemove != null && !isReordering) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "移除途經點",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// 輔助函數
private fun Modifier.alpha(alpha: Float): Modifier = this.graphicsLayer { this.alpha = alpha }

/**
 * 水平滾動的途經點列表（適用於導航中顯示）
 */
@Composable
fun HorizontalWaypointsList(
    waypoints: List<MapMarker>,
    currentWaypointIndex: Int,
    isReordering: Boolean = false,
    onReorder: ((fromIndex: Int, toIndex: Int) -> Unit)? = null,
    onRemove: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "途經點 (${currentWaypointIndex + 1}/${waypoints.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isReordering) {
                Text(
                    text = "調整模式",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(waypoints, key = { _, item -> item.id }) { index, waypoint ->
                HorizontalWaypointItem(
                    waypoint = waypoint,
                    index = index,
                    isCurrent = index == currentWaypointIndex,
                    isCompleted = index < currentWaypointIndex,
                    isReordering = isReordering,
                    canRemove = (waypoints.size > 1) && (onRemove != null),
                    onRemove = if (onRemove != null) { { onRemove(index) } } else null
                )
            }
        }
    }
}

/**
 * 水平途經點項目
 */
@Composable
private fun HorizontalWaypointItem(
    waypoint: MapMarker,
    index: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isReordering: Boolean,
    canRemove: Boolean,
    onRemove: (() -> Unit)?
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
                if (isReordering) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "拖曳調整",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
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
                }

                if (canRemove && onRemove != null && !isCurrent && !isCompleted) {
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
 * 順序調整控制面板
 */
@Composable
fun WaypointOrderControls(
    isReordering: Boolean,
    onStartReordering: () -> Unit,
    onStopReordering: () -> Unit,
    onOptimizeOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isReordering) "長按項目可拖曳調整" else "路線順序",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isReordering) {
                    TextButton(onClick = onStopReordering) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("完成")
                    }
                } else {
                    TextButton(onClick = onOptimizeOrder) {
                        Icon(
                            painter = painterResource(R.drawable.ic_auto_awesome),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("優化")
                    }

                    TextButton(onClick = onStartReordering) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("調整")
                    }
                }
            }
        }
    }
}