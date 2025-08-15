package com.lhr.flighttracker.core.utils.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun showDialog(
    extendToStatusBar: Boolean = false,
    extendToNavigationBar: Boolean = false,
    backgroundMaskColor: Color = Color.Black.copy(alpha = 0.2f),
    position: DialogPosition = DialogPosition.CENTER,
    content: @Composable () -> Unit
) {
    DialogManager.showDialog(
        DialogState(
            extendToStatusBar = extendToStatusBar,
            extendToNavigationBar = extendToNavigationBar,
            backgroundMaskColor = backgroundMaskColor,
            position = position,
            content = content
        )
    )
}