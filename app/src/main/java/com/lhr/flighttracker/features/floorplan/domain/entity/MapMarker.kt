package com.lhr.flighttracker.features.floorplan.domain.entity

import androidx.compose.ui.geometry.Offset
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource
import kotlin.math.sqrt

/**
 * 地圖標記的三種主要類型
 */
enum class MarkerType {
    /** 使用者目前所在的位置 */
    CURRENT_LOCATION,

    /** 一般的地標標記，如登機門、商店、餐廳等 */
    STANDARD_MARKER,

    /** 導航路徑中的節點，用於連接不同地點 */
    PATH_NODE
}

/**
 * 更新後的 MapMarker，支援導航功能
 */
data class MapMarker(
    /** 標點的唯一識別碼 */
    val id: Int,

    /** 標點的名稱 (例如: "A1登機門", "星巴克") */
    val name: String,

    /** 座標 (coordinates) 需基於原始圖片的像素位置 */
    val coordinates: Offset,

    /** 標點的圖示來源 */
    val imageSource: ImageSource,

    /** 標點類型 */
    val type: MarkerType = MarkerType.STANDARD_MARKER,

    /** 詳細描述 */
    val description: String = "",

    /** 所在的區域、航廈或樓層 */
    val area: String = "",

    /** 營業時間 */
    val operatingHours: String = "",

    /** 聯絡電話 */
    val phoneNumber: String = "",

    /** 額外資訊 */
    val additionalInfo: String = "",

    /** 導航優先級 (數字越小優先級越高) */
    val navigationPriority: Int = 0,

    /** 是否為無障礙友善 */
    val isAccessible: Boolean = true,

    /** 搜尋關鍵字 */
    val searchKeywords: List<String> = emptyList(),

    /** 標點的分類標籤 (可用於篩選和分組) */
    val category: String = "",

    /** 自定義屬性 (可存放額外的鍵值對資料) */
    val customProperties: Map<String, String> = emptyMap()
)

/**
 * 檢查標記是否符合搜尋關鍵字
 */
fun MapMarker.matchesSearch(query: String): Boolean {
    val lowerQuery = query.lowercase()
    return name.lowercase().contains(lowerQuery) ||
            description.lowercase().contains(lowerQuery) ||
            area.lowercase().contains(lowerQuery) ||
            type.name.lowercase().contains(lowerQuery) ||
            category.lowercase().contains(lowerQuery) ||
            searchKeywords.any { it.lowercase().contains(lowerQuery) }
}

/**
 * 根據距離排序標記
 */
fun List<MapMarker>.sortByDistanceFrom(coordinates: Offset): List<MapMarker> {
    return this.sortedBy { marker ->
        val dx = marker.coordinates.x - coordinates.x
        val dy = marker.coordinates.y - coordinates.y
        sqrt(dx * dx + dy * dy)
    }
}

/**
 * 根據分類分組標記
 */
fun List<MapMarker>.groupByCategory(): Map<String, List<MapMarker>> {
    return this.groupBy { it.category }
}

/**
 * 根據區域分組標記
 */
fun List<MapMarker>.groupByArea(): Map<String, List<MapMarker>> {
    return this.groupBy { it.area }
}

/**
 * 篩選特定類型的標記
 */
fun List<MapMarker>.filterByType(typeName: MarkerType): List<MapMarker> {
    return this.filter { it.type == typeName }
}

/**
 * 篩選特定分類的標記
 */
fun List<MapMarker>.filterByCategory(category: String): List<MapMarker> {
    return this.filter { it.category.equals(category, ignoreCase = true) }
}

/**
 * 篩選無障礙友善的標記
 */
fun List<MapMarker>.filterAccessible(): List<MapMarker> {
    return this.filter { it.isAccessible }
}