package com.lhr.flighttracker.features.floorplan.domain.entity

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

/**
 * 路徑網絡 - 管理所有節點和邊的關係
 *
 * 提供圖論演算法所需的數據結構和查詢方法
 */
data class PathNetwork(
    /** 所有可通行的節點列表 */
    val nodes: List<PathNode>,

    /** 所有連接節點的邊列表 */
    val edges: List<PathEdge>
) {
    /** 節點ID到節點對象的快速查找映射 */
    private val nodeMap = nodes.associateBy { it.id }

    /** 鄰接表：每個節點ID對應其出發的所有邊，用於圖論演算法 */
    private val adjacencyList = buildAdjacencyList()

    /**
     * 建立鄰接表數據結構
     * 將邊按起始節點分組，便於路徑搜尋演算法使用
     */
    private fun buildAdjacencyList(): Map<Int, List<PathEdge>> {
        return edges.groupBy { it.fromNodeId }
    }

    /**
     * 根據節點ID獲取節點對象
     */
    fun getNode(nodeId: Int): PathNode? = nodeMap[nodeId]

    /**
     * 獲取從指定節點出發的所有可通行邊
     * 用於路徑搜尋演算法中的鄰居節點查找
     */
    fun getEdgesFrom(nodeId: Int): List<PathEdge> = adjacencyList[nodeId] ?: emptyList()


    /**
     * 尋找最近的節點
     */
    fun findNearestNode(coordinates: Offset, maxDistance: Float = 200f): PathNode? {
        return nodes.minByOrNull { node ->
            val dx = node.coordinates.x - coordinates.x
            val dy = node.coordinates.y - coordinates.y
            sqrt(dx * dx + dy * dy)
        }?.takeIf { node ->
            val distance = sqrt(
                (node.coordinates.x - coordinates.x).let { it * it } +
                        (node.coordinates.y - coordinates.y).let { it * it }
            )
            distance <= maxDistance
        }
    }
}