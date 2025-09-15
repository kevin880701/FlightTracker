package com.lhr.flighttracker.features.floorplan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import android.graphics.PointF
import android.util.Log
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.lhr.flighttracker.features.floorplan.data.repository.MapViewRepositoryImpl
import com.lhr.flighttracker.features.floorplan.domain.PathfindingService
import com.lhr.flighttracker.features.floorplan.domain.entity.GpsCoordinate
import com.lhr.flighttracker.features.floorplan.domain.entity.MapDefinition
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNetwork
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNode
import com.lhr.flighttracker.features.floorplan.domain.repository.LocationRepository
import com.lhr.flighttracker.features.floorplan.domain.repository.MapViewRepository
import com.lhr.flighttracker.features.floorplan.domain.usecase.CalculateMarkersPositionsUseCase
import com.lhr.flighttracker.features.floorplan.presentation.state.FloorPlanState
import javax.inject.Inject

/**
 * 平面圖 ViewModel
 *
 * 負責平面圖的狀態管理和業務邏輯協調，遵循 MVVM 架構模式：
 * - 管理單一的 UI 狀態（FloorPlanUiState）
 * - 協調各種 UseCase 的執行
 * - 處理用戶交互事件
 * - 不直接操作 Android View
 */
@HiltViewModel
class FloorPlanViewModel @Inject constructor(
    /** 位置服務 Repository */
    private val locationRepository: LocationRepository,
    /** 地圖視圖操作 Repository */
    private val mapViewRepository: MapViewRepository,
    /** 計算標記位置用例 */
    private val calculateMarkersPositionsUseCase: CalculateMarkersPositionsUseCase,
    /** 路徑尋找服務 */
    private val pathfindingService: PathfindingService
) : ViewModel() {

    /**
     * 平面圖 UI 狀態的唯一來源
     */
    private val _uiState = MutableStateFlow(FloorPlanState())
    val uiState: StateFlow<FloorPlanState> = _uiState.asStateFlow()

    init {
        observeLocationUpdates()
    }

    /**
     * 觀察監聽位置與感測器的更新
     */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            try {
                val enabledFlow = locationRepository.isLocationEnabledFlow()
                val azimuthFlow = locationRepository.getAzimuthFlow()

                enabledFlow.flatMapLatest { isEnabled ->
                    if (isEnabled) {
                        combine(
                            locationRepository.getCurrentLocationFlow(),
                            azimuthFlow,
                            flowOf(isEnabled)
                        ) { location, azimuth, enabled ->
                            Triple(location, azimuth, enabled)
                        }
                    } else {
                        combine(
                            flowOf(null),
                            azimuthFlow,
                            flowOf(isEnabled)
                        ) { location, azimuth, enabled ->
                            Triple(location, azimuth, enabled)
                        }
                    }
                }.collect { (location, azimuth, isEnabled) ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            currentLocationMarker = location,
                            deviceAzimuth = azimuth,
                            isLocationEnabled = isEnabled
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("FloorPlanVM", "Location monitoring error", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearNavigation() {
        _uiState.update { it.copy(navigationRoute = null) }
    }

    fun setMapDefinition(mapDefinition: MapDefinition) {
        _uiState.update { currentState ->
            currentState.copy(
                allMarkers = mapDefinition.markers,
                mapDefinition = mapDefinition
            )
        }
        updateAllPositions()
    }

    fun setMapView(mapView: SubsamplingScaleImageView) {
        (mapViewRepository as? MapViewRepositoryImpl)?.setMapView(mapView)
        _uiState.update { it.copy(isMapViewConnected = true) }

        if (mapView.isReady) {
            updateAllPositions()
        }
    }

    fun updateMapState(
        newScale: Float? = null,
        newRotation: Float? = null,
        newCenter: PointF? = null
    ) {
        _uiState.update { currentState ->
            val hasScaleChanged = newScale != null && newScale != currentState.scale
            val hasRotationChanged = newRotation != null && newRotation != currentState.rotation
            val hasCenterChanged = newCenter != null && newCenter != currentState.center

            val hasChanged = hasScaleChanged || hasRotationChanged || hasCenterChanged

            if (hasChanged) {
                updateAllPositions()
                currentState.copy(
                    scale = newScale ?: currentState.scale,
                    rotation = newRotation ?: currentState.rotation,
                    center = newCenter ?: currentState.center
                )
            } else {
                currentState
            }
        }
    }

    fun resetRotation() {
        _uiState.update { it.copy(rotation = 0f) }
        updateAllPositions()
    }

    fun setCurrentMarkers(markers: List<MapMarker>) {
        _uiState.update { it.copy(allMarkers = markers) }
        updateAllPositions()
    }

    fun setNavigationRoute(route: NavigationRoute?) {
        _uiState.update { it.copy(navigationRoute = route) }
        updateAllPositions()
    }

    fun animateToCoordinate(coordinate: Offset) {
        mapViewRepository.animateToCoordinate(Offset(coordinate.x, coordinate.y))
    }

    fun onMarkerClick(marker: MapMarker) {
        _uiState.update { it.copy(selectedMarker = marker) }
    }

    internal fun updateAllPositions() {
        updateMarkerPositions()
        updateNavigationRoutePositions()
    }

    private fun updateMarkerPositions() {
        val currentState = _uiState.value
        if (currentState.allMarkers.isNotEmpty() && currentState.isMapViewConnected) {
            val mapBounds = currentState.mapDefinition?.mapBounds
            val newPositions = calculateMarkersPositionsUseCase(
                markers = currentState.allMarkers,
                scale = currentState.scale,
                rotation = currentState.rotation,
                mapWidth = mapBounds?.mapWidth?: 0f,
                mapHeight = mapBounds?.mapHeight?: 0f
            )
            _uiState.update { it.copy(markerScreenPositions = newPositions) }
        }
    }

    private fun updateNavigationRoutePositions() {
        val currentState = _uiState.value
        if (currentState.isMapViewConnected) {
            val mapBounds = currentState.mapDefinition?.mapBounds
            val newPositions = mapViewRepository.calculateRoutePositions(
                route = currentState.navigationRoute,
                scale = currentState.scale,
                rotation = currentState.rotation,
                mapWidth = mapBounds?.mapWidth?: 0f,
                mapHeight = mapBounds?.mapHeight?: 0f
            )
            _uiState.update { it.copy(navigationRouteScreenPositions = newPositions) }
        }
    }

    fun onLocationButtonClick(): Boolean {
        val marker = _uiState.value.currentLocationMarker
        return if (marker != null) {
            animateToCoordinate(marker.coordinates)
            true
        } else {
            requestLocationUpdate()
            false
        }
    }

    fun requestLocationUpdate() {
        locationRepository.requestLocationUpdate()
    }

    fun findNearestPathNode(gpsCoordinate: GpsCoordinate, maxDistance: Float = 200f): PathNode? {
        return _uiState.value.mapDefinition?.findNearestPathNode(gpsCoordinate, maxDistance)
    }

    fun findNearestPathNode(pixelCoordinate: Offset, maxDistance: Float = 200f): PathNode? {
        return _uiState.value.mapDefinition?.findNearestPathNode(pixelCoordinate, maxDistance)
    }

    fun searchMarkers(query: String): List<MapMarker> {
        return _uiState.value.mapDefinition?.searchMarkers(query) ?: emptyList()
    }

    // ================================
    // 新增的導航相關方法 - 供 Controller 調用
    // ================================

    /**
     * 計算到指定標記的路線
     *
     * @param destination 目標標記
     * @param pathNetwork 路徑網絡
     * @return 計算是否成功
     */
    suspend fun calculateRouteTo(destination: MapMarker, pathNetwork: PathNetwork): Boolean {
        return try {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentLocation = _uiState.value.currentLocationMarker?.coordinates
            if (currentLocation == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "無法取得當前位置"
                    )
                }
                return false
            }

            val route = pathfindingService.findPathToMarker(
                userLocation = currentLocation,
                targetMarker = destination,
                network = pathNetwork
            )

            if (route != null) {
                _uiState.update {
                    it.copy(
                        navigationRoute = route,
                        selectedMarker = destination,
                        isLoading = false,
                        error = null
                    )
                }
                updateAllPositions()
                true
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "無法計算路線"
                    )
                }
                false
            }
        } catch (e: Exception) {
            Log.e("FloorPlanVM", "Route calculation failed", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "路線計算失敗"
                )
            }
            false
        }
    }

    /**
     * 計算多點導航路線
     *
     * @param destinations 目標標記列表
     * @param pathNetwork 路徑網絡
     * @return 計算是否成功
     */
    suspend fun calculateMultiPointRoute(destinations: List<MapMarker>, pathNetwork: PathNetwork): Boolean {
        return try {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val currentLocation = _uiState.value.currentLocationMarker?.coordinates
            if (currentLocation == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "無法取得當前位置"
                    )
                }
                return false
            }

            // 簡化版本：計算到第一個目標的路線
            // 後續可以擴展為真正的多點路線規劃
            val firstDestination = destinations.firstOrNull()
            if (firstDestination != null) {
                return calculateRouteTo(firstDestination, pathNetwork)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "沒有指定目標"
                    )
                }
                false
            }
        } catch (e: Exception) {
            Log.e("FloorPlanVM", "Multi-point route calculation failed", e)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "多點路線計算失敗"
                )
            }
            false
        }
    }

    /**
     * 設置錯誤訊息
     */
    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }

    /**
     * 清除錯誤訊息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}