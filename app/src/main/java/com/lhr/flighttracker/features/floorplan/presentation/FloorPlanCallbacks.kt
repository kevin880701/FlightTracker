package com.lhr.flighttracker.features.floorplan.presentation

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker

/**
 * 平面圖事件回調
 */
data class FloorPlanCallbacks(

    /** 地圖點擊事件 */
    val onMapClick: (Offset) -> Unit = {},

    /** 縮放變化事件 */
    val onScaleChanged: (Float) -> Unit = {},

    /** 旋轉變化事件 */
    val onRotationChanged: (Float) -> Unit = {},

    /** 地圖中心變化事件 */
    val onCenterChanged: (PointF) -> Unit = {},
)