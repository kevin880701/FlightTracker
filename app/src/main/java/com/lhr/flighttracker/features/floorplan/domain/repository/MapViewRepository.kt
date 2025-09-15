package com.lhr.flighttracker.features.floorplan.domain.repository

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import android.graphics.PointF
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute

/**
 * 地圖視圖操作 Repository 介面
 *
 * 負責地圖視圖相關的座標計算和視圖操作，
 * 將具體的地圖實作細節從 Domain 層中抽象出來
 */
interface MapViewRepository {

    /**
     * 計算標記在螢幕上的位置
     *
     * 根據地圖的縮放、旋轉、中心位置等狀態，
     * 計算每個標記在螢幕上的實際顯示位置
     *
     * @param markers 要計算位置的標記列表
     * @param scale 當前地圖縮放比例
     * @param rotation 當前地圖旋轉角度（度）
     * @param mapWidth 地圖視圖寬度
     * @param mapHeight 地圖視圖高度
     * @return 標記ID對應螢幕座標的對應表
     */
    fun calculateMarkerPositions(
        markers: List<MapMarker>,
        scale: Float,
        rotation: Float,
        mapWidth: Float,
        mapHeight: Float
    ): Map<Int, IntOffset>

    /**
     * 計算導航路線在螢幕上的位置
     *
     * 將導航路線的座標點轉換為螢幕顯示座標，
     * 用於在地圖上繪製導航路線覆蓋層
     *
     * @param route 導航路線物件，可為空
     * @param scale 當前地圖縮放比例
     * @param rotation 當前地圖旋轉角度（度）
     * @param mapWidth 地圖視圖寬度
     * @param mapHeight 地圖視圖高度
     * @return 路線點的螢幕座標列表
     */
    fun calculateRoutePositions(
        route: NavigationRoute?,
        scale: Float,
        rotation: Float,
        mapWidth: Float,
        mapHeight: Float
    ): List<PointF>

    /**
     * 以動畫方式移動地圖中心到指定座標
     *
     * @param coordinate 目標座標（在原始圖片座標系統中）
     */
    fun animateToCoordinate(coordinate: Offset)

    /**
     * 重置地圖旋轉角度為 0（正北方向）
     *
     * 使用場景：
     * - 用戶點擊指北針按鈕
     * - 需要恢復默認方向時
     */
    fun resetRotation()
}