package com.lhr.flighttracker.features.floorplan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapDefinition
import com.lhr.flighttracker.features.floorplan.domain.entity.MapInitialScaleType
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute

/**
 * 平面圖配置
 */
data class FloorPlanConfiguration(

    /** 地圖定義 - 包含所有地圖相關資訊 */
    val mapDefinition: MapDefinition,

    /** 當前導航步驟索引 */
    val currentNavigationStep: Int = 0,

    /** 是否顯示導航路線 */
    val showNavigationRoute: Boolean = true,

    /** 是否啟用縮放 */
    val zoomEnabled: Boolean = true,

    /** 是否啟用平移 */
    val panEnabled: Boolean = true,

    /** 是否啟用旋轉 */
    val rotationEnabled: Boolean = true,

    /** 是否顯示指北針 */
    val showCompass: Boolean = true,

    /** 初始縮放類型 */
    val initialScaleType: MapInitialScaleType = MapInitialScaleType.CENTER_INSIDE,

    /** 最小縮放比例因子 */
    val minScaleFactor: Float = 0.8f,

    /** 旋轉靈敏度 */
    val rotationSensitivity: Float = 0.8f,

    /**
     * 自定義一般標記渲染器
     * 如果為 null，則使用預設的 MapMarkerItem
     *
     * @param marker 標記物件
     * @param screenPosition 螢幕座標
     */
    val markerComposable: (@Composable (MapMarker, IntOffset) -> Unit)? = null,

    /**
     * 自定義當前位置標記渲染器
     * 如果為 null，則使用預設的 CurrentLocationMarkerItem
     *
     * @param marker 當前位置標記物件
     * @param screenPosition 螢幕座標
     * @param deviceAzimuth 設備方位角
     * @param mapRotation 地圖旋轉角度
     */
    val currentLocationComposable: (@Composable (MapMarker, IntOffset, Float, Float) -> Unit)? = null,

    /**
     * 自定義指北針渲染器
     * 如果為 null，則使用預設的 CompassWidget
     *
     * @param azimuth 設備方位角
     * @param mapRotation 地圖旋轉角度
     */
    val compassComposable: (@Composable (azimuth: Float, mapRotation: Float) -> Unit)? = null,

    /**
     * 指北針在螢幕上的對齊位置
     * 預設為 Alignment.BottomEnd
     */
    val compassAlignment: Alignment = Alignment.BottomEnd,

    /**
     * 自定義位置按鈕渲染器
     * 如果為 null，則使用預設的 LocationButton
     *
     * @param isLocationEnabled 位置服務是否啟用
     * @param hasCurrentLocation 是否有當前位置
     * @param onLocationClick 位置按鈕點擊事件回調
     */
    val locationComposable: (@Composable (isLocationEnabled: Boolean, hasCurrentLocation: Boolean) -> Unit)? = null,

    /**
     * 位置按鈕在螢幕上的對齊位置
     * 預設為 Alignment.BottomEnd
     */
    val locationAlignment: Alignment = Alignment.BottomEnd
){
    // 提供方便的屬性存取
    val imageSource get() = mapDefinition.imageSource
    val markers get() = mapDefinition.markers
    val pathNetwork get() = mapDefinition.pathNetwork
    val mapBounds get() = mapDefinition.mapBounds
}