package com.lhr.flighttracker.features.floorplan.domain.entity

/**
 * 路徑邊 - 連接兩個節點的可通行路徑
 */
data class PathEdge(
    /** 邊的唯一識別碼 */
    val id: Int,

    /** 起始節點ID，指向PathNode的id */
    val fromNodeId: Int,

    /** 目標節點ID，指向PathNode的id */
    val toNodeId: Int,

    /** 兩節點間的實際距離（像素單位），用於路徑演算法的成本計算 */
    val distance: Float,

    /** 路徑類型，影響移動速度和導航指令生成 */
    val pathType: PathType = PathType.WALKWAY,

    /** 是否支援無障礙通行，影響無障礙路線的可行性 */
    val isAccessible: Boolean = true,

    /** 預估步行時間（秒），用於總時間計算和用戶預期管理 */
    val estimatedWalkTime: Int = 0,

    /** 路徑使用限制列表，控制不同用戶類型的通行權限 */
    val restrictions: List<PathRestriction> = emptyList()
) {
    companion object {
        /**
         * 根據兩個節點自動建立路徑邊的便利方法
         * 自動計算距離和預估時間
         */
        fun create(id: Int, fromNode: PathNode, toNode: PathNode, pathType: PathType = PathType.WALKWAY): PathEdge {
            val distance = fromNode.distanceTo(toNode)
            val walkTime = (distance * 0.02).toInt() // 假設每像素0.02秒（可調整移動速度）

            return PathEdge(
                id = id,
                fromNodeId = fromNode.id,
                toNodeId = toNode.id,
                distance = distance,
                pathType = pathType,
                isAccessible = fromNode.isAccessible && toNode.isAccessible,
                estimatedWalkTime = walkTime
            )
        }
    }
}

/**
 * 路徑類型 - 定義移動方式和速度
 */
enum class PathType {
    WALKWAY,     // 步行道：標準步行速度的平坦路面
    CORRIDOR,    // 走廊：室內通道，可能較窄但平坦
    ESCALATOR,   // 手扶梯路徑：機械輔助的斜坡移動
    ELEVATOR,    // 電梯路徑：垂直快速移動
    STAIRS,      // 樓梯路徑：需要爬升，速度較慢
    MOVING_WALKWAY // 電動步道：機械輔助的平面移動，速度較快
}

/**
 * 路徑限制 - 定義通行的特殊條件
 */
enum class PathRestriction {
    STAFF_ONLY,     // 僅限員工：員工專用通道，一般旅客不可使用
    SECURITY_CHECK, // 需要安檢：必須通過安全檢查才能通行的區域
    TICKET_REQUIRED,// 需要票券：需要有效機票或通行證
    MAINTENANCE,    // 維修中：暫時不可通行，需要尋找替代路徑
    EMERGENCY_ONLY  // 僅限緊急時使用：平時禁止通行的緊急通道
}