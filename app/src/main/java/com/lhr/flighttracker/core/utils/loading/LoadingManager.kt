package com.lhr.flighttracker.core.utils.loading

import androidx.compose.runtime.mutableStateOf

object LoadingManager {
    var isLoading = mutableStateOf(false)
        private set

    var loadingText = mutableStateOf<String?>(null)
        private set

    var defaultLoadingType = mutableStateOf<LoadingType>(LoadingType.Circular())
        private set

    var loadingType = mutableStateOf<LoadingType>(defaultLoadingType.value)
        private set

    fun setDefaultLoadingType(type: LoadingType) {
        defaultLoadingType.value = type
        loadingType.value = type
    }

    fun showLoading(message: String? = null, type: LoadingType? = null) {
        isLoading.value = true
        loadingText.value = message
        type?.let { loadingType.value = it }
    }

    fun dismissLoading() {
        isLoading.value = false
        loadingText.value = null
        loadingType.value = defaultLoadingType.value
    }
}
