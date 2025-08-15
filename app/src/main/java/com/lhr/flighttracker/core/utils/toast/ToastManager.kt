package com.lhr.flighttracker.core.utils.toast

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object ToastManager {
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _messages.emit(message)
        }
    }
}