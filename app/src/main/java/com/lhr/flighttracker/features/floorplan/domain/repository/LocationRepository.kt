package com.lhr.flighttracker.features.floorplan.domain.repository

import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import kotlinx.coroutines.flow.Flow

/**
 * 位置服務 Repository 介面
 */
interface LocationRepository {
    /**
     * 獲取位置更新的 Flow
     * @return 位置標記的 Flow，用於持續監聽位置變化
     */
    fun getCurrentLocationFlow(): Flow<MapMarker?>

    /**
     * 獲取方位角更新的 Flow
     * @return 方位角的 Flow，用於指北針和方向指示
     */
    fun getAzimuthFlow(): Flow<Float>

    /**
     * 獲取位置服務啟用狀態的 Flow
     * @return 布林值的 Flow，用於持續監聽服務狀態
     */
    fun isLocationEnabledFlow(): Flow<Boolean>

    /**
     * 請求位置更新
     * 觸發一次性的位置獲取請求
     */
    fun requestLocationUpdate()
}