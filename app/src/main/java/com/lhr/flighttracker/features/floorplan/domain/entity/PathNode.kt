package com.lhr.flighttracker.features.floorplan.domain.entity

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

/**
 * 節點類型 - 定義節點的功能性質
 */
enum class NodeType {
    NORMAL,      // 普通通道節點：一般走道上的節點
    INTERSECTION,// 交叉路口：多條路徑的匯聚點，導航時的關鍵轉折點
    ENTRANCE,    // 入口：進入某區域的起始點
    EXIT,        // 出口：離開某區域的終點
    ELEVATOR,    // 電梯：垂直交通節點，連接不同樓層
    ESCALATOR,   // 手扶梯：斜坡式垂直交通節點
    STAIRS       // 樓梯：階梯式垂直交通節點
}

/**
 * 路徑節點 - 代表地圖上一個可以通行的點
 *
 * 例如：走廊轉角、電梯口、登機門前、商店入口等關鍵位置
 */
data class PathNode(
    /** 節點的唯一識別碼，用於建立邊的連接關係 */
    val id: Int,

    /** 節點在地圖上的像素座標位置 */
    val coordinates: Offset,

    /** 節點類型，影響導航演算法的權重計算和路線規劃 */
    val nodeType: NodeType = NodeType.NORMAL,

    /** 是否支援無障礙通行（輪椅、嬰兒車等），影響無障礙路線規劃 */
    val isAccessible: Boolean = true,

    /** 節點所在的樓層或區域標識，用於多樓層導航 */
    val area: String = "1F"
) {
    /**
     * 計算到另一個節點的直線距離（歐幾里得距離）
     * 用於路徑演算法中的成本估算
     */
    fun distanceTo(other: PathNode): Float {
        val dx = coordinates.x - other.coordinates.x
        val dy = coordinates.y - other.coordinates.y
        return sqrt(dx * dx + dy * dy)
    }
}