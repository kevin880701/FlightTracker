package com.lhr.flighttracker.features.floorplan.domain

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationDirection
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationInstruction
import com.lhr.flighttracker.features.floorplan.domain.entity.NavigationRoute
import com.lhr.flighttracker.features.floorplan.domain.entity.NodeType
import com.lhr.flighttracker.features.floorplan.domain.entity.PathEdge
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNetwork
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNode
import com.lhr.flighttracker.features.floorplan.domain.entity.PathRestriction
import com.lhr.flighttracker.features.floorplan.domain.entity.PathType
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * A* 路徑搜尋演算法實作
 */
@Singleton
class PathfindingService @Inject constructor() {

    companion object {
        private const val TAG = "PathfindingService"
    }

    /**
     * 使用 A* 演算法尋找最短路徑
     */
    fun findPath(
        network: PathNetwork,
        startNodeId: Int,
        endNodeId: Int,
        preferences: NavigationPreferences = NavigationPreferences()
    ): NavigationRoute? {
        val startNode = network.getNode(startNodeId) ?: return null
        val endNode = network.getNode(endNodeId) ?: return null

        Log.d(TAG, "Finding path from node $startNodeId to node $endNodeId")

        val openSet = PriorityQueue<AStarNode>(compareBy { it.fCost })
        val closedSet = mutableSetOf<Int>()
        val gCosts = mutableMapOf<Int, Float>()
        val parents = mutableMapOf<Int, Int>()

        // 初始化起點
        openSet.add(AStarNode(startNodeId, 0f, heuristic(startNode, endNode)))
        gCosts[startNodeId] = 0f

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()

            if (current.nodeId == endNodeId) {
                // 找到目標，重建路徑
                Log.d(TAG, "Path found! Reconstructing...")
                return reconstructPath(network, parents, startNodeId, endNodeId, gCosts)
            }

            closedSet.add(current.nodeId)

            // 檢查所有相鄰節點
            network.getEdgesFrom(current.nodeId).forEach { edge ->
                if (edge.toNodeId in closedSet) return@forEach

                // 根據偏好設定過濾路徑
                if (!isEdgeAllowed(edge, network.getNode(edge.toNodeId), preferences)) {
                    return@forEach
                }

                val tentativeGCost = gCosts[current.nodeId]!! + edge.distance

                if (tentativeGCost < (gCosts[edge.toNodeId] ?: Float.MAX_VALUE)) {
                    val neighbor = network.getNode(edge.toNodeId)!!

                    parents[edge.toNodeId] = current.nodeId
                    gCosts[edge.toNodeId] = tentativeGCost

                    val hCost = heuristic(neighbor, endNode)
                    val fCost = tentativeGCost + hCost

                    openSet.add(AStarNode(edge.toNodeId, fCost, hCost))
                }
            }
        }

