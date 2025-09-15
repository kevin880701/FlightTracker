package com.lhr.flighttracker.core.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class AppUiState {
    object Idle : AppUiState()
    object Loading : AppUiState()
}

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    private var _uiState = mutableStateOf<AppUiState>(AppUiState.Idle)
    val uiState get() = _uiState

    fun setLoading() {
        _uiState.value = AppUiState.Loading
    }

    fun resetState() {
        _uiState.value = AppUiState.Idle
    }
}