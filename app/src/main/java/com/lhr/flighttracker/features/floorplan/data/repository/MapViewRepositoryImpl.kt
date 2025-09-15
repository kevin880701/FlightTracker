package com.lhr.flighttracker.features.floorplan.data.repository

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import android.graphics.PointF
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute
import com.lhr.flighttracker.features.floorplan.domain.repository.MapViewRepository
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * 地圖視圖 Repository 實作
 *
 * 負責實際的座標計算和地圖視圖操作，
 * 封裝了對 SubsamplingScaleImageView 的直接操作
 */
@Singleton
class MapViewRepositoryImpl @Inject constructor() : MapViewRepository {

    /**
     * 地圖視圖引用
     *
     * 持有對實際地圖視圖的引用，用於座標轉換和操作。
     * 這是唯一直接接觸 Android View 的地方，
     * 符合 Clean Architecture 的依賴方向原則
     */
    private var mapViewRef: SubsamplingScaleImageView? = null

    /**
     * 設置地圖視圖引用
     *
     * 當地圖視圖創建時調用，建立 Repository 與實際視圖的連接
     *
     * @param mapView SubsamplingScaleImageView 實例
     */
    fun setMapView(mapView: SubsamplingScaleImageView) {
        this.mapViewRef = mapView
    }

    /**
     * 計算標記在螢幕上的位置
     *
     * 實作標記位置計算的核心邏輯：
     * 1. 將圖片座標轉換為視圖座標
     * 2. 根據地圖旋轉角度調整座標
     * 3. 返回最終的螢幕座標
     *
     * @param markers 要計算位置的標記列表
     * @param scale 當前地圖縮放比例
     * @param rotation 當前地圖旋轉角度（度）
     * @param mapWidth 地圖視圖寬度
     * @param mapHeight 地圖視圖高度
     * @return 標記ID對應螢幕座標的對應表
     */
    override fun calculateMarkerPositions(
        markers: List<MapMarker>,
        scale: Float,
        rotation: Float,
        mapWidth: Float,
        mapHeight: Float
    ): Map<Int, IntOffset> {
        val mapView = mapViewRef

        if (mapView?.isReady != true) {
            return emptyMap()
        }

        val positions = mutableMapOf<Int, IntOffset>()

        // 獲取實際的視圖尺寸
        val viewWidth = mapView.width
        val viewHeight = mapView.height

        if (viewWidth <= 0 || viewHeight <= 0) {
            return emptyMap()
        }

        // 視圖中心點
        val viewCenterX = viewWidth / 2f
        val viewCenterY = viewHeight / 2f

        // 遍歷所有標記，計算每個標記的螢幕位置
        markers.forEach { marker ->
            // 將圖片座標轉換為視圖座標
            val viewCoord = mapView.sourceToViewCoord(marker.coordinates.x, marker.coordinates.y)
            if (viewCoord != null) {
                // 根據地圖旋轉角度調整座標
                val rotatedPosition = rotatePoint(
                    viewCoord.x,
                    viewCoord.y,
                    viewCenterX,
                    viewCenterY,
                    rotation
                )
                // 儲存最終的螢幕座標
                positions[marker.id] = IntOffset(
                    rotatedPosition.x.toInt(),
                    rotatedPosition.y.toInt()
                )
            }
        }

        return positions
    }

    /**
     * 計算導航路線在螢幕上的位置
     *
     * 實作導航路線位置計算的邏輯：
     * 1. 檢查路線是否存在
     * 2. 遍歷路線上的每個座標點
     * 3. 將每個點轉換為螢幕座標
     *
     * @param route 導航路線物件，可為空
     * @param scale 當前地圖縮放比例
     * @param rotation 當前地圖旋轉角度（度）
     * @param mapWidth 地圖視圖寬度
     * @param mapHeight 地圖視圖高度
     * @return 路線點的螢幕座標列表
     */
    override fun calculateRoutePositions(
        route: NavigationRoute?,
        scale: Float,
        rotation: Float,
        mapWidth: Float,
        mapHeight: Float
    ): List<PointF> {
        val mapView = mapViewRef
        if (mapView?.isReady != true || route == null) return emptyList()

        // 獲取實際的視圖尺寸
        val viewWidth = mapView.width
        val viewHeight = mapView.height

        if (viewWidth <= 0 || viewHeight <= 0) {
            return emptyList()
        }

        // 視圖中心點
        val viewCenterX = viewWidth / 2f
        val viewCenterY = viewHeight / 2f

        // 將路線上的每個座標點轉換為螢幕座標
        return route.getPathCoordinates().mapNotNull { coordinate ->
            val viewCoord = mapView.sourceToViewCoord(coordinate.x, coordinate.y)
            if (viewCoord != null) {
                rotatePoint(
                    viewCoord.x,
                    viewCoord.y,
                    viewCenterX,
                    viewCenterY,
                    rotation
                )
            } else null
        }
    }

    /**
     * 以動畫方式移動地圖中心到指定座標
     *
     * 委託給 SubsamplingScaleImageView 執行平滑的動畫移動
     *
     * @param coordinate 目標座標（在原始圖片座標系統中）
     */
    override fun animateToCoordinate(coordinate: Offset) {
        mapViewRef?.let { view ->
            view.animateCenter(PointF(coordinate.x, coordinate.y))?.start()
        }
    }

    /**
     * 重置地圖旋轉角度
     *
     * 這裡只是介面實作，實際的重置邏輯由 ViewModel 處理，
     * 因為旋轉狀態由 ViewModel 管理
     */
    override fun resetRotation() {
        // 實際重置由 ViewModel 處理
    }

    /**
     * 點繞中心旋轉的輔助函數
     *
     * 使用旋轉矩陣計算點在旋轉後的新位置：
     * 1. 將點移動到原點（減去中心座標）
     * 2. 應用旋轉變換
     * 3. 將點移回原位置（加上中心座標）
     *
     * @param x 點的原始 X 座標
     * @param y 點的原始 Y 座標
     * @param centerX 旋轉中心的 X 座標
     * @param centerY 旋轉中心的 Y 座標
     * @param angleDegrees 旋轉角度（度）
     * @return 旋轉後的點座標
     */
    private fun rotatePoint(
        x: Float,
        y: Float,
        centerX: Float,
        centerY: Float,
        angleDegrees: Float
    ): PointF {
        // 將角度轉換為弧度
        val angleRad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angleRad).toFloat()
        val sin = sin(angleRad).toFloat()

        // 將點移動到原點
        val translatedX = x - centerX
        val translatedY = y - centerY

        // 應用旋轉變換
        val rotatedX = translatedX * cos - translatedY * sin
        val rotatedY = translatedX * sin + translatedY * cos

        // 將點移回原位置
        return PointF(rotatedX + centerX, rotatedY + centerY)
    }
}