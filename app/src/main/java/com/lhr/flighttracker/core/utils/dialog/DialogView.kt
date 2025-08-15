package com.lhr.flighttracker.core.utils.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DialogView() {
    val dialogState by DialogManager.dialogState.collectAsState()

    if (dialogState.content != null) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dialogState.backgroundMaskColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { DialogManager.dismissDialog() }
                )
                .windowInsetsPadding(WindowInsets.ime),
            contentAlignment = when (dialogState.position) {
                DialogPosition.TOP -> Alignment.TopCenter
                DialogPosition.CENTER -> Alignment.Center
                DialogPosition.BOTTOM -> Alignment.BottomCenter
            }
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                dialogState.content?.invoke()
            }
        }
    }
}