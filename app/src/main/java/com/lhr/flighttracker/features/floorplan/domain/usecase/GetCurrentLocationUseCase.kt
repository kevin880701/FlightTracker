package com.lhr.flighttracker.features.floorplan.domain.usecase

import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 獲取當前位置用例
 *
 * 封裝獲取用戶當前位置的業務邏輯，
 * 將位置資料轉換為地圖標記格式
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * 執行獲取當前位置邏輯
     *
     * @return 當前位置標記的 Flow
     */
    operator fun invoke(): Flow<MapMarker?> {
        return locationRepository.getCurrentLocationFlow()
    }
}