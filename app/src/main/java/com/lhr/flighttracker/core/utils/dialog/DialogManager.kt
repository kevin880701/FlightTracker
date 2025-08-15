package com.lhr.flighttracker.core.utils.dialog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DialogManager {
    private val _dialogState = MutableStateFlow(DialogState())
    val dialogState: StateFlow<DialogState> = _dialogState

    fun showDialog(dialog: DialogState) {
        _dialogState.value = dialog
    }

    fun dismissDialog() {
        _dialogState.value = DialogState()
    }
}
