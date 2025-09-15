package com.lhr.flighttracker.features.floorplan.domain.entity

import androidx.compose.ui.geometry.Offset

/**
 * 導航路徑結果 - 路徑規劃演算法的輸出
 *
 * 包含完整的路徑資訊和導航指令
 */
data class NavigationRoute(
    /** 路徑經過的所有節點，按順序排列 */
    val nodes: List<PathNode>,

    /** 路徑使用的所有邊，按順序排列 */
    val edges: List<PathEdge>,

    /** 路徑總距離（像素單位） */
    val totalDistance: Float,

    /** 總預估步行時間（秒） */
    val estimatedWalkTime: Int,

    /** 逐步導航指令列表，用於引導用戶 */
    val instructions: List<NavigationInstruction>
) {
    /**
     * 獲取路徑上的所有座標點
     * 用於在地圖上繪製路徑線
     */
    fun getPathCoordinates(): List<Offset> = nodes.map { it.coordinates }

    /**
     * 檢查整條路徑是否完全支援無障礙通行
     * 所有節點和邊都必須支援無障礙才返回true
     */
    fun isAccessible(): Boolean = nodes.all { it.isAccessible } && edges.all { it.isAccessible }
}

/**
 * 導航指令 - 單步導航引導資訊
 *
 * 為用戶提供具體的行走指示
 */
data class NavigationInstruction(
    /** 指令序號，從1開始 */
    val stepNumber: Int,

    /** 人類可讀的導航指令文字，如"直行50公尺後左轉" */
    val instruction: String,

    /** 此指令對應的地圖座標位置 */
    val coordinates: Offset,

    /** 移動方向類型，用於生成圖標和語音 */
    val direction: NavigationDirection,

    /** 此步驟需要移動的距離 */
    val distance: Float,

    /** 此步驟的預估時間 */
    val estimatedTime: Int
)

/**
 * 導航方向 - 定義移動指令的類型
 */
enum class NavigationDirection {
    START,           // 起點：導航開始
    GO_STRAIGHT,     // 直行：繼續向前移動
    TURN_LEFT,       // 左轉：向左改變方向
    TURN_RIGHT,      // 右轉：向右改變方向
    TURN_AROUND,     // 迴轉：180度轉向
    UP_STAIRS,       // 上樓：使用樓梯向上移動
    DOWN_STAIRS,     // 下樓：使用樓梯向下移動
    TAKE_ELEVATOR,   // 搭乘電梯：使用電梯改變樓層
    TAKE_ESCALATOR,  // 搭乘手扶梯：使用手扶梯改變樓層
    ARRIVE           // 抵達目的地：導航結束
}