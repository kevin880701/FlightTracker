package com.lhr.flighttracker.features.floorplan.data.repository

import com.lhr.flighttracker.features.floorplan.data.source.LocationProvider
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 位置服務 Repository 實作
 *
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationProvider: LocationProvider?
) : LocationRepository {

    /**
     * 獲取當前位置的 Flow
     * 直接從 locationProvider 代理數據流
     */
    override fun getCurrentLocationFlow(): Flow<MapMarker?> {
        return locationProvider?.listenToLocation() ?: flowOf(null)
    }

    /**
     * 獲取方位角的 Flow
     * 直接從 locationProvider 代理數據流
     */
    override fun getAzimuthFlow(): Flow<Float> {
        return locationProvider?.listenToAzimuth() ?: flowOf(0f)
    }

    /**
     * 獲取位置服務啟用狀態的 Flow
     * 直接從 locationProvider 代理數據流
     */
    override fun isLocationEnabledFlow(): Flow<Boolean> {
        return locationProvider?.listenToLocationEnabled() ?: flowOf(false)
    }

    /**
     * 請求位置更新
     * 將請求委託給 LocationProvider
     */
    override fun requestLocationUpdate() {
        locationProvider?.requestLocationUpdate()
    }
}