package com.lhr.flighttracker.features.floorplan.data.source

import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import kotlinx.coroutines.flow.Flow

/**
 * 位置數據源介面
 */
interface LocationProvider {
    /** * 當前位置標記的數據流
     */
    fun listenToLocation(): Flow<MapMarker?>

    /** * 方向資訊（方位角）的數據流
     */
    fun listenToAzimuth(): Flow<Float>

    /** * 位置服務啟用狀態的數據流
     */
    fun listenToLocationEnabled(): Flow<Boolean>

    /** * 請求一次性的位置更新
     */
    fun requestLocationUpdate()
}