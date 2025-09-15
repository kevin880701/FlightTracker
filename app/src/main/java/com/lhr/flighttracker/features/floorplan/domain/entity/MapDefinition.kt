package com.lhr.flighttracker.features.floorplan.domain.entity

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset

/**
 * 地圖圖片來源
 */
sealed class MapImageSource {
    data class FromAsset(val assetName: String) : MapImageSource()
    data class FromResource(@DrawableRes val resourceId: Int) : MapImageSource()
    data class FromUri(val uri: Uri) : MapImageSource()
}

/**
 * 完整的地圖定義
 *
 * 包含地圖顯示、導航路徑、標記點等所有必要資訊
 */
data class MapDefinition(
    /** 地圖唯一識別碼 */
    val id: String,

    /** 地圖名稱 */
    val name: String,

    /** 地圖描述 */
    val description: String = "",

    /** 地圖圖片資源 */
    val imageSource: MapImageSource,

    /** 地圖邊界和座標系統 */
    val mapBounds: MapBounds,

    /** 路徑網絡（用於導航） */
    val pathNetwork: PathNetwork,

    /** 地圖標記列表 */
    val markers: List<MapMarker> = emptyList(),

    /** 地圖層級（樓層、區域等） */
    val level: String = "1F",

    /** 地圖類型 */
    val mapType: MapType = MapType.INDOOR,

    /** 地圖版本 */
    val version: String = "1.0",

    /** 創建時間戳 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 更新時間戳 */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 根據GPS座標尋找最近的路徑節點
     */
    fun findNearestPathNode(gpsCoordinate: GpsCoordinate, maxDistance: Float = 200f): PathNode? {
        val pixelCoordinate = mapBounds.gpsToPixel(gpsCoordinate)
        return pathNetwork.findNearestNode(pixelCoordinate, maxDistance)
    }

    /**
     * 根據像素座標尋找最近的路徑節點
     */
    fun findNearestPathNode(pixelCoordinate: Offset, maxDistance: Float = 200f): PathNode? {
        return pathNetwork.findNearestNode(pixelCoordinate, maxDistance)
    }

    /**
     * 根據區域過濾標記
     */
    fun getMarkersByArea(area: String): List<MapMarker> {
        return markers.filter { it.area.equals(area, ignoreCase = true) }
    }

    /**
     * 搜尋標記
     */
    fun searchMarkers(query: String): List<MapMarker> {
        return markers.filter { marker ->
            val lowerQuery = query.lowercase()
            marker.name.lowercase().contains(lowerQuery) ||
                    marker.description.lowercase().contains(lowerQuery) ||
                    marker.area.lowercase().contains(lowerQuery) ||
                    marker.searchKeywords.any { it.lowercase().contains(lowerQuery) }
        }
    }

    /**
     * 驗證地圖定義是否完整
     */
    fun validate(): MapValidationResult {
        val errors = mutableListOf<String>()

        if (name.isBlank()) {
            errors.add("地圖名稱不能為空")
        }

        if (mapBounds.mapWidth <= 0 || mapBounds.mapHeight <= 0) {
            errors.add("地圖尺寸必須大於0")
        }

        if (pathNetwork.nodes.isEmpty()) {
            errors.add("路徑網絡不能為空")
        }

        // 檢查標記座標是否在地圖範圍內
        markers.forEach { marker ->
            if (marker.coordinates.x < 0 || marker.coordinates.x > mapBounds.mapWidth ||
                marker.coordinates.y < 0 || marker.coordinates.y > mapBounds.mapHeight) {
                errors.add("標記 ${marker.name} 的座標超出地圖範圍")
            }
        }

        // 檢查路徑節點座標是否在地圖範圍內
        pathNetwork.nodes.forEach { node ->
            if (node.coordinates.x < 0 || node.coordinates.x > mapBounds.mapWidth ||
                node.coordinates.y < 0 || node.coordinates.y > mapBounds.mapHeight) {
                errors.add("路徑節點 ${node.id} 的座標超出地圖範圍")
            }
        }

        return if (errors.isEmpty()) {
            MapValidationResult.Valid
        } else {
            MapValidationResult.Invalid(errors)
        }
    }
}

/**
 * 地圖類型
 */
enum class MapType {
    INDOOR,     // 室內地圖
    OUTDOOR,    // 室外地圖
    MIXED       // 混合地圖
}

/**
 * 地圖驗證結果
 */
sealed class MapValidationResult {
    object Valid : MapValidationResult()
    data class Invalid(val errors: List<String>) : MapValidationResult()
}

/**
 * 地圖定義建造者模式
 */
class MapDefinitionBuilder {
    private var id: String = ""
    private var name: String = ""
    private var description: String = ""
    private var imageSource: MapImageSource? = null
    private var mapBounds: MapBounds? = null
    private var pathNetwork: PathNetwork? = null
    private var markers: List<MapMarker> = emptyList()
    private var level: String = "1F"
    private var mapType: MapType = MapType.INDOOR
    private var version: String = "1.0"

    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun description(description: String) = apply { this.description = description }
    fun imageSource(imageSource: MapImageSource) = apply { this.imageSource = imageSource }
    fun mapBounds(mapBounds: MapBounds) = apply { this.mapBounds = mapBounds }
    fun pathNetwork(pathNetwork: PathNetwork) = apply { this.pathNetwork = pathNetwork }
    fun markers(markers: List<MapMarker>) = apply { this.markers = markers }
    fun level(level: String) = apply { this.level = level }
    fun mapType(mapType: MapType) = apply { this.mapType = mapType }
    fun version(version: String) = apply { this.version = version }

    fun build(): MapDefinition {
        require(id.isNotBlank()) { "地圖ID不能為空" }
        require(name.isNotBlank()) { "地圖名稱不能為空" }
        requireNotNull(imageSource) { "地圖圖片來源不能為空" }
        requireNotNull(mapBounds) { "地圖邊界不能為空" }
        requireNotNull(pathNetwork) { "路徑網絡不能為空" }

        return MapDefinition(
            id = id,
            name = name,
            description = description,
            imageSource = imageSource!!,
            mapBounds = mapBounds!!,
            pathNetwork = pathNetwork!!,
            markers = markers,
            level = level,
            mapType = mapType,
            version = version
        )
    }
}