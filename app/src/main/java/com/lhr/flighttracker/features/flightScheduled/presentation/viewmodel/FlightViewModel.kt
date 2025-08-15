package com.lhr.flighttracker.features.flightScheduled.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.notification.NotificationScheduler
import com.lhr.flighttracker.core.utils.LanguageManager
import com.lhr.flighttracker.core.permission.PermissionGuideManager
import com.lhr.flighttracker.core.utils.ResourceProvider.Companion.getString
import com.lhr.flighttracker.features.flightScheduled.data.repositories.FlightTrackingRepository
import com.lhr.flighttracker.features.flightScheduled.domain.entity.FlightStatus
import com.lhr.flighttracker.features.flightScheduled.domain.usecase.GetFlightScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val getFlightScheduleUseCase: GetFlightScheduleUseCase,
    private val flightTrackingRepository: FlightTrackingRepository,
    private val languageManager: LanguageManager,
    private val notificationScheduler: NotificationScheduler,
    private val permissionGuideManager: PermissionGuideManager
) : ViewModel() {

    val shouldShowPermissionGuide: StateFlow<Boolean> = permissionGuideManager.shouldShowPermissionGuide

    val languageCodeFlow: Flow<String> = languageManager.getLanguageCodeFlow()

    // 儲存所有未篩選的航班資料
    private val _allFlights = MutableStateFlow<List<FlightStatus>>(emptyList())

    private val _filterFlightNumber = MutableStateFlow<String?>(null)
    val filterFlightNumber: StateFlow<String?> = _filterFlightNumber.asStateFlow()

    private val _filterUpAirport = MutableStateFlow<String?>(null)
    val filterUpAirport: StateFlow<String?> = _filterUpAirport.asStateFlow()

    // 儲存被追蹤航班 ID 的 StateFlow
    private val _trackedFlightIds = MutableStateFlow<Set<String>>(emptySet())
    val trackedFlightIds: StateFlow<Set<String>> = _trackedFlightIds.asStateFlow()

    // 有航班資料和篩選條件的組合
    val flights: StateFlow<List<FlightStatus>> = combine(
        _allFlights,
        _filterFlightNumber,
        _filterUpAirport
    ) { allFlights, flightNumberFilter, upAirportFilter ->
        if (flightNumberFilter.isNullOrBlank() && upAirportFilter.isNullOrBlank()) {
            allFlights
        } else {
            val currentLang = languageManager.getLanguageCode()

            allFlights.filter { flight ->
                val matchFlightNumber = flightNumberFilter.isNullOrBlank() ||
                        flight.airLineNum.contains(flightNumberFilter, ignoreCase = true)

                val airportName = flight.upAirportName.get(currentLang)

                val matchUpAirport = upAirportFilter.isNullOrBlank() ||
                        airportName.contains(upAirportFilter, ignoreCase = true)

                matchFlightNumber && matchUpAirport
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _lastUpdateTime = MutableStateFlow("")
    val lastUpdateTime: StateFlow<String> = _lastUpdateTime.asStateFlow()

    init {
        startFetchingData()
        loadTrackedFlights()
    }

    private fun loadTrackedFlights() {
        viewModelScope.launch {
            flightTrackingRepository.getTrackedFlights().collect { trackedList ->
                _trackedFlightIds.value = trackedList.map { it.flightId }.toSet()
            }
        }
    }

    private fun startFetchingData() {
        viewModelScope.launch {
            while (true) {
                fetchArrivalFlights()
                delay(10000000)
            }
        }
    }

    private fun fetchArrivalFlights() {
        viewModelScope.launch {
            if (_allFlights.value.isEmpty()) {
                _isLoading.value = true
            }

            getFlightScheduleUseCase()
                .onSuccess { flightList ->
                    _allFlights.value = flightList
                    val timeStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    _lastUpdateTime.value = getString(R.string.last_update_time, timeStamp)
                }
                .onFailure { error ->
                    error.printStackTrace()
                }

            _isLoading.value = false
        }
    }

    fun filterFlights(flightNumber: String, upAirport: String) {
        _filterFlightNumber.value = flightNumber.ifBlank { null }
        _filterUpAirport.value = upAirport.ifBlank { null }
    }

    fun onTrackFlightClicked(flight: FlightStatus) {
        viewModelScope.launch {
            val flightId = generateFlightId(flight)
            val currentTrackedIds = _trackedFlightIds.value

            if (currentTrackedIds.contains(flightId)) {
                flightTrackingRepository.untrackFlight(flightId)
                notificationScheduler.cancelNotification(flightId)
            } else {
                val trackedEntity = flight.toTrackedFlightEntity()
                flightTrackingRepository.trackFlight(trackedEntity)
                notificationScheduler.scheduleNotification(flight, languageCodeFlow.first())
            }
        }
    }

    fun generateFlightId(flight: FlightStatus): String {
        return "${flight.airLineNum}-${flight.upAirportCode}-${flight.expectTime}"
    }

    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            permissionGuideManager.checkAndTriggerGuideIfNeeded()
        }
    }

    fun onPermissionGuideDismissed() {
        viewModelScope.launch {
            permissionGuideManager.onGuideDismissed()
        }
    }
}