package com.lhr.flighttracker.features.floorplan.domain.entity

/**
 * 多點導航路線資料容器
 * 封裝多點導航所需的所有路線資訊和統計數據
 */
data class MultiPointRoute(
    /** 完整的導航路線（從起點到終點，包含所有途經點的連續路徑） */
    val fullRoute: NavigationRoute,

    /** 所有途經點（按訪問順序排列） */
    val waypoints: List<MapMarker>,

    /** 各段路線（每兩個相鄰途經點之間的獨立路線） */
    val segmentRoutes: List<NavigationRoute>,

    /** 總距離（公尺），所有路線段的距離總和 */
    val totalDistance: Float,

    /** 總預估時間（秒），所有路線段的時間總和 */
    val totalEstimatedTime: Int
) {
    /**
     * 取得到達指定途經點的累計距離
     * @param waypointIndex 途經點索引（0-based）
     * @return 從起點到該途經點的累計距離（公尺）
     */
    fun getCumulativeDistanceToWaypoint(waypointIndex: Int): Float {
        return segmentRoutes.take(waypointIndex + 1).sumOf { it.totalDistance.toDouble() }.toFloat()
    }

    /**
     * 取得到達指定途經點的累計時間
     * @param waypointIndex 途經點索引（0-based）
     * @return 從起點到該途經點的累計預估時間（秒）
     */
    fun getCumulativeTimeToWaypoint(waypointIndex: Int): Int {
        return segmentRoutes.take(waypointIndex + 1).sumOf { it.estimatedWalkTime }
    }
}