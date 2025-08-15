package com.lhr.flighttracker.core.utils.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class DialogState(
    val extendToStatusBar: Boolean = false,   // 是否延展到狀態欄
    val extendToNavigationBar: Boolean = false, // 是否延展到下方導覽列
    val backgroundMaskColor: Color = Color.Black.copy(alpha = 0.2f), // 背景遮罩色
    val position: DialogPosition = DialogPosition.CENTER, // Dialog 顯示位置
    val content: (@Composable () -> Unit)? = null  // 若為 null，表示 Dialog 不顯示
)
