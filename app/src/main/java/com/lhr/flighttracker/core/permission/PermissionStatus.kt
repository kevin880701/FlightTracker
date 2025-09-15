package com.lhr.flighttracker.core.permission

import androidx.compose.runtime.Immutable

/**
 * 權限請求的狀態模型。
 * 使用 Sealed Class 來確保狀態的互斥性與完整性。
 */
@Immutable
sealed class PermissionStatus {
    // 初始狀態，尚未請求
    object Idle : PermissionStatus()

    // 權限已被授予
    object Granted : PermissionStatus()

    // 權限已被拒絕
    sealed class Denied : PermissionStatus() {
        /**
         * 使用者已拒絕一次，應顯示理由。
         * @param rationale 要顯示給使用者的理由文字。
         */
        data class NeedsRationale(val rationale: String) : Denied()

        /**
         * 使用者已永久拒絕，應引導至系統設定。
         */
        object Permanently : Denied()
    }
}