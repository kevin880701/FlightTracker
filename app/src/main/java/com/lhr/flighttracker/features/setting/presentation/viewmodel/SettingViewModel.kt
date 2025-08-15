package com.lhr.flighttracker.features.setting.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.core.room.entity.TrackedFlightEntity
import com.lhr.flighttracker.core.utils.LanguageManager
import com.lhr.flighttracker.core.utils.ThemeManager
import com.lhr.flighttracker.features.flightScheduled.data.repositories.FlightTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val themeManager: ThemeManager,
    private val flightTrackingRepository: FlightTrackingRepository
) : ViewModel() {

    val languageCodeFlow: Flow<String> = languageManager.getLanguageCodeFlow()

    // 已追蹤的航班列表
    val trackedFlights: StateFlow<List<TrackedFlightEntity>> =
        flightTrackingRepository.getTrackedFlights()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * 取消追蹤指定航班
     */
    fun untrackFlight(flightId: String) {
        viewModelScope.launch {
            flightTrackingRepository.untrackFlight(flightId)
        }
    }

    /**
     * 更新應用程式語言設定
     */
    suspend fun updateLanguage(languageCode: String) {
        languageManager.setLanguage(languageCode)
    }

    /**
     * 更新應用程式主題設定
     */
    fun updateTheme(themeCode: String) {
        viewModelScope.launch {
            themeManager.setTheme(themeCode)
        }
    }
}