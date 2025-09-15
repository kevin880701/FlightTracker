package com.lhr.flighttracker.features.floorplan.presentation

import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.presentation.viewmodel.FloorPlanViewModel
import kotlinx.coroutines.launch

class FloorPlanController internal constructor(
    internal val viewModel: FloorPlanViewModel
) {
    val uiState = viewModel.uiState

    /**
     * 清除目前的導航路線
     */
    fun clearNavigation() {
        viewModel.clearNavigation()
    }

    /**
     * 請求一次性的位置更新
     */
    fun requestLocationUpdate() {
        viewModel.requestLocationUpdate()
    }

    /**
     * 開始導航到指定標記
     *
     * @param destination 目標標記
     */
    fun navigateToMarker(destination: MapMarker) {
        val pathNetwork = viewModel.uiState.value.mapDefinition?.pathNetwork
        if (pathNetwork == null) {
            viewModel.setError("地圖路徑網絡未初始化")
            return
        }

        viewModel.viewModelScope.launch {
            val success = viewModel.calculateRouteTo(destination, pathNetwork)
            if (!success) {
                //TODO: 錯誤處理已在 ViewModel 中完成，這裡可以添加額外的處理邏輯
            }
        }
    }

    /**
     * 重置旋轉角度
     */
    fun resetRotation() {
        viewModel.resetRotation()
    }

    /**
     * 開始多點導航
     *
     * @param destinations 目標標記列表
     */
    fun navigateToMultipleMarkers(destinations: List<MapMarker>) {
        val pathNetwork = viewModel.uiState.value.mapDefinition?.pathNetwork
        if (pathNetwork == null) {
            viewModel.setError("地圖路徑網絡未初始化")
            return
        }

        viewModel.viewModelScope.launch {
            val success = viewModel.calculateMultiPointRoute(destinations, pathNetwork)
            if (!success) {
                // 錯誤處理已在 ViewModel 中完成
            }
        }
    }

    /**
     * 搜尋標記
     *
     * @param query 搜尋查詢字串
     * @return 匹配的標記列表
     */
    fun searchMarkers(query: String): List<MapMarker> {
        return viewModel.searchMarkers(query)
    }

    /**
     * 清除錯誤訊息
     */
    fun clearError() {
        viewModel.clearError()
    }
}