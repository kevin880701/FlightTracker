package com.lhr.flighttracker.features.floorplan.domain.usecase

import androidx.compose.ui.unit.IntOffset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.repository.MapViewRepository
import javax.inject.Inject

/**
 * 計算標記位置用例
 *
 * 封裝標記位置計算的業務邏輯，
 * 協調地圖狀態和標記資料的轉換
 */
class CalculateMarkersPositionsUseCase @Inject constructor(
    private val mapViewRepository: MapViewRepository
) {
    /**
     * 執行標記位置計算
     *
     * @param markers 標記列表
     * @param scale 地圖縮放比例
     * @param rotation 地圖旋轉角度
     * @param centerX 地圖中心 X 座標
     * @param centerY 地圖中心 Y 座標
     * @param mapWidth 地圖寬度
     * @param mapHeight 地圖高度
     * @return 標記螢幕位置對應表
     */
    operator fun invoke(
        markers: List<MapMarker>,
        scale: Float,
        rotation: Float,
        mapWidth: Float,
        mapHeight: Float
    ): Map<Int, IntOffset> {
        return mapViewRepository.calculateMarkerPositions(
            markers, scale, rotation, mapWidth, mapHeight
        )
    }
}