        Log.w(TAG, "No path found from $startNodeId to $endNodeId")
        return null // 找不到路徑
    }

    /**
     * 啟發式函數 (曼哈頓距離)
     */
    private fun heuristic(from: PathNode, to: PathNode): Float {
        return from.distanceTo(to)
    }

    /**
     * 從標記座標找到最近的路徑節點（不依賴pathNodeId）
     */
    fun findNearestPathNode(network: PathNetwork, coordinates: Offset): PathNode? {
        Log.d(TAG, "尋找最近節點 - 座標: $coordinates")

        val nearestNode = network.findNearestNode(coordinates, maxDistance = 200f)

        if (nearestNode == null) {
            Log.w(TAG, "在 200m 範圍內找不到路徑節點")

            // 嘗試增加搜尋範圍
            val extendedNode = network.findNearestNode(coordinates, maxDistance = 500f)
            if (extendedNode != null) {
                Log.d(TAG, "在擴展範圍內找到節點: ${extendedNode.id}, 距離: ${calculateDistance(coordinates, extendedNode.coordinates)}")
                return extendedNode
            }

            // 列出最近的幾個節點用於調試
            val allDistances = network.nodes.map { node ->
                val distance = calculateDistance(coordinates, node.coordinates)
                "${node.id}:${distance.toInt()}m"
            }.take(5)
            Log.d(TAG, "最近的 5 個節點距離: ${allDistances.joinToString()}")
        } else {
            Log.d(TAG, "找到最近節點: ${nearestNode.id}, 距離: ${calculateDistance(coordinates, nearestNode.coordinates)}")
        }

        return nearestNode
    }

    /**
     * 計算從用戶位置到目標標記的路徑
     */
    fun findPathToMarker(
        userLocation: Offset,
        targetMarker: MapMarker,
        network: PathNetwork
    ): NavigationRoute? {
        Log.d(TAG, "findPathToMarker - 用戶位置: $userLocation, 目標: ${targetMarker.name}")

        // 檢查網絡數據
        if (network.nodes.isEmpty()) {
            Log.e(TAG, "路徑網絡為空")
            return null
        }

        // 找到起點和終點的最近節點
        val startNode = findNearestPathNode(network, userLocation)
        val endNode = findNearestPathNode(network, targetMarker.coordinates)

        Log.d(TAG, "起點節點: ${startNode?.id}, 終點節點: ${endNode?.id}")

        if (startNode == null || endNode == null) {
            Log.e(TAG, "找不到合適的路徑節點")
            return null
        }

        // 使用A*算法計算路徑
        return findPath(network, startNode.id, endNode.id)
    }

    /**
     * 檢查路徑邊是否符合導航偏好
     */
    private fun isEdgeAllowed(edge: PathEdge, toNode: PathNode?, preferences: NavigationPreferences): Boolean {
        // 無障礙需求檢查
        if (preferences.requireAccessible && !edge.isAccessible) return false

        // 避免樓梯
        if (preferences.avoidStairs && edge.pathType == PathType.STAIRS) return false

        // 避免手扶梯
        if (preferences.avoidEscalators && edge.pathType == PathType.ESCALATOR) return false

        // 檢查路徑限制
        edge.restrictions.forEach { restriction ->
            when (restriction) {
                PathRestriction.STAFF_ONLY -> if (!preferences.isStaff) return false
                PathRestriction.MAINTENANCE -> return false
                PathRestriction.EMERGENCY_ONLY -> if (!preferences.isEmergency) return false
                else -> {}
            }
        }

        return true
    }

    /**
     * 重建找到的路徑
     */
    private fun reconstructPath(
        network: PathNetwork,
        parents: Map<Int, Int>,
        startNodeId: Int,
        endNodeId: Int,
        gCosts: Map<Int, Float>
    ): NavigationRoute {
        val pathNodes = mutableListOf<PathNode>()
        val pathEdges = mutableListOf<PathEdge>()

        // 從終點開始往回追溯
        var currentId = endNodeId
        while (currentId != startNodeId) {
            val node = network.getNode(currentId)!!
            pathNodes.add(0, node)

            val parentId = parents[currentId]!!
            val edge = network.getEdgesFrom(parentId).find { it.toNodeId == currentId }!!
            pathEdges.add(0, edge)

            currentId = parentId
        }

        // 加入起點
        pathNodes.add(0, network.getNode(startNodeId)!!)

        val totalDistance = pathEdges.sumOf { it.distance.toDouble() }.toFloat()
        val totalTime = pathEdges.sumOf { it.estimatedWalkTime }

        val instructions = generateInstructions(pathNodes, pathEdges)

        return NavigationRoute(
            nodes = pathNodes,
            edges = pathEdges,
            totalDistance = totalDistance,
            estimatedWalkTime = totalTime,
            instructions = instructions
        )
    }

    /**
     * 生成導航指令
     */
    private fun generateInstructions(nodes: List<PathNode>, edges: List<PathEdge>): List<NavigationInstruction> {
        val instructions = mutableListOf<NavigationInstruction>()

        if (nodes.isEmpty()) return instructions

        // 起點指令
        instructions.add(
            NavigationInstruction(
                stepNumber = 1,
                instruction = "從 ${nodes.first().nodeType.getLocalizedName()} 開始",
                coordinates = nodes.first().coordinates,
                direction = NavigationDirection.START,
                distance = 0f,
                estimatedTime = 0
            )
        )

        // 中間路徑指令
        for (i in 1 until nodes.size - 1) {
            val prevNode = nodes[i - 1]
            val currentNode = nodes[i]
            val nextNode = nodes[i + 1]
            val edge = edges[i - 1]

            val direction = calculateDirection(prevNode, currentNode, nextNode)
            val instruction = generateInstructionText(currentNode, edge, direction)

            instructions.add(
                NavigationInstruction(
                    stepNumber = i + 1,
                    instruction = instruction,
                    coordinates = currentNode.coordinates,
                    direction = direction,
                    distance = edge.distance,
                    estimatedTime = edge.estimatedWalkTime
                )
            )
        }

        // 終點指令
        if (nodes.size > 1) {
            instructions.add(
                NavigationInstruction(
                    stepNumber = nodes.size,
                    instruction = "抵達目的地",
                    coordinates = nodes.last().coordinates,
                    direction = NavigationDirection.ARRIVE,
                    distance = edges.lastOrNull()?.distance ?: 0f,
                    estimatedTime = edges.lastOrNull()?.estimatedWalkTime ?: 0
                )
            )
        }

        return instructions
    }

    /**
     * 計算轉向方向
     */
    private fun calculateDirection(prev: PathNode, current: PathNode, next: PathNode): NavigationDirection {
        val angle1 = atan2(current.coordinates.y - prev.coordinates.y, current.coordinates.x - prev.coordinates.x)
        val angle2 = atan2(next.coordinates.y - current.coordinates.y, next.coordinates.x - current.coordinates.x)

        var angleDiff = angle2 - angle1
        // 將 PI 轉為 Float 來進行運算
        val PI_FLOAT = PI.toFloat()
        if (angleDiff < -PI_FLOAT) angleDiff += 2 * PI_FLOAT
        if (angleDiff > PI_FLOAT) angleDiff -= 2 * PI_FLOAT

        return when {
            abs(angleDiff) < PI/6 -> NavigationDirection.GO_STRAIGHT
            angleDiff > 0 -> NavigationDirection.TURN_LEFT
            else -> NavigationDirection.TURN_RIGHT
        }
    }

    /**
     * 生成指令文字
     */
    private fun generateInstructionText(node: PathNode, edge: PathEdge, direction: NavigationDirection): String {
        return when (edge.pathType) {
            PathType.STAIRS -> if (direction == NavigationDirection.GO_STRAIGHT) "使用樓梯" else "轉向樓梯"
            PathType.ELEVATOR -> "搭乘電梯"
            PathType.ESCALATOR -> "搭乘手扶梯"
            PathType.MOVING_WALKWAY -> "使用電動步道"
            else -> when (direction) {
                NavigationDirection.GO_STRAIGHT -> "直行 ${edge.distance.toInt()}m"
                NavigationDirection.TURN_LEFT -> "左轉"
                NavigationDirection.TURN_RIGHT -> "右轉"
                else -> "繼續前行"
            }
        }
    }

    /**
     * 計算兩點間距離
     */
    private fun calculateDistance(point1: Offset, point2: Offset): Float {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return sqrt(dx * dx + dy * dy)
    }
}

/**
 * A* 演算法節點
 */
private data class AStarNode(
    val nodeId: Int,
    val fCost: Float,
    val hCost: Float
)

/**
 * 導航偏好設定
 */
data class NavigationPreferences(
    val requireAccessible: Boolean = false,  // 需要無障礙路徑
    val avoidStairs: Boolean = false,        // 避免樓梯
    val avoidEscalators: Boolean = false,    // 避免手扶梯
    val preferMovingWalkways: Boolean = false, // 偏好電動步道
    val isStaff: Boolean = false,            // 是否為員工
    val isEmergency: Boolean = false         // 是否為緊急情況
)

/**
 * NodeType 擴展函數
 */
private fun NodeType.getLocalizedName(): String {
    return when (this) {
        NodeType.NORMAL -> "通道"
        NodeType.INTERSECTION -> "路口"
        NodeType.ENTRANCE -> "入口"
        NodeType.EXIT -> "出口"
        NodeType.ELEVATOR -> "電梯"
        NodeType.ESCALATOR -> "手扶梯"
        NodeType.STAIRS -> "樓梯"
    }
}