package com.lhr.flighttracker.features.floorplan.domain.entity

/**
 * 導航狀態封裝類
 * 使用 sealed class 確保類型安全，涵蓋所有可能的導航狀態
 */
sealed class NavigationState {
    /** 閒置狀態 - 沒有進行任何導航活動 */
    object Idle : NavigationState()

    /**
     * 搜尋目的地狀態
     * @param query 當前搜尋關鍵字，預設為空字串
     */
    data class SearchingDestination(val query: String = "") : NavigationState()

    /**
     * 多點導航規劃狀態 - 用戶正在規劃多個途經點的路線
     * @param waypoints 已選定的途經點列表，按順序排列
     * @param isAddingWaypoint 是否正在添加新的途經點
     * @param isReordering 是否正在調整途經點順序（拖拽模式）
     */
    data class PlanningMultiPoint(
        val waypoints: List<MapMarker> = emptyList(),
        val isAddingWaypoint: Boolean = false,
        val isReordering: Boolean = false
    ) : NavigationState()

    /**
     * 導航進行中狀態 - 支援單點或多點路線導航
     * @param route 完整的導航路線（包含所有途經點）
     * @param waypoints 所有途經點列表，按訪問順序排列
     * @param currentWaypointIndex 當前目標途經點的索引（0-based）
     * @param currentStepIndex 當前導航步驟的索引（0-based）
     * @param isInstructionsVisible 是否顯示詳細導航指令面板
     * @param isAddingWaypoint 是否正在添加新途經點（導航中動態添加）
     * @param isReordering 是否正在調整途經點順序（導航中動態調整）
     */
    data class Navigating(
        val route: NavigationRoute,
        val waypoints: List<MapMarker> = emptyList(),
        val currentWaypointIndex: Int = 0,
        val currentStepIndex: Int = 0,
        val isInstructionsVisible: Boolean = false,
        val isAddingWaypoint: Boolean = false,
        val isReordering: Boolean = false
    ) : NavigationState() {

        /**
         * 取得當前目標途經點
         * @return 當前要前往的途經點，如果索引超出範圍則返回 null
         */
        val currentTargetWaypoint: MapMarker?
            get() = waypoints.getOrNull(currentWaypointIndex)

        /**
         * 檢查是否已到達最終目的地
         * @return true 如果當前途經點是最後一個
         */
        val isAtFinalDestination: Boolean
            get() = currentWaypointIndex >= waypoints.size - 1

        /**
         * 取得剩餘途經點數量
         * @return 包含當前途經點在內的剩餘途經點數
         */
        val remainingWaypoints: Int
            get() = (waypoints.size - 1 - currentWaypointIndex).coerceAtLeast(0)
    }

    /**
     * 導航錯誤狀態
     * @param error 錯誤訊息，用於向用戶顯示問題描述
     */
    data class NavigationError(val error: String) : NavigationState()
}
