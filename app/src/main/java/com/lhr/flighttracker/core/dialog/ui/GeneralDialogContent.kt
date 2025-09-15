package com.lhr.flighttracker.core.dialog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 一個更通用的對話框內容 Composable，用於顯示資訊或請求使用者確認。
 *
 * @param title 要顯示在對話框頂部的標題。
 * @param content (可選) 對話框的主要描述性文字內容。如果為 null，則不顯示內容區域。
 * @param confirmButtonText 主操作按鈕上顯示的文字。
 * @param onConfirmClick 當主操作按鈕被點擊時執行的 Lambda。
 * @param dismissButtonText (可選) 次要操作按鈕上顯示的文字。
 * @param onDismissRequest (可選) 當對話框應該被關閉時執行的 Lambda。
 */
@Composable
fun GeneralDialogContent(
    title: String,
    content: String? = null,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    dismissButtonText: String? = null,
    onDismissRequest: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(24.dp),
    ) {
        // 標題
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 內容 (僅當 content 不為 null 時顯示)
        if (content != null) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 按鈕區域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // 關閉按鈕 (僅當文字和事件處理器都提供時才顯示)
            if (dismissButtonText != null && onDismissRequest != null) {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(dismissButtonText)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 確認按鈕
            Button(
                onClick = onConfirmClick
            ) {
                Text(confirmButtonText)
            }
        }
    }
}