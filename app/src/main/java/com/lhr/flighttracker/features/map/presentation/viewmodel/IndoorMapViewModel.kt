package com.lhr.flighttracker.features.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.core.utils.LocationServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 重構後的室內地圖 ViewModel
 *
 * 職責簡化：
 * - 只負責位置服務管理
 * - 移除所有導航相關邏輯（已移至 FloorPlanController）
 * - 專注於與 LocationServiceManager 的交互
 *
 * 變更說明：
 * 1. 移除 NavigationViewModel 相關功能
 * 2. 保留位置請求和位置服務狀態管理
 * 3. 與 FloorPlanController 配合使用
 */
@HiltViewModel
class IndoorMapViewModel @Inject constructor(
    private val locationServiceManager: LocationServiceManager
) : ViewModel() {

    companion object {
        private const val PERMISSION_GRANTED_TIMEOUT = 10000L
    }

    // ================================
    // 位置服務相關狀態
    // ================================

    /**
     * 是否正在請求位置
     */
    private val _isRequestingLocation = MutableStateFlow(false)
    val isRequestingLocation: StateFlow<Boolean> = _isRequestingLocation.asStateFlow()

    /**
     * 位置服務是否啟用
     */
    val isLocationEnabled = locationServiceManager.isLocationEnabled
    private var locationRequestTimeoutJob: Job? = null

    init {
        observeLocationServiceState()
    }

    /**
     * 觀察位置服務狀態變化
     */
    private fun observeLocationServiceState() {
        viewModelScope.launch {
            isLocationEnabled.collectLatest { enabled ->
                // 如果位置服務被禁用，重置請求狀態
                if (!enabled && _isRequestingLocation.value) {
                    resetLocationRequestState()
                }
            }
        }
    }

    /**
     * 在權限已授予的情況下請求位置
     *
     * 這個方法應該在權限檢查通過後調用
     */
    fun requestLocationWithPermission() {
        if (_isRequestingLocation.value) return

        _isRequestingLocation.value = true
        locationServiceManager.startLocationTracking()
        startLocationRequestTimeout()
    }

    /**
     * 停止位置追蹤
     */
    fun stopLocationTracking() {
        _isRequestingLocation.value = false
        locationServiceManager.stopLocationTracking()
        locationRequestTimeoutJob?.cancel()
    }

    /**
     * 手動重置位置請求狀態
     */
    fun resetLocationRequestState() {
        _isRequestingLocation.value = false
        locationRequestTimeoutJob?.cancel()
    }

    /**
     * 啟動位置請求超時機制
     */
    private fun startLocationRequestTimeout() {
        locationRequestTimeoutJob?.cancel()
        locationRequestTimeoutJob = viewModelScope.launch {
            delay(PERMISSION_GRANTED_TIMEOUT)
            if (_isRequestingLocation.value) {
                resetLocationRequestState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationServiceManager.stopLocationTracking()
        locationRequestTimeoutJob?.cancel()
    }
}